package net.disy.wps.lkn.mpa.types;

import net.disy.wps.lkn.utils.FeatureCollectionUtil;

import net.disy.wps.lkn.utils.MPAUtils;


/**
 * Diese Klasse repraesentiert einen Ergebnis-Record als Teil eines
 * Bewertungsergebnisses
 * @see net.disy.wps.lkn.mpa.types.MPBResult
 * @author woessner
 * 
 */
public class MPBResultRecord implements Cloneable {

	public Integer year;
	public Double totalWattAreaNF, totalWattAreaDI;
	public Integer ZS_numTypesNF, ZS_numTypesDI;
	public Double ZS_totalareaNF, ZS_40areaNF, ZS_60areaNF, ZS_totalareaDI,
			ZS_40areaDI, ZS_60areaDI;
	public Double OP_totalareaNF, OP_40areaNF, OP_60areaNF, OP_totalareaDI,
			OP_40areaDI, OP_60areaDI;

	public Double p1Value, p2Value, p3Value, p4Value, p5Value;
	public Double p1EQR, p2EQR, p3EQR, p4EQR, p5EQR;
	public String p1Class, p2Class, p3Class, p4Class, p5Class;
	public String weightClass;
	public Double weightEQR;

	public String getYearStr() {
		return this.year.toString();
	}

	public Integer getYear() {
		return this.year;
	}

	public MPBResultRecord(Integer year, Double totalWattAreaNF,
			Double totalWattAreaDI, Integer ZS_numTypesNF,
			Integer ZS_numTypesDI, Double ZS_totalareaNF, Double ZS_40areaNF,
			Double ZS_60areaNF, Double ZS_totalareaDI, Double ZS_40areaDI,
			Double ZS_60areaDI, Double OP_totalareaNF, Double OP_40areaNF,
			Double OP_60areaNF, Double OP_totalareaDI, Double OP_40areaDI,
			Double OP_60areaDI) {
		this.year = year;
		this.totalWattAreaNF = totalWattAreaNF;
		this.totalWattAreaDI = totalWattAreaDI;
		this.ZS_numTypesNF = ZS_numTypesNF;
		this.ZS_numTypesDI = ZS_numTypesDI;
		this.ZS_totalareaNF = ZS_totalareaNF;
		this.ZS_40areaNF = ZS_40areaNF;
		this.ZS_60areaNF = ZS_60areaNF;
		this.ZS_totalareaDI = ZS_totalareaDI;
		this.ZS_40areaDI = ZS_40areaDI;
		this.ZS_60areaDI = ZS_60areaDI;
		this.OP_totalareaNF = OP_totalareaNF;
		this.OP_40areaNF = OP_40areaNF;
		this.OP_60areaNF = OP_60areaNF;
		this.OP_totalareaDI = OP_totalareaDI;
		this.OP_40areaDI = OP_40areaDI;
		this.OP_60areaDI = OP_60areaDI;
	}

	// Standardkonstruktor
	public MPBResultRecord() {
	}

	/*
	 * Gesamtflaechen
	 */
	public String getTotalWattAreaNF() {
		return FeatureCollectionUtil.tokm2Str(this.totalWattAreaNF);
	}

	public String getTotalWattAreaDI() {
		return FeatureCollectionUtil.tokm2Str(this.totalWattAreaDI);
	}

	public String getTotalWattArea() {
		return FeatureCollectionUtil.tokm2Str(this.totalWattAreaNF + this.totalWattAreaDI);
	}

	/*
	 * Seegras
	 */
	public String getZStotalAreaNF() {
		return FeatureCollectionUtil.tokm2Str(this.ZS_totalareaNF);
	}

	public String getZS40AreaNF() {
		return FeatureCollectionUtil.tokm2Str(this.ZS_40areaNF);
	}

	public String getZS60AreaNF() {
		return FeatureCollectionUtil.tokm2Str(this.ZS_60areaNF);
	}

	public String getZStotalAreaDI() {
		return FeatureCollectionUtil.tokm2Str(this.ZS_totalareaDI);
	}

	public String getZS40AreaDI() {
		return FeatureCollectionUtil.tokm2Str(this.ZS_40areaDI);
	}

	public String getZS60AreaDI() {
		return FeatureCollectionUtil.tokm2Str(this.ZS_60areaDI);
	}

	public String getZStotalArea() {
		return FeatureCollectionUtil.tokm2Str(this.ZS_totalareaNF + this.ZS_totalareaDI);
	}

	/*
	 * Algen
	 */
	public String getOPtotalAreaNF() {
		return FeatureCollectionUtil.tokm2Str(this.OP_totalareaNF);
	}

	public String getOP40AreaNF() {
		return FeatureCollectionUtil.tokm2Str(this.OP_40areaNF);
	}

	public String getOP60AreaNF() {
		return FeatureCollectionUtil.tokm2Str(this.OP_60areaNF);
	}

	public String getOPtotalAreaDI() {
		return FeatureCollectionUtil.tokm2Str(this.OP_totalareaDI);
	}

	public String getOP40AreaDI() {
		return FeatureCollectionUtil.tokm2Str(this.OP_40areaDI);
	}

	public String getOP60AreaDI() {
		return FeatureCollectionUtil.tokm2Str(this.OP_60areaDI);
	}

	public String getOPtotalArea() {
		return FeatureCollectionUtil.tokm2Str(this.OP_totalareaNF + this.OP_totalareaDI);
	}

	/*
	 * Bewertungsparameter
	 */

	public String getP1ValueStr() {
		return FeatureCollectionUtil.toDecimalStr(p1Value);
	}

	public void setP1Value(Double val) {
		this.p1Value = val;
	}

	public String getP2ValueStr() {
		return FeatureCollectionUtil.toDecimalStr(p2Value);
	}

	public void setP2Value(Double val) {
		this.p2Value = val;
	}

	public String getP3ValueStr() {
		return FeatureCollectionUtil.toDecimalStr(p3Value);
	}

	public void setP3Value(Double val) {
		this.p3Value = val;
	}

	public String getP4ValueStr() {
		return FeatureCollectionUtil.toDecimalStr(p4Value);
	}

	public void setP4Value(Double val) {
		this.p4Value = val;

	}

	public String getP5ValueStr() {
		return FeatureCollectionUtil.toDecimalStr(p3Value);
	}

	public void setP5Value(Double val) {
		this.p5Value = val;
	}

	public String getP1Class() {
		return p1Class;
	}

	public String getP2Class() {
		return p2Class;
	}

	public String getP3Class() {
		return p3Class;
	}

	public String getP4Class() {
		return p4Class;
	}

	public String getP5Class() {
		return p5Class;
	}

	public String getP1EQR() {
		return FeatureCollectionUtil.toDecimalStr(p1EQR);
	}

	public String getP2EQR() {
		return FeatureCollectionUtil.toDecimalStr(p2EQR);
	}

	public String getP3EQR() {
		return FeatureCollectionUtil.toDecimalStr(p3EQR);
	}

	public String getP4EQR() {
		return FeatureCollectionUtil.toDecimalStr(p4EQR);
	}

	public String getP5EQR() {
		return FeatureCollectionUtil.toDecimalStr(p5EQR);
	}

	public String getWeightEQR() {
		return FeatureCollectionUtil.toDecimalStr(weightEQR);
	}

	public String getWeightClass() {
		return weightClass;
	}

	/**
	 * Berechnet die fuenf zu bewertenden Parameter aus den Rohdaten
	 * @param evalMatrix
	 */
	public void calculateParameters(MPBEvalMatrix evalMatrix) {
		if (evalMatrix.areaOfValidity == MPAUtils.NORDFRIESLAND) {
			setP1Value(this.ZS_totalareaNF / this.totalWattAreaNF * 100);
			p2Value = this.ZS_60areaNF / this.ZS_totalareaNF * 100;
			// p3 ist konstant - basierend auf Annahmen
			p3Value = 75.73;
			p4Value = this.OP_totalareaNF / this.totalWattAreaNF * 100;
			p5Value = this.OP_60areaNF / this.OP_totalareaNF * 100;
		} else if (evalMatrix.areaOfValidity == MPAUtils.DITHMARSCHEN) {
			setP1Value(this.ZS_totalareaDI / this.totalWattAreaDI * 100);
			p2Value = this.ZS_60areaDI / this.ZS_totalareaDI * 100;
			// p3 ist konstant - basierend auf Annahmen
			p3Value = 0.0;
			p4Value = this.OP_totalareaDI / this.totalWattAreaDI * 100;
			p5Value = this.OP_60areaDI / this.OP_totalareaDI * 100;
		}

		p1Class = evalMatrix.evaluateToString(1, MPBEvalMatrix.EVALUATE_VALUE,
				p1Value);
		p1EQR = evalMatrix.getEQR(1, p1Value);

		p2Class = evalMatrix.evaluateToString(2, MPBEvalMatrix.EVALUATE_VALUE,
				p2Value);
		p2EQR = evalMatrix.getEQR(2, p2Value);

		p3Class = evalMatrix.evaluateToString(3, MPBEvalMatrix.EVALUATE_VALUE,
				p3Value);
		p3EQR = evalMatrix.getEQR(3, p3Value);

		p4Class = evalMatrix.evaluateToString(4, MPBEvalMatrix.EVALUATE_VALUE,
				p4Value);
		p4EQR = evalMatrix.getEQR(4, p4Value);

		p5Class = evalMatrix.evaluateToString(5, MPBEvalMatrix.EVALUATE_VALUE,
				p5Value);
		p5EQR = evalMatrix.getEQR(5, p5Value);

		this.weightClass = evalMatrix.evaluateToString(-1,
				MPBEvalMatrix.EVALUATE_EQR, getWeightedEQR());
		this.weightEQR = getWeightedEQR();
	}

	/**
	 * Erzeugt einen String mit Rohdaten eines ResultRecords Die einzelnen Werte
	 * sind auf zwei Stellen gerundet, haben die Einheit Quadratkilometer und
	 * werden mit Komma-Zeichen getrennt
	 * 
	 * @return String
	 */
	public String rawToString() {
		return this.getYearStr() + "," + this.getTotalWattAreaNF() + ","
				+ this.getTotalWattAreaDI() + "," + this.getTotalWattArea()
				+ "," + this.getZStotalAreaNF() + "," + this.getZS40AreaNF()
				+ "," + this.getZS60AreaNF() + "," + this.getZStotalAreaDI()
				+ "," + this.getZS40AreaDI() + "," + this.getZS60AreaDI() + ","
				+ this.getZStotalArea() + "," + this.getOPtotalAreaNF() + ","
				+ this.getOP40AreaNF() + "," + this.getOP60AreaNF() + ","
				+ this.getOPtotalAreaDI() + "," + this.getOP40AreaDI() + ","
				+ this.getOP60AreaDI() + "," + this.getOPtotalArea();
	}

	/**
	 * Berechnet den gewichteten EQR-Wert
	 * 
	 * @return
	 */
	public Double getWeightedEQR() {
		if (p1EQR >= 0.0 && p2EQR >= 0.0 & p3EQR >= 0.0 && p4EQR >= 0.0
				&& p5EQR >= 0.0) {
			return p1EQR * 0.5 + p2EQR * 0.1 + p3EQR * 0.1 + p4EQR * 0.2
					+ p5EQR * 0.1;
		} else {
			return -1.0;
		}
	}

	/**
	 * Erzeugt einen String mit Bewertungsergebnissen
	 * 
	 * @return
	 */
	public String evaluateToString() {
		// Werte mit Komma-Trennung ausgeben
		return this.getYearStr() + "," + FeatureCollectionUtil.toDecimalStr(p1Value) + ","
				+ p1Class + "," + FeatureCollectionUtil.toDecimalStr(p1EQR) + ","
				+ FeatureCollectionUtil.toDecimalStr(p2Value) + "," + p2Class + ","
				+ FeatureCollectionUtil.toDecimalStr(p2EQR) + "," + FeatureCollectionUtil.toDecimalStr(p3Value)
				+ "," + p3Class + "," + FeatureCollectionUtil.toDecimalStr(p3EQR) + ","
				+ FeatureCollectionUtil.toDecimalStr(p4Value) + "," + p4Class + ","
				+ FeatureCollectionUtil.toDecimalStr(p4EQR) + "," + FeatureCollectionUtil.toDecimalStr(p5Value)
				+ "," + p5Class + "," + FeatureCollectionUtil.toDecimalStr(p5EQR) + ","
				+ this.weightClass + "," + FeatureCollectionUtil.toDecimalStr(this.weightEQR);
	}

	/**
	 * Klont das MPBResultRecord-Objekt
	 */
	public MPBResultRecord clone() {
		return new MPBResultRecord(this.year, this.totalWattAreaNF,
				this.totalWattAreaDI, this.ZS_numTypesNF, this.ZS_numTypesDI,
				this.ZS_totalareaNF, this.ZS_40areaNF, this.ZS_60areaNF,
				this.ZS_totalareaDI, this.ZS_40areaDI, this.ZS_60areaDI,
				this.OP_totalareaNF, this.OP_40areaNF, this.OP_60areaNF,
				this.OP_totalareaDI, this.OP_40areaDI, this.OP_60areaDI);
	}
}
