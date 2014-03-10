package net.disy.wps.lkn.mpa.types;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

/**

 * Wrapper for ObservationFeatureCollections.
 * Necessary for marshalling.
 * @author dalcacer
 */
@XmlRootElement(name = "IntersectionFeatureCollectionList")
public class IntersectionFeatureCollectionList extends ArrayList<IntersectionFeatureCollection> {

    
    public File persist() {
        File f = null;
        try {
            JAXBContext context = JAXBContext.newInstance(IntersectionFeatureCollectionList.class);
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

}
