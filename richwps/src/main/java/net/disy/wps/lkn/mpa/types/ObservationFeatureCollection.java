package net.disy.wps.lkn.mpa.types;

import javax.xml.bind.annotation.XmlElement;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.joda.time.DateTime;

/**
 * Wrapper-Klasse fuer ObservationFeatureCollection, die als
 * SimpleFeatureCollction angeboten werden.
 *
 * @author woessner
 */
public class ObservationFeatureCollection implements Comparable<ObservationFeatureCollection> {

    private DateTime obsTime;
    private Double area;
    private SimpleFeatureCollection sfc;
    
    public ObservationFeatureCollection(DateTime obsTime, SimpleFeatureCollection sfc, Double area) {
        this.obsTime = obsTime;
        this.area = area;
        this.sfc = sfc;
    }

    @XmlElement(name = "ObservationTime")
    public DateTime getDateTime() {
        return this.obsTime;
    }

    @XmlElement(name = "ObservationArea")
    public Double getArea() {
        return this.area;
    }

    /*@XmlElement(name = "ObservationFeature")
     public SimpleFeatureCollection getFeatureCollectionPersistence() {
     return  (SimpleFeatureCollection) this.sfc;
    }*/

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
