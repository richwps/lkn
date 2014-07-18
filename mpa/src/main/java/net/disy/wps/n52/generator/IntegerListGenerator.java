package net.disy.wps.n52.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import net.disy.wps.lkn.mpa.types.IntegerList;

import net.disy.wps.n52.binding.IntegerListBinding;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;

public class IntegerListGenerator extends AbstractGenerator {

    public IntegerListGenerator() {
        super();
        supportedIDataTypes.add(IntegerListBinding.class);
    }

    @Override
    //fixme remove file creation
    public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {
        IntegerListBinding ilb = (IntegerListBinding) data;
        net.disy.wps.lkn.mpa.types.IntegerList li = ilb.getPayload();

        File f = null;
        try {
            JAXBContext context = JAXBContext.newInstance(IntegerList.class);
            Marshaller m = context.createMarshaller();
            String filename = this.getClass().getCanonicalName();
            filename += System.currentTimeMillis();
            f = File.createTempFile(filename, "tmp");
            f.deleteOnExit();
            m.marshal(li, f);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return new FileInputStream(f);
    }
}
