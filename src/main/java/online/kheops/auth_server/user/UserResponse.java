package online.kheops.auth_server.user;

import online.kheops.auth_server.capability.CapabilitiesResponse;
import online.kheops.auth_server.entity.AlbumUser;
import online.kheops.auth_server.entity.Capability;
import online.kheops.auth_server.entity.ReportProvider;
import online.kheops.auth_server.report_provider.ReportProviderResponse;

import javax.xml.bind.annotation.XmlElement;

public class UserResponse  implements Comparable<UserResponse> {

    //Mandatory
    @XmlElement(name = "email")
    private String email;
    @XmlElement(name = "last_name")
    private String lastName;
    @XmlElement(name = "first_name")
    private String firstName;
    @XmlElement(name = "sub")
    private String sub;

    //For event
    @XmlElement(name = "can_access")
    private Boolean canAccess;

    //For users in album
    @XmlElement(name = "is_admin")
    private Boolean isAdmin;

    //For users
    @XmlElement(name = "study_access")
    private Boolean studyAccess;
    @XmlElement(name = "album_access")
    private Boolean albumAccess;

    //For webhook new series
    //@XmlElement(name = "report_provider")
    //private ReportProviderResponse reportProvider;
    //@XmlElement(name = "capability_token")
    //private CapabilitiesResponse capability;

    private UserResponse() { /*empty*/ }

    public UserResponse(AlbumUser albumUser) {
        email = albumUser.getUser().getEmail();
        firstName = albumUser.getUser().getFirstName();
        lastName = albumUser.getUser().getLastName();
        isAdmin = albumUser.isAdmin();
        sub = albumUser.getUser().getKeycloakId();
    }

    protected UserResponse(UserResponseBuilder userResponseBuilder) {
        email = userResponseBuilder.getEmail();
        sub = userResponseBuilder.getSub();
        firstName = userResponseBuilder.getFirstName();
        lastName = userResponseBuilder.getLastName();

        albumAccess = userResponseBuilder.getAlbumAccess();
        studyAccess = userResponseBuilder.getStudyAccess();
        userResponseBuilder.getCanAccess().ifPresent(value -> canAccess = value);
    }

    /*public void setReportProvider(ReportProvider reportProvider) {
        this.reportProvider = new ReportProviderResponse(reportProvider);
        this.reportProvider.webhookResponse();
    }

    public void setCapabilityToken(Capability capability) {
        this.capability = new CapabilitiesResponse(capability);
    }*/

    public String getSub() { return sub; }

    @Override
    public int compareTo(UserResponse userResponse) {
        return email.compareTo(userResponse.email);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof UserResponse) {
            final UserResponse userAlbumResponse = (UserResponse) obj;
            return  userAlbumResponse.sub.compareTo(sub) == 0 &&
                    userAlbumResponse.isAdmin == isAdmin &&
                    userAlbumResponse.email.compareTo(email) == 0;
        }
        return false;
    }

    private int hashCode;
    @Override
    public int hashCode() {
        int result = hashCode;
        if(result == 0) {
            result = sub.hashCode();
            result = 31 * result + isAdmin.hashCode();
            result = 31 * result + email.hashCode();
            result = 31 * result + lastName.hashCode();
            result = 31 * result + firstName.hashCode();
            hashCode = result;
        }
        return result;
    }
}