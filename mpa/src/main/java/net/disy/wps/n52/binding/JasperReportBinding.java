package net.disy.wps.n52.binding;

import java.io.File;

import org.n52.wps.io.data.IComplexData;

public class JasperReportBinding implements IComplexData {

	private static final long serialVersionUID = 905521883368917579L;
	protected transient File document;
	
	public JasperReportBinding(File payload) {
		this.document = payload;
	}
	
	@Override
	public File getPayload() {
		return document;
	}
	
	@Override
	public Class<?> getSupportedClass() {
		return File.class;
	}

	@Override
	public void dispose() {
	
	}
}