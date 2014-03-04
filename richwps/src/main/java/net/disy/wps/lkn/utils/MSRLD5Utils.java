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

    private SimpleFeatureCollection MSRLD5s;
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

    public MSRLD5Utils(SimpleFeatureCollection MSRLD5s) {
        this.MSRLD5s = MSRLD5s;
    }

    /**
     * Liefert eine Liste mit eindeutigen Beobachtungszeitpunkten (DateTime),
     * die im 'ATTRIB_OBSV_PHENOMENONTIME'-Attribut einer
     * SimpleFeatureCollection enthalten sind
     *
     * @param sfc - Eingabe-SimpleFeatureCollection
     * @return Liste mit Beobachtungszeitpunkten
     */
    public ArrayList<DateTime> getObservationDates(final SimpleFeatureCollection sfc) {
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

    public Observations getRelevantObservationsByParameterAndYear(String param,
            Integer assementyear) {

        // observations: List of ObservationCollections
        Observations observations = new Observations();
        // relevantObservations: List of relevant ObservationCollections
        Observations relevantObservations = new Observations();

        String[] keys = {MSRLD5Utils.ATTRIB_OBSV_PARAMNAME};
        String[] values = {param};
        SimpleFeatureCollection sfc = FeatureCollections.newCollection();
        // SimpleFeatureCollection des aktuellen Parameters extrahieren
        sfc = FeatureCollectionUtil.extract(this.MSRLD5s, keys, values);

        // Liste von Beobachtungszeitpunkten erzeugen, fuer die mindestens
        // eine Beobachtung vorhanden ist
            /*LOGGER.debug("getObservationDates: Fuer den Parameter "
         + obsParams[i]
         + " sind folgende Beobachtungszeitpunkte vorhanden:");*/
        ArrayList<DateTime> obsDates = this.getObservationDates(sfc);

        // Liste von ObservationCollections anhand von
        // Beobachtungszeitpunkten
        observations = this.extractObservationsByListOfDates(sfc, obsDates);

        // Relevante ObservationCollections ausgehend von Bewertungsjahr
        // ermitteln
            /*LOGGER.debug("getRelevantFeatureCollections: Folgende Datensaetze fuer den Parameter "
         + obsParams[i]
         + " und das Bewertungsjahr "
         + inputAssesmentYear + " ausgewaehlt");*/
        relevantObservations = this.extractRelevantObservationsByYear(observations, assementyear);
        return relevantObservations;
        // Relevante ObservationCollections der paramArrayListe hinzufuegen
        //paramArrayList.add(relevantObservations);
        //return paramArrayList;
    }

    public SimpleFeatureCollection getFeatureCollection() {
        return this.MSRLD5s;
    }

    /**
     * Gibt eine Liste von ObservationCollections zurueck, die ein Element fuer
     * jeden Eintrag in der uebergebenen Liste mit Beobachtungszeitpunkten
     * enthaelt Returns an Array of ObservationsCollections. The list contains
     * entries for each DateTime including the corresponding SimpleFeatures in a
     * collection and the total area
     *
     * @param sfc
     * @param obsDates
     * @return Liste mit ObservationCollections
     */
    private Observations extractObservationsByListOfDates(
            SimpleFeatureCollection sfc, ArrayList<DateTime> obsDates) {
        String compareStr;
        Double area;
        SimpleFeatureCollection groupCollection;
        Observations obsCollections = new Observations();

        // Schleife ueber die Beobachtungszeitpunkte
        for (int i = 0; i < obsDates.size(); i++) {
            groupCollection = FeatureCollections.newCollection();
            // String zum Vergleich von Beobachtungszeitpunkten erzeugen
            if (FeatureCollectionUtil.attributeIsTimeStamp(sfc, ATTRIB_OBSV_PHENOMENONTIME)) {
                compareStr = obsDates.get(i).toString(
                        DateTimeFormatter4TimeStamp);
            } else {
                compareStr = obsDates.get(i).toString(DateTimeFormatter);
            }
            // Entsprechende SimpleFeatureCollection aus der gesamten
            // FeatureCollection extrahieren
            groupCollection = FeatureCollectionUtil.extract(sfc,
                    new String[]{ATTRIB_OBSV_PHENOMENONTIME},
                    new String[]{compareStr});
            // Gesamtflaeche der Features berechnen
            area = FeatureCollectionUtil.getArea(groupCollection);
            // ObservationCollection erzeugen und der Ausgabe-Liste hinzufuegen
            obsCollections.add(new ObservationCollection(obsDates.get(i),
                    groupCollection, area));
        }

        // Debug
        for (ObservationCollection obsColl : obsCollections) {
            /*LOGGER.debug("extractObservationsByListOfDates: "
             + obsColl.getDateTime().getYear() + "-"
             + obsColl.getDateTime().getMonthOfYear() + "-"
             + obsColl.getDateTime().getDayOfMonth() + ": "
             + FeatureCollectionUtil.tokm2Str(obsColl.getArea()) + " km2");*/
        }

        return obsCollections;
    }

    /**
     * Versucht aus einer Liste von ObservationCollections (MSRL-Daten) die
     * sechs fuer das Bewertungsjahr relevanten Beobachtungssammlungen zu
     * ermitteln und zurueck zu geben. Falls vom Bewertungsjahr ausgehend keine
     * sechs ObservationCollections verfuegbar sind, wird eine RuntimeException
     * ausgeloest.
     *
     * @param observations - Liste von ObservationCollections fuer jeden
     * Beobachtungszeitpunkt
     * @param bewertungsjahr
     * @return Liste mit den sechs relevanten ObservationCollections
     */
    private Observations extractRelevantObservationsByYear(
            Observations observations,
            Integer bewertungsjahr) {
        Observations preSelCollections = new Observations();
        Observations finalCollections = new Observations();
        HashSet<Integer> existingYears = new HashSet<Integer>();

        // Schleife ueber alle ObservationCollections
        for (int i = 0; i < observations.size(); i++) {
            ObservationCollection obsColl = observations.get(i);

            // Pruefen, ob die Liste der ausgewaehlten ObservationCollections
            // Eintraege enthaelt
            if (preSelCollections.size() > 0) {
                // ueber bestehende Eintraege in selCollections iterieren,
                // um Jahr und Flaeche der neuen obsColl zu vergleichen
                Integer initSize = preSelCollections.size();
                for (int j = 0; j < initSize; j++) {
                    ObservationCollection selColl = preSelCollections.get(j);
                    // Wenn unter den schon selektierten Sammlungen eine mit
                    // gleichem Jahr und kleinerer Flaeche ist...
                    if (selColl.getDateTime().getYear() == obsColl
                            .getDateTime().getYear()) {
                        if (selColl.getArea() < obsColl.getArea()) {
                            // ...dann wird diese geloescht...
                            preSelCollections.remove(selColl);
                            // ...und die neue hinzugefuegt
                            preSelCollections.add(obsColl);
                        }
                    } // Wenn Jahre unterschiedlich, muss geprueft werden, ob es
                    // schon einen Eintrag mit dem Jahr gibt. Falls nicht, kann
                    // die Sammlung hinzugefuegt werden.
                    else if (!existingYears.contains(obsColl.getDateTime()
                            .getYear())) {
                        preSelCollections.add(obsColl);
                        existingYears.add(obsColl.getDateTime().getYear());
                    }
                }
            } // Falls die Liste noch leer ist, wird die erste
            // Beobachtungssammlung hinzugefuegt
            else {
                preSelCollections.add(obsColl);
                // Das Jahr des hinzugefuegten Eintrags in die Liste der bereits
                // uebernommenen Jahre hinzufuegen
                existingYears.add(obsColl.getDateTime().getYear());
            }
        }
        // StartIndex ermitteln
        Integer startIndex = preSelCollections.getIdxOfObsCollbyYear(bewertungsjahr);
        try {
            for (int i = 0; i < 6; i++) {
                finalCollections.add(preSelCollections.get(startIndex + i));
            }
        } catch (Exception e) {
            // RuntimeException ausloesen, falls keine sechs
            // ObservationCollections vorhanden
            throw new RuntimeException(
                    "There is not enough data for 6 years available.");
        }
        // Debug
        /*for (int k = 0; k < preSelCollections.size(); k++) {
         LOGGER.debug("getRelevantFeatureCollections: "
         + preSelCollections.get(k).getDateTime().getYear() + "-"
         + preSelCollections.get(k).getDateTime().getMonthOfYear()
         + "-"
         + preSelCollections.get(k).getDateTime().getDayOfMonth()
         + " ausgewaehlt: "
         + FeatureCollectionUtil.tokm2Str(preSelCollections.get(k).getArea()));
         }*/
        return finalCollections;
    }

}
