package net.disy.wps.lkn;

import java.util.ArrayList;

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
	
	public void addCategory(MPBEvalCategory cat) {
		this.categories.add(cat);
	}
	
	public ArrayList<MPBEvalCategory> getCategories() {
		return categories;
	}
}
