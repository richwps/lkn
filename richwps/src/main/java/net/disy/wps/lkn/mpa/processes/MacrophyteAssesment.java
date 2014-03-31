package net.disy.wps.lkn.mpa.processes;

import java.io.File;
import net.disy.wps.lkn.mpa.types.IntegerList;
import net.disy.wps.lkn.utils.FeatureCollectionUtil;

import net.disy.wps.n52.binding.MPBResultBinding;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
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
import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollection;
import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollectionList;
import net.disy.wps.lkn.mpa.types.MPBResult;
import net.disy.wps.lkn.mpa.types.MPBResultRecord;
import net.disy.wps.lkn.mpa.types.ObservationFeatureCollection;

import net.disy.wps.lkn.utils.MSRLD5Utils;
import net.disy.wps.lkn.mpa.types.ObservationFeatureCollectionList;
import net.disy.wps.lkn.utils.MPAUtils;
import net.disy.wps.lkn.utils.ReportingAreaUtils;
import net.disy.wps.lkn.utils.TopographyUtils;

@Algorithm(version = "0.9.5", title = "Makrophytenbewertung", abstrakt = "Prozess zur Bewertung der Berichtsgebiete Nordfriesland und Dithmarschen anhand von MSRL-D5 Daten")
public class MacrophyteAssesment extends AbstractAnnotatedAlgorithm {

    // Logger fuer Debugging erzeugen
    // use with wps 3.2.0
    protected static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MacrophyteAssesment.class);
    //use with wps 3.1.0
    //protected static org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(MacrophyteAssesment.class);

    /**
     * Reporting Areas.
     */
    private SimpleFeatureCollection inputReportingAreas;
    /**
     * Topography.
     */
    private SimpleFeatureCollection inputTopography;
    /**
     * MSRLD05 Algea and Seagras..
     */
    private SimpleFeatureCollection inputMSRLD5;
    /**
     * The relevantTopoYear for the assement.
     */
    private Integer inputAssesmentYear;

    /**
     * Dealing with topography data.
     */
    private TopographyUtils topgraphy;
    /**
     * Dealing with reportingareas.
     */
    private ReportingAreaUtils reportingareas;
    /**
     * Dealing with MSRLD5 measurements.
     */
    private MSRLD5Utils msrld5;
    /**
     * Assesment specific tasks.
     */
    private MPAUtils mpa;

    // Debug
	/*
     * private SimpleFeatureCollection debugA = FeatureCollections
     * .newCollection(); private SimpleFeatureCollection debugB =
     * FeatureCollections .newCollection();
     */
    /**
     * The result of this process.
     */
    private MPBResult result = new MPBResult();

    private String outputRawValues;
    private String outputEvalValues;
    private File outputXMLFile;
    private SimpleFeatureCollection outputCollection = FeatureCollections.newCollection();

    /**
     * Constructs a new WPS-Process MacrophyteAssesment.
     */
    public MacrophyteAssesment() {
        super();
        //LOGGER.setLevel(Level.ALL);
    }

    @Execute
    public void runMPB() {
        this.topgraphy = new TopographyUtils(this.inputTopography);
        this.reportingareas = new ReportingAreaUtils(this.inputReportingAreas);
        this.msrld5 = new MSRLD5Utils(this.inputMSRLD5);
        this.mpa = new MPAUtils(this.topgraphy, this.reportingareas, this.msrld5);

        // Ergebnis-Objekt mit Bewertungsjahr initialisieren
        this.result.setBewertungsjahr(this.inputAssesmentYear);

        ////////////////////////////////////////////////////////////////////////
        //BEGIN //////////////////////////////////////////////PHASE MSRLD5 SELECTION
        ////////////////////////////////////////////////////////////////////////
        //Selektion von MSRLD5-FeatureCollections algen und seegras anhand des
        //MSRLD5-Attributs OBSV_PARAMNAME. COV_OP = Algen. COV_ZS = Seegras.
        //
        //[
        // relevantAlgea::ArrayList<SimpleFeatureCollection>+Attributes,
        // relevantSeagras::ArrayList<SimpleFeatureCollection>+Attributes,
        // relevantYears::ArrayList<Integer>] process::selectMSRLD5Parameters(this.inputMSRLD5, this.inputAssesmentYear)
        final ObservationFeatureCollectionList relevantAlgea;
        relevantAlgea = msrld5.getRelevantObservationsByParameterAndYear(MSRLD5Utils.ATTRIB_OBS_PARAMNAME_OP, this.inputAssesmentYear);
        final int amountAlgaeObservations = relevantAlgea.size();

        final ObservationFeatureCollectionList relevantSeagras;
        relevantSeagras = msrld5.getRelevantObservationsByParameterAndYear(MSRLD5Utils.ATTRIB_OBS_PARAMNAME_ZS, this.inputAssesmentYear);
        final int amountSeagrasObservations = relevantSeagras.size();

        // Validierung der vorherigen Selektion
        // Test: Gibt es zwei Eintraege in der msrld5Parameters? (Seegras und
        // Algen
        if (relevantAlgea == null || relevantSeagras == null) {
            throw new RuntimeException(
                    "Es sind nicht genuegend Parameter zur Bewertung vorhanden!");
        }

        if (amountSeagrasObservations != amountAlgaeObservations) {
            throw new RuntimeException(
                    "Es sind nicht genuegend Parameter zur Bewertung vorhanden!");
        }

        // Test: Entsprechen sich die Jahre von Seegras- und Algen-Datensaetze?
        for (int i = 0; i < amountSeagrasObservations; i++) {
            int seagrasyear = relevantSeagras.get(i).getDateTime().getYear();
            int algeayear = relevantAlgea.get(i).getDateTime().getYear();
            if (seagrasyear != algeayear) {
                throw new RuntimeException(
                        "Die relevanten Jahre der"
                        + "beiden Parameter 'Algen' und 'Seegras' entsprechen sich nicht!");
            }
        }

        IntegerList relevantYears = new IntegerList();

        for (int i = 0; i < amountSeagrasObservations; i++) {
            int seagrasyear = relevantSeagras.get(i).getDateTime().getYear();
            //Hinzufuegen/Merken eines der beiden Jahre.   
            relevantYears.add(seagrasyear);
        }
        ////////////////////////////////////////////////////////////////////////
        //ENDE //////////////////////////////////////////////PHASE MSRLD5 SELECTION
        ////////////////////////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////////////////////
        //BEGIN ///////////////////PHASE SELEKTION BERICHTSGEBIET
        ////////////////////////////////////////////////////////////////////////
        //Selektion von Berichtsgebieten und Topography anhand verschiedener Kriterien.
        //
        //[
        // reportingAreasNF::SimpleFeatureCollection,
        // reportingAreasDI::SimpleFeatureCollection] process::mpa::selectReportingsAreas(this.reportingareas)
        // Ggf. parameterisierter Prozess fuer Berichtsgebiete.
        // [reportingAreas::SimpleFeatureCollection] process::lkn::selectReportingsAreas(this.reportingareas, "NF")
        // [reportingAreas::SimpleFeatureCollection] process::lkn::selectReportingsAreas(this.reportingareas, "DI")
        // Inseln aus Berichtsgebieten entfernen
        this.inputReportingAreas = this.reportingareas.clearReportingAreas();
        // Nordfriesland (NF) und Dithmarschen (DI) aus Berichtsgebieten
        // extrahieren
        // SimpleFeatureCollections
        SimpleFeatureCollection reportingAreasNF = this.reportingareas.extractNF();
        SimpleFeatureCollection reportingAreasDI = this.reportingareas.extractDI();

        ////////////////////////////////////////////////////////////////////////
        //ENDE ///////////////////PHASE SELEKTION BERICHTSGEBIET
        ////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////
        //BEGIN ///////////////////PHASE SELEKTION TOPOGRAPHY
        ////////////////////////////////////////////////////////////////////////
        //Selektion von Topography anhand verschiedener Kriterien.
        //
        //[
        // existingTopoYears::ArrayList<Integer>,
        // relevantTopoYears::ArrayList<Integer>,
        // relevantTopos::ArrayList<SimpleFeatureCollection>+Attributes] process::mpa::selectTpoography(this.topgraphy)
        // existingTopoYears: Liste von Jahren, zu denen ein Topographie
        // Datensatz vorhanden ist
        IntegerList existingTopoYears = new IntegerList();
        // Vorhandene Topographie-Jahre ermitteln
        existingTopoYears = this.topgraphy.getExistingTopographyYears();

        // relevantTopoYears: Liste von Jahren, die fuer die Bewertung
        // relevant sind
        IntegerList relevantTopoYears = new IntegerList();
        // Relevante Topographie-Jahre ermitteln
        relevantTopoYears = this.mpa.getRelevantTopoYears(existingTopoYears, relevantYears);

        // relevantTopos: Liste mit ObservationFeatureCollection,
        // die jeweils die FeatureCollection der Topographiegeometrien fuer ein
        // Jahr enthalten
        // Liste von ObservationCollections mit Topographien fuer jedes Jahr
        // erstellen und der relevantTopos hinzufuegen
        ObservationFeatureCollectionList relevantTopos = new ObservationFeatureCollectionList();

        for (Integer relevantTopoYear : relevantTopoYears) {
            SimpleFeatureCollection sfc = FeatureCollections.newCollection();
            sfc = this.topgraphy.extractByYear(relevantTopoYear.toString());

            DateTime obsTime = new DateTime(relevantTopoYear, 1, 1, 0, 0);
            Double area = FeatureCollectionUtil.getArea(sfc);
            relevantTopos.add(new ObservationFeatureCollection(obsTime, sfc, area));
        }

        ////////////////////////////////////////////////////////////////////////
        //ENDE ///////////////////PHASE SELEKTION TOPOGRAPHY 
        ////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////
        //BEGIN //////////////////////////////////////////PHASE VERSCHNEIDUNG (NF)
        ////////////////////////////////////////////////////////////////////////
        //[intersections::ArrayList<IntersectionFeatureCollection>]
        //process::mpa::intersect(nfreportingAreas::SimpleFeatureCollection, relevanttopos, relevantTopos::ArrayList<SimpleFeatureCollection>+Attributes], "NF"
        // Schleife ueber existingTopoYears zur Verschneidung jedes
        // Topographie-Datensatzes mit dem Berichtsgebiet NF.
        IntersectionFeatureCollectionList intersectionsTidelandReportingAreasNF = new IntersectionFeatureCollectionList();
        for (Integer relevantTopoYear : relevantTopoYears) {

            // Verschneidung mit NF
            final SimpleFeatureCollection intersecWattBGebiet = MPAUtils.intersectReportingsareasAndTidelands(reportingAreasNF, relevantTopos.getObsCollByYear(relevantTopoYear).getFeatureCollection());
            final DateTime obsTime = new DateTime(relevantTopoYear, 1, 1, 0, 0);
            final Double area = FeatureCollectionUtil.getArea(intersecWattBGebiet);

            final IntersectionFeatureCollection intersecColl = new IntersectionFeatureCollection(MPAUtils.NORDFRIESLAND, obsTime, intersecWattBGebiet, area);
            intersectionsTidelandReportingAreasNF.add(intersecColl);
            LOGGER.debug("Verschneidung NF und Wattflaechen "
                    + relevantTopoYear + " abgeschlossen");
        }//of for

        ////////////////////////////////////////////////////////////////////////
        //ENDE //////////////////////////////////////////PHASE VERSCHNEIDUNG (NF)
        ////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////
        //BEGIN //////////////////////////////////////////PHASE VERSCHNEIDUNG (DI)
        ////////////////////////////////////////////////////////////////////////
        //[intersections::ArrayList<IntersectionFeatureCollection>]
        //process::mpa::intersect(direportingAreas::SimpleFeatureCollection, relevanttopos, relevantTopos::ArrayList<SimpleFeatureCollection>+Attributes], "DI"
        // Schleife ueber existingTopoYears zur Verschneidung jedes
        // Topographie-Datensatzes mit dem Berichtsgebiet DI.
        IntersectionFeatureCollectionList intersectionsTidelandReportingAreasDI = new IntersectionFeatureCollectionList();
        for (Integer relevantTopoYear : relevantTopoYears) {

            // Verschneidung mit DI
            final SimpleFeatureCollection intersecWattBGebiet = MPAUtils.intersectReportingsareasAndTidelands(
                    reportingAreasDI,
                    relevantTopos.getObsCollByYear(relevantTopoYear)
                    .getFeatureCollection());
            LOGGER.debug("Intersection-Result valid: "
                    + FeatureCollectionUtil.checkValid(intersecWattBGebiet));

            final DateTime obsTime = new DateTime(relevantTopoYear, 1, 1, 0, 0);
            final Double area = FeatureCollectionUtil.getArea(intersecWattBGebiet);

            final IntersectionFeatureCollection intersecColl = new IntersectionFeatureCollection(MPAUtils.DITHMARSCHEN, obsTime, intersecWattBGebiet, area);
            intersectionsTidelandReportingAreasDI.add(intersecColl);
            LOGGER.debug("Verschneidung DI und Wattflaechen "
                    + relevantTopoYear + " abgeschlossen");
        }//of for

        ////////////////////////////////////////////////////////////////////////
        //ENDE //////////////////////////////////////////PHASE VERSCHNEIDUNG (DI)
        ////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////
        //BEGIN ////////////////////////////////////PHASE KENNGROESSENBESTIMMUNG
        ////////////////////////////////////////////////////////////////////////
        //[ ArrayList<MPBAreaResult>]
        //process::mpa::characteristics(
        // relevantYears::ArrayList<Integer>,
        // existingTopoYears::ArrayList<Integer>,
        // intersectionsTidelandsReportingAreas::ArrayList<IntersectionFeatureCollection>,
        // 
        //)
        SimpleFeatureCollection intersectionReportingAreasTidelandsMSRL = FeatureCollections.newCollection();

        // Area - Flaechenangaben
        Double totalWattAreaNF;
        Integer ZS_numTypesNF;
        Double ZS_totalareaNF, ZS_40areaNF, ZS_60areaNF;
        Double OP_totalareaNF, OP_40areaNF, OP_60areaNF;

        // Schleife ueber relevantYears zur jahresweisen Verschneidung
        for (Integer relevantYear : relevantYears) {
            IntersectionFeatureCollection intsecColl;
            ObservationFeatureCollection msrlColl;
            final Integer topoYear = this.mpa.getTopoYear(relevantYear, existingTopoYears.getPayload());

            // Nordfriesland
            intsecColl = MPAUtils.getIntersecCollByYear(
                    intersectionsTidelandReportingAreasNF.getIntersections(), topoYear);
            totalWattAreaNF = intsecColl.getArea();
            // ZS --> Seegras
            msrlColl = relevantSeagras.getObsCollByYear(relevantYear);

            intersectionReportingAreasTidelandsMSRL = this.mpa.intersectIntersectionAndMSRL(intsecColl.getFeatureCollection(), msrlColl.getFeatureCollection());

            ZS_totalareaNF = FeatureCollectionUtil.getArea(intersectionReportingAreasTidelandsMSRL);
            SimpleFeatureCollection ZS_40areaNFSF = FeatureCollectionUtil.extractEquals(intersectionReportingAreasTidelandsMSRL, new String[]{MSRLD5Utils.ATTRIB_OBSV_PARAMETERVALUE}, new String[]{"4"});
            ZS_40areaNF = FeatureCollectionUtil.getArea(ZS_40areaNFSF);

            SimpleFeatureCollection ZS_60areaNFSF = FeatureCollectionUtil.extractEquals(intersectionReportingAreasTidelandsMSRL, new String[]{MSRLD5Utils.ATTRIB_OBSV_PARAMETERVALUE}, new String[]{"6"});
            ZS_60areaNF = FeatureCollectionUtil.getArea(ZS_60areaNFSF);

            if (ZS_40areaNF > 0 && ZS_60areaNF > 0) {
                ZS_numTypesNF = 2;
            } else {
                ZS_numTypesNF = 1;
            }

            // OP --> Algen
            msrlColl = relevantAlgea.getObsCollByYear(relevantYear);
            intersectionReportingAreasTidelandsMSRL = MPAUtils.intersectIntersectionAndMSRL(
                    intsecColl.getFeatureCollection(), msrlColl.getFeatureCollection());

            OP_totalareaNF = FeatureCollectionUtil.getArea(intersectionReportingAreasTidelandsMSRL);
            final SimpleFeatureCollection OP_40areaNFSF = FeatureCollectionUtil.extractEquals(intersectionReportingAreasTidelandsMSRL, new String[]{MSRLD5Utils.ATTRIB_OBSV_PARAMETERVALUE}, new String[]{"4"});
            OP_40areaNF = FeatureCollectionUtil.getArea(OP_40areaNFSF);
            final SimpleFeatureCollection OP_60areaNFSF = FeatureCollectionUtil.extractEquals(intersectionReportingAreasTidelandsMSRL, new String[]{MSRLD5Utils.ATTRIB_OBSV_PARAMETERVALUE}, new String[]{"6"});
            OP_60areaNF = FeatureCollectionUtil.getArea(OP_60areaNFSF);

            ///////////////////////////DI 
            Double totalWattAreaDI;
            Integer ZS_numTypesDI;
            Double ZS_totalareaDI, ZS_40areaDI, ZS_60areaDI;
            Double OP_totalareaDI, OP_40areaDI, OP_60areaDI;

            // Dithmarschen
            intsecColl = MPAUtils.getIntersecCollByYear(intersectionsTidelandReportingAreasDI.getIntersections(), topoYear);
            totalWattAreaDI = intsecColl.getArea();
            // ZS --> Seegras
            msrlColl = relevantSeagras.getObsCollByYear(relevantYear);
            intersectionReportingAreasTidelandsMSRL = MPAUtils.intersectIntersectionAndMSRL(
                    intsecColl.getFeatureCollection(), msrlColl.getFeatureCollection());
            ZS_totalareaDI = FeatureCollectionUtil.getArea(intersectionReportingAreasTidelandsMSRL);
            ZS_40areaDI = FeatureCollectionUtil.getArea(FeatureCollectionUtil.extractEquals(
                    intersectionReportingAreasTidelandsMSRL,
                    new String[]{MSRLD5Utils.ATTRIB_OBSV_PARAMETERVALUE},
                    new String[]{"4"}));
            ZS_60areaDI = FeatureCollectionUtil.getArea(FeatureCollectionUtil.extractEquals(
                    intersectionReportingAreasTidelandsMSRL,
                    new String[]{MSRLD5Utils.ATTRIB_OBSV_PARAMETERVALUE},
                    new String[]{"6"}));
            if (ZS_40areaDI > 0 && ZS_60areaDI > 0) {
                ZS_numTypesDI = 2;
            } else {
                ZS_numTypesDI = 1;
            }
            LOGGER.debug("Verschneidung: WattXBerichtsgebiete & MSRL-Seegras "
                    + relevantYear + " in Dithmarschen");
            // OP --> Algen
            msrlColl = relevantAlgea.getObsCollByYear(relevantYear);
            intersectionReportingAreasTidelandsMSRL = MPAUtils.intersectIntersectionAndMSRL(
                    intsecColl.getFeatureCollection(), msrlColl.getFeatureCollection());
            OP_totalareaDI = FeatureCollectionUtil.getArea(intersectionReportingAreasTidelandsMSRL);
            OP_40areaDI = FeatureCollectionUtil.getArea(FeatureCollectionUtil.extractEquals(
                    intersectionReportingAreasTidelandsMSRL,
                    new String[]{MSRLD5Utils.ATTRIB_OBSV_PARAMETERVALUE},
                    new String[]{"4"}));
            OP_60areaDI = FeatureCollectionUtil.getArea(FeatureCollectionUtil.extractEquals(
                    intersectionReportingAreasTidelandsMSRL,
                    new String[]{MSRLD5Utils.ATTRIB_OBSV_PARAMETERVALUE},
                    new String[]{"6"}));
            LOGGER.debug("Verschneidung: WattXBerichtsgebiete & MSRL-Algen "
                    + relevantYear + " in Dithmarschen");

            /// ERGEBNISSE ZUSAMMENFUEHREN
            // ResultRecord erzeugen
            MPBResultRecord resultRec = new MPBResultRecord(
                    relevantYear, totalWattAreaNF, totalWattAreaDI,
                    ZS_numTypesNF, ZS_numTypesDI, ZS_totalareaNF, ZS_40areaNF,
                    ZS_60areaNF, ZS_totalareaDI, ZS_40areaDI, ZS_60areaDI,
                    OP_totalareaNF, OP_40areaNF, OP_60areaNF, OP_totalareaDI,
                    OP_40areaDI, OP_60areaDI);
            this.result.addRecord(resultRec);
        }

        ////////////////////////////////////////////////////////////////////////
        //ENDE //////////////////////////////////////////PHASE KENNGROESSENBESTIMMUNG
        ////////////////////////////////////////////////////////////////////////
        // Process Outputs
        // FeatureCollection mit den bewerteten Featuren Nordfriesland und
        // Dithmarschen
        this.outputCollection = MPAUtils.getEvaluatedAreas(this.result, reportingAreasNF, reportingAreasDI);

        //Persistence moved to generator.
        //File f = this.result.persist();
        //this.outputXMLFile = f;

        // Debug
        this.outputRawValues = result.getRawRecordsString(false);
        this.outputEvalValues = result.getEvalRecordsString(false);

    }

    @ComplexDataInput(identifier = "berichtsgebiete", title = "Berichtsgebiete", abstrakt = "Berichtsgebiete die die Werte 'DI' und 'NF' im Attribut 'DISTR' enthalten.", binding = GTVectorDataBinding.class)
    public void setReportingArea(final FeatureCollection<?, ?> evalAreaCollection) {
        this.inputReportingAreas = (SimpleFeatureCollection) evalAreaCollection;
    }

    @ComplexDataInput(identifier = "topographie", title = "Topographie", abstrakt = "Topographie Layer", minOccurs = 1, maxOccurs = 1, binding = GTVectorDataBinding.class)
    public void setTopography(final FeatureCollection<?, ?> topoCollection) {
        this.inputTopography = (SimpleFeatureCollection) topoCollection;
    }

    @ComplexDataInput(identifier = "msrl-d5", title = "MSRL D5 Daten", abstrakt = "MSRL D5 Daten, die Algen- und Seegras- Polygone enthalten.", binding = GTVectorDataBinding.class)
    public void setMSRLD5(final FeatureCollection<?, ?> inputCollection) {
        this.inputMSRLD5 = (SimpleFeatureCollection) inputCollection;
    }

    @LiteralDataInput(identifier = "bewertungsjahr", title = "Bewertungsjahr", abstrakt = "Bewertungsjahr, von dem die durchzufuehrende Bewertung ausgeht.", binding = LiteralStringBinding.class)
    public void setAssesmentYear(String assesmentYear) {
        this.inputAssesmentYear = Integer.parseInt(assesmentYear);
    }

    @LiteralDataOutput(identifier = "rawValues", title = "Rohdaten", abstrakt = "CSV-Tabelle mit Rohdaten der Verschneidungsergebnisse (Flaechen in Quadratkilometer).", binding = LiteralStringBinding.class)
    public String getRawValues() {
        return this.outputRawValues;
    }

    @LiteralDataOutput(identifier = "evalValues", title = "Bewertungsergebnisse", abstrakt = "CSV-Tabelle mit bewerteten Flaechenverhaeltnissen und EQR-Werten.", binding = LiteralStringBinding.class)
    public String getEvalValues() {
        return this.outputEvalValues;
    }

    // Achtung: Output Type muss hier FeatureCollection sein (nicht
    // SimpleFeatureCollection)!
    @ComplexDataOutput(identifier = "mpbResultGml", title = "Bewertete Berichtsgebiete", abstrakt = "FeatureCollection der bewerteten Berichtsgebiete", binding = GTVectorDataBinding.class)
    public FeatureCollection getResultGml() {
        return this.outputCollection;
    }

    @ComplexDataOutput(identifier = "mpbResultXml", title = "XML-Rohdaten Datei", abstrakt = "XML-Datei mit Rohdaten der Bewertung", binding = MPBResultBinding.class)
    public net.disy.wps.lkn.mpa.types.MPBResult getResultXml() {
        return this.result;
    }
}
