package net.disy.wps.lkn;

import net.disy.wps.lkn.utils.FeatureCollectionUtil;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * MPBDebug Prozess Zum Testen von Verschneidungen
 *
 * @author woessner
 *
 */
@Algorithm(
        version = "0.0.5",
        title = "MPBDebug"
)
public class MPBDebug extends AbstractAnnotatedAlgorithm {

    private SimpleFeatureCollection wattSfc;
    private SimpleFeatureCollection msrlSfc;
    private SimpleFeatureCollection resutSfc;

    public MPBDebug() {
        super();
        wattSfc = FeatureCollections.newCollection();
        msrlSfc = FeatureCollections.newCollection();
        resutSfc = FeatureCollections.newCollection();
    }

    @ComplexDataInput(
            identifier = "wattSfc", title = "Wattflaechen",
            abstrakt = "FeatureCollection der Eingabe-Geometrien", binding = GTVectorDataBinding.class)
    public void setWattCollection(FeatureCollection<?, ?> fc) {
        this.wattSfc = (SimpleFeatureCollection) fc;
    }

    @ComplexDataInput(
            identifier = "msrlSfc", title = "MSRL-Flaechen",
            abstrakt = "FeatureCollection der Eingabe-Geometrien", binding = GTVectorDataBinding.class)
    public void setMSRLCollection(FeatureCollection<?, ?> fc) {
        this.msrlSfc = (SimpleFeatureCollection) fc;
    }

    @ComplexDataOutput(
            identifier = "resultCollection", title = "ResultCollection",
            abstrakt = "SimpleFeatureCollection des Ergebnisses", binding = GTVectorDataBinding.class)
    public FeatureCollection getBBCollection() {
        return this.resutSfc;
    }

    @Execute
    public void runMPBDebug() {

    	// MultiGeometries --> SimpleGeometries
        //this.wattSfc = FeatureCollectionUtil.getSimpleGeometryCollection(this.wattSfc);
    	// Create Union of Wattflaechen
        //this.wattSfc = FeatureCollectionUtil.getUnionSfc(wattSfc);
        // Intersection
        System.out.println("FC1 valid: " + FeatureCollectionUtil.checkValid(wattSfc));
        System.out.println("FC2 valid: " + FeatureCollectionUtil.checkValid(msrlSfc));
        this.resutSfc = intersect(this.wattSfc, this.msrlSfc);
        System.out.println("Ergebnis FC valid " + FeatureCollectionUtil.checkValid(this.resutSfc));

        //this.resutSfc = this.wattSfc;
    }

    public static SimpleFeatureCollection intersect(SimpleFeatureCollection wattSfc, SimpleFeatureCollection msrlSfc) {
        SimpleFeatureCollection sfc = FeatureCollections.newCollection();
        SimpleFeatureIterator iterWatt, iterMSRL;
        Geometry intersect;
        SimpleFeatureBuilder builder
                = new SimpleFeatureBuilder(FeatureCollectionUtil.refactorFeatureType(wattSfc.getSchema(), null, "DebugFeature", MultiPolygon.class));

        iterWatt = wattSfc.features();
        while (iterWatt.hasNext()) {
            SimpleFeature wattFeature = iterWatt.next();
            Geometry wattGeom = (Geometry) wattFeature.getDefaultGeometry();

            iterMSRL = msrlSfc.features();
            while (iterMSRL.hasNext()) {
                SimpleFeature msrlFeature = iterMSRL.next();

                Geometry msrlGeom = (Geometry) msrlFeature.getDefaultGeometry();
                intersect = wattGeom.intersection(msrlGeom);

                if (!intersect.isEmpty()) {
                    builder = FeatureCollectionUtil.initBuilderValues(builder, wattFeature);
                    builder.set(wattSfc.getSchema().getGeometryDescriptor().getLocalName(), intersect);
                    sfc.add(builder.buildFeature(null));
                }
            }
        }
        return sfc;
    }

}
