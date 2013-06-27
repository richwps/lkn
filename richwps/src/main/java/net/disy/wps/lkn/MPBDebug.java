package net.disy.wps.lkn;

import net.disy.wps.common.Util;

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

@Algorithm(
		version = "0.0.5",
		title = "MPBDebug"
		)
public class MPBDebug extends AbstractAnnotatedAlgorithm {

    public MPBDebug() {
        super();
    }
    
    SimpleFeatureCollection wattSfc = FeatureCollections.newCollection();
    SimpleFeatureCollection msrlSfc = FeatureCollections.newCollection();
    SimpleFeatureCollection resutSfc = FeatureCollections.newCollection();
    
    @ComplexDataInput(
    		identifier="wattSfc", title="Wattflächen",
    		abstrakt="FeatureCollection der Eingabe-Geometrien", binding=GTVectorDataBinding.class)
    public void setWattCollection(FeatureCollection<?, ?> fc) {
    	this.wattSfc = (SimpleFeatureCollection) fc;
    }
    @ComplexDataInput(
    		identifier="msrlSfc", title="MSRL-Flächen",
    		abstrakt="FeatureCollection der Eingabe-Geometrien", binding=GTVectorDataBinding.class)
    public void setMSRLCollection(FeatureCollection<?, ?> fc) {
    	this.msrlSfc = (SimpleFeatureCollection) fc;
    }
    
    @ComplexDataOutput(
    		identifier ="resultCollection", title="ResultCollection",
    		abstrakt="SimpleFeatureCollection des Ergebnisses", binding = GTVectorDataBinding.class)
    public FeatureCollection getBBCollection() {
    	return this.resutSfc;
    }

    @Execute
    public void runMPBDebug() {
    	
    	// MultiGeometries --> SimpleGeometries
    	//this.wattSfc = Util.getSimpleGeometryCollection(this.wattSfc);
    	
    	// Create Union of Wattflächen
    	//this.wattSfc = Util.getUnionSfc(wattSfc);
    	
    	// Intersection
    	System.out.println("FC1 valid: " + Util.checkValid(wattSfc));
    	System.out.println("FC2 valid: " + Util.checkValid(msrlSfc));
    	this.resutSfc = intersect(this.wattSfc, this.msrlSfc);
    	System.out.println("Ergebnis FC valid " + Util.checkValid(this.resutSfc));
    	
    	//this.resutSfc = this.wattSfc;
    }
    
    public static SimpleFeatureCollection intersect(SimpleFeatureCollection wattSfc, SimpleFeatureCollection msrlSfc) {
    	SimpleFeatureCollection sfc = FeatureCollections.newCollection();
    	SimpleFeatureIterator iterWatt, iterMSRL;
    	Geometry intersect;
    	SimpleFeatureBuilder builder =
    			new SimpleFeatureBuilder(Util.refactorFeatureType(wattSfc.getSchema(), null, "DebugFeature", MultiPolygon.class));
    	
    	iterWatt = wattSfc.features();
    	while (iterWatt.hasNext()) {
    		SimpleFeature wattFeature = iterWatt.next();
    		Geometry wattGeom = (Geometry) wattFeature.getDefaultGeometry();
    		
    		iterMSRL = msrlSfc.features();
    		while(iterMSRL.hasNext()) {
    			SimpleFeature msrlFeature = iterMSRL.next();
    			
    			Geometry msrlGeom = (Geometry) msrlFeature.getDefaultGeometry();
    			intersect = wattGeom.intersection(msrlGeom);
    			
    			if (!intersect.isEmpty()) {
	    			builder = Util.initBuilderValues(builder, wattFeature);
	    			builder.set(wattSfc.getSchema().getGeometryDescriptor().getLocalName(), intersect);
	    			sfc.add(builder.buildFeature(null));
    			}
    		}
    	}
    	return sfc;
    }
    
}