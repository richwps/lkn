package net.disy.wps.lkn.mpa.types;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import net.disy.wps.lkn.utils.FeatureCollectionUtil;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.joda.time.DateTime;

/**
 * Wrapper-Klasse zum Vorhalten von Verschneidungsergebnissen als
 * SimpleFeatureCollection.
 *
 * @author woessner
 * @author dalcacer
 * @see ObservationFeatureCollection
 */
public class IntersectionFeatureCollection implements Comparable<IntersectionFeatureCollection> {

    private Integer reportingArea;
    private transient DateTime obsTime;
    private Double area;
    private SimpleFeatureCollection sfc;

    public IntersectionFeatureCollection() {
    }

    public IntersectionFeatureCollection(Integer reportingArea, DateTime obsTime,
            SimpleFeatureCollection fc, Double area) {
        this.sfc = fc;

        this.reportingArea = reportingArea;
        this.area = area;
        this.obsTime = obsTime;
    }

    @XmlElement(name = "IntersectionArea")
    public Integer getReportingArea() {
        return this.reportingArea;
    }

    @XmlElement(name = "ObservationTime")
    public Long getDateTimeJAXB() {
        return this.obsTime.getMillis();
    }

    public void setDateTimeJAXB(Long time) {
        this.obsTime = new DateTime(time);
    }

    @XmlTransient
    public DateTime getDateTime() {
        return this.obsTime;
    }

    public void setDateTime(DateTime dt) {
        this.obsTime = dt;
    }

    @XmlElement(name = "ObservationArea")
    public Double getArea() {
        return this.area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    @XmlAnyElement
    public org.w3c.dom.Node getFeatureCollectionJAXB() {
        return FeatureCollectionUtil.toNode(this.sfc);
    }

    public void setFeatureCollectionJAXB(org.w3c.dom.Node xmlcontent) {
        this.sfc = FeatureCollectionUtil.fromNode(xmlcontent);
    }

    public SimpleFeatureCollection getFeatureCollection() {
        return this.sfc;
    }

    @Override
    public int compareTo(IntersectionFeatureCollection coll) {
        return this.getDateTime().compareTo(coll.getDateTime());
    }

}
