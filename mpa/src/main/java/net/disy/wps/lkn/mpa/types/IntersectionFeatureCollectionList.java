package net.disy.wps.lkn.mpa.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper for ObservationFeatureCollections. Necessary for marshalling.
 *
 * @author dalcacer
 */
//Tricky: do not use inheritance or the corresponding parser will not work.
@XmlRootElement(name = "IntersectionFeatureCollectionList")
public class IntersectionFeatureCollectionList
        implements Iterable<IntersectionFeatureCollection> {

    private ArrayList<IntersectionFeatureCollection> payload;

    public IntersectionFeatureCollectionList() {
        this.payload = new ArrayList();
    }

    public void add(IntersectionFeatureCollection value) {
        this.payload.add(value);
    }

    public IntersectionFeatureCollection get(int index) {
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

    public void addAll(Collection<? extends IntersectionFeatureCollection> col) {
        this.payload.addAll(col);
    }

    public void addAll(IntersectionFeatureCollectionList il) {
        this.payload.addAll(il.getPayload());
    }

    @XmlElement(name = "Intersection")
    @XmlElementWrapper(name = "Intersection")
    public ArrayList<IntersectionFeatureCollection> getPayload() {
        return this.payload;
    }

    @XmlElement(name = "Intersection")
    public ArrayList<IntersectionFeatureCollection> getIntersections() {
        return this.payload;
    }

    @Override
    public Iterator<IntersectionFeatureCollection> iterator() {
        return this.payload.iterator();
    }

}
