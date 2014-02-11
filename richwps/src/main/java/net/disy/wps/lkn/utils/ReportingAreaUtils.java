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
    private SimpleFeatureCollection reportingareas;
    
    public ReportingAreaUtils(SimpleFeatureCollection reportingareas){
        this.reportingareas=reportingareas;
    
    }
    /**
     * Iteriert ueber bestehende Berichtsgebiete und uebernimmt die fuer die das
     * Attribut "TEMPLATE" gegeben ist.
     *
     * @param ea Berichtsgebiete FeatureCollection
     * @return bereinigte Berichtsgebiete FeatureCollection
     */
    public SimpleFeatureCollection clearReportingAreas() {

        SimpleFeatureCollection clearedCollection = FeatureCollections.newCollection();

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        // Welches Attribut ist hier ausschlaggebend? Provisorisch wird TEMPLATE
        // verwendet
        Filter filter = ff.notEqual(ff.property(ATTRIB_TEMPLATE), null);
        FeatureIterator<SimpleFeature> iter = this.reportingareas.features();
        while (iter.hasNext()) {
            SimpleFeature feature = iter.next();
            if (filter.evaluate(feature)) {
                clearedCollection.add(feature);
            }
        }
        return clearedCollection;
    }

    public SimpleFeatureCollection extractNF() {
        String[] keys = {ATTRIB_DISTR, ATTRIB_TEMPLATE};
        String[] values = {ATTRIB_DISTR_NF, ""};
        SimpleFeatureCollection NFCollection = FeatureCollectionUtil.extract(
                this.reportingareas, keys, values);
        return NFCollection;
    }

    
    public SimpleFeatureCollection extractDI() {
        String[] keys = {ATTRIB_DISTR, ATTRIB_TEMPLATE};
        String[] values = {ATTRIB_DISTR_DI, ""};
        SimpleFeatureCollection NFCollection = FeatureCollectionUtil.extract(
                this.reportingareas, keys, values);
        return NFCollection;
    }
    
    public SimpleFeatureCollection getFeatureCollection(){
        return this.reportingareas;
    }
}
