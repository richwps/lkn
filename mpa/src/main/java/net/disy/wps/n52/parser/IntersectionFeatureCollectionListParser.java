package net.disy.wps.n52.parser;

import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollection;
import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollectionList;

import org.n52.wps.io.datahandler.parser.AbstractParser;
import net.disy.wps.n52.binding.IntersectionFeatureCollectionListBinding;

/**
 * Parser for IntersectionFeatureCollectionBindings.
 *
 * @author dalcacer
 */
public class IntersectionFeatureCollectionListParser extends AbstractParser {

    public IntersectionFeatureCollectionListParser() {
        super();
        supportedIDataTypes.add(IntersectionFeatureCollectionListBinding.class);
    }

    @Override
    public IntersectionFeatureCollectionListBinding parse(InputStream input,
            String mimeType, String schema) {
        IntersectionFeatureCollectionList list;
        IntersectionFeatureCollectionListBinding binding;
        try {
            JAXBContext context = JAXBContext.newInstance(
                    IntersectionFeatureCollectionList.class,
                    IntersectionFeatureCollection.class);

            Unmarshaller um = context.createUnmarshaller();
            list = (IntersectionFeatureCollectionList) um.unmarshal(input);
            binding = new IntersectionFeatureCollectionListBinding(list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return binding;
    }
}
