package net.disy.wps.lkn.mpa.types;

import javax.xml.bind.annotation.XmlElement;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.joda.time.DateTime;

/**
 * Wrapper-Klasse zum Vorhalten von Verschneidungsergebnissen als
 * SimpleFeatureCollection.
 * 
 * @author woessner
 * 
 */
public class IntersectionFeatureCollection extends ObservationFeatureCollection {

	private final Integer area;

	public IntersectionFeatureCollection(Integer gebiet, DateTime obsTime,
			SimpleFeatureCollection fc, Double area) {
		super(obsTime, fc, area);
		this.area = gebiet;
	}

        @XmlElement(name = "IntersectionArea")
	public Integer getGebiet() {
		return this.area;
	}

}
