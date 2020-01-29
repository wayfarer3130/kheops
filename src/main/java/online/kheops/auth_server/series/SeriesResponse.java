package online.kheops.auth_server.series;

import online.kheops.auth_server.entity.Series;

import javax.xml.bind.annotation.XmlElement;

public class SeriesResponse {

    @XmlElement(name = "modality")
    private String modality;
    @XmlElement(name = "series_description")
    private String seriesDescription;
    @XmlElement(name = "series_uid")
    private String seriesUid;
    @XmlElement(name = "number_of_series_related_instance")
    private long numberOfSeriesRelatedInstance;
    @XmlElement(name = "time_zone_offset_from_utc")
    private String timeZoneOffsetFromUTC;
    @XmlElement(name = "series_number")
    private long seriesNumber;
    @XmlElement(name = "body_part_examined")
    private String bodyPartExamined;


    private SeriesResponse() { /*empty*/ }

    public SeriesResponse(Series series) {
        modality = series.getModality();
        numberOfSeriesRelatedInstance = series.getNumberOfSeriesRelatedInstances();
        seriesDescription = series.getSeriesDescription();
        seriesUid = series.getSeriesInstanceUID();
        timeZoneOffsetFromUTC = series.getTimezoneOffsetFromUTC();
        seriesNumber = series.getSeriesNumber();
        bodyPartExamined = series.getBodyPartExamined();
    }
}
