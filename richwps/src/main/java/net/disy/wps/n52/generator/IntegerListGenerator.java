package net.disy.wps.n52.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.disy.wps.n52.binding.IntegerListBinding;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;

public class IntegerListGenerator extends AbstractGenerator{
	
	public IntegerListGenerator() {
		super();
		supportedIDataTypes.add(IntegerListBinding.class);
	}

	@Override
	public InputStream generateStream(IData data, String mimeType, String schema)throws IOException {
		IntegerListBinding ilb = (IntegerListBinding) data;
                net.disy.wps.lkn.mpa.types.IntegerList li = ilb.getPayload();
                File f = li.persist();
		return new FileInputStream(f);
	}
}
