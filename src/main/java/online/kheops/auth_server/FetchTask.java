package online.kheops.auth_server;

import online.kheops.auth_server.entity.Series;
import online.kheops.auth_server.entity.Study;
import org.dcm4che3.data.Attributes;

import javax.persistence.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class FetchTask implements Runnable {

    private final URI dicomWebURI;

    public FetchTask(URI dicomWebURI) {
        this.dicomWebURI = dicomWebURI;
    }

    private static class UIDPair {
        private String studyInstanceUID;
        private String seriesInstanceUID;

        public UIDPair(String studyInstanceUID, String seriesInstanceUID) {
            this.studyInstanceUID = studyInstanceUID;
            this.seriesInstanceUID = seriesInstanceUID;
        }

        String getStudyInstanceUID() {
            return studyInstanceUID;
        }

        String getSeriesInstanceUID() {
            return seriesInstanceUID;
        }
    }

    @Override
    public void run() {
        System.out.println("fetching series");

        fetchUnpopulatedSeries(unpopulatedSeriesUIDs());
        fetchUnpopulatedStudies(unpopulatedStudyUIDs());
    }

    private List<UIDPair> unpopulatedSeriesUIDs() {
        EntityManagerFactory factory = PersistenceUtils.createEntityManagerFactory();
        EntityManager em = factory.createEntityManager();
        List<UIDPair> unpopulatedSeriesUIDs;

        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            //noinspection JpaQlInspection
            TypedQuery<UIDPair> query = em.createQuery("select new online.kheops.auth_server.FetchTask$UIDPair(s.study.studyInstanceUID, s.seriesInstanceUID) from Series s where s.populated = false", UIDPair.class);
            query.setMaxResults(30);
            unpopulatedSeriesUIDs = query.getResultList();

            tx.commit();
        } finally {
            em.close();
            factory.close();
        }

        return unpopulatedSeriesUIDs;
    }

    private List<String> unpopulatedStudyUIDs() {
        EntityManagerFactory factory = PersistenceUtils.createEntityManagerFactory();
        EntityManager em = factory.createEntityManager();
        List<String> unpopulatedStudyUIDs;

        try {
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            TypedQuery<String> query = em.createQuery("select s.studyInstanceUID from Study s where s.populated = false", String.class);
            query.setMaxResults(30);
            unpopulatedStudyUIDs = query.getResultList();

            tx.commit();
        } finally {
            em.close();
            factory.close();
        }

        return unpopulatedStudyUIDs;
    }

    private void fetchUnpopulatedSeries(List<UIDPair> unpopulatedSeriesUIDs) {
        UriBuilder uriBuilder = UriBuilder.fromUri(dicomWebURI).path("studies/{StudyInstanceUID}/series").queryParam("SeriesInstanceUID", "{SeriesInstanceUID}");

        Client client = ClientBuilder.newClient();
        client.register(AttributesListMarshaller.class);

        for (UIDPair seriesUID: unpopulatedSeriesUIDs) {
            URI uri = uriBuilder.build(seriesUID.getStudyInstanceUID(), seriesUID.getSeriesInstanceUID());

            try {
                List<Attributes> seriesList = client.target(uri).request().accept("application/dicom+json").get(new GenericType<List<Attributes>>() {});
                if (seriesList == null || seriesList.size() < 1) {
                    continue;
                }
                Attributes attributes = seriesList.get(0);

                EntityManagerFactory factory = PersistenceUtils.createEntityManagerFactory();
                EntityManager em = factory.createEntityManager();
                try {
                    EntityTransaction tx = em.getTransaction();
                    tx.begin();

                    TypedQuery<Series> query = em.createQuery("select s from Series s where s.seriesInstanceUID = :seriesInstanceUID", Series.class);
                    query.setParameter("seriesInstanceUID", seriesUID.seriesInstanceUID);
                    query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

                    Series series = query.getSingleResult();
                    series.mergeAttributes(attributes);

                    tx.commit();
                } finally {
                    em.close();
                    factory.close();
                }
            } catch (Throwable t) {
                System.out.println(t.getLocalizedMessage());
            }
        }
    }

    private void fetchUnpopulatedStudies(List<String> unpopulatedStudyUIDs) {
        UriBuilder uriBuilder = UriBuilder.fromUri(dicomWebURI).path("studies").queryParam("StudyInstanceUID", "{StudyInstanceUID}");

        Client client = ClientBuilder.newClient();
        client.register(AttributesListMarshaller.class);

        for (String studyInstanceUID: unpopulatedStudyUIDs) {
            URI uri = uriBuilder.build(studyInstanceUID);

            try {
                List<Attributes> studyList = client.target(uri).request().accept("application/dicom+json").get(new GenericType<List<Attributes>>() {});
                if (studyList == null || studyList.size() < 1) {
                    continue;
                }
                Attributes attributes = studyList.get(0);

                EntityManagerFactory factory = PersistenceUtils.createEntityManagerFactory();
                EntityManager em = factory.createEntityManager();
                try {
                    EntityTransaction tx = em.getTransaction();
                    tx.begin();

                    TypedQuery<Study> query = em.createQuery("select s from Study s where s.studyInstanceUID = :studyInstanceUID", Study.class);
                    query.setParameter("studyInstanceUID", studyInstanceUID);
                    query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

                    Study study = query.getSingleResult();
                    study.mergeAttributes(attributes);

                    tx.commit();
                } finally {
                    em.close();
                    factory.close();
                }
            } catch (Throwable t) {
                System.out.println(t.getLocalizedMessage());
            }
        }

    }

}
