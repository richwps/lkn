package net.disy.wps.lkn.utils;

import java.util.ArrayList;
import java.util.Collections;
import net.disy.wps.lkn.processes.mpa.types.ObservationCollection;

/**
 *
 * @author dalcacer
 */
public class Observations extends ArrayList<ObservationCollection> {

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
    public Integer getIdxOfObsCollbyYear(Integer year) {

        Integer i = 0;
        // sort descending by DateTime of ObservationCollection
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
     * Liefert eine durch ein Jahr bestimme ObservationCollection aus einer
     * Liste von ObservationCollections
     *
     * @param obsCollList - Liste von ObservationCollections
     * @param year - Jahr, zu dem die ObservationCollection zurueckgegeben
     * werden soll
     * @return ObservationCollection
     */
    public ObservationCollection getObsCollByYear(Integer year) {
        ObservationCollection obsColl = null;

        for (int j = 0; j < this.size(); j++) {
            if (this.get(j).getDateTime().getYear() == year) {
                obsColl = this.get(j);
            }
        }
        return obsColl;
    }

}
