package net.disy.wps.lkn.mpa.types;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.joda.time.DateTime;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.generator.SimpleGMLGenerator;
import org.n52.wps.io.datahandler.parser.SimpleGMLParser;

/**
 * Wrapper-Klasse fuer ObservationFeatureCollection, die als
 * SimpleFeatureCollction angeboten werden.
 *
 * @author woessner
 */
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
        GTVectorDataBinding binding = new GTVectorDataBinding(this.sfc);
        StringWriter buffer = new StringWriter();
        SimpleGMLGenerator generator = new SimpleGMLGenerator();
        generator.write(binding, buffer);
        org.w3c.dom.Node node = generator.generateXML(binding, "");
        return node;

    }

    public void setFeatureCollectionJAXB(org.w3c.dom.Node xmlcontent) {
        String content = xmlcontent.toString();
        this.setFeatureCollectionJAXB(content);
    }

    public void setFeatureCollectionJAXB(String xmlcontent) {
        SimpleGMLParser parser = new SimpleGMLParser();

        InputStream stream = new ByteArrayInputStream(xmlcontent.getBytes());
        // use a default configuration for the parser by requesting the first supported format and schema
        GTVectorDataBinding data = parser.parse(stream, parser.getSupportedFormats()[0], parser.getSupportedEncodings()[0]);
        this.sfc = (SimpleFeatureCollection) data.getPayload();
    }

    @XmlTransient
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
