package net.disy.wps.lkn.mpa.types;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper for lists of Integers, e.g. years.
 * Necessary for marshalling.
 * @author dalcacer
 */
@XmlRootElement(name = "IntegerList")
public class IntegerList extends ArrayList<Integer>{
    
    public IntegerList(){}
    
    @XmlElement(name = "Size")
    public int size(){
        return super.size();
    }

    @Override
    public Object[] toArray() {
        return super.toArray(); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
