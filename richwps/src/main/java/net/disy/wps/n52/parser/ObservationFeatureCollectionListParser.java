package net.disy.wps.n52.parser;

import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import net.disy.wps.lkn.mpa.types.ObservationFeatureCollection;
import net.disy.wps.lkn.mpa.types.ObservationFeatureCollectionList;

import org.n52.wps.io.datahandler.parser.AbstractParser;
import net.disy.wps.n52.binding.ObeservationFeatureCollectionListBinding;

/**
 *
 * @author dalcacer
 */
public class ObservationFeatureCollectionListParser extends AbstractParser {

    public ObservationFeatureCollectionListParser() {
        super();
        supportedIDataTypes.add(ObeservationFeatureCollectionListBinding.class);
    }

    @Override
    public ObeservationFeatureCollectionListBinding parse(InputStream input, String mimeType, String schema) {
        ObservationFeatureCollectionList list;
        ObeservationFeatureCollectionListBinding ilb;
        try {
            //write XML to temporary file
            /*String filename = this.getClass().getCanonicalName();
             filename += System.currentTimeMillis();
             File f = File.createTempFile(filename, ".tmp");
             FileOutputStream out = new FileOutputStream(f);
             IOUtils.copy(input, out);*/

            JAXBContext context = JAXBContext.newInstance(ObservationFeatureCollectionList.class, ObservationFeatureCollection.class);
            Unmarshaller um = context.createUnmarshaller();
            list = (ObservationFeatureCollectionList) um.unmarshal(input);
            //encapsulate object in binding
            ilb = new ObeservationFeatureCollectionListBinding(list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ilb;
    }
}
