package net.disy.wps.testing;

import java.util.ArrayList;

import net.disy.wps.common.DescriptorContainer;
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
import com.vividsolutions.jts.geom.Polygon;

@Algorithm(
		version = "0.0.5",
		title = "BoundingBox Polygons"
		)
public class BoundingBoxPolygons extends AbstractAnnotatedAlgorithm {

    public BoundingBoxPolygons() {
        super();
    }
    
    SimpleFeatureCollection sfc = FeatureCollections.newCollection();
    SimpleFeatureCollection bb = FeatureCollections.newCollection();
    
    @ComplexDataInput(
    		identifier="inputCollection", title="Eingabe-Geometrien",
    		abstrakt="FeatureCollection der Eingabe-Geometrien", binding=GTVectorDataBinding.class)
    public void setInputCollection(FeatureCollection<?, ?> fc) {
    	this.sfc = (SimpleFeatureCollection) fc;
    }
    
    @ComplexDataOutput(
    		identifier ="outputCollections", title="BoundingBox Polygone",
    		abstrakt="FeatureCollection der BoundingBox Polygone", binding = GTVectorDataBinding.class)
    public FeatureCollection getBBCollection() {
    	return this.bb;
    }

    @Execute
    public void runPDTest() {
    	
    	SimpleFeatureIterator iter = this.sfc.features();
    	while (iter.hasNext()) {
    		SimpleFeature feature = iter.next();
    		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(
    				Util.refactorFeatureType(sfc.getSchema(), new ArrayList<DescriptorContainer>(), "BoundingBoxPolygons", Polygon.class)
    		);
    		
    		Geometry geom = (Geometry) feature.getDefaultGeometry();
    		
    		featureBuilder = Util.initBuilderValues(featureBuilder,
					feature);
			featureBuilder.set(sfc.getSchema()
					.getGeometryDescriptor().getLocalName(), geom.getEnvelope());
			
			SimpleFeature bbFeature = featureBuilder.buildFeature(null);
			bb.add(bbFeature);
    	}
    	
    }
}