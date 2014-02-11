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

    public static final String ATTRIB_TEMPLATE = "TEMPLATE";
    public static final String ATTRIB_DISTR = "DISTR";
    public static final String ATTRIB_DISTR_DI = "DI";
    public static final String ATTRIB_DISTR_NF = "NF";

    /**
     * Iteriert ueber bestehende Berichtsgebiete und uebernimmt die fuer die das
     * Attribut "TEMPLATE" gegeben ist.
     *
     * @param ea Berichtsgebiete FeatureCollection
     * @return bereinigte Berichtsgebiete FeatureCollection
     */
    public static SimpleFeatureCollection clearReportingAreas(final SimpleFeatureCollection ea) {

        SimpleFeatureCollection clearedCollection = FeatureCollections.newCollection();

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        // Welches Attribut ist hier ausschlaggebend? Provisorisch wird TEMPLATE
        // verwendet
        Filter filter = ff.notEqual(ff.property(ATTRIB_TEMPLATE), null);
        FeatureIterator<SimpleFeature> iter = ea.features();
        while (iter.hasNext()) {
            SimpleFeature feature = iter.next();
            if (filter.evaluate(feature)) {
                clearedCollection.add(feature);
            }
        }
        return clearedCollection;
    }

    /**
     *
     * @return
     */
    public static SimpleFeatureCollection extractNF(SimpleFeatureCollection inputCollection) {
        String[] keys = {ATTRIB_DISTR, ATTRIB_TEMPLATE};
        String[] values = {ATTRIB_DISTR_NF, ""};
        SimpleFeatureCollection NFCollection = FeatureCollectionUtil.extract(
                inputCollection, keys, values);
        return NFCollection;
    }

    /**
     *
     * @return
     */
    public static SimpleFeatureCollection extractDI(SimpleFeatureCollection inputCollection) {
        String[] keys = {ATTRIB_DISTR, ATTRIB_TEMPLATE};
        String[] values = {ATTRIB_DISTR_DI, ""};
        SimpleFeatureCollection NFCollection = FeatureCollectionUtil.extract(
                inputCollection, keys, values);
        return NFCollection;
    }
}
