package net.disy.wps.lkn.utils;

import net.disy.wps.lkn.ObservationCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.joda.time.DateTime;

/**
 * Klasse zum Vorhalten von Verschneidungs-Ergebnissen im MPBMain-Prozess
 * 
 * @author woessner
 * 
 */
public class IntersectionCollection extends ObservationCollection {

	private Integer gebiet;

	public IntersectionCollection(Integer gebiet, DateTime obsTime,
			SimpleFeatureCollection fc, Double area) {
		super(obsTime, fc, area);
		this.gebiet = gebiet;
	}

	public Integer getGebiet() {
		return this.gebiet;
	}

}
