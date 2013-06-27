package net.disy.wps.n52.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.disy.wps.n52.binding.MPBResultBinding;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;

public class MPBResultGenerator extends AbstractGenerator{
	
	public MPBResultGenerator() {
		super();
		supportedIDataTypes.add(MPBResultBinding.class);
	}

	@Override
	public InputStream generateStream(IData data, String mimeType, String schema)
			throws IOException {
		
		File mpbReport = (File) data.getPayload();
		return new FileInputStream(mpbReport);
	}
}
