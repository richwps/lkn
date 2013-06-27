package net.disy.wps.testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;

public class SampleMultiplication extends AbstractSelfDescribingAlgorithm {

	@Override
	// Input Identifiers
	public List<String> getInputIdentifiers() {
		List<String> identifiers = new ArrayList<String>();
		identifiers.add("zahl1");
		identifiers.add("zahl2");
		return identifiers;
	}

	@Override
	// Output Identifiers
	public List<String> getOutputIdentifiers() {
		List<String> identifiers = new ArrayList<String>();
		identifiers.add("ergebnis");
		return identifiers;
	}
	
	@Override
	// Input Data Type
	public Class<?> getInputDataType(String identifier) {
		if (identifier.equals("zahl1")) {
			return LiteralStringBinding.class;
		}
		else if (identifier.equals("zahl2")) {
			return LiteralStringBinding.class;
		}
		throw new RuntimeException("Error: Falscher Identifier!");
	}

	@Override
	// Output Data Type
	public Class<?> getOutputDataType(String identifier) {
		if (identifier.equals("ergebnis")) {
			return LiteralDoubleBinding.class;
		}
		throw new RuntimeException("Error: Falsche Indentifier!");
	}

	@Override
	// Run
	public Map<String, IData> run(Map<String, List<IData>> inputMap){
		
		Double dErgebnis;
		
		// Input zahl1 bearbeiten
		List<IData> zahl1list = inputMap.get("zahl1");
		if (zahl1list.size()==0) {
			throw new RuntimeException("Zahl1 wurde nicht angegeben!");
		}
		IData zahl1 = zahl1list.get(0);
		String SZahl1 = (String) zahl1.getPayload();
		
		// Input zahl2 bearbeiten
		List<IData> zahl2list = inputMap.get("zahl2");
		if (zahl2list.size()==0) {
			throw new RuntimeException("Zahl2 wurde nicht angegeben!");
		}
		IData zahl2 = zahl2list.get(0);
		String SZahl2 = (String) zahl2.getPayload();
		
		Double dZahl1 = Double.parseDouble(SZahl1);
		Double dZahl2 = Double.parseDouble(SZahl2);
		
		dErgebnis = dZahl1 * dZahl2;
		
		//create the response. In this case a GenericFileDataBinding is used (see this.getOutputDataType(...)
		IData result = new LiteralDoubleBinding(dErgebnis);
		
		//new Map created
		Map<String, IData> resultMap = new HashMap<String, IData>();
				
		//created response added to corresponding identifier (see this.getOutputIdentifiers())
		resultMap.put("ergebnis", result);
		
		//throws ExceptionReport {
		// TODO Auto-generated method stub
		return resultMap;
	}

}
