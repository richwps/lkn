package net.disy.wps.common;

public class DescriptorContainer {

	public int minOccurs;
	public int maxOccurs;
	public boolean isNillable;
	public String name;
	public Class<?> binding;
	
	
	public DescriptorContainer(int minOccurs, int maxOccurs, boolean isNillable, String name, Class<?> binding) {
		this.minOccurs = minOccurs;
		this.maxOccurs = maxOccurs;
		this.isNillable = isNillable;
		this.name = name;
		this.binding = binding;
	}
	
}
