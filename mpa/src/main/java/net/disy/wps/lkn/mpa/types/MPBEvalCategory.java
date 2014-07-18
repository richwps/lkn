package net.disy.wps.lkn.mpa.types;

/**
 * Diese Klasse Repraesentiert eine Bewertungskategorie 
 * @author woessner
 *
 */
public class MPBEvalCategory {
	Integer category;
	Double catUpperLimit, catLowerLimit;
	Double evalUpperLimit, evalLowerLimit;
	
	public MPBEvalCategory(Integer category, Double catLowerLimit,
			Double catUpperLimit, Double evalLowerLimit, Double evalUpperLimit) {
		this.category = category;
		this.catLowerLimit = catLowerLimit;
		this.catUpperLimit = catUpperLimit;
		this.evalLowerLimit = evalLowerLimit;
		this.evalUpperLimit = evalUpperLimit;
	}
	
	/**
	 * Bewertet einen Wert an der Bewertungskategorie und gibt true zurueck, wenn
	 * der Wert in die Kategorie faellt
	 * 
	 * @param value
	 * @return Boolean
	 */
	public boolean evaluate(Double value) {
		if (this.evalLowerLimit <= value && value < this.evalUpperLimit) {
			return true;
		} else {
			return false;
		}
	}
	
}
