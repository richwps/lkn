package net.disy.wps.lkn;

import java.util.ArrayList;

/**
 * Diese Klasse repräsentiert einen Bewertungsparameter als Teil einer
 * Bewertungsmatrix
 * 
 * @author woessner
 * 
 */
public class MPBEvalParameter {

	private Integer number;
	private String name;
	private ArrayList<MPBEvalCategory> categories;

	public MPBEvalParameter(Integer number, String name) {
		this.number = number;
		this.name = name;
		this.categories = new ArrayList<MPBEvalCategory>();
	}

	public Integer getNumber() {
		return number;
	}

	public String getName() {
		return name;
	}

	/**
	 * Fuer dem Bewertungsparameter eine Kategorie hinzu
	 * 
	 * @param cat
	 */
	public void addCategory(MPBEvalCategory cat) {
		this.categories.add(cat);
	}

	/**
	 * Gibt alle Kategorien des Bewertungsparameters zurueck
	 * 
	 * @return
	 */
	public ArrayList<MPBEvalCategory> getCategories() {
		return categories;
	}
}
