package net.disy.wps.lkn.mpa.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper for ObservationFeatureCollections. Necessary for marshalling. Tricky:
 * don't use inheritance or the corresponding parser won't kick in.
 *
 * @author dalcacer
 */
@XmlRootElement(name = "ObservationFeatureCollectionList")
public class ObservationFeatureCollectionList implements Iterable<ObservationFeatureCollection> {

    private ArrayList<ObservationFeatureCollection> payload;

    public ObservationFeatureCollectionList() {
        this.payload = new ArrayList();
    }

    public void add(ObservationFeatureCollection value) {
        this.payload.add(value);
    }

    public ObservationFeatureCollection get(int index) {
        return this.payload.get(index);
    }

    public boolean remove(Object o) {
        return this.payload.remove(o);
    }

    public int size() {
        return this.payload.size();
    }

    public void clear() {
        this.payload.clear();
    }

    public void addAll(Collection<? extends ObservationFeatureCollection> col) {
        this.payload.addAll(col);
    }

    @XmlElement(name = "Observation")
    @XmlElementWrapper(name = "Observations")
    public ArrayList<ObservationFeatureCollection> getPayload() {
        return this.payload;
    }

    public void addAll(ObservationFeatureCollectionList il) {
        this.payload.addAll(il.getPayload());
    }

    @Override
    public Iterator<ObservationFeatureCollection> iterator() {
        return this.payload.iterator();
    }

    /**
     * Ermittelt zu aus einer Liste von ObservationFeatureCollectionList und
     * einer Jahres-Angabe den Index des entsprechenden Element falls das Jahr
     * kleiner ist als die vorhanden Jahre in der Liste wird eine
     * RuntimeException ausgeloest. Falls das Jahr groesser ist als die
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
        Collections.sort(this.getPayload(), Collections.reverseOrder());
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
     * Liefert eine durch ein Jahr bestimme ObservationFeatureCollectionList aus
     * einer Liste von ObservationFeatureCollectionLists
     *
     * @param obsCollList - Liste von ObservationFeatureCollectionLists
     * @param year - Jahr, zu dem die ObservationFeatureCollectionList
     * zurueckgegeben werden soll
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

    
    @XmlElement(name = "Observation")
    public ArrayList<ObservationFeatureCollection> getObservations() {
        return this.payload;
    }
}
