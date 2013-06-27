package net.disy.wps.lkn;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.disy.wps.common.Util;

import org.joda.time.DateTime;

@XmlRootElement(name = "MPBResult")
public class MPBResult {
	
	private DateTime timestamp;
	private Integer bewertungsjahr;
	private ArrayList<MPBAreaResult> areaResults = new ArrayList<MPBAreaResult>();
	
	public MPBResult() {
		timestamp = DateTime.now();
		MPBEvalMatrix nfMatrix = new MPBEvalMatrix(MPBMain.NORDFRIESLAND);
		areaResults.add(new MPBAreaResult(timestamp, bewertungsjahr,
				MPBMain.NORDFRIESLAND, nfMatrix));
		MPBEvalMatrix diMatrix = new MPBEvalMatrix(MPBMain.DITHMARSCHEN);
		areaResults.add(new MPBAreaResult(timestamp, bewertungsjahr,
				MPBMain.DITHMARSCHEN, diMatrix));
	}
	
	private void initEvalMatrices() {
		for (int i = 0; i < areaResults.size(); i++) {
			if (areaResults.get(i).getEvalMatrix() == null) {
				areaResults.get(i).setEvalMatrix(
						new MPBEvalMatrix(areaResults.get(i).getGebiet()));
			}
		}
	}
	
	public DateTime getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(DateTime ts) {
		this.timestamp = ts;
	}
	
	public MPBAreaResult getNFResult() {
		for (int i = 0; i < areaResults.size(); i++) {
			if (areaResults.get(i).getGebiet() == MPBMain.NORDFRIESLAND) {
				return areaResults.get(i);
			}
		}
		return null;
	}
	
	public MPBAreaResult getDIResult() {
		for (int i = 0; i < areaResults.size(); i++) {
			if (areaResults.get(i).getGebiet() == MPBMain.DITHMARSCHEN) {
				return areaResults.get(i);
			}
		}
		return null;
	}
	
	@XmlElement(name = "Bewertungsjahr")
	public Integer getBewertungsjahr() {
		return this.bewertungsjahr;
	}
	
	public void setBewertungsjahr(Integer bewertungsjahr) {
		this.bewertungsjahr = bewertungsjahr;
		getAreaResult(MPBMain.NORDFRIESLAND).setBewertungsjahr(bewertungsjahr);
		getAreaResult(MPBMain.DITHMARSCHEN).setBewertungsjahr(bewertungsjahr);
	}
	
	public MPBAreaResult getAreaResult(Integer gebiet) {
		switch (gebiet) {
		case MPBMain.NORDFRIESLAND:
			return getNFResult();
		case MPBMain.DITHMARSCHEN:
			return getDIResult();
		}
		return null;
	}
	
	@XmlElement(name = "MPBAreaResult")
	public ArrayList<MPBAreaResult> getAreaResults() {
		return areaResults;
	}
	
	public void addRecord(MPBResultRecord rec) {
		for (int i = 0; i < areaResults.size(); i++) {
			areaResults.get(i).addRecord(rec.clone());
		}
	}
	
	public void calculateParameters() {
		initEvalMatrices();
		for (int i = 0; i < areaResults.size(); i++) {
			areaResults.get(i).calculateParameters();
		}
	}

	public String getMeanEqrOfNF() {
		return Util.toDecimalStr(getAreaResult(MPBMain.NORDFRIESLAND).getMeanEQR());
	}
	public String getMeanEqrOfDI() {
		return Util.toDecimalStr(getAreaResult(MPBMain.DITHMARSCHEN).getMeanEQR());
	}

	public String getMeanEqrStringOfNF() {
		return getAreaResult(MPBMain.NORDFRIESLAND).getMeanEQREvalString();
	}
	public String getMeanEqrStringOfDI() {
		return getAreaResult(MPBMain.DITHMARSCHEN).getMeanEQREvalString();
	}
	
	public String getRawRecordsString(boolean metadata) {
		initEvalMatrices();
		// Sollte für beide AreaResults gleich sein!
		return getAreaResult(MPBMain.DITHMARSCHEN)
				.getRawRecordsString(metadata);
	}
	
	public String getEvalRecordsString(boolean metadata) {
		String resultStr = "";
		initEvalMatrices();
		if (metadata) {
			resultStr = resultStr + "Makrophytenbewertung ausgehend von "
					+ this.bewertungsjahr + "\n";
			resultStr = resultStr + "Ausgabe der Bewertungsergebnisse";
			resultStr = resultStr + "Erstellt am " + this.timestamp.toString()
					+ "\n\n";
			resultStr = resultStr
					+ "Ergebnismatrix für das Berichtsgebiet Nordfriesland"
					+ "\n\n";
		}
		
		resultStr += getAreaResult(MPBMain.NORDFRIESLAND)
				.getEvalRecordsString();
		resultStr = resultStr
				+ Util.toDecimalStr(getAreaResult(MPBMain.NORDFRIESLAND)
						.getMeanEQR()) + ","
				+ getAreaResult(MPBMain.NORDFRIESLAND).getMeanEQREvalString()
				+ "\n";
		
		if (metadata) {
			resultStr = resultStr + "\n"
					+ "Ergebnismatrix für das Berichtsgebiet Dithmarschen"
					+ "\n\n";
		}
		resultStr += getAreaResult(MPBMain.DITHMARSCHEN).getEvalRecordsString();
		resultStr = resultStr
				+ Util.toDecimalStr(getAreaResult(MPBMain.DITHMARSCHEN)
						.getMeanEQR()) + ","
				+ getAreaResult(MPBMain.DITHMARSCHEN).getMeanEQREvalString()
				+ "\n";
		
		return resultStr;
	}
	
}
