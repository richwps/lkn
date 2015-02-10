package net.disy.wps.lkn.mpa.processes;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import static net.disy.wps.lkn.mpa.processes.MacrophyteAssessment.LOGGER;
import net.disy.wps.lkn.mpa.types.IntegerList;
import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollection;
import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollectionList;
import net.disy.wps.lkn.mpa.types.MPBResult;
import net.disy.wps.lkn.mpa.types.MPBResultRecord;
import net.disy.wps.lkn.mpa.types.ObservationFeatureCollection;
import net.disy.wps.lkn.mpa.types.ObservationFeatureCollectionList;
import net.disy.wps.lkn.utils.FeatureCollectionUtil;
import net.disy.wps.lkn.utils.MPAUtils;
import static net.disy.wps.lkn.utils.MPAUtils.OBSV_PARAMETERVALUE;
import net.disy.wps.lkn.utils.MSRLD5Utils;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import net.disy.wps.n52.binding.IntegerListBinding;
import net.disy.wps.n52.binding.IntersectionFeatureCollectionListBinding;
import net.disy.wps.n52.binding.MPBResultBinding;
import net.disy.wps.n52.binding.ObeservationFeatureCollectionListBinding;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

@Algorithm(version = "0.0.1", title = "Characteristics", abstrakt = ".")
public class Characteristics extends AbstractAnnotatedAlgorithm {

    private MPBResult result = new MPBResult();
    private SimpleFeatureCollection outputCollection;

    private Integer inputAssessmentYear;
    
    private SimpleFeatureCollection reportingAreasNF;
    private SimpleFeatureCollection reportingAreasDI;
    private IntegerList relevantYears;
    private IntegerList existingTopographyYears;

    private IntersectionFeatureCollectionList intersectionsTidelandReportingAreasNF;
    private IntersectionFeatureCollectionList intersectionsTidelandReportingAreasDI;

    private ObservationFeatureCollectionList relevantSeagras;
    private ObservationFeatureCollectionList relevantAlgea;

    public Characteristics() {
        super();
    }

    @Execute
    public void runMPB() {
        SimpleFeatureCollection intersectionReportingAreasTidelandsMSRL = FeatureCollections.newCollection();

        // Area - Flaechenangaben
        Double totalWattAreaNF;
        Integer ZS_numTypesNF;
        Double ZS_totalareaNF, ZS_40areaNF, ZS_60areaNF;
        Double OP_totalareaNF, OP_40areaNF, OP_60areaNF;

        result.setBewertungsjahr(inputAssessmentYear);
        
        // Schleife ueber relevantYears zur jahresweisen Verschneidung
        for (Integer relevantYear : relevantYears) {
            IntersectionFeatureCollection intsecColl;
            ObservationFeatureCollection msrlColl;
            final Integer topoYear = this.getTopoYear(relevantYear, existingTopographyYears.getPayload());

            // Nordfriesland
            intsecColl = MPAUtils.getIntersecCollByYear(
                    intersectionsTidelandReportingAreasNF.getIntersections(), topoYear);
            totalWattAreaNF = intsecColl.getArea();
            // ZS --> Seegras
            msrlColl = relevantSeagras.getObsCollByYear(relevantYear);

            intersectionReportingAreasTidelandsMSRL = this.intersectIntersectionAndMSRL(intsecColl.getFeatureCollection(), msrlColl.getFeatureCollection());

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
            this.outputCollection = MPAUtils.getEvaluatedAreas(this.result, reportingAreasNF, reportingAreasDI);

        }//of for
    }

    /**
     * Liefert ein einem Topographie-Datensatz zugehoeriges Jahr aus einer Liste
     * von moeglichen Jahren, welches die minimale zeitliche Distanz zu einem
     * Eingabejahr aufweist. Im Fall von gleichen Zeitunterschieden zu zwei
     * Jahren, wird das spaetere zurueck gegeben.
     *
     * @param year - Eingabejahr, zu dem ein passendes Topographie-Jahr
     * ermittelt werden soll
     * @param topoYearList - Liste mit vorhandenen Topographie-Jahren
     * @return Jahr
     */
    public int getTopoYear(final int year, final ArrayList<Integer> topoYearList) {
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
     * Verschneidet das Verschneidungs-Ergebnis von Berichtsgebieten &
     * Wattflaechen mit den MSRL-Daten (Seegras-, Algen-Geometrien) eines
     * Jahres. Die zurueckgegebene SimpleFeatureCollection enthaelt alle
     * MSRL-Attribute und die Angaben 'DISTR' und 'NAME'
     *
     * @param intersecColl - SimpleFeatureCollection des
     * Verschneidungs-Ergebnisses Berichtsgebiete und Wattflaechen
     * @param msrlColl - SimpleFeatureCollection der MSRL-Daten
     * @return SimpleFeatureCollection des Verschneidungs-Ergebnisses
     */
    public static SimpleFeatureCollection intersectIntersectionAndMSRL(
            SimpleFeatureCollection intersecColl,
            SimpleFeatureCollection msrlColl) {
        SimpleFeatureCollection intersecFeatureCollection = FeatureCollections
                .newCollection();
        // Relevante Features aus SimpleFeatureCollection des
        // Verschneidungs-Ergebnisses
        // von Berichtsgebieten und Wattflaechen
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
        // Iteration ueber BoundingBoxen
        try {
            while (iterA.hasNext()) {
                bWattFeature = iterA.next();
                bWattBB = ((Geometry) bWattFeature.getDefaultGeometry())
                        .getEnvelope();

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
                // FeatureType der Ziel-Collection aus der MSRLCollection erstellen und
                // Geometrietyp auf Multipolygon setzen
                FeatureCollectionUtil.refactorFeatureType(
                        (SimpleFeatureType) msrlColl.getSchema(), null,
                        "FinalIntersectionFeatures", MultiPolygon.class));

        // ArrayLists aus HashSets befuellen
        relBWFeat.addAll(relBWFeatHs);
        relMFeat.addAll(relMFeatHs);

        // Schleife ueber Intersection-Ergebnis (Berichtsgebiete&Wattflaechen)
        for (int i = 0; i < relBWFeat.size(); i++) {
            bWattFeature = relBWFeat.get(i);
            bWattGeom = (Geometry) bWattFeature.getDefaultGeometry();

            // Schleife ueber MSRL-Features
            for (int j = 0; j < relMFeat.size(); j++) {
                msrlFeature = relMFeat.get(j);
                msrlGeom = (Geometry) msrlFeature.getDefaultGeometry();

                intersec = bWattGeom.intersection(msrlGeom);
                if (!intersec.isEmpty()) {
                    featureBuilder.set(msrlColl.getSchema()
                            .getGeometryDescriptor().getLocalName(), intersec);
                    featureBuilder.set(OBSV_PARAMETERVALUE,
                            msrlFeature.getAttribute(OBSV_PARAMETERVALUE));
                    SimpleFeature intersectFeature = featureBuilder
                            .buildFeature(null);
                    intersecFeatureCollection.add(intersectFeature);
                }
            }
        }
        return intersecFeatureCollection;
    }
    
    @LiteralDataInput(identifier = "bewertungsjahr", title = "Bewertungsjahr", abstrakt = "Bewertungsjahr, von dem die durchzufuehrende Bewertung ausgeht.", binding = LiteralStringBinding.class, minOccurs = 1)
    public void setAssessmentYear(String assessmentYear) {
        this.inputAssessmentYear = Integer.parseInt(assessmentYear);
    }

    @ComplexDataInput(identifier = "relevantYears",
            title = "relevantYears.", abstrakt = "None.", binding = IntegerListBinding.class)
    public void relevantYears(final IntegerList in) {
        this.relevantYears = in;
    }

    @ComplexDataInput(identifier = "existingTopographyYears",
            title = "existingTopographyYears.", abstrakt = "None.", binding = IntegerListBinding.class)
    public void setExistingTopographyYears(final IntegerList in) {
        this.existingTopographyYears = in;
    }

    @ComplexDataInput(identifier = "intersectionTidelandsReportingAreasNF",
            title = "intersectionTidelandsReportingAreasNF", abstrakt = "None.", binding = IntersectionFeatureCollectionListBinding.class)
    public void setIntersectionsTidelandReportingAreasNF(final IntersectionFeatureCollectionList in) {
        this.intersectionsTidelandReportingAreasNF = in;
    }

    @ComplexDataInput(identifier = "intersectionTidelandsReportingAreasDI",
            title = "intersectionTidelandsReportingAreasDI", abstrakt = "None.", binding = IntersectionFeatureCollectionListBinding.class)
    public void setIntersectionsTidelandReportingAreasDI(final IntersectionFeatureCollectionList in) {
        this.intersectionsTidelandReportingAreasDI = in;
    }

    @ComplexDataInput(identifier = "relevantSeagras",
            title = "relevantSeagras.", abstrakt = "None.", binding = ObeservationFeatureCollectionListBinding.class)
    public void setRelevantSeagras(final ObservationFeatureCollectionList in) {
        this.relevantSeagras = in;
    }

    @ComplexDataInput(identifier = "relevantAlgea",
            title = "relevantAlgea.", abstrakt = "None.", binding = ObeservationFeatureCollectionListBinding.class)
    public void setRelevantAlgea(final ObservationFeatureCollectionList in) {
        this.relevantAlgea = in;
    }

    @ComplexDataInput(identifier = "reportingAreasNF",
            title = "reportingAreasNF.", abstrakt = "None.", binding = GTVectorDataBinding.class)
    public void setReportingAreasNF(final FeatureCollection<?, ?> in) {
        this.reportingAreasNF = (SimpleFeatureCollection) in;
    }

    @ComplexDataInput(identifier = "reportingAreasDI",
            title = "reportingAreasDI.", abstrakt = "None.", binding = GTVectorDataBinding.class)
    public void setReportingAreasDI(final FeatureCollection<?, ?> in) {
        this.reportingAreasDI = (SimpleFeatureCollection) in;
    }

    @ComplexDataOutput(identifier = "mpbResultGml", title = "Bewertete Berichtsgebiete", abstrakt = "FeatureCollection der bewerteten Berichtsgebiete", binding = GTVectorDataBinding.class)
    public FeatureCollection getResultGml() {
        return this.outputCollection;
    }
    
    @ComplexDataOutput(identifier = "mpbResultXml", title = "XML-Rohdaten Datei", abstrakt = "XML-Datei mit Rohdaten der Bewertung", binding = MPBResultBinding.class)
    public net.disy.wps.lkn.mpa.types.MPBResult getResultXml() {
        return this.result;
    }
}
