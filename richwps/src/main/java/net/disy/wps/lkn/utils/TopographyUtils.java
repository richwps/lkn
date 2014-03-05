package net.disy.wps.lkn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

/**
 *
 * @author dalcacer
 * @author rwoessner
 */
public class TopographyUtils {

    public static final String ATTRIB_YEAR = "YEAR";
    public static final String ATTRIB_POSKEY = "POSKEY";

    private SimpleFeatureCollection topography;

    public TopographyUtils(final SimpleFeatureCollection topography) {
        this.topography = topography;
    }

    /**
     * Ermittelt aus der SimpleFeatureCollection der Topographien alle
     * eindeutigen im Feld "YEAR" enthaltenen Werte
     *
     * @return
     */
    public ArrayList<Integer> getExistingTopographyYears() {
        
        ArrayList<Integer> existingTopoYears = new ArrayList<Integer>();
        
        FeatureIterator<SimpleFeature> iter = this.topography.features();
        try {
            // Iteration ueber alle Features
            while (iter.hasNext()) {
                final SimpleFeature feature = (SimpleFeature) iter.next();
                final String existingYear = (String) feature.getAttribute(ATTRIB_YEAR);
                existingTopoYears.add(Integer.parseInt(existingYear));
            }
        } finally {
            // Iterator schliessen
            iter.close();
        }

        HashSet<Integer> hs = new HashSet<Integer>();
        // Ergebnisliste in eine Liste mit eindeutigen Werten konvertieren
        hs.addAll(existingTopoYears);
        existingTopoYears.clear();
        existingTopoYears.addAll(hs);
        // Aufsteigend sortieren
        Collections.sort(existingTopoYears);
        return existingTopoYears;
    }

    public SimpleFeatureCollection extractTidelands() {
        String[] keys = {ATTRIB_POSKEY, ATTRIB_POSKEY};
        String[] values = {"Watt", "watt"};
        SimpleFeatureCollection rsfc = FeatureCollectionUtil.extractEquals(this.topography, keys, values);
        return rsfc;
    }

    public SimpleFeatureCollection extractByYear(final String year) {
        String[] keys = {ATTRIB_YEAR};
        String[] values = {year};
        SimpleFeatureCollection rsfc = FeatureCollectionUtil.extractEquals(this.topography, keys, values);
        return rsfc;
    }

   public SimpleFeatureCollection getFeatureCollection(){
        return this.topography;
    }
}
