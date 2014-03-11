package net.disy.wps.lkn.mpa.types;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper for lists of Integers, e.g. years. Necessary for marshalling.
 *
 * @author dalcacer
 */
@XmlRootElement(name = "IntegerList")
public class IntegerList extends ArrayList<Integer> {

    public IntegerList() {
        super();
    }

    @XmlElement(name = "Size")
    public int getSize() {
        return super.size();
    }

    @XmlElement(name = "Value")
    public Object[] getArray() {
        return super.toArray();
    }

    public File persist() {
        File f = null;
        try {
            JAXBContext context = JAXBContext.newInstance(IntegerList.class);
            Marshaller m = context.createMarshaller();
            String filename = this.getClass().getCanonicalName();
            filename += System.currentTimeMillis();
            f = File.createTempFile(filename, "tmp");

            m.marshal(this, f);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return f;
    }

    public static IntegerList read(File f) {
        IntegerList list = null;
        try {
            JAXBContext context = JAXBContext.newInstance(IntegerList.class);
            Unmarshaller um = context.createUnmarshaller();
            list = (IntegerList) um.unmarshal(new FileReader(f));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return list;
    }
}
