package net.disy.wps.lkn.utils;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

/**
 *
 * @author dalcacer
 * @author rwoessner
 */
public class ReportingAreaUtils {

    /**
     *
     * @param ea Berichtsgebiete FeatureCollection
     * @return bereinigte Berichtsgebiete FeatureCollection
     * @deprecated
     * @see net.disy.wps.lkn.types.ReportingAreaFeatureCollection
     */
    public static SimpleFeatureCollection clearEvalAreas(SimpleFeatureCollection ea) {
        SimpleFeatureCollection clearedCollection = FeatureCollections
                .newCollection();

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        // Welches Attribut ist hier ausschlaggebend? Provisorisch wird TEMPLATE
        // verwendet
        Filter filter = ff.notEqual(ff.property("TEMPLATE"), null);
        FeatureIterator<SimpleFeature> iter = ea.features();
        while (iter.hasNext()) {
            SimpleFeature feature = iter.next();
            if (filter.evaluate(feature)) {
                clearedCollection.add(feature);
            }
        }
        return clearedCollection;
    }
}
