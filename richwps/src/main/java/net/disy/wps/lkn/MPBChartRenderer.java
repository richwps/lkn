package net.disy.wps.lkn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;

/**
 * Angepasster BarRenderer, der die Balken nach dem WRRL-Farbschema rendert
 * 
 * @author woessner
 * 
 */
public class MPBChartRenderer extends BarRenderer {

	private static final long serialVersionUID = -5108158814154679009L;

	public MPBChartRenderer() {
		super();
	}
	
	// WRRL-Farben
	static final Color blau = new Color(0, 0, 204);
	static final Color gruen = new Color(0, 205, 0);
	static final Color gelb = new Color(255, 255, 0);
	static final Color orange = new Color(255, 185, 15);
	static final Color rot = new Color(254, 0, 0);

	@Override
	public Paint getItemPaint(int row, int column) {
		CategoryDataset dataset = getPlot().getDataset();
		String rowKey = (String) dataset.getRowKey(row);
		String colKey = (String) dataset.getColumnKey(column);
		Double value = dataset.getValue(rowKey, colKey).doubleValue();

		if (value >= 0.0 && value < 0.2) {
			return rot;
		} else if (value >= 0.2 && value < 0.4) {
			return orange;
		} else if (value >= 0.4 && value < 0.6) {
			return gelb;
		} else if (value >= 0.6 && value < 0.8) {
			return gruen;
		} else if (value >= 0.8 && value <= 1.0) {
			return blau;
		} else {
			return Color.black;
		}
	}
}