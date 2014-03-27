package net.disy.wps.n52.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import net.disy.wps.lkn.mpa.types.ObservationFeatureCollection;
import net.disy.wps.lkn.mpa.types.ObservationFeatureCollectionList;
import net.disy.wps.n52.binding.ObeservationFeatureCollectionListBinding;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;

/**^
 * Generator for ObservationFeatureCollectionBindings.
 *
 * @author dalcacer
 */
public class ObservationFeatureCollectionListGenerator extends AbstractGenerator {

    public ObservationFeatureCollectionListGenerator() {
        super();
        supportedIDataTypes.add(ObeservationFeatureCollectionListBinding.class);
    }

    @Override
    public InputStream generateStream(IData data, String mimeType, String schema)
            throws IOException {
        ObeservationFeatureCollectionListBinding binding = (ObeservationFeatureCollectionListBinding) data;
        ObservationFeatureCollectionList list = binding.getPayload();

        File f = null;
        try {
            JAXBContext context = JAXBContext.newInstance(
                    ObservationFeatureCollectionList.class,
                    ObservationFeatureCollection.class);
            Marshaller m = context.createMarshaller();

            String filename = this.getClass().getCanonicalName();
            filename += System.currentTimeMillis();
            f = File.createTempFile(filename, "tmp");

            m.marshal(list, f);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new FileInputStream(f);
    }
}
