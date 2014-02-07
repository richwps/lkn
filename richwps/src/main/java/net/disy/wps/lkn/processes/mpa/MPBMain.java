package net.disy.wps.lkn.processes.mpa;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.disy.wps.common.DescriptorContainer;
import net.disy.wps.lkn.utils.FeatureCollectionUtil;
import net.disy.wps.n52.binding.MPBResultBinding;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.joda.time.DateTime;
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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import net.disy.wps.lkn.processes.mpa.types.IntersectionCollection;
import net.disy.wps.lkn.processes.mpa.types.MPBResult;
import net.disy.wps.lkn.processes.mpa.types.MPBResultRecord;
import net.disy.wps.lkn.processes.mpa.types.ObservationCollection;
import net.disy.wps.lkn.utils.MPAUtils;
import net.disy.wps.lkn.utils.MSRLD5Utils;
import net.disy.wps.lkn.utils.ReportingAreaUtils;
import net.disy.wps.lkn.utils.TopographyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Algorithm(version = "0.9.5", title = "Makrophytenbewertung", abstrakt = "Prozess zur Bewertung der Berichtsgebiete Nordfriesland und Dithmarschen anhand von MSRL-D5 Daten")
public class MPBMain extends AbstractAnnotatedAlgorithm {

    // Logger fuer Debugging erzeugen
    protected static Logger LOGGER = LoggerFactory.getLogger(MPBMain.class);
    //Logger.getLogger(MPBMain.class);

    public MPBMain() {
        super();
    }

    public static final int NORDFRIESLAND = 1;
    public static final int DITHMARSCHEN = 2;
    public static final String NORDFRIESLAND_NAME = "Nordfriesland";
    public static final String DITHMARSCHEN_NAME = "Dithmarschen";

    // MPBResult List
    private MPBResult result = new MPBResult();

    private Integer InputBewertungsjahr;
    private SimpleFeatureCollection inputEvalAreaCollection;
    private SimpleFeatureCollection inputTopoCollection;
    private SimpleFeatureCollection inputMSRLCollection;
    private SimpleFeatureCollection outputCollection = FeatureCollections
            .newCollection();

    // Debug
	/*
     * private SimpleFeatureCollection debugA = FeatureCollections
     * .newCollection(); private SimpleFeatureCollection debugB =
     * FeatureCollections .newCollection();
     */
    private String outputRawValues;
    private String outputEvalValues;
    private File outputXMLFile;

    @ComplexDataInput(identifier = "berichtsgebiete", title = "Berichtsgebiete", abstrakt = "Berichtsgebiete die die Werte 'DI' und 'NF' im Attribut 'DISTR' enthalten.", binding = GTVectorDataBinding.class)
    public void setEvaluationArea(FeatureCollection<?, ?> evalAreaCollection) {
        this.inputEvalAreaCollection = (SimpleFeatureCollection) evalAreaCollection;
    }

    @ComplexDataInput(identifier = "topographie", title = "Topographie", abstrakt = "Topographie Layer", minOccurs = 1, maxOccurs = 1, binding = GTVectorDataBinding.class)
    public void setTopography(FeatureCollection<?, ?> topoCollection) {
        this.inputTopoCollection = (SimpleFeatureCollection) topoCollection;
    }

    @ComplexDataInput(identifier = "msrl-d5", title = "MSRL D5 Daten", abstrakt = "MSRL D5 Daten, die Algen- und Seegras- Polygone enthalten", binding = GTVectorDataBinding.class)
    public void setMSRLCollection(FeatureCollection<?, ?> inputCollection) {
        this.inputMSRLCollection = (SimpleFeatureCollection) inputCollection;
    }

    @LiteralDataInput(identifier = "bewertungsjahr", title = "Bewertungsjahr", abstrakt = "Bewertungsjahr, von dem die durchzufuehrende Bewertung ausgeht", binding = LiteralStringBinding.class)
    public void setBewertungsjahr(String bewertungsjahr) {
        this.InputBewertungsjahr = Integer.parseInt(bewertungsjahr);
    }

    @LiteralDataOutput(identifier = "rawValues", title = "Rohdaten", abstrakt = "CSV-Tabelle mit Rohdaten der Verschneidungsergebnisse (Flaechen in Quadratkilometer)", binding = LiteralStringBinding.class)
    public String getRawValues() {
        return this.outputRawValues;
    }

    @LiteralDataOutput(identifier = "evalValues", title = "Bewertungsergebnisse", abstrakt = "CSV-Tabelle mit bewerteten Flaechenverhaeltnissen und EQR-Werten", binding = LiteralStringBinding.class)
    public String getEvalValues() {
        return this.outputEvalValues;
    }

    // Achtung: Output Type muss hier FeatureType sein (nicht
    // SimpleFeatureType!)
    @ComplexDataOutput(identifier = "mpbResultGml", title = "Bewertete Berichtsgebiete", abstrakt = "FeatureCollection der bewerteten Berichtsgebiete", binding = GTVectorDataBinding.class)
    public FeatureCollection getResultGml() {
        return this.outputCollection;
    }

    @ComplexDataOutput(identifier = "mpbResultXml", title = "XML-Rohdaten Datei", abstrakt = "XML-Datei mit Rohdaten der Bewertung", binding = MPBResultBinding.class)
    public File getResultXml() {
        return this.outputXMLFile;
    }

    // Debug
	/*
     * @ComplexDataOutput(identifier = "debugA", title = "debugA", abstrakt =
     * "debugA", binding = GTVectorDataBinding.class) public FeatureCollection
     * getDebugA() { return this.debugA; }
     * 
     * @ComplexDataOutput(identifier = "debugB", title = "debugB", abstrakt =
     * "debugB", binding = GTVectorDataBinding.class) public FeatureCollection
     * getDebugB() { return this.debugB; }
     */
    @Execute
    public void runMPB() {
        /*
         * LOGGER Level
         */
        //LOGGER.setLevel(Level.ALL);

        /*
         * Array der Parameternamen: COV_OP = Algen COV_ZS = Seegras
         */
        String[] obsParams = {"COV_OP", "COV_ZS"};

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
        // die jeweils die FeatureCollection der Topographie-Geometrien fuer ein
        // Jahr enthalten
        ArrayList<ObservationCollection> topoArrayList = new ArrayList<ObservationCollection>();

        ArrayList<IntersectionCollection> intersecWattBerichtsgebiete = new ArrayList<IntersectionCollection>();

        // relevantYears: Liste der in die Bewertung einfliessenden Jahre
        ArrayList<Integer> relevantYears = new ArrayList<Integer>();

        // existingTopoYears: Liste von Jahren, zu denen ein Topographie
        // Datensatz vorhanden ist
        ArrayList<Integer> existingTopoYears = new ArrayList<Integer>();

        // relevantTopoYears: Liste von Jahren, die fuer die Bewertung
        // relevant sind
        ArrayList<Integer> relevantTopoYears = new ArrayList<Integer>();

        // SimpleFeatureCollections
        SimpleFeatureCollection wattCollection;
        SimpleFeatureCollection bGebietNFCollection, bGebietDICollection;
        SimpleFeatureCollection intersecWattBGebiet = FeatureCollections
                .newCollection();
        SimpleFeatureCollection intersecBGebietWattMSRL = FeatureCollections
                .newCollection();

        // Area - Flaechenangaben
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
                + FeatureCollectionUtil.size(inputEvalAreaCollection)
                + " Features, "
                + FeatureCollectionUtil.getNumVerticesFromFC(inputEvalAreaCollection)
                + " Stuetzpunkte, "
                + FeatureCollectionUtil.getMeanVerticesFromFC(inputEvalAreaCollection)
                + " Stuetzpunkte im Mittel");
        LOGGER.debug("SFC-Info:Input-Topographie: "
                + FeatureCollectionUtil.size(inputTopoCollection)
                + " Features, "
                + FeatureCollectionUtil.getNumVerticesFromFC(inputTopoCollection)
                + " Stuetzpunkte, "
                + FeatureCollectionUtil.getMeanVerticesFromFC(inputTopoCollection)
                + " Stuetzpunkte im Mittel");
        LOGGER.debug("SFC-Info:Input-MSRL-D5: "
                + FeatureCollectionUtil.size(inputMSRLCollection)
                + " Features, "
                + FeatureCollectionUtil.getNumVerticesFromFC(inputMSRLCollection)
                + " Stuetzpunkte, "
                + FeatureCollectionUtil.getMeanVerticesFromFC(inputMSRLCollection)
                + " Stuetzpunkte im Mittel");

        for (int i = 0; i < obsParams.length; i++) {
            SimpleFeatureCollection sfc = FeatureCollections.newCollection();

            // SimpleFeatureCollection des aktuellen Parameters extrahieren
            sfc = FeatureCollectionUtil.extract(inputMSRLCollection,
                    new String[]{"OBSV_PARAMETERNAME"},
                    new String[]{obsParams[i]});

            // Liste von Beobachtungszeitpunkten erzeugen, fuer die mindestens
            // eine Beobachtung vorhanden ist
            LOGGER.debug("getObservationDates: Fuer den Parameter "
                    + obsParams[i]
                    + " sind folgende Beobachtungszeitpunkte vorhanden:");
            ArrayList<DateTime> obsDates = MSRLD5Utils.getObservationDates(sfc);

            // Liste von ObservationCollections anhand von
            // Beobachtungszeitpunkten
            obsCollArrayList = MSRLD5Utils.getObsCollByDateList(sfc, obsDates);

            // Relevante ObservationCollections ausgehend von Bewertungsjahr
            // ermitteln
            LOGGER.debug("getRelevantFeatureCollections: Folgende Datensaetze fuer den Parameter "
                    + obsParams[i]
                    + " und das Bewertungsjahr "
                    + InputBewertungsjahr + " ausgewaehlt");
            relCollArrayList = MSRLD5Utils.getRelevantObservationCollections(
                    obsCollArrayList, InputBewertungsjahr);

            // Relevante ObservationCollections der paramArrayListe hinzufuegen
            paramArrayList.add(relCollArrayList);

        }
        // Ende der Schleife ueber die Parameter (Seegras, Algen)
        LOGGER.info("PHASE::Auswahl Seegras und Algen::Ende");

        /*
         * ::: SourceCode-Abschnitt ::: Integritaetstest Seegras & Algen
         */
        LOGGER.info("PHASE::Integritaetstest der Auswahl::Start");
        // Test: Gibt es zwei Eintraege in der paramArrayList? (Seegras und
        // Algen
        if (paramArrayList.size() != 2) {
            throw new RuntimeException(
                    "Es sind nicht genuegend Parameter zur Bewertung vorhanden!");
        }
        // Test: Entsprechen sich die Jahre von Seegras- und Algen-Datensaetzen
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
        LOGGER.info("Integritaetstest erfolgreich!");
        LOGGER.info("PHASE::Integritaetstest der Auswahl::Ende");

        /*
         * ::: SourceCode-Abschnitt ::: Auswahl Berichtsgebiete & Wattflaechen
         */
        LOGGER.info("PHASE::Auswahl Berichtsgebiete und Wattflaechen::Start");
        // Inseln aus Berichtsgebieten entfernen
        this.inputEvalAreaCollection = ReportingAreaUtils.clearEvalAreas(this.inputEvalAreaCollection);

        // Nordfriesland (NF) und Dithmarschen (DI) aus Berichtsgebieten
        // extrahieren
        bGebietNFCollection = FeatureCollectionUtil.extract(
                this.inputEvalAreaCollection, new String[]{"DISTR",
                    "TEMPLATE"}, new String[]{"NF", ""});
        LOGGER.debug("Filter-Info:Berichtsgebiet Nordfriesland - "
                + bGebietNFCollection.size() + " Feautures");

        bGebietDICollection = FeatureCollectionUtil.extract(
                this.inputEvalAreaCollection, new String[]{"DISTR",
                    "TEMPLATE"}, new String[]{"DI", ""});
        LOGGER.debug("Filter-Info:Berichtsgebiet Nordfriesland - "
                + bGebietDICollection.size() + " Feautures");

        // Wattflaechen aus Topographie extrahieren
        wattCollection = FeatureCollectionUtil.extract(inputTopoCollection,
                new String[]{"POSKEY", "POSKEY"}, new String[]{"Watt",
                    "watt"});
        LOGGER.debug("Filter-Info:Wattflaechen - " + wattCollection.size()
                + " Feautures");

        // Vorhandene Topographie-Jahre ermitteln
        existingTopoYears = TopographyUtils.getTopoYears(wattCollection);

        // Relevante Topographie-Jahre ermitteln
        relevantTopoYears = MPAUtils.getRelevantTopoYears(existingTopoYears,
                relevantYears);

        // Liste von ObservationCollections mit Topographien fuer jedes Jahr
        // erstellen und der topoArrayList hinzufuegen
        String relevantTopoYearsString = "";
        for (int i = 0; i < relevantTopoYears.size(); i++) {
            SimpleFeatureCollection sfc = FeatureCollections.newCollection();
            sfc = FeatureCollectionUtil.extract(wattCollection,
                    new String[]{"YEAR"}, new String[]{relevantTopoYears
                        .get(i).toString()});
            DateTime dt = new DateTime(relevantTopoYears.get(i), 1, 1, 0, 0);
            Double area = FeatureCollectionUtil.getArea(sfc);
            topoArrayList.add(new ObservationCollection(dt, sfc, area));
            relevantTopoYearsString += " " + relevantTopoYears.get(i);
        }

        LOGGER.debug("Relevante Topographie-Datensaetze: "
                + relevantTopoYearsString);

        LOGGER.info("PHASE::Auswahl Berichtsgebiete und Wattflaechen::Ende");

        /*
         * Verschneidung
         */
        LOGGER.info("PHASE::Verschneidung und Kenngroessenbestimmung::Start");

        // Schleife ueber existingTopoYears zur Verschneidung jedes
        // Topographie-Datensatzes mit den Berichtsgebieten
        for (int i = 0; i < relevantTopoYears.size(); i++) {
            DateTime dt;
            Double area;
            IntersectionCollection intersecColl;

            // Verschneidung mit NF
            intersecWattBGebiet = MPAUtils.intersectBerichtsgebieteAndTopography(
                    bGebietNFCollection,
                    MSRLD5Utils.getObsCollByYear(topoArrayList, relevantTopoYears.get(i))
                    .getSfc());
            LOGGER.debug("Intersection-Result valid: "
                    + FeatureCollectionUtil.checkValid(intersecWattBGebiet));

            dt = new DateTime(relevantTopoYears.get(i), 1, 1, 0, 0);
            area = FeatureCollectionUtil.getArea(intersecWattBGebiet);

            intersecColl = new IntersectionCollection(MPBMain.NORDFRIESLAND,
                    dt, intersecWattBGebiet, area);
            intersecWattBerichtsgebiete.add(intersecColl);
            LOGGER.debug("Verschneidung NF und Wattflaechen "
                    + relevantTopoYears.get(i) + " abgeschlossen");

            // Verschneidung mit DI
            intersecWattBGebiet = MPAUtils.intersectBerichtsgebieteAndTopography(
                    bGebietDICollection,
                    MSRLD5Utils.getObsCollByYear(topoArrayList, relevantTopoYears.get(i))
                    .getSfc());
            LOGGER.debug("Intersection-Result valid: "
                    + FeatureCollectionUtil.checkValid(intersecWattBGebiet));

            dt = new DateTime(relevantTopoYears.get(i), 1, 1, 0, 0);
            area = FeatureCollectionUtil.getArea(intersecWattBGebiet);

            intersecColl = new IntersectionCollection(MPBMain.DITHMARSCHEN, dt,
                    intersecWattBGebiet, area);
            intersecWattBerichtsgebiete.add(intersecColl);
            LOGGER.debug("Verschneidung DI und Wattflaechen "
                    + relevantTopoYears.get(i) + " abgeschlossen");
        }

        // Schleife ueber relevantYears zur jahresweisen Verschneidung
        for (int i = 0; i < relevantYears.size(); i++) {
            IntersectionCollection intsecColl;
            ObservationCollection msrlColl;
            Integer topoYear = TopographyUtils.getTopoYear(relevantYears.get(i),
                    existingTopoYears);

            LOGGER.debug("Verschneidung: MSRL D5 "
                    + relevantYears.get(i).toString() + " & Topographie "
                    + topoYear.toString());

            // Nordfriesland
            intsecColl = MPAUtils.getIntersecCollByYearAndGebiet(
                    intersecWattBerichtsgebiete, topoYear,
                    MPBMain.NORDFRIESLAND);
            totalWattAreaNF = intsecColl.getArea();
            // ZS --> Seegras
            msrlColl = MSRLD5Utils.getObsCollByYear(paramArrayList.get(1),
                    relevantYears.get(i));

            intersecBGebietWattMSRL = MPAUtils.intersectIntersectionAndMSRL(
                    intsecColl.getSfc(), msrlColl.getSfc());

            // Debug
			/*
             * if (relevantYears.get(i) == 2003) { this.debugA =
             * intsecColl.getSfc(); this.debugB = intersecBGebietWattMSRL; }
             */
            LOGGER.debug("Intersection-Result valid: "
                    + FeatureCollectionUtil.checkValid(intersecBGebietWattMSRL));

            ZS_totalareaNF = FeatureCollectionUtil.getArea(intersecBGebietWattMSRL);
            ZS_40areaNF = FeatureCollectionUtil.getArea(FeatureCollectionUtil.extract(
                    intersecBGebietWattMSRL,
                    new String[]{"OBSV_PARAMETERVALUE"},
                    new String[]{"4"}));
            ZS_60areaNF = FeatureCollectionUtil.getArea(FeatureCollectionUtil.extract(
                    intersecBGebietWattMSRL,
                    new String[]{"OBSV_PARAMETERVALUE"},
                    new String[]{"6"}));
            if (ZS_40areaNF > 0 && ZS_60areaNF > 0) {
                ZS_numTypesNF = 2;
            } else {
                ZS_numTypesNF = 1;
            }
            LOGGER.debug("Verschneidung: WattXBerichtsgebiete & MSRL-Seegras "
                    + relevantYears.get(i) + " in Nordfriesland");
            // OP --> Algen
            msrlColl = MSRLD5Utils.getObsCollByYear(paramArrayList.get(0),
                    relevantYears.get(i));
            intersecBGebietWattMSRL = MPAUtils.intersectIntersectionAndMSRL(
                    intsecColl.getSfc(), msrlColl.getSfc());

            OP_totalareaNF = FeatureCollectionUtil.getArea(intersecBGebietWattMSRL);
            OP_40areaNF = FeatureCollectionUtil.getArea(FeatureCollectionUtil.extract(
                    intersecBGebietWattMSRL,
                    new String[]{"OBSV_PARAMETERVALUE"},
                    new String[]{"4"}));
            OP_60areaNF = FeatureCollectionUtil.getArea(FeatureCollectionUtil.extract(
                    intersecBGebietWattMSRL,
                    new String[]{"OBSV_PARAMETERVALUE"},
                    new String[]{"6"}));
            LOGGER.debug("Verschneidung: WattXBerichtsgebiete & MSRL-Algen "
                    + relevantYears.get(i) + " in Nordfriesland");

            // Dithmarschen
            intsecColl = MPAUtils.getIntersecCollByYearAndGebiet(
                    intersecWattBerichtsgebiete, topoYear, MPBMain.DITHMARSCHEN);
            totalWattAreaDI = intsecColl.getArea();
            // ZS --> Seegras
            msrlColl = MSRLD5Utils.getObsCollByYear(paramArrayList.get(1),
                    relevantYears.get(i));
            intersecBGebietWattMSRL = MPAUtils.intersectIntersectionAndMSRL(
                    intsecColl.getSfc(), msrlColl.getSfc());
            ZS_totalareaDI = FeatureCollectionUtil.getArea(intersecBGebietWattMSRL);
            ZS_40areaDI = FeatureCollectionUtil.getArea(FeatureCollectionUtil.extract(
                    intersecBGebietWattMSRL,
                    new String[]{"OBSV_PARAMETERVALUE"},
                    new String[]{"4"}));
            ZS_60areaDI = FeatureCollectionUtil.getArea(FeatureCollectionUtil.extract(
                    intersecBGebietWattMSRL,
                    new String[]{"OBSV_PARAMETERVALUE"},
                    new String[]{"6"}));
            if (ZS_40areaDI > 0 && ZS_60areaDI > 0) {
                ZS_numTypesDI = 2;
            } else {
                ZS_numTypesDI = 1;
            }
            LOGGER.debug("Verschneidung: WattXBerichtsgebiete & MSRL-Seegras "
                    + relevantYears.get(i) + " in Dithmarschen");
            // OP --> Algen
            msrlColl = MSRLD5Utils.getObsCollByYear(paramArrayList.get(0),
                    relevantYears.get(i));
            intersecBGebietWattMSRL = MPAUtils.intersectIntersectionAndMSRL(
                    intsecColl.getSfc(), msrlColl.getSfc());
            OP_totalareaDI = FeatureCollectionUtil.getArea(intersecBGebietWattMSRL);
            OP_40areaDI = FeatureCollectionUtil.getArea(FeatureCollectionUtil.extract(
                    intersecBGebietWattMSRL,
                    new String[]{"OBSV_PARAMETERVALUE"},
                    new String[]{"4"}));
            OP_60areaDI = FeatureCollectionUtil.getArea(FeatureCollectionUtil.extract(
                    intersecBGebietWattMSRL,
                    new String[]{"OBSV_PARAMETERVALUE"},
                    new String[]{"6"}));
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

        LOGGER.debug("PHASE::Verschneidung und Kenngroessenbestimmung::Ende");

        // Process Outputs
        // FeatureCollection mit den bewerteten Featuren Nordfriesland und
        // Dithmarschen
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
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        this.outputXMLFile = f;

        // Debug
        this.outputRawValues = result.getRawRecordsString(false);
        this.outputEvalValues = result.getEvalRecordsString(false);

    }

    /**
     * Erzeugt aus den Eingabe-FeatureCollections fuer die Ausgangsgeometrien
     * der beiden Berichtsgebiete NF und DI zwei verschmolzene Polygone mit
     * Bewertungsparametern
     *
     * @param nfCollection - SimpleFeatureCollection Berichtsgebiet
     * Nordfriesland
     * @param diCollection - SimpleFeatureCollection Berichtsgebiet Dithmarschen
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
        geom = FeatureCollectionUtil.union(geomCollection);
        featureBuilder.set("MPBGeom", geom);
        featureBuilder.set("DISTR", "NF");
        featureBuilder.set("MPBMeanEQR",
                result.getAreaResult(MPBMain.NORDFRIESLAND).getMeanEQR()
                .toString());
        featureBuilder.set("MPBEvalStringEQR",
                result.getAreaResult(MPBMain.NORDFRIESLAND)
                .getMeanEQREvalString());
        resultCollection.add(featureBuilder.buildFeature(null));
        geomCollection.clear();

        // Geometrien aus Dithmarschen sammeln
        FeatureIterator<SimpleFeature> diIter = diCollection.features();
        while (diIter.hasNext()) {
            SimpleFeature feature = diIter.next();
            geom = (Geometry) feature.getDefaultGeometry();
            geomCollection.add(geom);
        }
        geom = FeatureCollectionUtil.union(geomCollection);
        featureBuilder.set("MPBGeom", geom);
        featureBuilder.set("DISTR", "DI");
        featureBuilder.set("MPBMeanEQR",
                result.getAreaResult(MPBMain.DITHMARSCHEN).getMeanEQR()
                .toString());
        featureBuilder.set("MPBEvalStringEQR",
                result.getAreaResult(MPBMain.DITHMARSCHEN)
                .getMeanEQREvalString());
        resultCollection.add(featureBuilder.buildFeature(null));
        geomCollection.clear();

        return resultCollection;
    }
}
