/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.disy.wps.lkn.utils;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import java.util.ArrayList;
import java.util.HashSet;
import net.disy.wps.common.DescriptorContainer;
import net.disy.wps.lkn.processes.mpa.types.IntersectionCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 *
 * @author dalcacer
 */
public class MPAUtils {

    /**
     * Verschneidet eine SimpleFeatureCollection von Berichtsgebieten mit einer
     * SimpleFeatureCollection von Topographien eines Jahres. Die
     * zurueckgegebene SimpleFeatureCollection enthaelt alle Attribute der
     * Topographien und die Angaben 'DIST' und 'Name' der Berichtsgebiete
     *
     * @param berichtsFc - SimpleFeatureCollection von Berichtsgebieten
     * @param topoFc - SimpleFeatureCollection von Topographien eines Jahres
     * @return SimpleFeatureCollection des Verschneidungs-Ergebnisses
     */
    public static SimpleFeatureCollection intersectBerichtsgebieteAndTopography(
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
         * Verschneidung der BoundingBoxen zum Filtern der fuer die folgende
         * Verschneidung relevanten Features
         */
        // Iterator ueber Features der Berichtsgebiete
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

        // Liste mit zusaetzlichen Deskriptoren fuer die zu erzeugenden Features
        // erstellen
        ArrayList<DescriptorContainer> dcList = new ArrayList<DescriptorContainer>();
        dcList.add(new DescriptorContainer(1, 1, false, "DISTR", String.class));
        dcList.add(new DescriptorContainer(1, 1, false, "NAME", String.class));
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(
                FeatureCollectionUtil.refactorFeatureType(
                        (SimpleFeatureType) wattFc.getSchema(), dcList,
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
     * Ermittelt aus einer Liste vorhandener Topographie-Jahre und vorhandener
     * MSRL-Jahr die relevanten Topographie-Jahre
     *
     * @param topoYears
     * @param msrlYears
     * @return
     */
    public static ArrayList<Integer> getRelevantTopoYears(
            ArrayList<Integer> topoYears, ArrayList<Integer> msrlYears) {
        ArrayList<Integer> relTopoYears = new ArrayList<Integer>();
        HashSet<Integer> hs = new HashSet<Integer>();

        // Schleife ueber MSRL-Years, fuer die jeweils das entsprechende
        // TopoYear bestimmt werden soll
        for (int i = 0; i < msrlYears.size(); i++) {
            relTopoYears.add(TopographyUtils.getTopoYear(msrlYears.get(i), topoYears));
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
     * Gibt eine durch ein Bewertungsgebiet und ein Jahr spezifizierte
     * Intersection-Collection aus einer Liste zurueck
     *
     * @param intersecCollList
     * @param year
     * @param gebiet
     * @return
     */
    public static IntersectionCollection getIntersecCollByYearAndGebiet(
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

}
