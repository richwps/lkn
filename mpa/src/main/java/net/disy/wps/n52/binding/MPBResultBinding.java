package net.disy.wps.n52.binding;

import java.io.File;

import org.n52.wps.io.data.IComplexData;

public class MPBResultBinding implements IComplexData {
	
	private static final long serialVersionUID = 13154403015275324L;
	protected transient net.disy.wps.lkn.mpa.types.MPBResult mpbResult;
	
	public MPBResultBinding(net.disy.wps.lkn.mpa.types.MPBResult payload) {
		this.mpbResult = payload;
	}
	
	@Override
	public net.disy.wps.lkn.mpa.types.MPBResult getPayload() {
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