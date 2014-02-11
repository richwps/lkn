package net.disy.wps.lkn.utils;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import net.disy.wps.common.DescriptorContainer;
import net.disy.wps.lkn.processes.mpa.types.IntersectionCollection;
import net.disy.wps.lkn.processes.mpa.types.ObservationCollection;
import static net.disy.wps.lkn.utils.MSRLD5Utils.ATTRIB_OBSV_PHENOMENONTIME;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 *
 * @author dalcacer
 */
public class MPAUtils {

    public static final String OBSV_PARAMETERVALUE = "OBSV_PARAMETERVALUE";

            private static final DateTimeFormatter DateTimeFormatter = DateTimeFormat
            .forPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DateTimeFormatter4TimeStamp = DateTimeFormat
            .forPattern("yyyy-MM-dd HH:mm:ss.S");
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
    
    
    /**
     * Gibt eine Liste von ObservationCollections zurueck, die ein Element fuer
     * jeden Eintrag in der uebergebenen Liste mit Beobachtungszeitpunkten
     * enthaelt Returns an Array of ObservationsCollections. The list contains
     * entries for each DateTime including the corresponding SimpleFeatures in a
     * collection and the total area
     *
     * @param sfc
     * @param obsDates
     * @return Liste mit ObservationCollections
     */
    public static ArrayList<ObservationCollection> getObsCollByDateList(
            SimpleFeatureCollection sfc, ArrayList<DateTime> obsDates) {
        String compareStr;
        Double area;
        SimpleFeatureCollection groupCollection;
        ArrayList<ObservationCollection> obsCollections = new ArrayList<ObservationCollection>();

        // Schleife ueber die Beobachtungszeitpunkte
        for (int i = 0; i < obsDates.size(); i++) {
            groupCollection = FeatureCollections.newCollection();
            // String zum Vergleich von Beobachtungszeitpunkten erzeugen
            if (FeatureCollectionUtil.attributeIsTimeStamp(sfc, ATTRIB_OBSV_PHENOMENONTIME)) {
                compareStr = obsDates.get(i).toString(
                        DateTimeFormatter4TimeStamp);
            } else {
                compareStr = obsDates.get(i).toString(DateTimeFormatter);
            }
            // Entsprechende SimpleFeatureCollection aus der gesamten
            // FeatureCollection extrahieren
            groupCollection = FeatureCollectionUtil.extract(sfc,
                    new String[]{ATTRIB_OBSV_PHENOMENONTIME},
                    new String[]{compareStr});
            // Gesamtflaeche der Features berechnen
            area = FeatureCollectionUtil.getArea(groupCollection);
            // ObservationCollection erzeugen und der Ausgabe-Liste hinzufuegen
            obsCollections.add(new ObservationCollection(obsDates.get(i),
                    groupCollection, area));
        }

        // Debug
        for (ObservationCollection obsColl : obsCollections) {
            /*LOGGER.debug("getObsCollByDateList: "
             + obsColl.getDateTime().getYear() + "-"
             + obsColl.getDateTime().getMonthOfYear() + "-"
             + obsColl.getDateTime().getDayOfMonth() + ": "
             + FeatureCollectionUtil.tokm2Str(obsColl.getArea()) + " km2");*/
        }

        return obsCollections;
    }

    /**
     * Ermittelt zu aus einer Liste von ObservationCollectiond und einer
     * Jahres-Angabe den Index des entsprechenden Elements falls das Jahr
     * kleiner ist als die vorhanden Jahre in der Liste wird eine
     * RuntimeException ausgeloest Falls das Jahr groesser ist als die
     * vorhandene Jahre in der Liste wird der Index des zeitlich
     * naechstliegenden Jahres zurueckgegeben
     *
     * @param obsCollList - Liste mit allen ObservationCollections
     * @param year - Jahr
     * @return Index
     */
    public static Integer getIdxOfObsCollbyYear(
            ArrayList<ObservationCollection> obsCollList, Integer year) {

        Integer i = 0;
        // sort descending by DateTime of ObservationCollection
        Collections.sort(obsCollList, Collections.reverseOrder());
        Integer listYear = obsCollList.get(i).getDateTime().getYear();
        // Schleife, bis aktuelles Jahr aus der Liste nicht mehr kleier als das
        // Bewertungsjahr ist
        do {
            // RuntimException, wenn die Anzahl der Schleifendurchlaeufe die
            // Groesse der Liste ueberschreitet
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
     * Liefert eine durch ein Jahr bestimme ObservationCollection aus einer
     * Liste von ObservationCollections
     *
     * @param obsCollList - Liste von ObservationCollections
     * @param year - Jahr, zu dem die ObservationCollection zurueckgegeben
     * werden soll
     * @return ObservationCollection
     */
    public static ObservationCollection getObsCollByYear(
            ArrayList<ObservationCollection> obsCollList, Integer year) {
        ObservationCollection obsColl = null;

        for (int j = 0; j < obsCollList.size(); j++) {
            if (obsCollList.get(j).getDateTime().getYear() == year) {
                obsColl = obsCollList.get(j);
            }
        }
        return obsColl;
    }

    /**
     * Versucht aus einer Liste von ObservationCollections (MSRL-Daten) die
     * sechs fuer das Bewertungsjahr relevanten Beobachtungssammlungen zu
     * ermitteln und zurueck zu geben. Falls vom Bewertungsjahr ausgehend keine
     * sechs ObservationCollections verfuegbar sind, wird eine RuntimeException
     * ausgeloest.
     *
     * @param obsCollections - Liste von ObservationCollections fuer jeden
     * Beobachtungszeitpunkt
     * @param bewertungsjahr
     * @return Liste mit den sechs relevanten ObservationCollections
     */
    public static ArrayList<ObservationCollection> getRelevantObservationCollections(
            ArrayList<ObservationCollection> obsCollections,
            Integer bewertungsjahr) {
        ArrayList<ObservationCollection> preSelCollections = new ArrayList<ObservationCollection>();
        ArrayList<ObservationCollection> finalCollections = new ArrayList<ObservationCollection>();
        HashSet<Integer> existingYears = new HashSet<Integer>();

        // Schleife ueber alle ObservationCollections
        for (int i = 0; i < obsCollections.size(); i++) {
            ObservationCollection obsColl = obsCollections.get(i);

            // Pruefen, ob die Liste der ausgewaehlten ObservationCollections
            // Eintraege enthaelt
            if (preSelCollections.size() > 0) {
                // ueber bestehende Eintraege in selCollections iterieren,
                // um Jahr und Flaeche der neuen obsColl zu vergleichen
                Integer initSize = preSelCollections.size();
                for (int j = 0; j < initSize; j++) {
                    ObservationCollection selColl = preSelCollections.get(j);
                    // Wenn unter den schon selektierten Sammlungen eine mit
                    // gleichem Jahr und kleinerer Flaeche ist...
                    if (selColl.getDateTime().getYear() == obsColl
                            .getDateTime().getYear()) {
                        if (selColl.getArea() < obsColl.getArea()) {
                            // ...dann wird diese geloescht...
                            preSelCollections.remove(selColl);
                            // ...und die neue hinzugefuegt
                            preSelCollections.add(obsColl);
                        }
                    } // Wenn Jahre unterschiedlich, muss geprueft werden, ob es
                    // schon einen Eintrag mit dem Jahr gibt. Falls nicht, kann
                    // die Sammlung hinzugefuegt werden.
                    else if (!existingYears.contains(obsColl.getDateTime()
                            .getYear())) {
                        preSelCollections.add(obsColl);
                        existingYears.add(obsColl.getDateTime().getYear());
                    }
                }
            } // Falls die Liste noch leer ist, wird die erste
            // Beobachtungssammlung hinzugefuegt
            else {
                preSelCollections.add(obsColl);
                // Das Jahr des hinzugefuegten Eintrags in die Liste der bereits
                // uebernommenen Jahre hinzufuegen
                existingYears.add(obsColl.getDateTime().getYear());
            }
        }
        // StartIndex ermitteln
        Integer startIndex = MPAUtils.getIdxOfObsCollbyYear(preSelCollections,
                bewertungsjahr);
        try {
            for (int i = 0; i < 6; i++) {
                finalCollections.add(preSelCollections.get(startIndex + i));
            }
        } catch (Exception e) {
            // RuntimeException ausloesen, falls keine sechs
            // ObservationCollections vorhanden
            throw new RuntimeException(
                    "There is not enough data for 6 years available.");
        }
        // Debug
        /*for (int k = 0; k < preSelCollections.size(); k++) {
             LOGGER.debug("getRelevantFeatureCollections: "
             + preSelCollections.get(k).getDateTime().getYear() + "-"
             + preSelCollections.get(k).getDateTime().getMonthOfYear()
             + "-"
             + preSelCollections.get(k).getDateTime().getDayOfMonth()
             + " ausgewaehlt: "
             + FeatureCollectionUtil.tokm2Str(preSelCollections.get(k).getArea()));
        }*/
        return finalCollections;
    }

}
