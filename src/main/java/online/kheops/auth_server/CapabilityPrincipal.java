package online.kheops.auth_server;

import online.kheops.auth_server.album.AlbumNotFoundException;
import online.kheops.auth_server.album.UserNotMemberException;
import online.kheops.auth_server.capability.ScopeType;
import online.kheops.auth_server.entity.*;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserPermissionEnum;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;

import static online.kheops.auth_server.album.Albums.getAlbum;
import static online.kheops.auth_server.album.Albums.getAlbumUser;
import static online.kheops.auth_server.album.Albums.isMemberOfAlbum;
import static online.kheops.auth_server.series.Series.canAccessSeries;
import static online.kheops.auth_server.series.SeriesQueries.*;
import static online.kheops.auth_server.study.Studies.canAccessStudy;
import static online.kheops.auth_server.study.Studies.getStudy;


public class CapabilityPrincipal implements KheopsPrincipalInterface {

    private final Capability capability;
    private final User user;

    private EntityManager em;
    private EntityTransaction tx;

    /*public CapabilityPrincipal(ScopeType scopeType, boolean readPermission, boolean writePermission) {
        this.scopeType = scopeType;
        this.readPermission = readPermission;
        this.writePermission = writePermission;
    }*/

    //old version
    Long dbid;
    public CapabilityPrincipal(Capability capability, User user) {
        this.capability = capability;
        this.dbid=user.getPk();
        this.user = user;
        this.em = EntityManagerListener.createEntityManager();
    }
    @Override
    public long getDBID() {
        return dbid;
    }
    @Override
    public String getName() {
        return Long.toString(dbid);
    }
    //end old version

    @Override
    public boolean hasSeriesReadAccess(String studyInstanceUID, String seriesInstanceUID) throws SeriesNotFoundException {
        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();
            if(getScope() == ScopeType.USER) {
                if (canAccessSeries(user, studyInstanceUID, seriesInstanceUID, em)) {
                    return true;
                } else {
                    throw new SeriesNotFoundException("seriesInstanceUID : " + seriesInstanceUID + "not found");
                }
            } else if (getScope() == ScopeType.ALBUM && capability.isReadPermission()) {
                final AlbumUser albumUser = getAlbumUser(capability.getAlbum(), user, em);
                if (!albumUser.isAdmin()) {
                    return false;
                }
                return canAccessSeries(capability.getAlbum(), studyInstanceUID, seriesInstanceUID, em);
            }
        } catch (UserNotMemberException e) {
            return false;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return false;
    }

    @Override
    public boolean hasStudyReadAccess(String studyInstanceUID) {
        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();
            final Study study = getStudy(studyInstanceUID, em);
            if(getScope() == ScopeType.USER) {
                return canAccessStudy(user, study, em);
            } else if (getScope() == ScopeType.ALBUM && capability.isReadPermission()) {
                final AlbumUser albumUser = getAlbumUser(capability.getAlbum(), user, em);
                if (!albumUser.isAdmin()) {
                    return false;
                }
                return canAccessStudy(capability.getAlbum(), study, em);
            }
        } catch (StudyNotFoundException | UserNotMemberException e) {
            return false;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return false;
    }

    @Override
    public boolean hasUserAccess() { return getScope() == ScopeType.USER; }

    @Override
    public boolean hasSeriesWriteAccess(String studyInstanceUID, String seriesInstanceUID) {
        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        final User mergeUser = em.merge(user);
        if (getScope() == ScopeType.USER) {
            try {
                tx.begin();
                final Series series;
                try {
                    series = findSeriesByStudyUIDandSeriesUID(studyInstanceUID, seriesInstanceUID, em);
                } catch (NoResultException e) {
                    //if the series not exist
                    return true;
                }

                // we need to check here if the series that was found is owned by the user

                try {
                    findSeriesBySeriesAndUserInbox(mergeUser, series, em);
                    return true;
                } catch (NoResultException ignored) {/*empty*/}

                try {
                    findSeriesBySeriesAndAlbumWithSendPermission(mergeUser, series, em);
                    return true;
                } catch (NoResultException ignored) {
                    if (isOrphan(series, em)) {
                        return true;
                    }
                }
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }
            return false;

        } else if (getScope() == ScopeType.ALBUM) {
            if (!capability.isWritePermission()) {
                return false;
            }
            try {
                tx.begin();

                Capability mergeCapability = em.merge(capability);

                final AlbumUser albumUser = getAlbumUser(capability.getAlbum(), mergeUser, em);
                if (!albumUser.isAdmin()) {
                    return false;
                }
                final Series series;
                try {
                    series = findSeriesByStudyUIDandSeriesUID(studyInstanceUID, seriesInstanceUID, em);
                } catch (NoResultException e) {
                    //if the series not exist
                    return true;
                }
                if (isOrphan(series, em)) {
                    return true;
                }

                if(mergeCapability.getAlbum().containsSeries(series, em)) {
                    return true;
                }
            } catch (UserNotMemberException | NoResultException e) {
                return false;
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }
        }
        return false;
    }

    @Override
    public boolean hasStudyWriteAccess(String studyInstanceUID) {
        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        if (getScope() == ScopeType.USER) {
           return true;
        } else if (getScope() == ScopeType.ALBUM) {

            if (!capability.isWritePermission()) {
                return false;
            }
            try {
                tx.begin();
                final AlbumUser albumUser = getAlbumUser(capability.getAlbum(), user, em);
                if (albumUser.isAdmin()) {
                    return true;
                } else {
                    return false;
                }
            } catch (UserNotMemberException e) {
                return false;
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean hasAlbumPermission(UserPermissionEnum usersPermission, String albumId) {

        if (!this.hasAlbumAccess(albumId)) {
            return false;
        }

        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        try {
            tx.begin();
            if(getScope() == ScopeType.ALBUM){

                final Album album = em.merge(capability.getAlbum());
                isMemberOfAlbum(user, album, em);

                usersPermission.hasCapabilityPermission(capability);

            } else if (getScope() == ScopeType.USER) {

                final Album album = getAlbum(albumId, em);
                final AlbumUser albumUser = getAlbumUser(album, user, em);
                if(user.getInbox() == album) {
                    return false;
                }
                if (albumUser.isAdmin()) {
                    return true;
                }

                usersPermission.hasUserPermission(album);

            }
        } catch (UserNotMemberException | AlbumNotFoundException e) {
            return false;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        return false;
    }

    @Override
    public boolean hasAlbumAccess(String albumId) {
        this.em = EntityManagerListener.createEntityManager();
        this.tx = em.getTransaction();
        if (getScope() == ScopeType.ALBUM) {
            if (albumId  == null) {
                albumId = capability.getAlbum().getId();
            }
            if (albumId.compareTo(capability.getAlbum().getId()) == 0) {
                return false;
            }
            try {
                tx.begin();
                final Album album = getAlbum(albumId, em);
                final AlbumUser albumUser = getAlbumUser(album, user, em);
                if (!albumUser.isAdmin()) {
                    return false;
                }
                return user.getInbox() != album;
            } catch (UserNotMemberException | AlbumNotFoundException e) {
                return false;
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }
        } else if (getScope() == ScopeType.USER) {
            if (albumId == null) {
                return false;
            }
            try {
                tx.begin();
                final Album album = getAlbum(albumId, em);
                final AlbumUser albumUser = getAlbumUser(album, user, em);
                return user.getInbox() != album;
            } catch (UserNotMemberException  | AlbumNotFoundException e) {
                return false;
            } finally {
                if (tx.isActive()) {
                    tx.rollback();
                }
                em.close();
            }
        } else {
            return false;
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public ScopeType getScope() {
        return ScopeType.valueOf(capability.getScopeType().toUpperCase());
    }

    @Override
    public String getAlbumID() throws NotAlbumScopeTypeException {
        if(getScope() == ScopeType.ALBUM) {
            return capability.getAlbum().getId();
        } else {
            throw new NotAlbumScopeTypeException("");
        }
    }
}
