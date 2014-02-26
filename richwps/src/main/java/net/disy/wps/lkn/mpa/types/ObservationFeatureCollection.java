package net.disy.wps.lkn.processes.mpa.types;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.joda.time.DateTime;


/**
 * 
 * @author woessner
 */
public class ObservationCollection implements Comparable<ObservationCollection> {

	private DateTime obsTime;
	private Double area;
	private SimpleFeatureCollection sfc;
	
	public ObservationCollection(DateTime obsTime, SimpleFeatureCollection sfc, Double area) {
		this.obsTime = obsTime;
		this.area = area;
		this.sfc = sfc;
	}
	
	public DateTime getDateTime() {
		return this.obsTime;
	}
	
	public Double getArea() {
		return this.area;
	}
	
	public SimpleFeatureCollection getSfc() {
		return this.sfc;
	}
	
	public String toString() {
		String year = ((Integer) this.obsTime.getYear()).toString();
		String month = ((Integer) this.obsTime.getMonthOfYear()).toString();
		String day = ((Integer) this.obsTime.getDayOfMonth()).toString();
		String size = ((Integer) this.sfc.size()).toString();
		String area = this.area.toString();
		return year + '-' + month + '-' + day + ' ' + size + ' ' + area;
	}

	@Override
	public int compareTo(ObservationCollection coll) {
		return this.getDateTime().compareTo(coll.getDateTime());
	}

	
}
