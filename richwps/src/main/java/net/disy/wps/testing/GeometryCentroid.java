package net.disy.wps.testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.disy.wps.common.DescriptorContainer;
import net.disy.wps.common.Util;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class GeometryCentroid extends AbstractSelfDescribingAlgorithm {

	@Override
	/**
	 * Input Identifiers
	 * inputCollection
	 */
	public List<String> getInputIdentifiers() {
		List<String> identifiers = new ArrayList<String>();
		identifiers.add("inputCollection");
		return identifiers;
	}

	@Override
	/**
	 * Output Identifiers
	 * centroidCollection
	 */
	public List<String> getOutputIdentifiers() {
		List<String> identifiers = new ArrayList<String>();
		identifiers.add("centroidCollection");
		return identifiers;
	}
	
	@Override
	/**
	 * Input Data Type
	 * inputCollection -> GTVectorDataBinding
	 */
	public Class<?> getInputDataType(String identifier) {
		if (identifier.equals("inputCollection")) {
			return GTVectorDataBinding.class;
		}
		throw new RuntimeException("Error: Falscher Identifier!");
	}

	@Override
	/**
	 * Output Data Type
	 * centroidCollection -> GTVectorDataBinding
	 */
	public Class<?> getOutputDataType(String identifier) {
		if (identifier.equals("centroidCollection")) {
			return GTVectorDataBinding.class;
		}
		throw new RuntimeException("Error: Invalid indentifier!");
	}

	@Override
	/**
	 * Run
	 */
	public Map<String, IData> run(Map<String, List<IData>> inputMap){
		
				
		// Extract FeatureCollection from inputCollection
		List<IData> iClist = inputMap.get("inputCollection");
		if (iClist.size()==0) {
			throw new RuntimeException("InputCollection wurde nicht angegeben!");
		}
		IData iPdata = iClist.get(0);
		FeatureCollection<?, ?> featureCollection = ((GTVectorDataBinding) iPdata).getPayload();
		
		// Iterator on featureCollection
		FeatureIterator<?> iter = featureCollection.features();
		
		// Extract FeatureType of input collection
		FeatureType sourceType = featureCollection.getSchema();
		
		// Create FeatureColleaction and FeatureBuilder for Centroid features
		SimpleFeatureCollection centroidCollection = FeatureCollections.newCollection();
		//SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(createFeatureType());
		
		ArrayList<DescriptorContainer> descrList = new ArrayList<DescriptorContainer>();
		descrList.add(new DescriptorContainer(1, 1, false, "processID", Integer.class));
		descrList.add(new DescriptorContainer(1, 1, false, "sourceGeomArea", Double.class));
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(Util.refactorFeatureType((SimpleFeatureType) sourceType, descrList, "CentroidFeature", Point.class));
		
		// Initialization of i
		int i = 0;
		
		// Loop through featureCollection
		while(iter.hasNext()) {
			// id iteration for attributes
			i++;
			
			// Extract SimpleFeature from featureCollection
			SimpleFeature feature = (SimpleFeature) iter.next();
			if (feature.getDefaultGeometry() == null) {
				throw new NullPointerException("defaultGeometry is null in feature id " + feature.getID());
			}
			
			// Extract geometry and get its Centroid 
			Geometry geom = (Geometry) feature.getDefaultGeometry();
			Geometry centroid = geom.getCentroid();
			
			// Call function to initialize the builder values with those of the existing feature
			featureBuilder = Util.initBuilderValues(featureBuilder, feature);
			
			// Build feature
			featureBuilder.set(sourceType.getGeometryDescriptor().getLocalName(), centroid);
			featureBuilder.set("processID", i);
			featureBuilder.set("sourceGeomArea", geom.getArea());
			
			// Build new feature
			SimpleFeature centroidFeature = featureBuilder.buildFeature(null);
			
			// Add new feature to centroidCollection
			centroidCollection.add(centroidFeature);
		}
		
		// Create resultMap
		Map<String, IData> resultMap = new HashMap<String, IData>();
				
		// Add response to corresponding identifier in the resultMap
		resultMap.put("centroidCollection", new GTVectorDataBinding(centroidCollection));
		
		// Return resultMap
		return resultMap;
	}
}
