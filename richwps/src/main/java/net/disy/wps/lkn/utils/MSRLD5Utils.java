package net.disy.wps.lkn.utils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import net.disy.wps.lkn.processes.mpa.types.ObservationCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.opengis.feature.simple.SimpleFeature;

/**
 *
 * @author dalcacer
 * @author rwoessner
 *
 */
public class MSRLD5Utils {

    // DateTimeFormatter: Repraesentiert das Datums-/Zeit-Format in den
    // MSRL-Daten
    public static final String ATTRIB_OBSV_PHENOMENONTIME = "OBSV_PHENOMENONTIME";
    public static final String ATTRIB_OBSV_PARAMNAME = "OBSV_PARAMETERNAME";
    /**
     * COV_OP = Algen
     */
    public static final String ATTRIB_OBS_PARAMNAME_OP = "COV_OP";
    /**
     * COV_ZS = Seegras
     */
    public static final String ATTRIB_OBS_PARAMNAME_ZS = "COV_ZS";
    public static final String ATTRIB_OBSV_PARAMETERVALUE = "OBSV_PARAMETERVALUE";

    private static final DateTimeFormatter DateTimeFormatter = DateTimeFormat
            .forPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DateTimeFormatter4TimeStamp = DateTimeFormat
            .forPattern("yyyy-MM-dd HH:mm:ss.S");

    /**
     * Liefert eine Liste mit eindeutigen Beobachtungszeitpunkten (DateTime),
     * die im 'ATTRIB_OBSV_PHENOMENONTIME'-Attribut einer
     * SimpleFeatureCollection enthalten sind
     *
     * @param sfc - Eingabe-SimpleFeatureCollection
     * @return Liste mit Beobachtungszeitpunkten
     */
    public static ArrayList<DateTime> getObservationDates(final SimpleFeatureCollection sfc) {
        FeatureIterator<SimpleFeature> iter = sfc.features();
        ArrayList<DateTime> obsDates = new ArrayList<DateTime>();
        HashSet<DateTime> hs = new HashSet<DateTime>();
        DateTime dt;

        try {
            // Iteration ueber alle Features
            while (iter.hasNext()) {
                SimpleFeature feature = (SimpleFeature) iter.next();
                // Datums-/Zeit-Information extrahieren,...
                if (feature.getAttribute(ATTRIB_OBSV_PHENOMENONTIME) instanceof Timestamp) {
                    Timestamp timestamp = (Timestamp) feature
                            .getAttribute(ATTRIB_OBSV_PHENOMENONTIME);
                    dt = new DateTime(timestamp);
                } else {
                    String dateTimeStr = (String) feature
                            .getAttribute(ATTRIB_OBSV_PHENOMENONTIME);
                    // ... damit ein DateTime-Objekt instanziieren...
                    dt = DateTimeFormatter.parseDateTime(dateTimeStr);
                }
                // ...und einer Liste hinzufuegen
                obsDates.add(dt);
            }
        } finally {
            // Iterator schliessen
            iter.close();
        }

        // Ergebnisliste in eine Liste mit eindeutigen Werten konvertieren
        hs.addAll(obsDates);
        obsDates.clear();
        obsDates.addAll(hs);
        // Aufsteigend sortieren
        Collections.sort(obsDates);

        return obsDates;
    }

    /**
     *
     * @param msrld5collection
     * @param param
     * @param assementyear
     * @param obsCollArrayList
     * @param relCollArrayList
     * @param paramArrayList
     */
    public static ArrayList<ObservationCollection> getRelevantObservationsByParameterAndYear(
            SimpleFeatureCollection msrld5collection,
            String param,
            Integer assementyear) {
        

        // obsCollArrayList: List of ObservationCollections
        ArrayList<ObservationCollection> obsCollArrayList = new ArrayList<ObservationCollection>();
        // relCollArrayList: List of relevant ObservationCollections
        ArrayList<ObservationCollection> relCollArrayList = new ArrayList<ObservationCollection>();
        ArrayList<ArrayList<ObservationCollection>> paramArrayList = new ArrayList<ArrayList<ObservationCollection>>();
        
        String[] keys = {MSRLD5Utils.ATTRIB_OBSV_PARAMNAME};
        String[] values = {param};
        SimpleFeatureCollection sfc = FeatureCollections.newCollection();
        // SimpleFeatureCollection des aktuellen Parameters extrahieren
        sfc = FeatureCollectionUtil.extract(msrld5collection, keys, values);

        // Liste von Beobachtungszeitpunkten erzeugen, fuer die mindestens
        // eine Beobachtung vorhanden ist
            /*LOGGER.debug("getObservationDates: Fuer den Parameter "
         + obsParams[i]
         + " sind folgende Beobachtungszeitpunkte vorhanden:");*/
        ArrayList<DateTime> obsDates = MSRLD5Utils.getObservationDates(sfc);

        // Liste von ObservationCollections anhand von
        // Beobachtungszeitpunkten
        obsCollArrayList = MPAUtils.getObsCollByDateList(sfc, obsDates);

        // Relevante ObservationCollections ausgehend von Bewertungsjahr
        // ermitteln
            /*LOGGER.debug("getRelevantFeatureCollections: Folgende Datensaetze fuer den Parameter "
         + obsParams[i]
         + " und das Bewertungsjahr "
         + inputAssesmentYear + " ausgewaehlt");*/
        return relCollArrayList = MPAUtils.getRelevantObservationCollections(obsCollArrayList, assementyear);

        // Relevante ObservationCollections der paramArrayListe hinzufuegen
        //paramArrayList.add(relCollArrayList);
        //return paramArrayList;
    }
}
