package net.disy.wps.lkn;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.disy.wps.common.DescriptorContainer;
import net.disy.wps.common.Util;
import net.disy.wps.n52.binding.MPBResultBinding;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

@Algorithm(
		title="Makrophytenbewertung",
		abstrakt="Prozess zur Bewertung der Berichtsgebiete Nordfriesland und Dithmarschen anhand von MSRL-D5 Daten",
		version = "0.9.5"
		)
public class MPBMain extends AbstractAnnotatedAlgorithm {

	// Logger für Debugging erzeugen
	protected static Logger LOGGER = Logger.getLogger(MPBMain.class);

	// DateTimeFormatter: Repräsentiert das Datums-/Zeit-Format in den
	// MSRL-Daten
	private DateTimeFormatter DateTimeFormatter = DateTimeFormat
			.forPattern("yyyy-MM-dd'T'HH:mm:ss");
	private DateTimeFormatter DateTimeFormatter4TimeStamp = DateTimeFormat
			.forPattern("yyyy-MM-dd HH:mm:ss.S");

	public MPBMain() {
		super();
	}

	public static final int NORDFRIESLAND = 1;
	public static final int DITHMARSCHEN = 2;
	
	// MPBResult List
	private MPBResult result = new MPBResult();

	private Integer InputBewertungsjahr;
	private SimpleFeatureCollection inputEvalAreaCollection;
	private SimpleFeatureCollection inputTopoCollection;
	private SimpleFeatureCollection inputMSRLCollection;
	private SimpleFeatureCollection outputCollection = FeatureCollections
			.newCollection();
	
	private String outputRawValues;
	private String outputEvalValues;
	private File outputXMLFile;

	@ComplexDataInput(identifier = "berichtsgebiete", title = "Berichtsgebiete", abstrakt = "Berichtsgebiete die die Werte 'DI' und 'NF' im Attribut 'DISTR' enthalten.", binding = GTVectorDataBinding.class)
	public void setEvaluationArea(FeatureCollection<?, ?> evalAreaCollection) {
		this.inputEvalAreaCollection = (SimpleFeatureCollection) evalAreaCollection;
	}

	@ComplexDataInput(identifier = "topographie", title = "Topographie", abstrakt = "Topographie Layer", minOccurs=1, maxOccurs=1, binding = GTVectorDataBinding.class)
	public void setTopography(FeatureCollection<?, ?> topoCollection) {
		this.inputTopoCollection = (SimpleFeatureCollection) topoCollection;
	}

	@ComplexDataInput(identifier = "msrl-d5", title = "MSRL D5 Daten", abstrakt = "MSRL D5 Daten, die Algen- und Seegras- Polygone enthalten", binding = GTVectorDataBinding.class)
	public void setMSRLCollection(FeatureCollection<?, ?> inputCollection) {
		this.inputMSRLCollection = (SimpleFeatureCollection) inputCollection;
	}

	@LiteralDataInput(identifier = "bewertungsjahr", title = "Bewertungsjahr", abstrakt = "Bewertungsjahr, von dem die durchzuführende Bewertung ausgeht", binding = LiteralStringBinding.class)
	public void setBewertungsjahr(String bewertungsjahr) {
		this.InputBewertungsjahr = Integer.parseInt(bewertungsjahr);
	}
	
	
	@LiteralDataOutput(identifier = "rawValues", title = "Rohdaten", abstrakt = "CSV-Tabelle mit Rohdaten der Verschneidungsergebnisse (Flächen in Quadratkilometer)", binding = LiteralStringBinding.class)
	public String getRawValues() {
		return this.outputRawValues;
	}

	@LiteralDataOutput(identifier = "evalValues", title = "Bewertungsergebnisse", abstrakt = "CSV-Tabelle mit bewerteten Flächenverhältnissen und EQR-Werten", binding = LiteralStringBinding.class)
	public String getEvalValues() {
		return this.outputEvalValues;
	}
	
	
	// Achtung: Output Type muss hier FeatureType sein (nicht
	// SimpleFeatureType!)
	@ComplexDataOutput(identifier = "mpbResultGml", title = "Bewertete Berichtsgebiete", abstrakt = "FeatureCollection der bewerteten Berichtsgebiete", binding = GTVectorDataBinding.class)
	public FeatureCollection getResultGml() {
		return this.outputCollection;
	}
	
	@ComplexDataOutput(identifier = "mpbResultXml", title ="XML-Rohdaten Datei", abstrakt = "XML-Datei mit Rohdaten der Bewertung", binding = MPBResultBinding.class)
	public File getResultXml() {
		return this.outputXMLFile;
	}	
	
	@Execute
	public void runMPB() {
		/*
		 * LOGGER Level
		 */
		LOGGER.setLevel(Level.ALL);

		/*
		 * Array der Parameternamen: COV_OP = Algen COV_ZS = Seegras
		 */
		String[] obsParams = { "COV_OP", "COV_ZS" };

		/*
		 * Definition der notwendigen Variablen
		 */

		// paramArrayList: List with two entries, one for each parameter in the
		// obsParam-Array
		ArrayList<ArrayList<ObservationCollection>> paramArrayList = new ArrayList<ArrayList<ObservationCollection>>();

		// obsCollArrayList: List of ObservationCollections
		ArrayList<ObservationCollection> obsCollArrayList = new ArrayList<ObservationCollection>();

		// relCollArrayList: List of relevant ObservationCollections
		ArrayList<ObservationCollection> relCollArrayList = new ArrayList<ObservationCollection>();

		// topoArrayList: Liste mit ObservationCollection,
		// die jeweils die FeatureCollection der Topographie-Geometrien für ein
		// Jahr enthalten
		ArrayList<ObservationCollection> topoArrayList = new ArrayList<ObservationCollection>();

		ArrayList<IntersectionCollection> intersecWattBerichtsgebiete = new ArrayList<IntersectionCollection>();

		// relevantYears: Liste der in die Bewertung einfließenden Jahre
		ArrayList<Integer> relevantYears = new ArrayList<Integer>();

		// existingTopoYears: Liste von Jahren, zu denen ein Topographie
		// Datensatz vorhanden ist
		ArrayList<Integer> existingTopoYears = new ArrayList<Integer>();

		// relevantTopoYears: Liste von Jahren, die für die Bewertung
		// relevant sind
		ArrayList<Integer> relevantTopoYears = new ArrayList<Integer>();

		// SimpleFeatureCollections
		SimpleFeatureCollection wattCollection;
		SimpleFeatureCollection bGebietNFCollection, bGebietDICollection;
		SimpleFeatureCollection intersecWattBGebiet = FeatureCollections
				.newCollection();
		SimpleFeatureCollection intersecBGebietWattMSRL = FeatureCollections
				.newCollection();

		// Area - Flächenangaben
		Double totalWattAreaNF, totalWattAreaDI;
		Integer ZS_numTypesNF, ZS_numTypesDI;
		Double ZS_totalareaNF, ZS_40areaNF, ZS_60areaNF, ZS_totalareaDI, ZS_40areaDI, ZS_60areaDI;
		Double OP_totalareaNF, OP_40areaNF, OP_60areaNF, OP_totalareaDI, OP_40areaDI, OP_60areaDI;

		// Ergebnis-Objekt mit Bewertungsjahr initialisieren
		result.setBewertungsjahr(this.InputBewertungsjahr);

		/*
		 * ::: SourceCode-Abschnitt ::: Auswahl Seegras und Algen
		 */

		LOGGER.info("PHASE::Auswahl Seegras und Algen::Start");

		LOGGER.debug("SFC-Info:Input-Berichtsgebiete: "
				+ Util.getNumFeaturesFromFC(inputEvalAreaCollection)
				+ " Features, "
				+ Util.getNumVerticesFromFC(inputEvalAreaCollection)
				+ " Stützpunkte, "
				+ Util.getMeanVerticesFromFC(inputEvalAreaCollection)
				+ " Stützpunkte im Mittel");
		LOGGER.debug("SFC-Info:Input-Topographie: "
				+ Util.getNumFeaturesFromFC(inputTopoCollection)
				+ " Features, "
				+ Util.getNumVerticesFromFC(inputTopoCollection)
				+ " Stützpunkte, "
				+ Util.getMeanVerticesFromFC(inputTopoCollection)
				+ " Stützpunkte im Mittel");
		LOGGER.debug("SFC-Info:Input-MSRL-D5: "
				+ Util.getNumFeaturesFromFC(inputMSRLCollection)
				+ " Features, "
				+ Util.getNumVerticesFromFC(inputMSRLCollection)
				+ " Stützpunkte, "
				+ Util.getMeanVerticesFromFC(inputMSRLCollection)
				+ " Stützpunkte im Mittel");

		for (int i = 0; i < obsParams.length; i++) {
			SimpleFeatureCollection sfc = FeatureCollections.newCollection();

			// SimpleFeatureCollection des aktuellen Parameters extrahieren
			sfc = Util.getFeatureCollectionExtract(inputMSRLCollection,
					new String[] { "OBSV_PARAMETERNAME" },
					new String[] { obsParams[i] });

			// Liste von Beobachtungszeitpunkten erzeugen, für die mindestens
			// eine Beobachtung vorhanden ist
			LOGGER.debug("getObservationDates: Für den Parameter "
					+ obsParams[i]
					+ " sind folgende Beobachtungszeitpunkte vorhanden:");
			ArrayList<DateTime> obsDates = getObservationDates(sfc);

			// Liste von ObservationCollections anhand von
			// Beobachtungszeitpunkten
			obsCollArrayList = getObsCollByDateList(sfc, obsDates);

			// Relevante ObservationCollections ausgehend von Bewertungsjahr
			// ermitteln
			LOGGER.debug("getRelevantFeatureCollections: Folgende Datensätze für den Parameter "
					+ obsParams[i]
					+ " und das Bewertungsjahr "
					+ InputBewertungsjahr + " ausgewählt");
			relCollArrayList = getRelevantObservationCollections(
					obsCollArrayList, InputBewertungsjahr);

			// Relevante ObservationCollections der paramArrayListe hinzufügen
			paramArrayList.add(relCollArrayList);

		}
		// Ende der Schleife über die Parameter (Seegras, Algen)
		LOGGER.info("PHASE::Auswahl Seegras und Algen::Ende");

		/*
		 * ::: SourceCode-Abschnitt ::: Integritätstest Seegras & Algen
		 */
		LOGGER.info("PHASE::Integritätstest der Auswahl::Start");
		// Test: Gibt es zwei Einträge in der paramArrayList? (Seegras und Algen
		if (paramArrayList.size() != 2) {
			throw new RuntimeException(
					"Es sind nicht genügend Parameter zur Bewertung vorhanden!");
		}
		// Test: Entsprechen sich die Jahre von Seegras- und Algen-Datensätzen
		// in der paramArrayList Sammlung?
		for (int i = 0; i < relCollArrayList.size(); i++) {
			if (paramArrayList.get(0).get(i).getDateTime().getYear() != paramArrayList
					.get(1).get(i).getDateTime().getYear()) {
				throw new RuntimeException(
						"Die relevanten Jahre der"
								+ "beiden Parameter 'Algen' und 'Seegras' entsprechen sich nicht!");
			} else {
				relevantYears.add(paramArrayList.get(0).get(i).getDateTime()
						.getYear());
			}
		}
		LOGGER.info("Integritätstest erfolgreich!");
		LOGGER.info("PHASE::Integritätstest der Auswahl::Ende");

		/*
		 * ::: SourceCode-Abschnitt ::: Auswahl Berichtsgebiete & Wattflächen
		 */
		LOGGER.info("PHASE::Auswahl Berichtsgebiete und Wattflächen::Start");
		// Inseln aus Berichtsgebieten entfernen
		this.inputEvalAreaCollection = clearEvalAreas(this.inputEvalAreaCollection);
		
		// Nordfriesland (NF) und Dithmarschen (DI) aus Berichtsgebieten
		// extrahieren
		bGebietNFCollection = Util.getFeatureCollectionExtract(
				this.inputEvalAreaCollection, new String[] { "DISTR", "TEMPLATE" },
				new String[] { "NF", "" });
		LOGGER.debug("Filter-Info:Berichtsgebiet Nordfriesland - "
				+ bGebietNFCollection.size() + " Feautures");

		bGebietDICollection = Util.getFeatureCollectionExtract(
				this.inputEvalAreaCollection, new String[] { "DISTR", "TEMPLATE" },
				new String[] { "DI", "" });
		LOGGER.debug("Filter-Info:Berichtsgebiet Nordfriesland - "
				+ bGebietDICollection.size() + " Feautures");

		// Wattflächen aus Topographie extrahieren
		wattCollection = Util.getFeatureCollectionExtract(
				inputTopoCollection, new String[] { "POSKEY", "POSKEY" },
				new String[] { "Watt", "watt" });
		LOGGER.debug("Filter-Info:Wattflächen - "
				+ wattCollection.size() + " Feautures");

		// Vorhandene Topographie-Jahre ermitteln
		existingTopoYears = getTopoYears(wattCollection);

		// Relevante Topographie-Jahre ermitteln
		relevantTopoYears = getRelevantTopoYears(existingTopoYears,
				relevantYears);

		// Liste von ObservationCollections mit Topographien für jedes Jahr
		// erstellen und der topoArrayList hinzufügen
		String relevantTopoYearsString = "";
		for (int i = 0; i < relevantTopoYears.size(); i++) {
			SimpleFeatureCollection sfc = FeatureCollections.newCollection();
			sfc = Util.getFeatureCollectionExtract(wattCollection,
					new String[] { "YEAR" }, new String[] { relevantTopoYears
							.get(i).toString() });
			DateTime dt = new DateTime(relevantTopoYears.get(i), 1, 1, 0, 0);
			Double area = Util.getAreaFromFC(sfc);
			topoArrayList.add(new ObservationCollection(dt, sfc, area));
			relevantTopoYearsString += " " + relevantTopoYears.get(i);
		}

		LOGGER.debug("Relevante Topographie-Datensätze: "
				+ relevantTopoYearsString);

		LOGGER.info("PHASE::Auswahl Berichtsgebiete und Wattflächen::Ende");

		/*
		 * Verschneidung
		 */
		LOGGER.info("PHASE::Verschneidung und Kenngrößenbestimmung::Start");

		// Schleife über existingTopoYears zur Verschneidung jedes
		// Topographie-Datensatzes mit den Berichtsgebieten
		for (int i = 0; i < relevantTopoYears.size(); i++) {
			DateTime dt;
			Double area;
			IntersectionCollection intersecColl;

			// Verschneidung mit NF
			intersecWattBGebiet = intersectBerichtsgebieteAndTopography(
					bGebietNFCollection,
					getObsCollByYear(topoArrayList, relevantTopoYears.get(i))
							.getSfc());
			LOGGER.debug("Intersection-Result valid: " + Util.checkValid(intersecWattBGebiet));
			
			dt = new DateTime(relevantTopoYears.get(i), 1, 1, 0, 0);
			area = Util.getAreaFromFC(intersecWattBGebiet);

			intersecColl = new IntersectionCollection(MPBMain.NORDFRIESLAND, dt,
					intersecWattBGebiet, area);
			intersecWattBerichtsgebiete.add(intersecColl);
			LOGGER.debug("Verschneidung NF und Wattflächen "
					+ relevantTopoYears.get(i) + " abgeschlossen");

			// Verschneidung mit DI
			intersecWattBGebiet = intersectBerichtsgebieteAndTopography(
					bGebietDICollection,
					getObsCollByYear(topoArrayList, relevantTopoYears.get(i))
							.getSfc());
			LOGGER.debug("Intersection-Result valid: " + Util.checkValid(intersecWattBGebiet));

			dt = new DateTime(relevantTopoYears.get(i), 1, 1, 0, 0);
			area = Util.getAreaFromFC(intersecWattBGebiet);

			intersecColl = new IntersectionCollection(MPBMain.DITHMARSCHEN, dt,
					intersecWattBGebiet, area);
			intersecWattBerichtsgebiete.add(intersecColl);
			LOGGER.debug("Verschneidung DI und Wattflächen "
					+ relevantTopoYears.get(i) + " abgeschlossen");
		}

		// Schleife über relevantYears zur jahresweisen Verschneidung
		for (int i = 0; i < relevantYears.size(); i++) {
			IntersectionCollection intsecColl;
			ObservationCollection msrlColl;
			Integer topoYear = getTopoYear(relevantYears.get(i),
					existingTopoYears);

			LOGGER.debug("Verschneidung: MSRL D5 "
					+ relevantYears.get(i).toString() + " & Topographie "
					+ topoYear.toString());

			// Nordfriesland
			intsecColl = getIntersecCollByYearAndGebiet(
					intersecWattBerichtsgebiete, topoYear, MPBMain.NORDFRIESLAND);
			totalWattAreaNF = intsecColl.getArea();
			// ZS --> Seegras
			msrlColl = getObsCollByYear(paramArrayList.get(1),
					relevantYears.get(i));
			
			intersecBGebietWattMSRL = intersectIntersectionAndMSRL(
					intsecColl.getSfc(), msrlColl.getSfc());
			
			LOGGER.debug("Intersection-Result valid: " + Util.checkValid(intersecBGebietWattMSRL));
			
			ZS_totalareaNF = Util.getAreaFromFC(intersecBGebietWattMSRL);
			ZS_40areaNF = Util.getAreaFromFC(Util.getFeatureCollectionExtract(
					intersecBGebietWattMSRL,
					new String[] { "OBSV_PARAMETERVALUE" },
					new String[] { "4" }));
			ZS_60areaNF = Util.getAreaFromFC(Util.getFeatureCollectionExtract(
					intersecBGebietWattMSRL,
					new String[] { "OBSV_PARAMETERVALUE" },
					new String[] { "6" }));
			if (ZS_40areaNF > 0 && ZS_60areaNF > 0) {
				ZS_numTypesNF = 2;
			} else {
				ZS_numTypesNF = 1;
			}
			LOGGER.debug("Verschneidung: WattXBerichtsgebiete & MSRL-Seegras "
					+ relevantYears.get(i) + " in Nordfriesland");
			// OP --> Algen
			msrlColl = getObsCollByYear(paramArrayList.get(0),
					relevantYears.get(i));
			intersecBGebietWattMSRL = intersectIntersectionAndMSRL(
					intsecColl.getSfc(), msrlColl.getSfc());
			
			OP_totalareaNF = Util.getAreaFromFC(intersecBGebietWattMSRL);
			OP_40areaNF = Util.getAreaFromFC(Util.getFeatureCollectionExtract(
					intersecBGebietWattMSRL,
					new String[] { "OBSV_PARAMETERVALUE" },
					new String[] { "4" }));
			OP_60areaNF = Util.getAreaFromFC(Util.getFeatureCollectionExtract(
					intersecBGebietWattMSRL,
					new String[] { "OBSV_PARAMETERVALUE" },
					new String[] { "6" }));
			LOGGER.debug("Verschneidung: WattXBerichtsgebiete & MSRL-Algen "
					+ relevantYears.get(i) + " in Nordfriesland");

			// Dithmarschen
			intsecColl = getIntersecCollByYearAndGebiet(
					intersecWattBerichtsgebiete, topoYear, MPBMain.DITHMARSCHEN);
			totalWattAreaDI = intsecColl.getArea();
			// ZS --> Seegras
			msrlColl = getObsCollByYear(paramArrayList.get(1),
					relevantYears.get(i));
			intersecBGebietWattMSRL = intersectIntersectionAndMSRL(
					intsecColl.getSfc(), msrlColl.getSfc());
			ZS_totalareaDI = Util.getAreaFromFC(intersecBGebietWattMSRL);
			ZS_40areaDI = Util.getAreaFromFC(Util.getFeatureCollectionExtract(
					intersecBGebietWattMSRL,
					new String[] { "OBSV_PARAMETERVALUE" },
					new String[] { "4" }));
			ZS_60areaDI = Util.getAreaFromFC(Util.getFeatureCollectionExtract(
					intersecBGebietWattMSRL,
					new String[] { "OBSV_PARAMETERVALUE" },
					new String[] { "6" }));
			if (ZS_40areaDI > 0 && ZS_60areaDI > 0) {
				ZS_numTypesDI = 2;
			} else {
				ZS_numTypesDI = 1;
			}
			LOGGER.debug("Verschneidung: WattXBerichtsgebiete & MSRL-Seegras "
					+ relevantYears.get(i) + " in Dithmarschen");
			// OP --> Algen
			msrlColl = getObsCollByYear(paramArrayList.get(0),
					relevantYears.get(i));
			intersecBGebietWattMSRL = intersectIntersectionAndMSRL(
					intsecColl.getSfc(), msrlColl.getSfc());
			OP_totalareaDI = Util.getAreaFromFC(intersecBGebietWattMSRL);
			OP_40areaDI = Util.getAreaFromFC(Util.getFeatureCollectionExtract(
					intersecBGebietWattMSRL,
					new String[] { "OBSV_PARAMETERVALUE" },
					new String[] { "4" }));
			OP_60areaDI = Util.getAreaFromFC(Util.getFeatureCollectionExtract(
					intersecBGebietWattMSRL,
					new String[] { "OBSV_PARAMETERVALUE" },
					new String[] { "6" }));
			LOGGER.debug("Verschneidung: WattXBerichtsgebiete & MSRL-Algen "
					+ relevantYears.get(i) + " in Dithmarschen");

			// ResultRecord erzeugen
			MPBResultRecord resultRec = new MPBResultRecord(
					relevantYears.get(i), totalWattAreaNF, totalWattAreaDI,
					ZS_numTypesNF, ZS_numTypesDI, ZS_totalareaNF, ZS_40areaNF,
					ZS_60areaNF, ZS_totalareaDI, ZS_40areaDI, ZS_60areaDI,
					OP_totalareaNF, OP_40areaNF, OP_60areaNF, OP_totalareaDI,
					OP_40areaDI, OP_60areaDI);
			result.addRecord(resultRec);
			LOGGER.debug("ResultRecord erzeugt");
		}

		LOGGER.debug("PHASE::Verschneidung und Kenngrößenbestimmung::Ende");

		// Process Outputs
		// FeatureCollection mit den bewerteten Featuren Nordfriesland und Dithmarschen
		this.outputCollection = getEvaluatedAreas(bGebietNFCollection,
				bGebietDICollection);
		
		File f = null;
		try {
			JAXBContext context = JAXBContext.newInstance(MPBResult.class);
			Marshaller m = context.createMarshaller();
			f = File.createTempFile("MPB", "Result");
			
			m.marshal(result, f);

		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (JAXBException e) {
			e.printStackTrace();
		}
		
		this.outputXMLFile = f;
		
		// Debug
		this.outputRawValues = result.getRawRecordsString(false);
		this.outputEvalValues = result.getEvalRecordsString(false);
		
	}

	/**
	 * Liefert eine Liste mit eindeutigen Beobachtungszeitpunkten (DateTime),
	 * die im 'OBSV_PHENOMENONTIME'-Attribut einer SimpleFeatureCollection
	 * enthalten sind
	 * 
	 * @param sfc
	 *            - Eingabe-SimpleFeatureCollection
	 * @return Liste mit Beobachtungszeitpunkten
	 */
	private ArrayList<DateTime> getObservationDates(SimpleFeatureCollection sfc) {
		FeatureIterator<SimpleFeature> iter = sfc.features();
		ArrayList<DateTime> obsDates = new ArrayList<DateTime>();
		HashSet<DateTime> hs = new HashSet<DateTime>();
		DateTime dt;

		try {
			// Iteration über alle Features
			while (iter.hasNext()) {
				SimpleFeature feature = (SimpleFeature) iter.next();
				// Datums-/Zeit-Information extrahieren,...
				if (feature.getAttribute("OBSV_PHENOMENONTIME") instanceof Timestamp) {
					Timestamp timestamp = (Timestamp) feature.getAttribute("OBSV_PHENOMENONTIME");
					 dt = new DateTime(timestamp);
				}
				else {
					String dateTimeStr = (String) feature
							.getAttribute("OBSV_PHENOMENONTIME");
					// ... damit ein DateTime-Objekt instanziieren...
					dt = DateTimeFormatter.parseDateTime(dateTimeStr);
				}
					// ...und einer Liste hinzufügen
					obsDates.add(dt);
			}
		} finally {
			// Iterator schließen
			iter.close();
		}

		// Ergebnisliste in eine Liste mit eindeutigen Werten konvertieren
		hs.addAll(obsDates);
		obsDates.clear();
		obsDates.addAll(hs);
		// Aufsteigend sortieren
		Collections.sort(obsDates);

		return obsDates;
	}

	private ArrayList<Integer> getTopoYears(SimpleFeatureCollection topoSfc) {
		FeatureIterator<SimpleFeature> iter = topoSfc.features();
		ArrayList<Integer> existingTopoYears = new ArrayList<Integer>();
		HashSet<Integer> hs = new HashSet<Integer>();
		String existingYear;

		try {
			// Iteration über alle Features
			while (iter.hasNext()) {
				SimpleFeature feature = (SimpleFeature) iter.next();
				existingYear = (String) feature.getAttribute("YEAR");
				existingTopoYears.add(Integer.parseInt(existingYear));
			}
		} finally {
			// Iterator schließen
			iter.close();
		}

		// Ergebnisliste in eine Liste mit eindeutigen Werten konvertieren
		hs.addAll(existingTopoYears);
		existingTopoYears.clear();
		existingTopoYears.addAll(hs);
		// Aufsteigend sortieren
		Collections.sort(existingTopoYears);
		return existingTopoYears;
	}

	private ArrayList<Integer> getRelevantTopoYears(
			ArrayList<Integer> topoYears, ArrayList<Integer> msrlYears) {
		ArrayList<Integer> relTopoYears = new ArrayList<Integer>();
		HashSet<Integer> hs = new HashSet<Integer>();

		// Schleife über MSRL-Years, für die jeweils das entsprechende TopoYear
		// bestimmt werden soll
		for (int i = 0; i < msrlYears.size(); i++) {
			relTopoYears.add(getTopoYear(msrlYears.get(i), topoYears));
		}
		// Liste auf eindeutige Werte beschränken
		hs.addAll(relTopoYears);
		relTopoYears.clear();
		relTopoYears.addAll(hs);

		return relTopoYears;
	}

	/**
	 * Gibt eine Liste von ObservationCollections zurück, die ein Element für
	 * jeden Eintrag in der übergebenen Liste mit Beobachtungszeitpunkten
	 * enthält Returns an Array of ObservationsCollections. The list contains
	 * entries for each DateTime including the corresponding SimpleFeatures in a
	 * collection and the total area
	 * 
	 * @param sfc
	 * @param obsDates
	 * @return Liste mit ObservationCollections
	 */
	private ArrayList<ObservationCollection> getObsCollByDateList(
			SimpleFeatureCollection sfc, ArrayList<DateTime> obsDates) {
		String compareStr;
		Double area;
		SimpleFeatureCollection groupCollection;
		ArrayList<ObservationCollection> obsCollections = new ArrayList<ObservationCollection>();

		// Schleife über die Beobachtungszeitpunkte
		for (int i = 0; i < obsDates.size(); i++) {
			groupCollection = FeatureCollections.newCollection();
			// String zum Vergleich von Beobachtungszeitpunkten erzeugen
			if (Util.attributeIsTimeStamp(sfc, "OBSV_PHENOMENONTIME")) {
				compareStr = obsDates.get(i).toString(DateTimeFormatter4TimeStamp);
			}
			else {
				compareStr = obsDates.get(i).toString(DateTimeFormatter);
			}
			// Entsprechende SimpleFeatureCollection aus der gesamten
			// FeatureCollection extrahieren
			groupCollection = Util.getFeatureCollectionExtract(sfc,
					new String[] { "OBSV_PHENOMENONTIME" },
					new String[] { compareStr });
			// Gesamtfläche der Features berechnen
			area = Util.getAreaFromFC(groupCollection);
			// ObservationCollection erzeugen und der Ausgabe-Liste hinzufügen
			obsCollections.add(new ObservationCollection(obsDates.get(i),
					groupCollection, area));
		}
		
		// Debug
		for (ObservationCollection obsColl : obsCollections) {
			LOGGER.debug("getObsCollByDateList: " + obsColl.getDateTime().getYear() + "-"
					+ obsColl.getDateTime().getMonthOfYear() + "-" + obsColl.getDateTime().getDayOfMonth()
					+ ": " + Util.tokm2Str(obsColl.getArea()) + " km2");
		}
		
		return obsCollections;
	}

	/**
	 * Versucht aus einer Liste von ObservationCollections (MSRL-Daten) die
	 * sechs für das Bewertungsjahr relevanten Beobachtungssammlungen zu
	 * ermitteln und zurück zu geben. Falls vom Bewertungsjahr ausgehend keine
	 * sechs ObservationCollections verfügbar sind, wir eine RuntimeException
	 * ausgelöst.
	 * 
	 * @param obsCollections
	 *            - Liste von ObservationCollections für jeden
	 *            Beobachtungszeitpunkt
	 * @param bewertungsjahr
	 * @return Liste mit den sechs relevanten ObservationCollections
	 */
	private ArrayList<ObservationCollection> getRelevantObservationCollections(
			ArrayList<ObservationCollection> obsCollections,
			Integer bewertungsjahr) {
		ArrayList<ObservationCollection> preSelCollections = new ArrayList<ObservationCollection>();
		ArrayList<ObservationCollection> finalCollections = new ArrayList<ObservationCollection>();
		HashSet<Integer> existingYears = new HashSet<Integer>();

		// Schleife über alle ObservationCollections
		for (int i = 0; i < obsCollections.size(); i++) {
			ObservationCollection obsColl = obsCollections.get(i);

			// Prüfen, ob die Liste der ausgewählten ObservationCollections
			// Einträge enthält
			if (preSelCollections.size() > 0) {
				// Über bestehende Einträge in selCollections iterieren,
				// um Jahr und Fläche der neuen obsColl zu vergleichen 
				Integer initSize = preSelCollections.size();
				for (int j = 0; j < initSize; j++) {
					ObservationCollection selColl = preSelCollections.get(j);
					// Wenn unter den schon selektierten Sammlungen eine mit
					// gleichem Jahr und kleinerer Fläche ist...
					if (selColl.getDateTime().getYear() == obsColl
							.getDateTime().getYear()) {
						if (selColl.getArea() < obsColl.getArea()) {
							// ...dann wird diese gelöscht...
							preSelCollections.remove(selColl);
							// ...und die neue hinzugefügt
							preSelCollections.add(obsColl);
						}
					}
					// Wenn Jahre unterschiedlich, muss geprüft werden, ob es
					// schon einen Eintrag mit dem Jahr gibt. Falls nicht, kann
					// die Sammlung hinzugefügt werden.
					else if (!existingYears.contains(obsColl.getDateTime()
							.getYear())) {
						preSelCollections.add(obsColl);
						existingYears.add(obsColl.getDateTime().getYear());
					}
				}
			}
			// Falls die Liste noch leer ist, wird die erste
			// Beobachtungssammlung hinzugefügt
			else {
				preSelCollections.add(obsColl);
				// Das Jahr des hinzugefügten Eintrags in die Liste der bereits
				// übernommenen Jahre hinzufügen
				existingYears.add(obsColl.getDateTime().getYear());
			}
		}
		// StartIndex ermitteln
		Integer startIndex = getIdxOfObsCollbyYear(preSelCollections,
				bewertungsjahr);
		try {
			for (int i = 0; i < 6; i++) {
				finalCollections.add(preSelCollections.get(startIndex + i));
			}
		} catch (Exception e) {
			// RuntimeException auslösen, falls keine sechs
			// ObservationCollections vorhanden
			throw new RuntimeException(
					"There is not enough data for 6 years available.");
		}
		// Debug
		for (int k = 0; k < preSelCollections.size(); k++) {
			LOGGER.debug("getRelevantFeatureCollections: "
					+ preSelCollections.get(k).getDateTime().getYear() + "-"
					+ preSelCollections.get(k).getDateTime().getMonthOfYear()
					+ "-"
					+ preSelCollections.get(k).getDateTime().getDayOfMonth()
					+ " ausgewählt: " + Util.tokm2Str(preSelCollections.get(k).getArea()));
		}
		return finalCollections;
	}

	/**
	 * Ermittelt zu aus einer Liste von ObservationCollectiond und einer
	 * Jahres-Angabe den Index des entsprechenden Elements falls das Jahr
	 * kleiner ist als die vorhanden Jahre in der Liste wird eine
	 * RuntimeException ausgelöst Falls das Jahr größer ist als die vorhandene
	 * Jahre in der Liste wird der Index des zeitlich nächstliegenden Jahres
	 * zurückgegeben
	 * 
	 * @param obsCollList
	 *            - Liste mit allen ObservationCollections
	 * @param year
	 *            - Jahr
	 * @return Index
	 */
	private Integer getIdxOfObsCollbyYear(
			ArrayList<ObservationCollection> obsCollList, Integer year) {

		Integer i = 0;
		// sort descending by DateTime of ObservationCollection
		Collections.sort(obsCollList, Collections.reverseOrder());
		Integer listYear = obsCollList.get(i).getDateTime().getYear();
		// Schleife, bis aktuelles Jahr aus der Liste nicht mehr kleier als das
		// Bewertungsjahr ist
		do {
			// RuntimException, wenn die Anzahl der Schleifendurchläufe die
			// Größe der Liste überschreitet
			if (i > obsCollList.size() - 1) {
				throw new RuntimeException(
						"Invalid year or non fitting data! Try to use "
								+ listYear + " as year.");
			}
			listYear = obsCollList.get(i).getDateTime().getYear();
			if (listYear.equals(year)) {
				return i;
			} else if (listYear < year) {
				return i;
			}
			i++;
		} while (listYear >= year);
		return null;
	}

	/**
	 * Liefert ein einem Topographie-Datensatz zugehöriges Jahr aus einer Liste
	 * von möglichen Jahren, welches die minimale zeitliche Distanz zu einem
	 * Eingabejahr aufweist. Im Fall von gleichen Zeitunterschieden zu zwei
	 * Jahren, wird das spätere zurück gegeben.
	 * 
	 * @param year
	 *            - Eingabejahr, zu dem ein passendes Topographie-Jahr ermittelt
	 *            werden soll
	 * @param topoYearList
	 *            - Liste mit vorhandenen Topographie-Jahren
	 * @return Jahr
	 */
	private int getTopoYear(int year, ArrayList<Integer> topoYearList) {
		ArrayList<Integer> dList = new ArrayList<Integer>();
		Integer minDiff;
		Integer minYearIndex = -1;

		// Wichtig: absteigend sortieren!
		Collections.sort(topoYearList, Collections.reverseOrder());

		for (int i = 0; i < topoYearList.size(); i++) {
			dList.add(Math.abs(year - topoYearList.get(i)));
		}
		minDiff = dList.get(0);
		for (int j = 1; j < dList.size() - 1; j++) {
			if (minDiff > dList.get(j)) {
				minDiff = dList.get(j);
			}
		}
		minYearIndex = dList.indexOf(minDiff);

		return topoYearList.get(minYearIndex);
	}

	/**
	 * Liefert eine durch ein Jahr bestimme ObservationCollection aus einer
	 * Liste von ObservationCollections
	 * 
	 * @param obsCollList
	 *            - Liste von ObservationCollections
	 * @param year
	 *            - Jahr, zu dem die ObservationCollection zurückgegeben werden
	 *            soll
	 * @return ObservationCollection
	 */
	private ObservationCollection getObsCollByYear(
			ArrayList<ObservationCollection> obsCollList, Integer year) {
		ObservationCollection obsColl = null;

		for (int j = 0; j < obsCollList.size(); j++) {
			if (obsCollList.get(j).getDateTime().getYear() == year) {
				obsColl = obsCollList.get(j);
			}
		}
		return obsColl;
	}

	private IntersectionCollection getIntersecCollByYearAndGebiet(
			ArrayList<IntersectionCollection> intersecCollList, Integer year,
			Integer gebiet) {
		IntersectionCollection intersecColl = null;

		for (int j = 0; j < intersecCollList.size(); j++) {
			if (intersecCollList.get(j).getDateTime().getYear() == year
					&& intersecCollList.get(j).getGebiet() == gebiet) {
				intersecColl = intersecCollList.get(j);
			}
		}
		return intersecColl;

	}

	/**
	 * 
	 * @param ea Berichtsgebiete FeatureCollection
	 * @return bereinigte Berichtsgebiete FeatureCollection
	 */
	private SimpleFeatureCollection clearEvalAreas(SimpleFeatureCollection ea) {
		SimpleFeatureCollection clearedCollection = FeatureCollections.newCollection();
    	
    	FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
    	// Welches Attribut ist hier ausschlaggebend? Provisorisch TEMPLATE verwendet
    	Filter filter = ff.notEqual(ff.property("TEMPLATE"), null);
    	FeatureIterator<SimpleFeature> iter = ea.features();
    	while(iter.hasNext()) {
    		SimpleFeature feature = iter.next();
    		if (filter.evaluate(feature)) {
    			clearedCollection.add(feature);
    		}
    	}
    	return clearedCollection;
	}

	
	/**
	 * Verschneidet eine SimpleFeatureCollection von Berichtsgebieten mit einer
	 * SimpleFeatureCollection von Topographien eines Jahres. Die zurückgegebene
	 * SimpleFeatureCollection enthält alle Attribute der Topographien und die
	 * Angaben 'DIST' und 'Name' der Berichtsgebiete
	 * 
	 * @param berichtsFc
	 *            - SimpleFeatureCollection von Berichtsgebieten
	 * @param topoFc
	 *            - SimpleFeatureCollection von Topographien eines Jahres
	 * @return SimpleFeatureCollection des Verschneidungs-Ergebnisses
	 */
	private SimpleFeatureCollection intersectBerichtsgebieteAndTopography(
			SimpleFeatureCollection berichtsFc, SimpleFeatureCollection wattFc) {

		SimpleFeatureCollection intersecFeatureCollection = FeatureCollections
				.newCollection();
		// Relevante Features aus der Berichtsgebiete-FeatureCollection
		HashSet<SimpleFeature> relBFeatHs = new HashSet<SimpleFeature>();
		ArrayList<SimpleFeature> relBFeat = new ArrayList<SimpleFeature>();
		// Relevante Features aus der Wattgebiete-FeatureCollection
		HashSet<SimpleFeature> relWFeatHs = new HashSet<SimpleFeature>();
		ArrayList<SimpleFeature> relWFeat = new ArrayList<SimpleFeature>();

		FeatureIterator<SimpleFeature> iterA;
		FeatureIterator<SimpleFeature> iterB;

		SimpleFeature berichtsFeature, wattFeature;
		Geometry berichtsBB, wattBB, berichtsGeom, wattGeom, intersec;

		/*
		 * Verschneidung der BoundingBoxen zum Filtern der für die folgende
		 * Verschneidung relevanten Features
		 */

		// Iterator über Features der Berichtsgebiete
		iterA = berichtsFc.features();
		try {
			while (iterA.hasNext()) {
				berichtsFeature = iterA.next();
				berichtsBB = ((Geometry) berichtsFeature.getDefaultGeometry())
						.getEnvelope();

				iterB = wattFc.features();
				try {
					while (iterB.hasNext()) {
						wattFeature = iterB.next();
						wattBB = ((Geometry) wattFeature.getDefaultGeometry())
								.getEnvelope();

						intersec = berichtsBB.intersection(wattBB);
						if (!intersec.isEmpty()) {
							relBFeatHs.add(berichtsFeature);
							relWFeatHs.add(wattFeature);
						}
					}
				} finally {
					iterB.close();
				}
			}
		} finally {
			iterA.close();
		}

		// Liste mit zusätzlichen Deskriptoren für die zu erzeugenden Features
		// erstellen
		ArrayList<DescriptorContainer> dcList = new ArrayList<DescriptorContainer>();
		dcList.add(new DescriptorContainer(1, 1, false, "DISTR", String.class));
		dcList.add(new DescriptorContainer(1, 1, false, "NAME", String.class));
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(
				Util.refactorFeatureType(
						(SimpleFeatureType) wattFc.getSchema(), dcList,
						"BerichtsgebieteTopographieIntersection", null));

		// ArrayLists aus HashSets efüllen
		relBFeat.addAll(relBFeatHs);
		relWFeat.addAll(relWFeatHs);
		// Schleife über Berichtsgebiete
		for (int i = 0; i < relBFeat.size(); i++) {
			berichtsFeature = relBFeat.get(i);
			berichtsGeom = (Geometry) berichtsFeature.getDefaultGeometry();

			// Schleife über Wattflächen
			for (int j = 0; j < relWFeat.size(); j++) {
				wattFeature = relWFeat.get(j);
				wattGeom = (Geometry) wattFeature.getDefaultGeometry();

				intersec = berichtsGeom.intersection(wattGeom);
				// Wenn die Intersection-Geometry nicht leer ist...
				if (!intersec.isEmpty()) {
					featureBuilder = Util.initBuilderValues(featureBuilder,
							wattFeature);
					featureBuilder.set(wattFc.getSchema()
							.getGeometryDescriptor().getLocalName(), intersec);
					featureBuilder.set("DISTR",
							berichtsFeature.getAttribute("DISTR"));
					featureBuilder.set("NAME",
							berichtsFeature.getAttribute("NAME"));

					SimpleFeature intersectFeature = featureBuilder
							.buildFeature(null);
					intersecFeatureCollection.add(intersectFeature);
				}
			}
		}
		return intersecFeatureCollection;
	}

	/**
	 * Verschneidet das Verschneidungs-Ergebnis von Berichtsgebieten &
	 * Wattflächen mit den MSRL-Daten (Seegras-, Algen-Geometrien) eines Jahres.
	 * Die zurückgegebene SimpleFeatureCollection enthält alle MSRL-Attribute
	 * und die Angaben 'DISTR' und 'NAME'
	 * 
	 * @param intersecColl
	 *            - SimpleFeatureCollection des Verschneidungs-Ergebnisses
	 *            Berichtsgebiete und Wattflächen
	 * @param msrlColl
	 *            - SimpleFeatureCollection der MSRL-Daten
	 * @return SimpleFeatureCollection des Verschneidungs-Ergebnisses
	 */
	private SimpleFeatureCollection intersectIntersectionAndMSRL(
			SimpleFeatureCollection intersecColl,
			SimpleFeatureCollection msrlColl) {
		SimpleFeatureCollection intersecFeatureCollection = FeatureCollections
				.newCollection();
		// Relevante Features aus SimpleFeatureCollection des
		// Verschneidungs-Ergebnisses
		// von Berichtsgebieten und Wattflächen
		HashSet<SimpleFeature> relBWFeatHs = new HashSet<SimpleFeature>();
		ArrayList<SimpleFeature> relBWFeat = new ArrayList<SimpleFeature>();
		// Relevante Features aus MSRL-Collection
		HashSet<SimpleFeature> relMFeatHs = new HashSet<SimpleFeature>();
		ArrayList<SimpleFeature> relMFeat = new ArrayList<SimpleFeature>();

		FeatureIterator<SimpleFeature> iterB;
		FeatureIterator<SimpleFeature> iterA;

		SimpleFeature bWattFeature, msrlFeature;
		Geometry bWattBB, msrlBB, bWattGeom, msrlGeom, intersec;

		iterA = intersecColl.features();
		// Iteration über BoundingBoxen
		try {
			while (iterA.hasNext()) {
				bWattFeature = iterA.next();
				bWattBB = ((Geometry) bWattFeature
						.getDefaultGeometry()).getEnvelope();

				iterB = msrlColl.features();
				try {
					while (iterB.hasNext()) {
						msrlFeature = iterB.next();
						msrlBB = ((Geometry) msrlFeature.getDefaultGeometry())
								.getEnvelope();

						intersec = bWattBB.intersection(msrlBB);
						if (!intersec.isEmpty()) {
							relBWFeatHs.add(bWattFeature);
							relMFeatHs.add(msrlFeature);
						}
					}
				} finally {
					iterB.close();
				}
			}
		} finally {
			iterA.close();
		}

		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(
		// FeatureType der Ziel-Collection aus der MSRLCollection erstellen und Geometrietyp auf Multipolygon setzen
		Util.refactorFeatureType(
				(SimpleFeatureType) msrlColl.getSchema(), null,
				"FinalIntersectionFeatures", MultiPolygon.class));

		// ArrayLists aus HashSets efüllen
		relBWFeat.addAll(relBWFeatHs);
		relMFeat.addAll(relMFeatHs);

		// Schleife über Intersection-Ergebnis (Berichtsgebiete&Wattflächen)
		for (int i = 0; i < relBWFeat.size(); i++) {
			bWattFeature = relBWFeat.get(i);
			bWattGeom = (Geometry) bWattFeature
					.getDefaultGeometry();

			// Schleife über MSRL-Features
			for (int j = 0; j < relMFeat.size(); j++) {
				msrlFeature = relMFeat.get(j);
				msrlGeom = (Geometry) msrlFeature.getDefaultGeometry();

				intersec = bWattGeom.intersection(msrlGeom);
				if (!intersec.isEmpty()) {
					featureBuilder.set(msrlColl.getSchema()
							.getGeometryDescriptor().getLocalName(), intersec);
					featureBuilder.set("OBSV_PARAMETERVALUE",
							msrlFeature.getAttribute("OBSV_PARAMETERVALUE"));
					SimpleFeature intersectFeature = featureBuilder
							.buildFeature(null);
					intersecFeatureCollection.add(intersectFeature);
				}
			}
		}
		return intersecFeatureCollection;
	}

	/**
	 * Erzeugt aus den Eingabe-FeatureCollections für die Ausgangsgeometrien der
	 * beiden Berichtsgebiete NF und DI zwei verschmolzene Polygone mit
	 * Bewertungsparametern
	 * 
	 * @param nfCollection
	 *            - SimpleFeatureCollection Berichtsgebiet Nordfriesland
	 * @param diCollection
	 *            - SimpleFeatureCollection Berichtsgebiet Dithmarschen
	 * @return SimpleFeatureCollection der bewerteten Berichtsgebiete
	 */
	private SimpleFeatureCollection getEvaluatedAreas(
			SimpleFeatureCollection nfCollection,
			SimpleFeatureCollection diCollection) {
		Geometry geom;
		SimpleFeatureCollection resultCollection = FeatureCollections
				.newCollection();
		ArrayList<DescriptorContainer> dcList = new ArrayList<DescriptorContainer>();
		dcList.add(new DescriptorContainer(1, 1, false, "MPBMeanEQR",
				String.class));

		// FeatureType bauen
		SimpleFeatureTypeBuilder ftBuilder = new SimpleFeatureTypeBuilder();
		ftBuilder.setName("MPBBerichtsgebiet");
		ftBuilder.setNamespaceURI("http://www.disy.net/MPBBerichtsgebiet");
		ftBuilder.add("MPBGeom", Polygon.class);
		ftBuilder.setDefaultGeometry("MPBGeom");
		ftBuilder.add("DISTR", String.class);
		ftBuilder.add("MPBMeanEQR", String.class);
		ftBuilder.add("MPBEvalStringEQR", String.class);
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(
				ftBuilder.buildFeatureType());

		// Geometrien aus Nordfriesland sammeln
		FeatureIterator<SimpleFeature> nfIter = nfCollection.features();
		ArrayList<Geometry> geomCollection = new ArrayList<Geometry>();
		while (nfIter.hasNext()) {
			SimpleFeature feature = nfIter.next();
			geom = (Geometry) feature.getDefaultGeometry();
			geomCollection.add(geom);
		}
		geom = Util.getUnion(geomCollection);
		featureBuilder.set("MPBGeom", geom);
		featureBuilder.set("DISTR", "NF");
		featureBuilder.set("MPBMeanEQR",
				result.getAreaResult(MPBMain.NORDFRIESLAND).getMeanEQR().toString());
		featureBuilder.set("MPBEvalStringEQR",
				result.getAreaResult(MPBMain.NORDFRIESLAND).getMeanEQREvalString());
		resultCollection.add(featureBuilder.buildFeature(null));
		geomCollection.clear();

		// Geometrien aus Dithmarschen sammeln
		FeatureIterator<SimpleFeature> diIter = diCollection.features();
		while (diIter.hasNext()) {
			SimpleFeature feature = diIter.next();
			geom = (Geometry) feature.getDefaultGeometry();
			geomCollection.add(geom);
		}
		geom = Util.getUnion(geomCollection);
		featureBuilder.set("MPBGeom", geom);
		featureBuilder.set("DISTR", "DI");
		featureBuilder.set("MPBMeanEQR",
				result.getAreaResult(MPBMain.DITHMARSCHEN).getMeanEQR().toString());
		featureBuilder.set("MPBEvalStringEQR",
				result.getAreaResult(MPBMain.DITHMARSCHEN).getMeanEQREvalString());
		resultCollection.add(featureBuilder.buildFeature(null));
		geomCollection.clear();

		return resultCollection;
	}
}