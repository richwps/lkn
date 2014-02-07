package net.disy.wps.lkn.processes.mpa.types;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import net.disy.wps.lkn.utils.FeatureCollectionUtil;

import org.joda.time.DateTime;

/**
 * Diese Klasse repraesentiert das Bewertungsergebnis fuer ein einzelnes
 * Berichtsgebiet
 * 
 * @author woessner
 * 
 */
public class MPBAreaResult {

	// Eigenschaften
	private DateTime timestamp;
	private Integer bewertungsjahr;
	private Integer gebiet;
	private MPBEvalMatrix evalMatrix;
	ArrayList<MPBResultRecord> records;

	// Kontruktoren
	public MPBAreaResult() {
		timestamp = null;
		bewertungsjahr = null;
		gebiet = null;
		evalMatrix = null;
		records = new ArrayList<MPBResultRecord>();
	}

	public MPBAreaResult(DateTime timestamp, Integer bewertungsjahr,
			Integer gebiet, MPBEvalMatrix evalMatrix) {
		this.timestamp = timestamp;
		this.bewertungsjahr = bewertungsjahr;
		this.gebiet = gebiet;
		this.evalMatrix = evalMatrix;
		this.records = new ArrayList<MPBResultRecord>();
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

	public MPBResultRecord getRecordByYear(Integer year) {
		for (int i = 0; i < records.size(); i++) {
			if (year.equals(records.get(i).getYear())) {
				return records.get(i);
			}
		}
		return null;
	}

	/**
	 * Berechnet die Bewertungsparameter fuer jeden im Ergebnis enthaltenen
	 * Record
	 */
	public void calculateParameters() {
		for (int i = 0; i < records.size(); i++) {
			records.get(i).calculateParameters(evalMatrix);
		}
	}

	/**
	 * Erzeugt eine CSV-Tabelle der Rodaten-Ergebnisse und gibt diese als String zurueck
	 * @param metadata
	 * @return
	 */
	public String getRawRecordsString(boolean metadata) {
		String resultStr = "";
		if (metadata) {
			resultStr = resultStr + "Makrophytenbewertung ausgehend von "
					+ this.bewertungsjahr + "\n";
			resultStr = resultStr
					+ "Ausgabe der in die Bewertung einfliessenden Rohdaten";
			resultStr = resultStr + "Erstellt am " + this.timestamp.toString()
					+ "\n\n";
		}
		for (int i = 0; i < this.records.size(); i++) {
			resultStr = resultStr + records.get(i).rawToString() + "\n";
		}
		return resultStr;
	}

	/**
	 * Erzeugt eine CSV-Tabelle mit den Bewertungsergebnissen aller Records
	 * 
	 * @return
	 */
	public String getEvalRecordsString() {
		String resultStr = "";
		for (int i = 0; i < this.records.size(); i++) {
			records.get(i).calculateParameters(this.evalMatrix);
			resultStr = resultStr + records.get(i).evaluateToString() + "\n";
		}
		return resultStr;
	}

	/**
	 * Berechnet den mittleren EQR-Wert aller Records nach dem arithmetischen
	 * Mittel
	 * 
	 * @return
	 */
	public Double getMeanEQR() {
		Double weightedEQR;
		Double sum = 0.0;
		Integer count = this.records.size();

		for (int i = 0; i < this.records.size(); i++) {
			records.get(i).calculateParameters(this.evalMatrix);
			weightedEQR = records.get(i).getWeightedEQR();
			if (weightedEQR >= 0.0 && weightedEQR <= 1.0) {
				sum += weightedEQR;
			} else {
				count -= 1;
			}
		}
		return sum / count;
	}

	/**
	 * Erzeugt einen formatierten String aus dem mittleren EQR ueber alle
	 * Records
	 * 
	 * @return
	 */
	public String getMeanEQRString() {
		return FeatureCollectionUtil.toDecimalStr(getMeanEQR());
	}

	/**
	 * Gibt die bewertete String-Repraesentation des mittleren EQR-Wertes
	 * zurueck
	 * 
	 * @return
	 */
	public String getMeanEQREvalString() {
		Double value = getMeanEQR();
		return this.evalMatrix.evaluateToString(-1, MPBEvalMatrix.EVALUATE_EQR,
				value);
	}

	/**
	 * Reset des MPMAreaResult-Objekts
	 */
	public void clear() {
		this.timestamp = null;
		this.bewertungsjahr = null;
		this.records.clear();
	}
}