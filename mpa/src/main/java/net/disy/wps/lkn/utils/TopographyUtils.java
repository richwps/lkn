package net.disy.wps.lkn.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import net.disy.wps.lkn.mpa.types.IntegerList;
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
    public ArrayList<Integer> getTopoYears() {
        FeatureIterator<SimpleFeature> iter = this.topography.features();
        ArrayList<Integer> existingTopoYears = new ArrayList<Integer>();
        HashSet<Integer> hs = new HashSet<Integer>();
        String existingYear;

        try {
            // Iteration ueber alle Features
            while (iter.hasNext()) {
                SimpleFeature feature = (SimpleFeature) iter.next();
                existingYear = (String) feature.getAttribute(ATTRIB_YEAR);
                existingTopoYears.add(Integer.parseInt(existingYear));
            }
        } finally {
            // Iterator schliessen
            iter.close();
        }

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

    
     /**
     * Ermittelt aus der SimpleFeatureCollection der Topographien alle
     * eindeutigen im Feld "YEAR" enthaltenen Werte
     *
     * @return
     */
    public IntegerList getExistingTopographyYears() {
        
        IntegerList existingTopoYears = new IntegerList();
        
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
        hs.addAll(existingTopoYears.getPayload());
        existingTopoYears.clear();
        existingTopoYears.addAll(hs);
        // Aufsteigend sortieren
        Collections.sort(existingTopoYears.getPayload());
        return existingTopoYears;
    }

    
   public SimpleFeatureCollection getFeatureCollection(){
        return this.topography;
    }
}
