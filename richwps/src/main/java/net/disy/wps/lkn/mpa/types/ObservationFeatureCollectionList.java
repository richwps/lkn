package net.disy.wps.lkn.mpa.types;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author dalcacer
 */
public class ObservationsList extends ArrayList<ObservationFeatureCollection> {

    /**
     * Ermittelt zu aus einer Liste von ObservationFeatureCollectionList und einer
 Jahres-Angabe den Index des entsprechenden Element falls das Jahr
 kleiner ist als die vorhanden Jahre in der Liste wird eine
 RuntimeException ausgeloest. Falls das Jahr groesser ist als die
     * vorhandene Jahre in der Liste wird der Index des zeitlich
     * naechstliegenden Jahres zurueckgegeben.
     *
     * @param obsCollList - Liste mit allen ObservationFeatureCollectionLists
     * @param year - Jahr
     * @return Index
     */
    public Integer getIdxOfObsCollbyYear(Integer year) {

        Integer i = 0;
        // sort descending by DateTime of ObservationFeatureCollection
        Collections.sort(this, Collections.reverseOrder());
        Integer listYear = this.get(i).getDateTime().getYear();
        // Schleife, bis aktuelles Jahr aus der Liste nicht mehr kleier als das
        // Bewertungsjahr ist
        do {
            // RuntimException, wenn die Anzahl der Schleifendurchlaeufe die
            // Groesse der Liste ueberschreitet
            if (i > this.size() - 1) {
                throw new RuntimeException(
                        "Invalid year or non fitting data! Try to use "
                        + listYear + " as year.");
            }
            listYear = this.get(i).getDateTime().getYear();
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
     * Liefert eine durch ein Jahr bestimme ObservationFeatureCollectionList aus einer Liste von
 ObservationFeatureCollectionLists
     *
     * @param obsCollList - Liste von ObservationFeatureCollectionLists
     * @param year - Jahr, zu dem die ObservationFeatureCollectionList zurueckgegeben werden
 soll
     * @return ObservationFeatureCollectionList
     */
    public ObservationFeatureCollection getObsCollByYear(Integer year) {
        ObservationFeatureCollection obsColl = null;

        for (int j = 0; j < this.size(); j++) {
            if (this.get(j).getDateTime().getYear() == year) {
                obsColl = this.get(j);
            }
        }
        return obsColl;
    }

}
