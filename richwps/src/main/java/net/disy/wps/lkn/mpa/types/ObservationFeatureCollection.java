package net.disy.wps.lkn.mpa.types;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.opengis.examples.packet.GMLPacketDocument;
import org.apache.xerces.dom.DocumentImpl;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.joda.time.DateTime;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.generator.GML3BasicGenerator;
import org.n52.wps.io.datahandler.generator.SimpleGMLGenerator;
import org.n52.wps.io.datahandler.parser.GML32BasicParser;
import org.n52.wps.io.datahandler.parser.SimpleGMLParser;
import org.w3c.dom.Document;

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
        //outsource to utils
        InputStream is = null;
        org.w3c.dom.Node node = null;
        GTVectorDataBinding binding = new GTVectorDataBinding(this.sfc);
        org.n52.wps.io.datahandler.generator.GML3BasicGenerator generator = new GML3BasicGenerator();
        try {
            is = generator.generateStream(binding, null, null);
            InputStreamReader isr = new InputStreamReader(is);
            org.w3c.dom.Document doc = ObservationFeatureCollection.loadXMLFrom(is);
            node = doc.getFirstChild();

        } catch (Exception ex) {
            Logger.getLogger(ObservationFeatureCollection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(ObservationFeatureCollection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return node;
    }

    public static org.w3c.dom.Document loadXMLFrom(java.io.InputStream is)
            throws org.xml.sax.SAXException, java.io.IOException {
        javax.xml.parsers.DocumentBuilderFactory factory
                = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
        }
        org.w3c.dom.Document doc = builder.parse(is);
        is.close();
        return doc;
    }

    public void setFeatureCollectionJAXB(org.w3c.dom.Node xmlcontent) {
        try {
            StringWriter writer = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(xmlcontent), new StreamResult(writer));
            String content = writer.toString();
            //System.err.println(this.getClass().toString() + "setFeatureCollectionJAXB#node " + content);
            this.setFeatureCollectionJAXB(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setFeatureCollectionJAXB(String xmlcontent) {
        //System.err.println(this.getClass().toString() + "setFeatureCollectionJAXB#string " + xmlcontent);
        InputStream stream = new ByteArrayInputStream(xmlcontent.getBytes());

        GML32BasicParser parser = new GML32BasicParser();
        //SimpleGMLParser parser = new SimpleGMLParser();

        // use a default configuration for the parser by requesting the  first supported format and schema
        //
        GTVectorDataBinding data = null;
        try {
          data = parser.parse(stream, parser.getSupportedFormats()[0], parser.getSupportedEncodings()[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.sfc = (SimpleFeatureCollection) data.getPayload();
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
