package net.disy.wps.n52.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import net.disy.wps.n52.binding.IntegerListBinding;
import org.apache.commons.io.IOUtils;

import org.n52.wps.io.datahandler.parser.AbstractParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegerListParser extends AbstractParser {

    private static Logger LOGGER = LoggerFactory.getLogger(IntegerListParser.class);

    public IntegerListParser() {
        super();
        supportedIDataTypes.add(IntegerListBinding.class);
    }

    @Override
    public IntegerListBinding parse(InputStream input, String mimeType, String schema) {
        net.disy.wps.lkn.mpa.types.IntegerList il;
        IntegerListBinding ilb;
        try {
            String filename = this.getClass().getCanonicalName();
            filename += System.currentTimeMillis();
            File f = File.createTempFile(filename, ".tmp");
            f.deleteOnExit();
            FileOutputStream out = null;
            out = new FileOutputStream(f);
            IOUtils.copy(input, out);
            il = net.disy.wps.lkn.mpa.types.IntegerList.read(f);
            ilb = new IntegerListBinding(il);
        } catch (IOException ioe) {
            LOGGER.error(ioe.getMessage());
            throw new RuntimeException(ioe);
        }
        return ilb;
    }

}
