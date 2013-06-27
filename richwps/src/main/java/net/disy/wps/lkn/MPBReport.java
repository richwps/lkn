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

import net.disy.wps.n52.binding.JasperReportBinding;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapViewport;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.Style;
import org.geotools.styling.StyledLayerDescriptor;
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
import org.xml.sax.SAXException;

@Algorithm(title = "Makrophytenbewertung Bericht", abstrakt = "Prozess zur Berichtserstellung auf Basis der Makrophytenbewertung-Ergebnisse", version = "0.0.1")
public class MPBReport extends AbstractAnnotatedAlgorithm {
	
	// Logger für Debugging erzeugen
	protected static Logger LOGGER = Logger.getLogger(MPBMain.class);
	
	// DateTimeFormatter: Repräsentiert das Datums-/Zeit-Format in den
	// MSRL-Daten
	DateTimeFormatter DateTimeFormatter = DateTimeFormat
			.forPattern("yyyy-MM-dd'T'HH:mm:ss");
	
	public MPBReport() {
		super();
	}
	
	// MPBResult
	MPBResult result;
	
	private GenericFileData inputXMLFile;
	private SimpleFeatureCollection inputSfc;
	private File outputPdf;
	
	@ComplexDataInput(identifier = "mpbResultXml", title = "XML-Rohdaten aus MPBMain", abstrakt = "XML-Datei mit Rohdaten der Bewertung", minOccurs = 1, maxOccurs = 1, binding = GenericFileDataBinding.class)
	public void setInputXml(GenericFileData gfd) {
		this.inputXMLFile = gfd;
	}
	
	@ComplexDataInput(identifier = "mpbResultGml", title = "GML-Rohdaten aus MPBMain", abstrakt = "XML-Datei mit Rohdaten der Bewertung", binding = GTVectorDataBinding.class)
	public void setInputGml(FeatureCollection<?, ?> fc) {
		this.inputSfc = (SimpleFeatureCollection) fc;
	}
	
	@ComplexDataOutput(identifier = "mpbResultPdf", title = "PDf-Bericht der Bewertungsergebnisse", binding = JasperReportBinding.class)
	public File getMPBResultPdf() {
		return outputPdf;
	}
	
	@Execute
	public void runMPBReport() {
		
		JAXBContext context;
		MPBResult result;
		
		try {
			context = JAXBContext.newInstance(MPBResult.class);
			Unmarshaller um = context.createUnmarshaller();
			
			result = (MPBResult) um
					.unmarshal(this.inputXMLFile.getDataStream());
			
			// Bewertungsparameter berechnen
			result.calculateParameters();
			
			// Jasper-Report erstellen und als Output übergeben
			this.outputPdf = createJasperReport(result);
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
	}
	
	public File createJasperReport(MPBResult res) {
		
		File f;
		InputStream jrXmlIn = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("MPB_Report.jrxml");
		String[] resources = { "blau.jpg", "gruen.jpg", "gelb.jpg",
				"orange.jpg", "rot.jpg", "eqr_formel.jpg",
				"matrix_di.jpg", "matrix_nf.jpg", "mdi_logo.jpg" };
		
		try {
			
			f = File.createTempFile("MPBResultJasperPdf", ".pdf");
			OutputStream output = new FileOutputStream(f);
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			
			// MPBResult Objekt als Parameter
			parameters.put("MPBResult", res);
			
			// Resources Parameter
			parameters = addResourceToParameters(resources, parameters);
			
			// Karte als Parameter übergeben
			parameters.put("MPBResultMap", getMapInputStream());
			
			// Schleife über AreaResults zur Erzeugung aller notwendingen
			// Report-Parameter
			for (int i = 1; i <= res.getAreaResults().size(); i++) {
				MPBAreaResult ares = res.getAreaResult(i);
				ArrayList<MPBResultRecord> records = ares.getRecords();
				// Area Präfix
				String areaPref = "";
				if (ares.getGebiet() == MPBMain.NORDFRIESLAND) {
					areaPref = "NF_";
				} else if (ares.getGebiet() == MPBMain.DITHMARSCHEN) {
					areaPref = "DI_";
				}
				
				// Schleife über Records in AreaResult
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
			}
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
	 * Erstellt für jede Image-Ressource ein InputStream-Objekt, welches als
	 * Parameter der Parameterliste angehängt wird
	 * 
	 * @param res
	 *            String-Array mit Dateinamen der Resourcen
	 * @param params
	 *            Parameter-Objekt, welches für das Füllen des Reports verwendet
	 *            wird
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
	 * Startet das Rendern der Karte und erzeugt einen InputStream auf die Grafik
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	private InputStream getMapInputStream() {
		File f = renderMap(400);
		try {
			return new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Rendert eine Karte mit den bewerteten Berichtsgebieten und erzeugt daras eine png-Grafik
	 * @param imageWidth Breite des Bildes
	 * @return png-Grafik
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws IOException 
	 */
	private File renderMap(Integer imageWidth) {
		
		InputStream sldIn = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("wrrl_bewertung.sld");
		
		InputStream topoBgIn = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("lkn_topo-ubgkal07.tif");
		
		Rectangle imageBounds = null;
		ReferencedEnvelope mapBounds = null;
		
		StyledLayerDescriptor sld;
		Style style;

		// Layer initialisieren
		FeatureLayer layBGebiete;
		// GridCoverageLayer layTopoBackground;
		
		// Karte
		MapContext map = new MapContext();
		map.setTitle("Makrophytenbewertung Berichtsgebiete");

		org.geotools.xml.Configuration configuration = new org.geotools.sld.SLDConfiguration();
		org.geotools.xml.Parser parser = new org.geotools.xml.Parser(configuration);

		try {
			sld = (StyledLayerDescriptor) parser.parse(sldIn);
			
		style = ((NamedLayer) sld.getStyledLayers()[0]).getStyles()[0];
		 
		layBGebiete = new FeatureLayer(inputSfc, style);
		map.addLayer(layBGebiete);
		 
		ReferencedEnvelope env = inputSfc.getBounds();
		MapViewport vp = new MapViewport(env);
		map.setViewport(vp);

		GTRenderer renderer = new StreamingRenderer();
		renderer.setContext(map);
		 
		try {
			mapBounds = map.getMaxBounds();
			double heightToWidth = mapBounds.getSpan(1) / mapBounds.getSpan(0);
			imageBounds = new Rectangle(0, 0, imageWidth, (int) Math.round(imageWidth * heightToWidth));
		} catch(Exception e) {
			throw new RuntimeException();
		}
		 
		BufferedImage image = new BufferedImage((int) imageBounds.getWidth(),(int) imageBounds.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D gr = image.createGraphics();
		gr.setPaint(Color.white);
		gr.fill(imageBounds);
		 
		try {
			renderer.paint(gr, imageBounds, mapBounds);
			File fileToSave = File.createTempFile("GTRenderer", ".png");
			ImageIO.write(image, "png", fileToSave);
			return fileToSave;
			 
		} catch (Exception e) {
			throw new RuntimeException();
		}
		 
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		return null;
	}
}