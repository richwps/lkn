package net.disy.wps.n52.parser;

import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import net.disy.wps.lkn.mpa.types.IntegerList;
import net.disy.wps.n52.binding.IntegerListBinding;

import org.n52.wps.io.datahandler.parser.AbstractParser;

/**
 *
 * @author dalcacer
 */
public class IntegerListParser extends AbstractParser {


    public IntegerListParser() {
        super();
        supportedIDataTypes.add(IntegerListBinding.class);
    }

    @Override
    public IntegerListBinding parse(InputStream input, String mimeType, String schema) {
        net.disy.wps.lkn.mpa.types.IntegerList il;
        IntegerListBinding ilb;
        try {
            //write XML to temporary file
            /*String filename = this.getClass().getCanonicalName();
             filename += System.currentTimeMillis();
             File f = File.createTempFile(filename, ".tmp");
             FileOutputStream out = new FileOutputStream(f);
             IOUtils.copy(input, out);*/

            JAXBContext context = JAXBContext.newInstance(IntegerList.class);
            Unmarshaller um = context.createUnmarshaller();
            il = (IntegerList) um.unmarshal(input);
             //encapsulate object in binding
            ilb = new IntegerListBinding(il);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ilb;
    }
}
