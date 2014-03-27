package net.disy.wps.lkn.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.style.ContrastMethod;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.disy.wps.common.DescriptorContainer;
import net.disy.wps.lkn.mpa.types.ObservationFeatureCollection;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.generator.GML3BasicGenerator;
import org.n52.wps.io.datahandler.parser.GML32BasicParser;

/**
 * Diese Klasse beinhaltet allgemeine Hilfsfunktionen und Tools fuer die
 * Implementierung von WPS-Prozessen im RichWPS-Projekt
 *
 * @author woessner
 * @author dalcacer
 */
//dalcacer transformation + persistence
public abstract class FeatureCollectionUtil {

    /**
     * Gibt ein String-Array mit eindeutigen Werten eines Feldes in einer
     * FeatureCollection zurueck.
     *
     * @param sfc - SimpleFeatureCollection
     * @param field - Feldname
     * @return
     * @deprecated
     */
    public static String[] getValuesFromField(SimpleFeatureCollection sfc,
            String field) {
        String[] values = {""};
        HashSet<String> valuesHS = new HashSet<String>();

        FeatureIterator<SimpleFeature> iter = sfc.features();

        while (iter.hasNext()) {
            SimpleFeature feature = iter.next();
            valuesHS.add(feature.getAttribute("field").toString());
        }

        Iterator<String> iter2 = valuesHS.iterator();
        int i = 0;
        while (iter2.hasNext()) {
            values[i] = iter2.next();
            i++;
        }
        return values;
    }

    /**
     * Berechnet die Gesamtflaeche aller Features einer SimpleFeatureCollection
     *
     * @param sfc SimpleFeatureCollection fuer die die Gesamtflaeche berechnet
     * werden soll
     * @return Gesamtflaeche der SimpleFeatureCollection
     */
    public static Double getArea(SimpleFeatureCollection sfc) {

        FeatureIterator<SimpleFeature> iter = sfc.features();
        Double area = 0.0;
        try {
            while (iter.hasNext()) {
                SimpleFeature feature = iter.next();
                area += ((Geometry) feature.getDefaultGeometry()).getArea();
            }
        } finally {
            iter.close();
        }
        return area;
    }

    /**
     * Gibt einen veraenderten FeatureType zurueck: Attribute in Form von
     * DescriptorContainer(n) werden einem bestehenden FeatureType hinzugefuegt,
     * ein neuer Name wird vergeben und der Geometrietyp kann veraendert werden.
     *
     * @param previousFeatureType - Bestehender FeatureType
     * @param descrContainerList - Liste von DescriptorContainern, Attribute die
     * hinzugefuegt werden sollen
     * @param newFeatureTypeName - Name des neuen FeatureTypes
     * @param newGeomType - Geometrietyp des neuen FeatureTypes
     * @return
     */
    public static SimpleFeatureType refactorFeatureType(
            SimpleFeatureType previousFeatureType,
            ArrayList<DescriptorContainer> descrContainerList,
            String newFeatureTypeName, Class<?> newGeomType) {

        // Get Builder with Descriptors of current FeatureType
        // SimpleFeatureTypeBuilder builder = getBuilderFromFeaturType(currFt);
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        String geomDescrName;

        // Initialize
        builder.init(previousFeatureType);
        builder.setName(newFeatureTypeName);

        if (previousFeatureType != null) {
            geomDescrName = previousFeatureType.getGeometryDescriptor()
                    .getLocalName();
        } else {
            geomDescrName = "GEOM";
        }
        // Remove Geometry Descriptor and readd it with specified type
        if (newGeomType != null) {
            builder.remove(geomDescrName);
            builder.add(geomDescrName, newGeomType);
        }
        builder.setDefaultGeometry(geomDescrName);

        // Add new Descriptors
        if (descrContainerList != null) {
            for (DescriptorContainer dc : descrContainerList) {
                builder.minOccurs(dc.minOccurs).maxOccurs(dc.maxOccurs)
                        .nillable(dc.isNillable).add(dc.name, dc.binding);
            }
        }

        SimpleFeatureType newFeatureType = builder.buildFeatureType();

        return newFeatureType;
    }

    /**
     * Initialisiert die Werte eines FeatureBuilders mit den eines bestehenden
     * Features Danach koennen dem Builder weitere Werte manuell hinzugefuegt
     * werden
     *
     * @param inputBuilder - SimpleFeatureBuilder, der initialisiert werden soll
     * @param sourceFeature - SimpleFeature, aus dem die Werte zur
     * Initialisierung entnommen werden
     * @return
     */
    public static SimpleFeatureBuilder initBuilderValues(
            SimpleFeatureBuilder inputBuilder, SimpleFeature sourceFeature) {
        int attrCount = sourceFeature.getAttributeCount();
        for (int i = 0; i < attrCount; i++) {
            inputBuilder.set(inputBuilder.getFeatureType()
                    .getAttributeDescriptors().get(i).getLocalName(),
                    sourceFeature.getAttribute(i));
        }
        return inputBuilder;
    }

    /**
     * Filtert eine SimpleFeatureCollection nach den in 'keys' und 'values'
     * vorgegebenen Attribut-Eigenschaften und gibt einen Extrakt aus der
     * uebergebenen Collection zurueck
     *
     * @param inputCollection - SimpleFeatureCollection,
     * @param keys - Array von Strings, das die Namen der Schluessel enthaelt
     * @param values - Array von String, das die Namen der Werte enthaelt
     * @return
     */
    public static SimpleFeatureCollection extractEquals(SimpleFeatureCollection inputCollection, String[] keys, String[] values) {
        SimpleFeatureCollection outputCollection = FeatureCollections.newCollection();
        // Schleife ueber keys, zum Erzeugen mehrerer Filter
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String value = values[i];
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
            Filter filter = ff.equals(ff.property(key), ff.literal(value));
            FeatureIterator<SimpleFeature> iter = inputCollection.features();
            // Schleife ueber Features und Evaluierung
            while (iter.hasNext()) {
                SimpleFeature feature = iter.next();
                if (filter.evaluate(feature)) {
                    // Feature der outputCollection hinzufuegen
                    outputCollection.add(feature);
                }
            }
        }
        return outputCollection;
    }

    /**
     * Erzeugt eine Union von Geometrie-Collection und gibt diese zurueck
     *
     * @param geometryCollection - Collection von Geometrien, aus denen die
     * Union gebildet werden soll
     * @return
     */
    public static Geometry union(Collection<Geometry> geometryCollection) {
        Geometry unionGeom = null;
        for (Iterator<Geometry> i = geometryCollection.iterator(); i.hasNext();) {
            Geometry geometry = i.next();
            if (geometry != null) {
                if (unionGeom == null) {
                    unionGeom = geometry;
                } else {
                    unionGeom = unionGeom.union(geometry);
                }
            }
        }
        return unionGeom;
    }

    /**
     * Erzeugt die Union der Features einer FeatureCollection
     *
     * @param sfc
     * @return
     * @deprecated
     */
    public static SimpleFeatureCollection getUnionSfc(
            SimpleFeatureCollection sfc) {
        SimpleFeatureCollection outputCollection = FeatureCollections
                .newCollection();
        Geometry union;
        ArrayList<Geometry> geomCollection = new ArrayList<Geometry>();
        SimpleFeatureType ft = sfc.getSchema();
        ft = refactorFeatureType(ft, null, "UnionFeatureType", Polygon.class);
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(ft);

        SimpleFeatureIterator iter = sfc.features();
        while (iter.hasNext()) {
            geomCollection.add((Geometry) iter.next().getDefaultGeometry());
        }

        union = union(geomCollection);

        builder.set(sfc.getSchema().getGeometryDescriptor().getLocalName(),
                union);
        outputCollection.add(builder.buildFeature(null));

        return outputCollection;
    }

    /**
     * Gibt die Anzahl der Features in einer SimpleFeatureCollection zurueck
     *
     * @param sfc SimpleFeatureCollection
     * @return Anzahl der Features
     */
    public static int size(SimpleFeatureCollection sfc) {
        return sfc.size();
    }

    /**
     * Gibt die Summe der Stuetzpunkte aller Geometrien einer
     * SimpleFeatureCollection zurueck
     *
     * @param sfc SimpleFeatureCollection
     * @return Summe der Stuetzpunkte
     */
    public static int getNumVerticesFromFC(SimpleFeatureCollection sfc) {
        int num = 0;
        SimpleFeatureIterator iter = sfc.features();

        try {
            while (iter.hasNext()) {
                num += ((Geometry) iter.next().getDefaultGeometry())
                        .getCoordinates().length;
            }
        } finally {
            iter.close();
        }
        return num;
    }

    /**
     * Gibt die mittlere Anzahl an Stuetzpunkten alle Features einer
     * SimpleFeatureCollection zurueck
     *
     * @param sfc SimpleFeaturecollection
     * @return Mittlere Anzahl Stuetzpunkte
     */
    public static double getMeanVerticesFromFC(SimpleFeatureCollection sfc) {
        int numFeatures = size(sfc);
        if (numFeatures != 0) {
            return (double) getNumVerticesFromFC(sfc) / (double) size(sfc);
        } else {
            return 0.0;
        }
    }

    public static String toDecimalStr(Double value) {
        return String.valueOf(Math.round(value * 100.0) / 100.0);
    }

    public static String tokm2Str(Double valueInSquareMeters) {
        return String.valueOf(Math
                .round((valueInSquareMeters / 1000000) * 100.0) / 100.0);
    }

    public static boolean attributeIsTimeStamp(SimpleFeatureCollection sfc,
            String attributeName) {
        SimpleFeatureIterator iter = sfc.features();

        SimpleFeature feature = iter.next();
        iter.close();
        if (feature.getAttribute(attributeName) instanceof java.sql.Timestamp) {
            return true;
        }
        return false;
    }

    /**
     * Erzeugt aus einer FeatureCollection von MultiGeometrien eine
     * FeatureCollection mit einfachen Geometrien
     *
     * Achtung: Wird nicht mehr verwendet, funktioniert aber fuer Polygone
     *
     * @param multiGeometryCollection
     * @return
     * @deprecated
     */
    public static SimpleFeatureCollection getSimpleGeometryCollection(
            SimpleFeatureCollection multiGeometryCollection) {
        SimpleFeatureCollection fc = FeatureCollections.newCollection();
        SimpleFeatureType ft = multiGeometryCollection.getSchema();

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(
                refactorFeatureType(ft, null, "SimpleGeometryFeatureType",
                        Polygon.class));

        SimpleFeatureIterator iter = multiGeometryCollection.features();
        while (iter.hasNext()) {
            SimpleFeature feature = iter.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            if (geometry instanceof GeometryCollection) {
                featureBuilder = initBuilderValues(featureBuilder, feature);
                for (int i = 0; i < geometry.getNumGeometries(); i++) {
                    Geometry partGeometry = geometry.getGeometryN(i);
                    featureBuilder.set(feature.getDefaultGeometryProperty()
                            .getName().toString(), partGeometry);
                    fc.add(featureBuilder.buildFeature(null));
                }
            } else {
                fc.add(feature);
            }
        }
        return fc;
    }

    /**
     * Prueft die Geometrien von Features einer FeatureCollection auf Validitaet
     *
     * @param multiGeometryCollection
     * @return
     */
    public static boolean checkValid(
            SimpleFeatureCollection multiGeometryCollection) {
        boolean valid = true;

        SimpleFeatureIterator iter = multiGeometryCollection.features();
        while (iter.hasNext()) {
            SimpleFeature feature = iter.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            if (!geometry.isValid()) {
                valid = false;
            }
        }
        return valid;
    }

    /**
     * Schreibt einen InputStream in eine temporaere Datei
     *
     * @param in InputStream
     * @param prefix Praefix der tempor�ren Datei
     * @param suffix Suffix der tempor�ren Datei
     * @return
     * @throws IOException
     */
    public static File stream2file(InputStream in, String prefix, String suffix)
            throws IOException {
        final File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();
        try {
            FileOutputStream out = new FileOutputStream(tempFile);
            IOUtils.copy(in, out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return tempFile;
    }

    /**
     * Erzeugt einen Graufstufen-Stil zum Rendern eines Coverage2DLayers
     *
     * @param band Farbband das zur Darstellung verwendet wird
     * @return
     */
    public static Style createGreyscaleStyle(int band) {
        final StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
        final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0),
                ContrastMethod.NORMALIZE);
        SelectedChannelType sct = sf.createSelectedChannelType(
                String.valueOf(band), ce);

        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);
    }

    /**
     * Erzeugt einen RGB-Stil zum Rendern einer GeoTIFF-Datei �bernommen aus
     * Geotools-Tutorial
     *
     * @param reader
     * @return
     */
    public static Style createRGBStyle(GeoTiffReader reader) {
        final StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
        final FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        GridCoverage2D cov = null;
        try {
            cov = reader.read(null);
        } catch (IOException giveUp) {
            throw new RuntimeException(giveUp);
        }

        int numBands = cov.getNumSampleDimensions();
        if (numBands < 3) {
            return null;
        }

        String[] sampleDimensionNames = new String[numBands];
        for (int i = 0; i < numBands; i++) {
            GridSampleDimension dim = cov.getSampleDimension(i);
            sampleDimensionNames[i] = dim.getDescription().toString();
        }
        final int RED = 0, GREEN = 1, BLUE = 2;
        int[] channelNum = {-1, -1, -1};

        for (int i = 0; i < numBands; i++) {
            String name = sampleDimensionNames[i].toLowerCase();
            if (name != null) {
                if (name.matches("red.*")) {
                    channelNum[RED] = i + 1;
                } else if (name.matches("green.*")) {
                    channelNum[GREEN] = i + 1;
                } else if (name.matches("blue.*")) {
                    channelNum[BLUE] = i + 1;
                }
            }
        }

        if (channelNum[RED] < 0 || channelNum[GREEN] < 0
                || channelNum[BLUE] < 0) {
            channelNum[RED] = 1;
            channelNum[GREEN] = 2;
            channelNum[BLUE] = 3;
        }

        SelectedChannelType[] sct = new SelectedChannelType[cov
                .getNumSampleDimensions()];
        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0),
                ContrastMethod.NONE);
        for (int i = 0; i < 3; i++) {
            sct[i] = sf.createSelectedChannelType(
                    String.valueOf(channelNum[i]), ce);
        }
        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct[RED], sct[GREEN],
                sct[BLUE]);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);
    }

    public static org.w3c.dom.Document toDocument(SimpleFeatureCollection sfc) {
        InputStream is = null;
        GTVectorDataBinding binding = new GTVectorDataBinding(sfc);
        org.w3c.dom.Document doc = null;
        org.n52.wps.io.datahandler.generator.GML3BasicGenerator generator = new GML3BasicGenerator();
        try {
            is = generator.generateStream(binding, null, null);
            InputStreamReader isr = new InputStreamReader(is);
            doc = FeatureCollectionUtil.loadXMLFrom(is);

        } catch (Exception ex) {
            Logger.getLogger(ObservationFeatureCollection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(ObservationFeatureCollection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return doc;
    }

    public static org.w3c.dom.Node toNode(SimpleFeatureCollection sfc) {
        org.w3c.dom.Document doc = toDocument(sfc);
        org.w3c.dom.Node node = doc.getFirstChild();
        return node;
    }

    public static SimpleFeatureCollection fromNode(org.w3c.dom.Node xmlcontent) {
        String stringcontent = FeatureCollectionUtil.stringFromNode(xmlcontent);
        InputStream stream = new ByteArrayInputStream(stringcontent.getBytes());
        GML32BasicParser parser = new GML32BasicParser();
        GTVectorDataBinding data = null;
        try {
            data = parser.parse(stream, parser.getSupportedFormats()[0], parser.getSupportedEncodings()[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return (SimpleFeatureCollection) data.getPayload();
    }

    //XMLUTILS
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

    //XMLUTILS
    public static String stringFromNode(org.w3c.dom.Node xmlcontent) {
        String content = null;
        try {
            StringWriter writer = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(new DOMSource(xmlcontent), new StreamResult(writer));
            content = writer.toString();
            return content;
        } catch (Exception e) {
            e.printStackTrace();;
        }
        return content;
    }
}
