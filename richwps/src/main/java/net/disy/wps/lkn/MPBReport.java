package net.disy.wps.lkn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;

import net.disy.ogc.wps.v_1_0_0.util.WpsUtils;
import net.disy.ogc.wpspd.v_1_0_0.LinkType;
import net.disy.ogc.wpspd.v_1_0_0.ObjectFactory;
import net.disy.ogc.wpspd.v_1_0_0.WpspdUtils;
import net.disy.wps.common.Util;
import net.disy.wps.n52.binding.PDLinkBinding;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.GridCoverageLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapViewport;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.Style;
import org.geotools.styling.StyledLayerDescriptor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.database.IDatabase;
import org.xml.sax.SAXException;

/**
 * MPBReport-Prozess - erzeugt aus einer GML-FeatureCollection der bewerteten
 * Berichtsgebiete und einer MPBResult-XML einen schriftlichen Bericht. Dieser
 * Bericht enthaelt Tabellen der Rohdaten und der bewerteten Parameter, eine
 * thematisch gerenderte Karte der Berichtsgebiete, Diagramme die den Trend der
 * EQR-Werte aufzeigen und einen Bewertungstext, der sich dynamisch aus
 * Textbausteinen zusammensetzt.
 * 
 * @author woessner
 * 
 */
@Algorithm(title = "Makrophytenbewertung Bericht", abstrakt = "Prozess zur Berichtserstellung auf Basis der Makrophytenbewertung-Ergebnisse", version = "0.0.1")
public class MPBReport extends AbstractAnnotatedAlgorithm {

	// Logger fuer Debugging erzeugen
	protected static Logger LOGGER = Logger.getLogger(MPBMain.class);

	// DateTimeFormatter: Repraesentiert das Datums-/Zeit-Format in den
	// MSRL-Daten
	DateTimeFormatter DateTimeFormatter = DateTimeFormat
			.forPattern("yyyy-MM-dd'T'HH:mm:ss");

	private final ObjectFactory objectFactory = WpspdUtils
			.createObjectFactory();

	public MPBReport() {
		super();
	}

	// MPBResult
	MPBResult result;

	private GenericFileData inputXMLFile;
	private SimpleFeatureCollection inputSfc;
	private LinkType outputPdfLink;

	@ComplexDataInput(identifier = "mpbResultXml", title = "XML-Rohdaten aus MPBMain", abstrakt = "XML-Datei mit Rohdaten der Bewertung", minOccurs = 1, maxOccurs = 1, binding = GenericFileDataBinding.class)
	public void setInputXml(GenericFileData gfd) {
		this.inputXMLFile = gfd;
	}

	@ComplexDataInput(identifier = "mpbResultGml", title = "GML-Rohdaten aus MPBMain", abstrakt = "XML-Datei mit Rohdaten der Bewertung", binding = GTVectorDataBinding.class)
	public void setInputGml(FeatureCollection<?, ?> fc) {
		this.inputSfc = (SimpleFeatureCollection) fc;
	}

	@ComplexDataOutput(identifier = "mpbResultPdf", title = "Pdf-Bericht der Bewertungsergebnisse", binding = PDLinkBinding.class)
	public LinkType getMPBResultPdf() {
		return outputPdfLink;
	}

	@Execute
	public void runMPBReport() {
		JAXBContext context;
		MPBResult result;
		File outputPdf;
		InputStream outputPdfIs;
		LinkType link;

		IDatabase db = DatabaseFactory.getDatabase();
		String hrefPdf;

		try {
			context = JAXBContext.newInstance(MPBResult.class);
			Unmarshaller um = context.createUnmarshaller();

			result = (MPBResult) um
					.unmarshal(this.inputXMLFile.getDataStream());

			// Bewertungsparameter berechnen
			result.calculateParameters();

			// Jasper-Report erstellen
			outputPdf = createJasperReport(result);
			outputPdfIs = new FileInputStream(outputPdf);

			// PDF in FileDatabase speichern und URL in pd:Link integrieren
			hrefPdf = db.storeComplexValue("MPBResultPdf", outputPdfIs,
					"ComplexDataResponse", "");

			link = objectFactory.createLinkType();
			link.setPresentationDirectiveTitle(WpsUtils
					.createLanguageStringType("MPB-Link"));
			link.setAbstract(WpsUtils
					.createLanguageStringType("Link zum Ergebnisbericht der Makrophytenbewertung"));
			link.setHref(hrefPdf);
			link.setType("simple");
			link.setShow("new");

			this.outputPdfLink = link;

		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Erzeugt die Referenz auf eine Pdf-Datei, in die zuvor ein Bericht aus
	 * einem MPBResult mit der JasperReports-API erstellt wurde
	 * 
	 * @param res
	 *            MPBResult-Objekt
	 * @return Referenz auf File-Objekt
	 */
	public File createJasperReport(MPBResult res) {

		File f;
		InputStream jrXmlIn = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("MPB_Report.jrxml");
		String[] resources = { "blau.jpg", "gruen.jpg", "gelb.jpg",
				"orange.jpg", "rot.jpg", "eqr_formel.jpg", "matrix_di.jpg",
				"matrix_nf.jpg", "mdi_logo.jpg" };

		try {

			f = File.createTempFile("MPBResultJasperPdf", ".pdf");
			OutputStream output = new FileOutputStream(f);

			Map<String, Object> parameters = new HashMap<String, Object>();

			// MPBResult Objekt als Parameter
			parameters.put("MPBResult", res);

			// Resources Parameter
			parameters = addResourceToParameters(resources, parameters);

			// Karte als Parameter uebergeben
			parameters.put("MPBResultMap", getMapInputStream());

			// Schleife ueber AreaResults zur Erzeugung aller notwendingen
			// Report-Parameter
			for (int i = 1; i <= res.getAreaResults().size(); i++) {
				MPBAreaResult ares = res.getAreaResult(i);
				ArrayList<MPBResultRecord> records = ares.getRecords();
				// Area Praefix
				String areaPref = "";
				if (ares.getGebiet() == MPBMain.NORDFRIESLAND) {
					areaPref = "NF_";
				} else if (ares.getGebiet() == MPBMain.DITHMARSCHEN) {
					areaPref = "DI_";
				}

				// Schleife ueber Records in AreaResult
				for (int j = 0; j < records.size(); j++) {
					MPBResultRecord rec = records.get(j);
					parameters.put("year" + "_" + j, rec.getYearStr());
					parameters.put("totalWattAreaNF" + "_" + j,
							rec.getTotalWattAreaNF());
					parameters.put("totalWattAreaDI" + "_" + j,
							rec.getTotalWattAreaDI());
					parameters.put("totalWattArea" + "_" + j,
							rec.getTotalWattArea());
					parameters.put("ZStotalareaNF" + "_" + j,
							rec.getZStotalAreaNF());
					parameters.put("ZS40AreaNF" + "_" + j, rec.getZS40AreaNF());
					parameters.put("ZS60AreaNF" + "_" + j, rec.getZS60AreaNF());
					parameters.put("ZStotalareaDI" + "_" + j,
							rec.getZStotalAreaDI());
					parameters.put("ZS40AreaDI" + "_" + j, rec.getZS40AreaDI());
					parameters.put("ZS60AreaDI" + "_" + j, rec.getZS60AreaDI());
					parameters.put("ZStotalarea" + "_" + j,
							rec.getZStotalArea());
					parameters.put("OPtotalareaNF" + "_" + j,
							rec.getOPtotalAreaNF());
					parameters.put("OP40AreaNF" + "_" + j, rec.getOP40AreaNF());
					parameters.put("OP60AreaNF" + "_" + j, rec.getOP60AreaNF());
					parameters.put("OPtotalareaDI" + "_" + j,
							rec.getOPtotalAreaDI());
					parameters.put("OP40AreaDI" + "_" + j, rec.getOP40AreaDI());
					parameters.put("OP60AreaDI" + "_" + j, rec.getOP60AreaDI());
					parameters.put("OPtotalarea" + "_" + j,
							rec.getOPtotalArea());
					parameters.put(areaPref + "p1Value" + "_" + j,
							rec.getP1ValueStr());
					parameters.put(areaPref + "p2Value" + "_" + j,
							rec.getP2ValueStr());
					parameters.put(areaPref + "p3Value" + "_" + j,
							rec.getP3ValueStr());
					parameters.put(areaPref + "p4Value" + "_" + j,
							rec.getP4ValueStr());
					parameters.put(areaPref + "p5Value" + "_" + j,
							rec.getP5ValueStr());
					parameters.put(areaPref + "p1Class" + "_" + j,
							rec.getP1Class());
					parameters.put(areaPref + "p2Class" + "_" + j,
							rec.getP2Class());
					parameters.put(areaPref + "p3Class" + "_" + j,
							rec.getP3Class());
					parameters.put(areaPref + "p4Class" + "_" + j,
							rec.getP4Class());
					parameters.put(areaPref + "p5Class" + "_" + j,
							rec.getP5Class());
					parameters
							.put(areaPref + "p1EQR" + "_" + j, rec.getP1EQR());
					parameters
							.put(areaPref + "p2EQR" + "_" + j, rec.getP2EQR());
					parameters
							.put(areaPref + "p3EQR" + "_" + j, rec.getP3EQR());
					parameters
							.put(areaPref + "p4EQR" + "_" + j, rec.getP4EQR());
					parameters
							.put(areaPref + "p5EQR" + "_" + j, rec.getP5EQR());
					parameters.put(areaPref + "weightEQR" + "_" + j,
							rec.getWeightEQR());
					parameters.put(areaPref + "weightClass" + "_" + j,
							rec.getWeightClass());
				}
				parameters.put(areaPref + "meanEQR", ares.getMeanEQRString());

				parameters.put(areaPref + "TrendZS",
						res.getSeegrasTrend(ares.getGebiet()));
				parameters.put(areaPref + "TrendEQR",
						res.getEQRTrend(ares.getGebiet()));
				parameters.put(areaPref + "BarChart", getChartInputStream(res, ares.getGebiet()));
			}
			LOGGER.debug("Parameter befuellt");

			// JRXML kompilieren
			JasperReport jasperReport = JasperCompileManager
					.compileReport(jrXmlIn);
			// Report mit Parametern erzeugen
			JasperPrint jasperPrint = JasperFillManager.fillReport(
					jasperReport, parameters, new JREmptyDataSource());
			// Report in pdf-Datei exportieren
			JasperExportManager.exportReportToPdfStream(jasperPrint, output);
			output.close();
			return f;
		} catch (JRException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Erstellt fuer jede Image-Ressource ein InputStream-Objekt, welches als
	 * Parameter der Parameterliste angehaengt wird
	 * 
	 * @param res
	 *            String-Array mit Dateinamen der Resourcen
	 * @param params
	 *            Parameter-Objekt, welches fuer das Fuellen des Reports
	 *            verwendet wird
	 * @return
	 */
	private Map<String, Object> addResourceToParameters(String[] res,
			Map<String, Object> params) {
		String paramname;
		InputStream is;

		for (int i = 0; i < res.length; i++) {
			paramname = FilenameUtils.removeExtension(res[i]);
			is = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(res[i]);
			params.put("img_" + paramname, is);
		}
		return params;
	}

	/**
	 * Startet das Rendern der Karte und erzeugt einen InputStream auf die
	 * Grafik
	 * 
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	private InputStream getMapInputStream() {
		File f = renderMap(600);
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Rendert eine Karte mit den bewerteten Berichtsgebieten und erzeugt daraus
	 * eine png-Grafik
	 * 
	 * @param imageWidth
	 *            Breite des Bildes
	 * @return png-Grafik
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private File renderMap(Integer imageWidth) {

		// InputStream auf SLD fuer Berichtsgebiete und Geotiff fuer
		// Topographie-Background
		InputStream sldIn = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("wrrl_bewertung.sld");
		InputStream topoBgIn = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("lkn_topo-ubgkal07_gk3.tif");

		ReferencedEnvelope mapBounds = null;
		Rectangle imageBounds = null;

		StyledLayerDescriptor sld;
		Style bGebieteStyle;

		// Geotiff Reader, GridCoverageLayer
		GeoTiffReader gtr = null;
		GridCoverage2D gc = null;
		GridCoverageLayer layTopoBackground;

		File gtTempFile;

		// Karte
		MapContext map = new MapContext();
		map.setTitle("Makrophytenbewertung Berichtsgebiete");

		// Geotiff Layer initialisieren
		try {
			// Geotiff-Stream in temporaere Datei ueberfuehren, da der
			// GeotiffReader nur so funktioniert!
			gtTempFile = Util.stream2file(topoBgIn, "Topo", ".tif");
			gtr = new GeoTiffReader(gtTempFile);
			gc = (GridCoverage2D) gtr.read(null);

		} catch (IOException e2) {
			e2.printStackTrace();
		}
		layTopoBackground = new GridCoverageLayer(gc, Util.createRGBStyle(gtr));
		map.addLayer(layTopoBackground);

		mapBounds = new ReferencedEnvelope(3415000.0, 3530000.0, 5970000.0,
				6110000.0, gc.getCoordinateReferenceSystem());

		// Layer initialisieren
		FeatureLayer layBGebiete = null;

		org.geotools.xml.Configuration configuration = new org.geotools.sld.SLDConfiguration();
		org.geotools.xml.Parser parser = new org.geotools.xml.Parser(
				configuration);

		// SLD parsen und Layer erstellen
		try {
			sld = (StyledLayerDescriptor) parser.parse(sldIn);
			bGebieteStyle = ((NamedLayer) sld.getStyledLayers()[0]).getStyles()[0];
			layBGebiete = new FeatureLayer(inputSfc, bGebieteStyle);
			map.addLayer(layBGebiete);
		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}

		// Rendern des MapContexts
		MapViewport vp = new MapViewport(mapBounds);
		map.setViewport(vp);

		GTRenderer renderer = new StreamingRenderer();
		renderer.setContext(map);

		double heightToWidth = mapBounds.getSpan(1) / mapBounds.getSpan(0);
		imageBounds = new Rectangle(0, 0, imageWidth,
				(int) Math.round(imageWidth * heightToWidth));

		BufferedImage image = new BufferedImage((int) imageBounds.getWidth(),
				(int) imageBounds.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D gr = image.createGraphics();
		gr.setPaint(Color.white);
		gr.fill(imageBounds);

		renderer.paint(gr, imageBounds, mapBounds);
		File fileToSave;
		try {
			fileToSave = File.createTempFile("GTRenderer", ".png");
			ImageIO.write(image, "png", fileToSave);
			return fileToSave;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Erzeugt einen InputStream auf die Grafik des Balkendiagramms
	 * 
	 * @param result
	 *            MPBResult-Objekt
	 * @param evalArea
	 *            Berichtsgebiet
	 * @return
	 */
	private InputStream getChartInputStream(MPBResult result, Integer evalArea) {
		try {
			File f = createBarChart(result, evalArea);
			return new FileInputStream(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Erzeugt ein Balkendiagramm der im Ergebnis enthaltenen EQR-Werte und
	 * schreibt es in eine .png-Datei. Die Funktion gibt das File-Objekt der
	 * Datei zurueck.
	 * 
	 * @param result
	 *            MPBResult-Objekt
	 * @param evalArea
	 *            Berichtsgebiet
	 * @return
	 */
	private File createBarChart(MPBResult result, Integer evalArea) {
		File file;
		CategoryDataset dataset = createDataset(result, evalArea);

		ChartFactory.setChartTheme(StandardChartTheme.createJFreeTheme());

		JFreeChart chart = ChartFactory.createBarChart("", "Jahr", "EQR",
				dataset, PlotOrientation.VERTICAL, false, false, false);

		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.WHITE);
		BarRenderer.setDefaultBarPainter(new StandardBarPainter());
		MPBChartRenderer cr = new MPBChartRenderer();
		plot.setRenderer(cr);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setRange(0, 1);

		try {
			file = File.createTempFile("BarChart", ".png");
			OutputStream os = new FileOutputStream(file);
			ChartUtilities.writeChartAsPNG(os, chart, 250, 180);
			return file;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Erzeugt das fuer das EQR-Balkendiagramm notwendige Dataset entsprechend
	 * dem angegebenen Berichtsgebiet
	 * 
	 * @param result
	 * @param evalArea
	 * @return
	 */
	private CategoryDataset createDataset(MPBResult result, Integer evalArea) {
		MPBAreaResult aRes = result.getAreaResult(evalArea);
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		Integer bewertungsjahr = result.getBewertungsjahr();

		// Serien
		String series = "EQR-Werte";
		
		// Kategorien
		String catY0 = String.valueOf(bewertungsjahr).substring(2, 4);
		String catY1 = String.valueOf(bewertungsjahr - 1).substring(2, 4);
		String catY2 = String.valueOf(bewertungsjahr - 2).substring(2, 4);
		String catY3 = String.valueOf(bewertungsjahr - 3).substring(2, 4);
		String catY4 = String.valueOf(bewertungsjahr - 4).substring(2, 4);
		String catY5 = String.valueOf(bewertungsjahr - 5).substring(2, 4);
		String catMean = "MW";

		// Werte
		dataset.addValue(aRes.getRecordByYear(bewertungsjahr - 5)
				.getWeightedEQR(), series, catY5);
		dataset.addValue(aRes.getRecordByYear(bewertungsjahr - 4)
				.getWeightedEQR(), series, catY4);
		dataset.addValue(aRes.getRecordByYear(bewertungsjahr - 3)
				.getWeightedEQR(), series, catY3);
		dataset.addValue(aRes.getRecordByYear(bewertungsjahr - 2)
				.getWeightedEQR(), series, catY2);
		dataset.addValue(aRes.getRecordByYear(bewertungsjahr - 1)
				.getWeightedEQR(), series, catY1);
		dataset.addValue(aRes.getRecordByYear(bewertungsjahr).getWeightedEQR(),
				series, catY0);
		dataset.addValue(aRes.getMeanEQR(), series, catMean);
		return dataset;
	}

}