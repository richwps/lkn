package net.disy.wps.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;

public abstract class Util {
	
	/**
	 * Gibt ein String-Array mit eindeutigen Werten eines Feldes in einer
	 * FeatureCollection zurück.
	 * 
	 * @param sfc
	 *            - SimpleFeatureCollection
	 * @param field
	 *            - Feldname
	 * @return
	 */
	public static String[] getValuesFromField(SimpleFeatureCollection sfc,
			String field) {
		String[] values = { "" };
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
	 * Berechnet die Gesamtfläche aller Features einer SimpleFeatureCollection
	 * 
	 * @param sfc
	 *            - SimpleFeatureCollection für die die Gesamtfläche berechnet
	 *            werden soll
	 * @return Gesamtfläche der SimpleFeatureCollection
	 */
	public static Double getAreaFromFC(SimpleFeatureCollection sfc) {
		
		FeatureIterator<SimpleFeature> iter = sfc.features();
		Double area = 0.0;
		Integer i = 0;
		try {
			while (iter.hasNext()) {
				i++;
				SimpleFeature feature = iter.next();
				area += ((Geometry) feature.getDefaultGeometry()).getArea();
			}
		} finally {
			iter.close();
		}
		return area;
	}
	
	/**
	 * Gibt einen veränderten FeatureType zurück: Attribute in Form von
	 * DescriptorContainer(n) werden einem bestehenden FeatureType hinzugefügt,
	 * ein neuer Name wird vergeben und der Geometrietyp kann verändert werden.
	 * 
	 * @param previousFeatureType
	 *            - Bestehender FeatureType
	 * @param descrContainerList
	 *            - Liste von DescriptorContainern, Attribute die hinzugefügt
	 *            werden sollen
	 * @param newFeatureTypeName
	 *            - Name des neuen FeatureTypes
	 * @param newGeomType
	 *            - Geometrietyp des neuen FeatureTypes
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
	 * Features Danach können dem Builder weitere Werte manuell hinzugefügt
	 * werden
	 * 
	 * @param inputBuilder
	 *            - SimpleFeatureBuilder, der initialisiert werden soll
	 * @param sourceFeature
	 *            - SimpleFeature, aus dem die Werte zur Initialisierung
	 *            entnommen werden
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
	 * übergebenen Collection zurück
	 * 
	 * @param inputCollection
	 *            - SimpleFeatureCollection,
	 * @param keys
	 *            - Array von Strings, das die Namen der Schlüssel enthält
	 * @param values
	 *            - Array von String, das die Namen der Werte enthält
	 * @return
	 */
	public static SimpleFeatureCollection getFeatureCollectionExtract(
			SimpleFeatureCollection inputCollection, String[] keys,
			String[] values) {
		SimpleFeatureCollection outputCollection = FeatureCollections
				.newCollection();
		// Schleife über keys, zum Erzeugen mehrerer Filter
		for (int i = 0; i < keys.length; i++) {
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
			Filter filter = ff.equals(ff.property(keys[i]),
					ff.literal(values[i]));
			FeatureIterator<SimpleFeature> iter = inputCollection.features();
			// Schleife über Features und Evaluierung
			while (iter.hasNext()) {
				SimpleFeature feature = iter.next();
				// System.out.println("Vergleichswert: " + values[i]);
				// System.out.println("Featurewert: " +
				// feature.getAttribute(keys[i]));
				if (filter.evaluate(feature)) {
					// Feature der outputCollection hinzufügen
					outputCollection.add(feature);
				}
			}
		}
		return outputCollection;
	}
	
	/**
	 * Erzeugt eine Union von Geometrien in einer Collection und gibt diese
	 * zurück
	 * 
	 * @param geometryCollection
	 *            - Collection von Geometrien, aus denen die Union gebildet
	 *            werden soll
	 * @return
	 */
	public static Geometry getUnion(Collection<Geometry> geometryCollection) {
		Geometry unionGeom = null;
		for (Iterator<Geometry> i = geometryCollection.iterator(); i.hasNext();) {
			Geometry geometry = i.next();
			if (geometry == null)
				continue;
			if (unionGeom == null) {
				unionGeom = geometry;
			} else {
				unionGeom = unionGeom.union(geometry);
			}
		}
		return unionGeom;
	}
	
	/**
	 * Erzeugt die Union der Features einer FeatureCollection
	 * 
	 * @param sfc
	 * @return
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
		
		union = getUnion(geomCollection);
		
		builder.set(sfc.getSchema().getGeometryDescriptor().getLocalName(),
				union);
		outputCollection.add(builder.buildFeature(null));
		
		return outputCollection;
	}
	
	/**
	 * Gibt die Anzahl der Features in einer SimpleFeatureCollection zurück
	 * 
	 * @param sfc
	 *            SimpleFeatureCollection
	 * @return Anzahl der Features
	 */
	public static Integer getNumFeaturesFromFC(SimpleFeatureCollection sfc) {
		return sfc.size();
	}
	
	/**
	 * Gibt die Summe der Stützpunkte aller Geometrien einer
	 * SimpleFeatureCollection zurück
	 * 
	 * @param sfc
	 *            SimpleFeatureCollection
	 * @return Summe der Stützpunkte
	 */
	public static Integer getNumVerticesFromFC(SimpleFeatureCollection sfc) {
		Integer num = 0;
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
	 * Gibt die mittlere Anzahl an Stützpunkten alle Features einer
	 * SimpleFeatureCollection zurück
	 * 
	 * @param sfc
	 *            SimpleFeaturecollection
	 * @return Mittlere Anzahl Stützpunkte
	 */
	public static Double getMeanVerticesFromFC(SimpleFeatureCollection sfc) {
		return Double.valueOf(getNumVerticesFromFC(sfc)
				/ getNumFeaturesFromFC(sfc));
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
	 * @param multiGeometryCollection
	 * @return
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
	 * Prüft die Geometrien von Features einer FeatureCollection auf Validität
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
	
}
