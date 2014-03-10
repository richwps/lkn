package net.disy.wps.n52.binding;

import java.io.File;

import org.n52.wps.io.data.IComplexData;

public class IntersectionFeatureCollectionListBinding implements IComplexData {
	
	private static final long serialVersionUID = 13154403015275324L;
	protected transient File payload;
	
	public IntersectionFeatureCollectionListBinding(File payload) {
		this.payload = payload;
	}
	
	@Override
	public File getPayload() {
		return this.payload;
	}
	
	@Override
	public Class<?> getSupportedClass() {
		return File.class;
	}

	@Override
	public void dispose() {	
	}
        
        
}