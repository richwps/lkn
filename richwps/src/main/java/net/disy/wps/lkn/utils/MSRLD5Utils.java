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
    private static DateTimeFormatter DateTimeFormatter = DateTimeFormat
            .forPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static DateTimeFormatter DateTimeFormatter4TimeStamp = DateTimeFormat
            .forPattern("yyyy-MM-dd HH:mm:ss.S");

    /**
     * Liefert eine Liste mit eindeutigen Beobachtungszeitpunkten (DateTime),
     * die im 'OBSV_PHENOMENONTIME'-Attribut einer SimpleFeatureCollection
     * enthalten sind
     *
     * @param sfc - Eingabe-SimpleFeatureCollection
     * @return Liste mit Beobachtungszeitpunkten
     */
    public static ArrayList<DateTime> getObservationDates(SimpleFeatureCollection sfc) {
        FeatureIterator<SimpleFeature> iter = sfc.features();
        ArrayList<DateTime> obsDates = new ArrayList<DateTime>();
        HashSet<DateTime> hs = new HashSet<DateTime>();
        DateTime dt;

        try {
            // Iteration ueber alle Features
            while (iter.hasNext()) {
                SimpleFeature feature = (SimpleFeature) iter.next();
                // Datums-/Zeit-Information extrahieren,...
                if (feature.getAttribute("OBSV_PHENOMENONTIME") instanceof Timestamp) {
                    Timestamp timestamp = (Timestamp) feature
                            .getAttribute("OBSV_PHENOMENONTIME");
                    dt = new DateTime(timestamp);
                } else {
                    String dateTimeStr = (String) feature
                            .getAttribute("OBSV_PHENOMENONTIME");
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
    public static ArrayList<ObservationCollection> getObsCollByDateList(
            SimpleFeatureCollection sfc, ArrayList<DateTime> obsDates) {
        String compareStr;
        Double area;
        SimpleFeatureCollection groupCollection;
        ArrayList<ObservationCollection> obsCollections = new ArrayList<ObservationCollection>();

        // Schleife ueber die Beobachtungszeitpunkte
        for (int i = 0; i < obsDates.size(); i++) {
            groupCollection = FeatureCollections.newCollection();
            // String zum Vergleich von Beobachtungszeitpunkten erzeugen
            if (FeatureCollectionUtil.attributeIsTimeStamp(sfc, "OBSV_PHENOMENONTIME")) {
                compareStr = obsDates.get(i).toString(
                        DateTimeFormatter4TimeStamp);
            } else {
                compareStr = obsDates.get(i).toString(DateTimeFormatter);
            }
            // Entsprechende SimpleFeatureCollection aus der gesamten
            // FeatureCollection extrahieren
            groupCollection = FeatureCollectionUtil.extract(sfc,
                    new String[]{"OBSV_PHENOMENONTIME"},
                    new String[]{compareStr});
            // Gesamtflaeche der Features berechnen
            area = FeatureCollectionUtil.getArea(groupCollection);
            // ObservationCollection erzeugen und der Ausgabe-Liste hinzufuegen
            obsCollections.add(new ObservationCollection(obsDates.get(i),
                    groupCollection, area));
        }

        // Debug
        for (ObservationCollection obsColl : obsCollections) {
            /*LOGGER.debug("getObsCollByDateList: "
             + obsColl.getDateTime().getYear() + "-"
             + obsColl.getDateTime().getMonthOfYear() + "-"
             + obsColl.getDateTime().getDayOfMonth() + ": "
             + FeatureCollectionUtil.tokm2Str(obsColl.getArea()) + " km2");*/
        }

        return obsCollections;
    }

    /**
     * Ermittelt zu aus einer Liste von ObservationCollectiond und einer
     * Jahres-Angabe den Index des entsprechenden Elements falls das Jahr
     * kleiner ist als die vorhanden Jahre in der Liste wird eine
     * RuntimeException ausgeloest Falls das Jahr groesser ist als die
     * vorhandene Jahre in der Liste wird der Index des zeitlich
     * naechstliegenden Jahres zurueckgegeben
     *
     * @param obsCollList - Liste mit allen ObservationCollections
     * @param year - Jahr
     * @return Index
     */
    public static Integer getIdxOfObsCollbyYear(
            ArrayList<ObservationCollection> obsCollList, Integer year) {

        Integer i = 0;
        // sort descending by DateTime of ObservationCollection
        Collections.sort(obsCollList, Collections.reverseOrder());
        Integer listYear = obsCollList.get(i).getDateTime().getYear();
        // Schleife, bis aktuelles Jahr aus der Liste nicht mehr kleier als das
        // Bewertungsjahr ist
        do {
            // RuntimException, wenn die Anzahl der Schleifendurchlaeufe die
            // Groesse der Liste ueberschreitet
            if (i > obsCollList.size() - 1) {
                throw new RuntimeException(
                        "Invalid year or non fitting data! Try to use "
                        + listYear + " as year.");
            }
            listYear = obsCollList.get(i).getDateTime().getYear();
            if (listYear.equals(year)) {
                return i;
            } else if (listYear < year) {
                return i;
            }
            i++;
        } while (listYear >= year);
        return null;
    }

    /**
     * Liefert eine durch ein Jahr bestimme ObservationCollection aus einer
     * Liste von ObservationCollections
     *
     * @param obsCollList - Liste von ObservationCollections
     * @param year - Jahr, zu dem die ObservationCollection zurueckgegeben
     * werden soll
     * @return ObservationCollection
     */
    public static ObservationCollection getObsCollByYear(
            ArrayList<ObservationCollection> obsCollList, Integer year) {
        ObservationCollection obsColl = null;

        for (int j = 0; j < obsCollList.size(); j++) {
            if (obsCollList.get(j).getDateTime().getYear() == year) {
                obsColl = obsCollList.get(j);
            }
        }
        return obsColl;
    }

    /**
     * Versucht aus einer Liste von ObservationCollections (MSRL-Daten) die
     * sechs fuer das Bewertungsjahr relevanten Beobachtungssammlungen zu
     * ermitteln und zurueck zu geben. Falls vom Bewertungsjahr ausgehend keine
     * sechs ObservationCollections verfuegbar sind, wird eine RuntimeException
     * ausgeloest.
     *
     * @param obsCollections - Liste von ObservationCollections fuer jeden
     * Beobachtungszeitpunkt
     * @param bewertungsjahr
     * @return Liste mit den sechs relevanten ObservationCollections
     */
    public static ArrayList<ObservationCollection> getRelevantObservationCollections(
            ArrayList<ObservationCollection> obsCollections,
            Integer bewertungsjahr) {
        ArrayList<ObservationCollection> preSelCollections = new ArrayList<ObservationCollection>();
        ArrayList<ObservationCollection> finalCollections = new ArrayList<ObservationCollection>();
        HashSet<Integer> existingYears = new HashSet<Integer>();

        // Schleife ueber alle ObservationCollections
        for (int i = 0; i < obsCollections.size(); i++) {
            ObservationCollection obsColl = obsCollections.get(i);

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
        Integer startIndex = MSRLD5Utils.getIdxOfObsCollbyYear(preSelCollections,
                bewertungsjahr);
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
        for (int k = 0; k < preSelCollections.size(); k++) {
            /*  LOGGER.debug("getRelevantFeatureCollections: "
             + preSelCollections.get(k).getDateTime().getYear() + "-"
             + preSelCollections.get(k).getDateTime().getMonthOfYear()
             + "-"
             + preSelCollections.get(k).getDateTime().getDayOfMonth()
             + " ausgewaehlt: "
             + FeatureCollectionUtil.tokm2Str(preSelCollections.get(k).getArea()));*/
        }
        return finalCollections;
    }
}
