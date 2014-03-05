package net.disy.wps.lkn.utils;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import net.disy.wps.common.DescriptorContainer;
import net.disy.wps.lkn.utils.FeatureCollectionUtil;
import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollection;
import net.disy.wps.lkn.mpa.types.MPBResult;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 *
 * @author dalcacer
 */
public class MPAUtils {

    public static final int NORDFRIESLAND = 1;
    public static final int DITHMARSCHEN = 2;
    public static final String NORDFRIESLAND_NAME = "Nordfriesland";
    public static final String DITHMARSCHEN_NAME = "Dithmarschen";

    public static final String OBSV_PARAMETERVALUE = "OBSV_PARAMETERVALUE";

    private TopographyUtils topgraphy;
    private ReportingAreaUtils reportingareas;
    private MSRLD5Utils msrld5;

    public MPAUtils(TopographyUtils topgraphy, ReportingAreaUtils reportingareas, MSRLD5Utils msrld5) {
        this.topgraphy = topgraphy;
        this.reportingareas = reportingareas;
        this.msrld5 = msrld5;
    }

    /**
     * Verschneidet eine SimpleFeatureCollection von Berichtsgebieten mit einer
     * SimpleFeatureCollection von Topographien eines Jahres. Die
     * zurueckgegebene SimpleFeatureCollection enthaelt alle Attribute der
     * Topographien und die Angaben 'DIST' und 'Name' der Berichtsgebiete
     *
     * @param reportingareas - SimpleFeatureCollection von Berichtsgebieten
     * @param topoFc - SimpleFeatureCollection von Topographien eines Jahres
     * @return SimpleFeatureCollection des Verschneidungs-Ergebnisses
     */
    public static SimpleFeatureCollection intersectReportingsareasAndTidelands(
            SimpleFeatureCollection reportingareas, SimpleFeatureCollection tidelands) {

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
         * Verschneidung der BoundingBoxen zum Filtern der fuer die folgende
         * Verschneidung relevanten Features
         */
        // Iterator ueber Features der Berichtsgebiete
        iterA = reportingareas.features();
        try {
            while (iterA.hasNext()) {
                berichtsFeature = iterA.next();
                berichtsBB = ((Geometry) berichtsFeature.getDefaultGeometry())
                        .getEnvelope();

                iterB = tidelands.features();
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

        // Liste mit zusaetzlichen Deskriptoren fuer die zu erzeugenden Features
        // erstellen
        ArrayList<DescriptorContainer> dcList = new ArrayList<DescriptorContainer>();
        dcList.add(new DescriptorContainer(1, 1, false, "DISTR", String.class));
        dcList.add(new DescriptorContainer(1, 1, false, "NAME", String.class));
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(
                FeatureCollectionUtil.refactorFeatureType(
                        (SimpleFeatureType) tidelands.getSchema(), dcList,
                        "BerichtsgebieteTopographieIntersection", null));

        // ArrayLists aus HashSets befuellen
        relBFeat.addAll(relBFeatHs);
        relWFeat.addAll(relWFeatHs);
        // Schleife ueber Berichtsgebiete
        for (int i = 0; i < relBFeat.size(); i++) {
            berichtsFeature = relBFeat.get(i);
            berichtsGeom = (Geometry) berichtsFeature.getDefaultGeometry();

            // Schleife ueber Wattflaechen
            for (int j = 0; j < relWFeat.size(); j++) {
                wattFeature = relWFeat.get(j);
                wattGeom = (Geometry) wattFeature.getDefaultGeometry();

                intersec = berichtsGeom.intersection(wattGeom);
                // Wenn die Intersection-Geometry nicht leer ist...
                if (!intersec.isEmpty()) {
                    featureBuilder = FeatureCollectionUtil.initBuilderValues(featureBuilder,
                            wattFeature);
                    featureBuilder.set(tidelands.getSchema()
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
     * Ermittelt aus einer Liste vorhandener Topographie-Jahre und vorhandener
     * MSRL-Jahr die relevanten Topographie-Jahre
     *
     * @param topoYears
     * @param msrlYears
     * @return
     */
    public ArrayList<Integer> getRelevantTopoYears(
            ArrayList<Integer> topoYears, ArrayList<Integer> msrlYears) {
        ArrayList<Integer> relTopoYears = new ArrayList<Integer>();
        HashSet<Integer> hs = new HashSet<Integer>();

        // Schleife ueber MSRL-Years, fuer die jeweils das entsprechende
        // TopoYear bestimmt werden soll
        for (int i = 0; i < msrlYears.size(); i++) {
            relTopoYears.add(this.getTopoYear(msrlYears.get(i), topoYears));
        }
        // Liste auf eindeutige Werte beschraenken
        hs.addAll(relTopoYears);
        relTopoYears.clear();
        relTopoYears.addAll(hs);

        return relTopoYears;
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

    /**
     * Gibt eine durch ein Bewertungsgebiet und ein Jahr spezifizierte
     * Intersection-Collection aus einer Liste zurueck
     *
     * @param intersecCollList
     * @param year
     * @param gebiet
     * @return
     */
    public static IntersectionFeatureCollection getIntersecCollByYear(
            ArrayList<IntersectionFeatureCollection> intersecCollList, Integer year) {
        IntersectionFeatureCollection intersecColl = null;

        for (int j = 0; j < intersecCollList.size(); j++) {
            if (intersecCollList.get(j).getDateTime().getYear() == year) {
                intersecColl = intersecCollList.get(j);
            }
        }
        return intersecColl;
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
     * Erzeugt aus den Eingabe-FeatureCollections fuer die Ausgangsgeometrien
     * der beiden Berichtsgebiete NF und DI zwei verschmolzene Polygone mit
     * Bewertungsparametern
     *
     * @param result -
     * @param nfCollection - SimpleFeatureCollection Berichtsgebiet
     * Nordfriesland
     * @param diCollection - SimpleFeatureCollection Berichtsgebiet Dithmarschen
     * @return SimpleFeatureCollection der bewerteten Berichtsgebiete
     */
    public static
            SimpleFeatureCollection getEvaluatedAreas(final MPBResult result,
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
        ftBuilder.add(ReportingAreaUtils.ATTRIB_DISTR, String.class);
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
        featureBuilder.set(ReportingAreaUtils.ATTRIB_DISTR, ReportingAreaUtils.ATTRIB_DISTR_NF);
        featureBuilder.set("MPBMeanEQR",
                result.getAreaResult(MPAUtils.NORDFRIESLAND).getMeanEQR()
                .toString());
        featureBuilder.set("MPBEvalStringEQR",
                result.getAreaResult(MPAUtils.NORDFRIESLAND)
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
        featureBuilder.set(ReportingAreaUtils.ATTRIB_DISTR, ReportingAreaUtils.ATTRIB_DISTR_DI);
        featureBuilder.set("MPBMeanEQR",
                result.getAreaResult(MPAUtils.DITHMARSCHEN).getMeanEQR()
                .toString());
        featureBuilder.set("MPBEvalStringEQR",
                result.getAreaResult(MPAUtils.DITHMARSCHEN)
                .getMeanEQREvalString());
        resultCollection.add(featureBuilder.buildFeature(null));
        geomCollection.clear();

        return resultCollection;
    }

}
