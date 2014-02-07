package net.disy.wps.common;

/**
 * Diese Klasse beiinhaltet eine einfache Beschreibung eines
 * Attribut-Deskriptors, die zur Erweiterung eines Geotools-FeautureTypes
 * in FeatureCollectionUtil.refactorFeatureType verwendet werden kann
 * 
 * @author woessner
 */
public class DescriptorContainer {

	public int minOccurs;
	public int maxOccurs;
	public boolean isNillable;
	public String name;
	public Class<?> binding;

	public DescriptorContainer(int minOccurs, int maxOccurs,
			boolean isNillable, String name, Class<?> binding) {
		this.minOccurs = minOccurs;
		this.maxOccurs = maxOccurs;
		this.isNillable = isNillable;
		this.name = name;
		this.binding = binding;
	}

}
