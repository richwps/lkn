package net.disy.wps.n52.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import net.disy.wps.n52.binding.IntersectionFeatureCollectionListBinding;
import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollectionList;
import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollection;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;

public class IntersectionFeatureCollectionListGenerator extends AbstractGenerator {

    public IntersectionFeatureCollectionListGenerator() {
        super();
        supportedIDataTypes.add(IntersectionFeatureCollectionListBinding.class);
    }

    @Override
    public InputStream generateStream(IData data, String mimeType, String schema)
            throws IOException {

        IntersectionFeatureCollectionListBinding binding = (IntersectionFeatureCollectionListBinding) data;
        IntersectionFeatureCollectionList list = binding.getPayload();

        File f = null;
        try {
            JAXBContext context = JAXBContext.newInstance(
                    IntersectionFeatureCollectionList.class,
                    IntersectionFeatureCollection.class);
            Marshaller m = context.createMarshaller();

            String filename = this.getClass().getCanonicalName();
            filename += System.currentTimeMillis();
            f = File.createTempFile(filename, "tmp");

            m.marshal(list, f);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return new FileInputStream(f);
    }
}
