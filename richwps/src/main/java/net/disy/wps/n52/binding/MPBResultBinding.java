package net.disy.wps.n52.binding;

import java.io.File;

import org.n52.wps.io.data.IComplexData;

public class MPBResultBinding implements IComplexData {
	
	private static final long serialVersionUID = 13154403015275324L;
	protected transient File mpbResult;
	
	public MPBResultBinding(File payload) {
		this.mpbResult = payload;
	}
	
	@Override
	public File getPayload() {
		return mpbResult;
	}
	
	@Override
	public Class<?> getSupportedClass() {
		return File.class;
	}

	@Override
	public void dispose() {
		
	}
}