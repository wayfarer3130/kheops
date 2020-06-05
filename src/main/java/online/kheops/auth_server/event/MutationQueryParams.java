package online.kheops.auth_server.event;

import online.kheops.auth_server.album.BadQueryParametersException;
import online.kheops.auth_server.capability.CapabilityNotFoundException;
import online.kheops.auth_server.entity.User;
import online.kheops.auth_server.report_provider.ReportProviderNotFoundException;
import online.kheops.auth_server.series.Series;
import online.kheops.auth_server.series.SeriesNotFoundException;
import online.kheops.auth_server.study.StudyNotFoundException;
import online.kheops.auth_server.user.UserNotFoundException;
import online.kheops.auth_server.util.ErrorResponse;

import javax.persistence.EntityManager;
import javax.ws.rs.core.MultivaluedMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.*;

import static online.kheops.auth_server.capability.CapabilitiesQueries.findCapabilityByCapabilityIDAndAlbumId;
import static online.kheops.auth_server.report_provider.ReportProviderQueries.getReportProviderWithClientIdAndAlbumId;
import static online.kheops.auth_server.study.Studies.getStudy;
import static online.kheops.auth_server.user.Users.getUser;
import static online.kheops.auth_server.util.ErrorResponse.Message.BAD_QUERY_PARAMETER;

public class MutationQueryParams {

    private List<MutationType> types = new ArrayList<>();
    private List<String> users = new ArrayList<>();
    private List<String> studies = new ArrayList<>();
    private List<String> series = new ArrayList<>();
    private List<String> capabilityTokens = new ArrayList<>();
    private List<String> reportProviders = new ArrayList<>();
    private Optional<LocalDateTime> startDate = Optional.empty();
    private Optional<LocalDateTime> endDate = Optional.empty();


    public MutationQueryParams(MultivaluedMap<String, String> queryParameters, String albumId, EntityManager em)
            throws BadQueryParametersException {

        extractTypes(queryParameters);
        extractFamilies(queryParameters);
        extractUsers(queryParameters, em);
        extractStudies(queryParameters, em);
        extractSeries(queryParameters, em);
        extractReportProviders(queryParameters, albumId, em);
        extractCapabilityToken(queryParameters, albumId, em);
        extractDate(queryParameters);

    }

    private void extractTypes(MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {

        if (queryParameters.containsKey("type")) {
            for (String type:queryParameters.get("type")) {
                try {
                    types.add(MutationType.valueOf(type));
                } catch (IllegalArgumentException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'type' is not valid")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
    }

    private void extractFamilies(MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {
        if (queryParameters.containsKey("family")) {
            for (String family:queryParameters.get("family")) {
                try {
                    types.addAll(MutationTypeFamily.valueOf(family.toUpperCase()).getMutationTypes());
                } catch (IllegalArgumentException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'family' is not valid")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
    }

    private void extractUsers(MultivaluedMap<String, String> queryParameters, EntityManager em)
            throws BadQueryParametersException {
        if (queryParameters.containsKey("user")) {
            for (String user:queryParameters.get("user")) {
                try {
                    final User user1 = getUser(user, em);
                    users.add(user1.getSub());
                } catch (UserNotFoundException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'user' not found")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
    }

    private void extractStudies(MultivaluedMap<String, String> queryParameters, EntityManager em)
            throws BadQueryParametersException {
        if (queryParameters.containsKey("studies")) {
            for (String study:queryParameters.get("studies")) {
                try {
                    getStudy(study, em);
                    studies.add(study);
                } catch (StudyNotFoundException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'studies' not found")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
    }

    private void extractSeries(MultivaluedMap<String, String> queryParameters, EntityManager em)
            throws BadQueryParametersException {
        if (queryParameters.containsKey("series")) {
            for (String seriesUID:queryParameters.get("series")) {
                try {
                    Series.getSeries(seriesUID, em);
                    series.add(seriesUID);
                } catch (SeriesNotFoundException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'series' not found")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
    }

    private void extractReportProviders(MultivaluedMap<String, String> queryParameters, String albumId, EntityManager em)
            throws BadQueryParametersException {
        if (queryParameters.containsKey("reportProvider")) {
            for (String reportProvider:queryParameters.get("reportProvider")) {
                try {
                    getReportProviderWithClientIdAndAlbumId(reportProvider, albumId, em);
                    reportProviders.add(reportProvider);
                } catch (ReportProviderNotFoundException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'reportProvider' not found")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
    }

    private void extractCapabilityToken(MultivaluedMap<String, String> queryParameters, String albumId, EntityManager em)
            throws BadQueryParametersException {
        if (queryParameters.containsKey("capabilityToken")) {
            for (String capabilityToken:queryParameters.get("capabilityToken")) {
                try {
                    findCapabilityByCapabilityIDAndAlbumId(capabilityToken, albumId, em);
                    capabilityTokens.add(capabilityToken);
                } catch (CapabilityNotFoundException e) {
                    final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                            .message(BAD_QUERY_PARAMETER)
                            .detail("'capabilityToken' not found")
                            .build();
                    throw new BadQueryParametersException(errorResponse);
                }
            }
        }
    }

    final DateTimeFormatter startDateformatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart()
            .appendPattern(" HH:mm:ss")
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter();
    final DateTimeFormatter endDateformatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .optionalStart()
            .appendPattern(" HH:mm:ss")
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 23)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 59)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 59)
            .toFormatter();

    private void extractDate(MultivaluedMap<String, String> queryParameters)
            throws BadQueryParametersException {
        try {
            if (queryParameters.containsKey("startDate")) {
                startDate = Optional.of(LocalDateTime.parse(queryParameters.get("startDate").get(0), startDateformatter));
            }
            if (queryParameters.containsKey("endDate")) {
                endDate = Optional.of(LocalDateTime.parse(queryParameters.get("endDate").get(0), endDateformatter));
            }
        } catch (DateTimeParseException e) {
            final ErrorResponse errorResponse = new ErrorResponse.ErrorResponseBuilder()
                    .message(BAD_QUERY_PARAMETER)
                    .detail("'startDate' or 'endDate' parsing error format must be 'yyyy-MM-dd HH:mm:ss' or 'yyyy-MM-dd'")
                    .build();
            throw new BadQueryParametersException(errorResponse);
        }
    }

    public List<MutationType> getTypes() { return types; }
    public List<String> getUsers() { return users; }
    public List<String> getStudies() { return studies; }
    public List<String> getSeries() { return series; }
    public List<String> getCapabilityTokens() { return capabilityTokens; }
    public List<String> getReportProviders() { return reportProviders; }
    public Optional<LocalDateTime> getStartDate() { return startDate; }
    public Optional<LocalDateTime> getEndDate() { return endDate; }
}
