package net.disy.wps.n52.parser;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import net.disy.wps.lkn.mpa.types.MPBResult;
import net.disy.wps.n52.binding.MPBResultBinding;

import org.n52.wps.io.datahandler.parser.AbstractParser;

/**
 *
 * @author woessner
 */
public class MpbResultParser extends AbstractParser {


    public MpbResultParser() {
        super();
        supportedIDataTypes.add(MPBResultBinding.class);
    }

    @Override
    public MPBResultBinding parse(InputStream input, String mimeType, String schema) {
        net.disy.wps.lkn.mpa.types.MPBResult mpbresult;
        MPBResultBinding mpbresultBinding;
        try {
            //write XML to temporary file
            /*String filename = this.getClass().getCanonicalName();
             filename += System.currentTimeMillis();
             File f = File.createTempFile(filename, ".tmp");
             FileOutputStream out = new FileOutputStream(f);
             IOUtils.copy(input, out);*/

            JAXBContext context = JAXBContext.newInstance(MPBResult.class);
            Unmarshaller um = context.createUnmarshaller();
            mpbresult = (MPBResult) um.unmarshal(input);
             //encapsulate object in binding
            mpbresultBinding = new MPBResultBinding(mpbresult);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mpbresultBinding;
    }
}
