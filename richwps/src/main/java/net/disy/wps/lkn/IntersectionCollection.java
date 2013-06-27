package net.disy.wps.lkn;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.joda.time.DateTime;

public class IntersectionCollection extends ObservationCollection {

	private Integer gebiet;

	public IntersectionCollection(Integer gebiet, DateTime obsTime, SimpleFeatureCollection fc,
			Double area) {
		super(obsTime, fc, area);
		this.gebiet = gebiet;
	}
	
	public Integer getGebiet () {
		return this.gebiet;
	}

}
