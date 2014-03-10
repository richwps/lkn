package net.disy.wps.n52.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.disy.wps.n52.binding.ObeservationFeatureCollectionListBinding;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;

public class ObservationFeatureCollectionListGenerator extends AbstractGenerator {

    public ObservationFeatureCollectionListGenerator() {
        super();
        supportedIDataTypes.add(ObeservationFeatureCollectionListBinding.class);
    }

    @Override
    public InputStream generateStream(IData data, String mimeType, String schema)
            throws IOException {

        File ofecolib = (File) data.getPayload();
        return new FileInputStream(ofecolib);
    }
}
