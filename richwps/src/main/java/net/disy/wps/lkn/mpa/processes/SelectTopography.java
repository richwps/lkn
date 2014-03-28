package net.disy.wps.lkn.mpa.processes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import net.disy.wps.lkn.mpa.types.IntegerList;
import net.disy.wps.lkn.mpa.types.ObservationFeatureCollection;
import net.disy.wps.lkn.mpa.types.ObservationFeatureCollectionList;
import net.disy.wps.lkn.utils.FeatureCollectionUtil;
import net.disy.wps.lkn.utils.TopographyUtils;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import net.disy.wps.n52.binding.IntegerListBinding;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.joda.time.DateTime;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;

@Algorithm(version = "0.0.1", title = "SelectTopography", abstrakt = ".")
public class SelectTopography extends AbstractAnnotatedAlgorithm {

    private IntegerList relevantYears;
    private SimpleFeatureCollection topography;
    private IntegerList existingTopographyYears;
    private IntegerList relevantTopographyYears;
    private ObservationFeatureCollectionList relevantTopographies;

    public SelectTopography() {
        super();
    }

    @Execute
    public void runMPB() {
        TopographyUtils topoutil = new TopographyUtils(this.topography);
        this.existingTopographyYears = new IntegerList();
        // Vorhandene Topographie-Jahre ermitteln
        existingTopographyYears = topoutil.getExistingTopographyYears();

        // relevantTopoYears: Liste von Jahren, die fuer die Bewertung
        // relevant sind
        this.relevantTopographyYears = new IntegerList();
        // Relevante Topographie-Jahre ermitteln
        this.relevantTopographyYears = this.getRelevantTopoYears(this.existingTopographyYears, relevantYears);

        // relevantTopos: Liste mit ObservationFeatureCollection,
        // die jeweils die FeatureCollection der Topographiegeometrien fuer ein
        // Jahr enthalten
        // Liste von ObservationCollections mit Topographien fuer jedes Jahr
        // erstellen und der relevantTopos hinzufuegen
        this.relevantTopographies = new ObservationFeatureCollectionList();

        for (Integer relevantTopoYear : this.relevantTopographyYears) {
            SimpleFeatureCollection sfc = FeatureCollections.newCollection();
            sfc = topoutil.extractByYear(relevantTopoYear.toString());

            DateTime obsTime = new DateTime(relevantTopoYear, 1, 1, 0, 0);
            Double area = FeatureCollectionUtil.getArea(sfc);
            relevantTopographies.add(new ObservationFeatureCollection(obsTime, sfc, area));
        }
    }

    /**
     * Ermittelt aus einer Liste vorhandener Topographie-Jahre und vorhandener
     * MSRL-Jahr die relevanten Topographie-Jahre
     *
     * @param topoYears
     * @param msrlYears
     * @return
     */
    private IntegerList getRelevantTopoYears(
            IntegerList topoYears, IntegerList msrlYears) {
        IntegerList relTopoYears = new IntegerList();
        HashSet<Integer> hs = new HashSet<Integer>();

        // Schleife ueber MSRL-Years, fuer die jeweils das entsprechende
        // TopoYear bestimmt werden soll
        for (int i = 0; i < msrlYears.size(); i++) {
            relTopoYears.add(this.getTopoYear(msrlYears.get(i), topoYears.getPayload()));
        }
        // Liste auf eindeutige Werte beschraenken
        hs.addAll(relTopoYears.getPayload());
        relTopoYears.clear();
        relTopoYears.addAll(hs);

        return relTopoYears;
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

    @ComplexDataInput(identifier = "topography",
            title = "ingoing topography.", abstrakt = "None.", binding = GTVectorDataBinding.class)
    public void setTopography(final FeatureCollection<?, ?> in) {
        this.topography = (SimpleFeatureCollection) in;
    }

    @ComplexDataInput(identifier = "relevantYears",
            title = "relevantYears.", abstrakt = "None.", binding = IntegerListBinding.class)
    public void setRelevantYears(final IntegerList in) {
        this.relevantYears = in;
    }

    @ComplexDataOutput(identifier = "relevantTopographies",
            title = ".", abstrakt = "None.", binding = net.disy.wps.n52.binding.ObeservationFeatureCollectionListBinding.class)
    public ObservationFeatureCollectionList getRelevantTopos() {
        return this.relevantTopographies;
    }

    @ComplexDataOutput(identifier = "relevantTopographyYears",
            title = ".", abstrakt = "None.", binding = IntegerListBinding.class)
    public IntegerList getRelevantTopographyYears() {
        return this.relevantTopographyYears;
    }

    @ComplexDataOutput(identifier = "existingTopographyYears",
            title = ".", abstrakt = "None.", binding = IntegerListBinding.class)
    public IntegerList getExistingTopographyYears() {
        return this.existingTopographyYears;
    }
}
