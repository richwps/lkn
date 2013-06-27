package net.disy.wps.lkn;

import java.util.ArrayList;

public class MPBEvalMatrix {
	
	String areaOfValidity;
	ArrayList<MPBEvalParameter> params = new ArrayList<MPBEvalParameter>();
	ArrayList<MPBEvalCategory> normEQRcategories = new ArrayList<MPBEvalCategory>();
	
	public static final int EVALUATE_VALUE = 1;
	public static final int EVALUATE_EQR = 2;
	
	public MPBEvalMatrix() {
	}
	
	public MPBEvalMatrix(Integer area) {
		params = new ArrayList<MPBEvalParameter>();
		if (area == MPBMain.NORDFRIESLAND) {
			createNFMatrix();
		}
		else if(area == MPBMain.DITHMARSCHEN) {
			createDIMatrix();
		}
		else {
			throw new RuntimeException("Das Bewertungsgebiet '" + area + "' ist unbekannt!");
		}
	}
	
	private void createNFMatrix() {
		this.areaOfValidity = "Nordfriesland";
		MPBEvalParameter param;
		MPBEvalCategory cat;
		
		// NormEQR Kategorien
		cat = new MPBEvalCategory(0, 0.0, 0.195, 0.0, 0.2);
		normEQRcategories.add(cat);
		cat = new MPBEvalCategory(1, 0.195, 0.395, 0.2, 0.4);
		normEQRcategories.add(cat);
		cat = new MPBEvalCategory(2, 0.395, 0.595, 0.4, 0.6);
		normEQRcategories.add(cat);
		cat = new MPBEvalCategory(3, 0.595, 0.795, 0.6, 0.8);
		normEQRcategories.add(cat);
		cat = new MPBEvalCategory(4, 0.795, 1.0, 0.8, 1.0);
		normEQRcategories.add(cat);
		
		// Parameter 1
		param = new MPBEvalParameter(1, "Seegras - Eulitorale Fläche (%)");
		cat = new MPBEvalCategory(0, 0.0, 2.0, 0.0, 2.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(1, 2.0, 4.95, 2.0, 5.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(2, 4.95, 9.95, 5.0, 10.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(3, 9.95, 19.95, 10.0, 20.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(4, 19.95, 100.0, 20.0, 100.0);
		param.addCategory(cat);
		params.add(param);
		
		// Parameter 2
		param = new MPBEvalParameter(2, "Seegras - Anteil >=60%-Bedeckung (%)");
		cat = new MPBEvalCategory(0, 0.0, 6.0, 0.0, 6.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(1, 6.0, 11.95, 6.0, 12.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(2, 11.95, 24.95, 12.0, 25.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(3, 24.95, 49.95, 25.0, 50.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(4, 49.95, 100.0, 50.0, 100.0);
		param.addCategory(cat);
		params.add(param);
		
		// Parameter 3
		param = new MPBEvalParameter(3, "Seegras - Präsenz beider Arten (%)");
		cat = new MPBEvalCategory(0, 0.0, 20.0, 0.0, 20.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(1, 20.0, 39.95, 20.0, 40.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(2, 39.95, 59.95, 40.0, 60.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(3, 59.95, 79.95, 60.0, 80.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(4, 79.95, 100.0, 80.0, 100.0);
		param.addCategory(cat);
		params.add(param);
		
		// Parameter 4
		param = new MPBEvalParameter(4, "Grünalgen - Eulitorale Fläche (%)");
		cat = new MPBEvalCategory(0, 14.95, 100.0, 15.0, 100.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(1, 6.95, 14.95, 7.0, 15.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(2, 2.95, 6.95, 3.0, 7.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(3, 1.0, 2.95, 1.0, 3.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(4, 0.0, 1.0, 0.0, 1.0);
		param.addCategory(cat);
		params.add(param);
		
		// Parameter 5
		param = new MPBEvalParameter(5, "Grünalgen - Anteil >=60%-Bedeckung (%)");
		cat = new MPBEvalCategory(0, 49.95, 100.0, 50.0, 100.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(1, 24.95, 49.95, 25.0, 50.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(2, 11.95, 24.95, 12.0, 25.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(3, 6.0, 11.95, 6.0, 12.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(4, 0.0, 6.0, 0.0, 6.0);
		param.addCategory(cat);
		params.add(param);
	}
	
	private void createDIMatrix() {
		this.areaOfValidity = "Dithmarschen";
		MPBEvalParameter param;
		MPBEvalCategory cat;
		
		// NormEQR Kategorien
		cat = new MPBEvalCategory(0, 0.0, 0.195, 0.0, 0.2);
		normEQRcategories.add(cat);
		cat = new MPBEvalCategory(1, 0.195, 0.395, 0.2, 0.4);
		normEQRcategories.add(cat);
		cat = new MPBEvalCategory(2, 0.395, 0.595, 0.4, 0.6);
		normEQRcategories.add(cat);
		cat = new MPBEvalCategory(3, 0.595, 0.795, 0.6, 0.8);
		normEQRcategories.add(cat);
		cat = new MPBEvalCategory(4, 0.795, 1.0, 0.8, 1.0);
		normEQRcategories.add(cat);
		
		// Parameter 1
		param = new MPBEvalParameter(1, "Seegras - Eulitorale Fläche (%)");
		cat = new MPBEvalCategory(0, 0.0, 0.3, 0.0, 0.3);
		param.addCategory(cat);
		cat = new MPBEvalCategory(1, 0.3, 0.695, 0.3, 0.7);
		param.addCategory(cat);
		cat = new MPBEvalCategory(2, 0.695, 1.495, 0.7, 1.5);
		param.addCategory(cat);
		cat = new MPBEvalCategory(3, 1.495, 2.95, 1.5, 3.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(4, 2.95, 100.0, 3.0, 100.0);
		param.addCategory(cat);
		params.add(param);
		
		// Parameter 2
		param = new MPBEvalParameter(2, "Seegras - Anteil >=60%-Bedeckung (%)");
		cat = new MPBEvalCategory(0, 0.0, 6.0, 0.0, 6.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(1, 6.0, 11.95, 6.0, 12.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(2, 11.95, 24.95, 12.0, 25.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(3, 24.95, 49.95, 25.0, 50.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(4, 49.95, 100.0, 50.0, 100.0);
		param.addCategory(cat);
		params.add(param);
		
		// Parameter 3
		param = new MPBEvalParameter(3, "Seegras - Präsenz beider Arten (%)");
		cat = new MPBEvalCategory(0, 0.0, 20.0, 0.0, 20.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(1, 20.0, 39.95, 20.0, 40.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(2, 39.95, 59.95, 40.0, 60.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(3, 59.95, 79.95, 60.0, 80.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(4, 79.95, 100.0, 80.0, 100.0);
		param.addCategory(cat);
		params.add(param);
		
		// Parameter 4
		param = new MPBEvalParameter(4, "Grünalgen - Eulitorale Fläche (%)");
		cat = new MPBEvalCategory(0, 14.95, 100.0, 15.0, 100.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(1, 6.95, 14.95, 7.0, 15.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(2, 2.95, 6.95, 3.0, 7.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(3, 1.0, 2.95, 1.0, 3.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(4, 0.0, 1.0, 0.0, 1.0);
		param.addCategory(cat);
		params.add(param);
		
		// Parameter 5
		param = new MPBEvalParameter(5, "Grünalgen - Anteil >=60%-Bedeckung (%)");
		cat = new MPBEvalCategory(0, 49.95, 100.0, 50.0, 100.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(1, 24.95, 49.95, 25.0, 50.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(2, 11.95, 24.95, 12.0, 25.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(3, 6.0, 11.95, 6.0, 12.0);
		param.addCategory(cat);
		cat = new MPBEvalCategory(4, 0.0, 6.0, 0.0, 6.0);
		param.addCategory(cat);
		params.add(param);
	}
	
	/**
	 * Liefert die zu einem Parameter und einem Wert die entsprechende Kategorie
	 * anhand der in der Matrix hinterlegten Kategoriegrenzen zurück
	 * @param parameter
	 * @param value
	 * @return MPBEvalCategory
	 */
	public MPBEvalCategory getEvalCategory (Integer parameter, Double value) {
		for (int i=0;i<this.params.size();i++) {
			MPBEvalParameter param = this.params.get(i);
			if (param.getNumber() == parameter) {
				for(int j=0;j<param.getCategories().size();j++) {
					if (param.getCategories().get(j).evaluate(value)) {
						return param.getCategories().get(j);
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Liefert die zu einem Wert die entsprechende EQR-Kategorie zurück
	 * @param value
	 * @return MPBEvalCategory
	 */
	public MPBEvalCategory getEvalEQRCategory(Double value) {
		for (int i=0;i<this.params.size();i++) {
			if (this.normEQRcategories.get(i).evaluate(value)) {
				return normEQRcategories.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Liefert zu einem Parameter und einem Wert den Integer der 
	 * entsprechenden Kategorie zurück
	 * @param parameter
	 * @param value
	 * @return Kategorie-Wert
	 */
	public Integer evaluateValueToInt(Integer parameter, Double value) {
		Integer cat;
		try {
			cat =  this.getEvalCategory(parameter, value).category;
		}
		catch (Exception e) {
			cat = -1;
		}
		return cat;
	}
	
	/**
	 * Liefert zu einem Wert den Integer der 
	 * entsprechenden EQR-Kategorie zurück
	 * @param value
	 * @return
	 */
	public Integer evaluateEQRToInt(Double value) {
		Integer cat;
		try {
			cat =  this.getEvalEQRCategory(value).category;
		}
		catch (Exception e) {
			cat = -1;
		}
		return cat;
	}
	
	/**
	 * Berechnet den NormEQR nach Dolch et al. 2010
	 * @param parameter - Bewertungsparameter
	 * @param value - Zu bewertender Wert, für den der NormEQR berechnet werden soll
	 * @return NormEQR Wert
	 */
	public Double getEQR(Integer parameter, Double value) {
		MPBEvalCategory cat = this.getEvalCategory(parameter, value);
		try {
			MPBEvalCategory normCat = normEQRcategories.get(evaluateValueToInt(parameter, value));
			return ((value-cat.catLowerLimit)*(normCat.catUpperLimit-normCat.catLowerLimit))/(cat.catUpperLimit-cat.catLowerLimit)+normCat.catLowerLimit;
		}
		catch (Exception e) {
			return -1.0;
		}
	}
	
	/**
	 * Gibt den zum Eingabe-Integer entsprechenden String der Qualitätskategorie zurück
	 * @param parameter - Integer im Wertebereich von 0-4, beliebig für Bewertung von EQR
	 * @param valueType - 	EVALUATE_VALUE für die Bewertung eines Flächenverhältnisses
	 * 						EVALUATE_EQR für die Bewertung eines EQR-Wertes
	 * @param value - Zu bewertender Wert
	 * @return
	 */
	public String evaluateToString(Integer parameter, int valueType, Double value) {
		String evalString = "";
		Integer evalInteger;
		if (valueType == 1) {
			evalInteger = this.evaluateValueToInt(parameter, value);
		}
		else if (valueType == 2) {
			evalInteger = this.evaluateEQRToInt(value);
		}
		else {
			throw new RuntimeException("evaluateToString: Ungültiger Wert für valueType!");
		}
		switch(evalInteger) {
			case 0: evalString = "schlecht"; break;
			case 1: evalString = "unbefr."; break;
			case 2: evalString = "mäßig"; break;
			case 3: evalString = "gut"; break;
			case 4: evalString = "sehr gut"; break;
			default: evalString = "n/a";
		}
		return evalString;
	}
}

