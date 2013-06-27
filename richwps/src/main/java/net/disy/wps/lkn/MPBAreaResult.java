package net.disy.wps.lkn;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import net.disy.wps.common.Util;

import org.joda.time.DateTime;

public class MPBAreaResult{

	// Eigenschaften
	private DateTime timestamp;
	private Integer bewertungsjahr;
	private Integer gebiet;
	private MPBEvalMatrix evalMatrix;
	
	ArrayList<MPBResultRecord> records = new ArrayList<MPBResultRecord>();
	
	// Kontruktoren
	public MPBAreaResult() {
		records = new ArrayList<MPBResultRecord>();
	}
	public MPBAreaResult(DateTime timestamp, Integer bewertungsjahr, Integer gebiet, MPBEvalMatrix evalMatrix) {
		this.timestamp = timestamp;
		this.bewertungsjahr = bewertungsjahr;
		this.gebiet = gebiet;
		this.evalMatrix = evalMatrix;
	}
	
	public Integer getBewertungsjahr() {
		return this.bewertungsjahr;
	}
	public void setBewertungsjahr(Integer year) {
		this.bewertungsjahr = year;
	}
	
	@XmlAttribute
	public Integer getGebiet() {
		return gebiet;
	}
	public void setGebiet(Integer val) {
		gebiet = val;
	}
	
	@XmlTransient
	public MPBEvalMatrix getEvalMatrix() {
		return evalMatrix;
	}
	public void setEvalMatrix(MPBEvalMatrix matrix) {
		this.evalMatrix = matrix;
	}
	
	public void addRecord(MPBResultRecord rec) {
		records.add(rec);
	}
	@XmlElement(name = "MPBRecord")
	public ArrayList<MPBResultRecord> getRecords() {
		return records;
	}
	
	public void calculateParameters() {
		for (int i=0;i<records.size();i++) {
			records.get(i).calculateParameters(evalMatrix);
		}
	}
	
	public String getRawRecordsString(boolean metadata) {
		String resultStr ="";
		if(metadata){
			resultStr = resultStr + "Makrophytenbewertung ausgehend von " + this.bewertungsjahr + "\n";
			resultStr = resultStr + "Ausgabe der in die Bewertung einfließenden Rohdaten";
			resultStr = resultStr + "Erstellt am " + this.timestamp.toString() + "\n\n";
		}
		for(int i=0;i<this.records.size();i++) {
			resultStr = resultStr + records.get(i).rawToString() + "\n";
		}
		return resultStr;
	}
	
	public String getEvalRecordsString() {
		String resultStr = "";
		for(int i=0;i<this.records.size();i++) {
			records.get(i).calculateParameters(this.evalMatrix);
			resultStr = resultStr + records.get(i).evaluateToString() + "\n";
		}
		return resultStr;
	}

	public Double getMeanEQR() {
		Double weightedEQR;
		Double sum = 0.0;
		Integer count = this.records.size();
		
		for(int i=0;i<this.records.size();i++) {
			  records.get(i).calculateParameters(this.evalMatrix);
			  weightedEQR = records.get(i).getWeightedEQR(); 
			  if (weightedEQR >=0.0 && weightedEQR <=1.0) {
				  sum += weightedEQR;
			  }
			  else {
				  count -= 1;
			  }
		}
		return sum / count;
	}
	public String getMeanEQRString() {
		return Util.toDecimalStr(getMeanEQR());
	}
	
	public void clear() {
		this.timestamp = null;
		this.bewertungsjahr = null;
		this.records.clear();
	}
	
	public String getMeanEQREvalString() {
		Double value = getMeanEQR();
		return this.evalMatrix.evaluateToString(-1, MPBEvalMatrix.EVALUATE_EQR, value);
	}
	
}
