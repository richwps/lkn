package net.disy.wps.lkn.mpa.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper for lists of Integers, e.g. years. Necessary for marshalling.
 * Tricky: don't use inheritance or the corresponding parser won't kick in.
 * @author dalcacer
 */
@XmlRootElement(name = "IntegerList")
public class IntegerList implements Iterable<Integer> {

    private ArrayList<Integer> payload;

    public IntegerList() {
        this.payload = new ArrayList<Integer>();
    }

    public void add(Integer value) {
        this.payload.add(value);
    }

    public Integer get(int index) {
        return this.payload.get(index);
    }

    public int size() {
        return this.payload.size();
    }

    public void clear() {
        this.payload.clear();
    }

    public void addAll(Collection<? extends Integer> col) {
        this.payload.addAll(col);
    }

    public ArrayList<Integer> getPayload() {
        return this.payload;
    }

    public void addAll(IntegerList il) {
        this.payload.addAll(il.getPayload());
    }

    @Override
    public Iterator<Integer> iterator() {
        return this.payload.iterator();
    }

    @XmlElement(name = "Value")
    public Object[] getArray() {
        return this.payload.toArray();
    }
    
    public void setArray(Object[] os){
        for(Object o:os){
            Integer tmp = (Integer)o;
            this.add(tmp);
        }
        
    }
}
