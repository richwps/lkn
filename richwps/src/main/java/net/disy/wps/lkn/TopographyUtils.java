package net.disy.wps.lkn.utils;

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
    
     /**
     * Ermittelt aus der SimpleFeatureCollection der Topographien alle
     * eindeutigen im Feld "YEAR" enthaltenen Werte
     *
     * @param topoSfc
     * @return
     */
    public static ArrayList<Integer> getTopoYears(SimpleFeatureCollection topoSfc) {
        FeatureIterator<SimpleFeature> iter = topoSfc.features();
        ArrayList<Integer> existingTopoYears = new ArrayList<Integer>();
        HashSet<Integer> hs = new HashSet<Integer>();
        String existingYear;

        try {
            // Iteration ueber alle Features
            while (iter.hasNext()) {
                SimpleFeature feature = (SimpleFeature) iter.next();
                existingYear = (String) feature.getAttribute("YEAR");
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
    
    
    /**
     * Liefert ein einem Topographie-Datensatz zugehoeriges Jahr aus einer Liste
     * von moeglichen Jahren, welches die minimale zeitliche Distanz zu einem
     * Eingabejahr aufweist. Im Fall von gleichen Zeitunterschieden zu zwei
     * Jahren, wird das spaetere zurueck gegeben.
     *
     * @param year - Eingabejahr, zu dem ein passendes Topographie-Jahr
     * ermittelt werden soll
     * @param topoYearList - Liste mit vorhandenen Topographie-Jahren
     * @return Jahr
     */
    public static int getTopoYear(int year, ArrayList<Integer> topoYearList) {
        ArrayList<Integer> dList = new ArrayList<Integer>();
        Integer minDiff;
        Integer minYearIndex = -1;

        // Wichtig: absteigend sortieren!
        Collections.sort(topoYearList, Collections.reverseOrder());

        for (int i = 0; i < topoYearList.size(); i++) {
            dList.add(Math.abs(year - topoYearList.get(i)));
        }
        minDiff = dList.get(0);
        for (int j = 1; j < dList.size() - 1; j++) {
            if (minDiff > dList.get(j)) {
                minDiff = dList.get(j);
            }
        }
        minYearIndex = dList.indexOf(minDiff);

        return topoYearList.get(minYearIndex);
    }

}
