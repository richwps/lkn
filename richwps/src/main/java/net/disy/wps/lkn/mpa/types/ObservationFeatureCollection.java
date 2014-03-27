package net.disy.wps.lkn.mpa.types;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import net.disy.wps.lkn.utils.FeatureCollectionUtil;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.joda.time.DateTime;

/**
 * Wrapper-Klasse fuer ObservationFeatureCollection, die als
 * SimpleFeatureCollction angeboten werden.
 *
 * @author woessner
 * @author dalcacer
 */
//woessner design
//dalcacer persistence
public class ObservationFeatureCollection implements Comparable<ObservationFeatureCollection> {

    private transient DateTime obsTime;
    private Double area;
    private SimpleFeatureCollection sfc;

    public ObservationFeatureCollection() {
    }

    public ObservationFeatureCollection(DateTime obsTime, SimpleFeatureCollection sfc, Double area) {
        this.obsTime = obsTime;
        this.area = area;
        this.sfc = sfc;
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

    public String toString() {
        String year = ((Integer) this.obsTime.getYear()).toString();
        String month = ((Integer) this.obsTime.getMonthOfYear()).toString();
        String day = ((Integer) this.obsTime.getDayOfMonth()).toString();
        String size = ((Integer) this.sfc.size()).toString();
        String area = this.area.toString();
        return year + '-' + month + '-' + day + ' ' + size + ' ' + area;
    }

    @Override
    public int compareTo(ObservationFeatureCollection coll) {
        return this.getDateTime().compareTo(coll.getDateTime());
    }
}
