package net.disy.wps.testing;

import java.awt.geom.AffineTransform;
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
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Geometry;

public class GeometryTranslation extends AbstractSelfDescribingAlgorithm {

	@Override
	/**
	 * Input Identifiers
	 * inputCollection
	 */
	public List<String> getInputIdentifiers() {
		List<String> identifiers = new ArrayList<String>();
		identifiers.add("inputCollection");
		identifiers.add("translateX");
		identifiers.add("translateY");
		return identifiers;
	}

	@Override
	/**
	 * Output Identifiers
	 * centroidCollection
	 */
	public List<String> getOutputIdentifiers() {
		List<String> identifiers = new ArrayList<String>();
		identifiers.add("translatedCollection");
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
		else if (identifier.equals("translateX")){
			return LiteralDoubleBinding.class;
		}
		else if (identifier.equals("translateY")){
			return LiteralDoubleBinding.class;
		}
		throw new RuntimeException("Error: Invalid identifier!");
	}

	@Override
	/**
	 * Output Data Type
	 * centroidCollection -> GTVectorDataBinding
	 */
	public Class<?> getOutputDataType(String identifier) {
		if (identifier.equals("translatedCollection")) {
			return GTVectorDataBinding.class;
		}
		throw new RuntimeException("Error: Invalid indentifier!");
	}

	@Override
	/**
	 * Run
	 */
	public Map<String, IData> run(Map<String, List<IData>> inputMap){
		
		Double transX = 0.0;
		Double transY = 0.0;
		
		// Extract FeatureCollection from inputCollection identifier
		List<IData> iClist = inputMap.get("inputCollection");
		if (iClist.size()==0) {
			throw new RuntimeException("inputCollection wurde nicht angegeben!");
		}
		IData iPdata = iClist.get(0);
		FeatureCollection<?, ?> featureCollection = ((GTVectorDataBinding) iPdata).getPayload();
		
		// Extract transX from translateX identifier
		List<IData> tXlist = inputMap.get("translateX");
		if (tXlist!=null) {
			if (tXlist.size()!=0) {
				IData tXdata = tXlist.get(0);
				try{
					transX = (Double) tXdata.getPayload();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		// Extract transY from translateY identifier
		List<IData> tYlist = inputMap.get("translateY");
		if (tYlist!=null) {
			if (tYlist.size()!=0) {
				IData tYdata = tYlist.get(0);
				try{
					transY = (Double) tYdata.getPayload();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		// Iterator on featureCollection
		FeatureIterator<?> iter = featureCollection.features();
		
		FeatureType sourceType = featureCollection.getSchema();
		
		// Create FeatureColleaction and FeatureBuilder for Centroid features
		SimpleFeatureCollection translatedCollection = FeatureCollections.newCollection();
		
		// Create list of additional descriptors for the resulting features
		ArrayList<DescriptorContainer> dcList = new ArrayList<DescriptorContainer>();		
		dcList.add(new DescriptorContainer(1, 1, false, "processID", Integer.class));
		dcList.add(new DescriptorContainer(1, 1, false, "translateX", Double.class));
		dcList.add(new DescriptorContainer(1, 1, false, "translateY", Double.class));
		dcList.add(new DescriptorContainer(1, 1, false, "coordString", String.class));
		
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(Util.refactorFeatureType((SimpleFeatureType) sourceType, dcList, "TranslatedGeometryType", null));
		//SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(Util.createBasicFeatureType());
		
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
			AffineTransform affineTransform = AffineTransform.getTranslateInstance(transX, transY);
			MathTransform mathTransform = new AffineTransform2D(affineTransform);
			Geometry translatedGeom = geom;
			try {
				translatedGeom = JTS.transform(geom, mathTransform);
			} catch (MismatchedDimensionException e) {
				e.printStackTrace();
			} catch (TransformException e) {
				e.printStackTrace();
			}

			// Call function to initialize the builder values with those of the existing feature
			featureBuilder = Util.initBuilderValues(featureBuilder, feature);

			// Set new attributes
			featureBuilder.set(sourceType.getGeometryDescriptor().getLocalName(), translatedGeom);
			featureBuilder.set("processID", i);
			featureBuilder.set("translateX", transX);
			featureBuilder.set("translateY", transY);
			
			String coordString ="";
			
			for (int j=0;j<translatedGeom.getCoordinates().length; j++) {
				coordString+=translatedGeom.getCoordinates()[j];
			}
			featureBuilder.set("coordString", coordString);
			
			// Build new feature
			SimpleFeature translatedFeature = featureBuilder.buildFeature(null);
			
			// Add new feature to returned collection
			translatedCollection.add(translatedFeature);
		}
		
		// Create resultMap
		Map<String, IData> resultMap = new HashMap<String, IData>();
				
		// Add response to corresponding identifier in the resultMap
		resultMap.put("translatedCollection", new GTVectorDataBinding(translatedCollection));
		
		// Return resultMap
		return resultMap;
	}

}
