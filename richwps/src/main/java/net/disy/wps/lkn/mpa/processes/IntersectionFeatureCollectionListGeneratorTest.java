package net.disy.wps.lkn.mpa.processes;

import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollection;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollectionList;
import net.disy.wps.n52.binding.IntersectionFeatureCollectionListBinding;

import net.disy.wps.lkn.mpa.types.ObservationFeatureCollection;
import net.disy.wps.lkn.mpa.types.ObservationFeatureCollectionList;
import net.disy.wps.lkn.utils.FeatureCollectionUtil;
import net.disy.wps.n52.binding.ObeservationFeatureCollectionListBinding;
import org.joda.time.DateTime;

@Algorithm(version = "0.0.1", title = "IntersectionFeatureCollectionListGeneratorTest", abstrakt = ".")
/**
 * ItnersectionFeatureCollectionListTest for testing-purpose only.
 *
 * @author dalcacer
 */
public class IntersectionFeatureCollectionListGeneratorTest extends AbstractAnnotatedAlgorithm {

    private ObservationFeatureCollectionList input;
    private IntersectionFeatureCollectionList output;

    /**
     * Constructs a new WPS-Process IntersectionFeatureCollectionListTest.
     */
    public IntersectionFeatureCollectionListGeneratorTest() {
        super();
    }

    @Execute
    public void runMPB() {
        System.out.println("Entering test." + this.getClass().getCanonicalName());
        IntersectionFeatureCollectionList tmp = new IntersectionFeatureCollectionList();
        final DateTime obsTime = new DateTime(2010, 1, 1, 0, 0);
        System.out.println("Iterating input." + this.getClass().getCanonicalName());
        for (ObservationFeatureCollection obs : this.input) {
            System.out.println("Creating IFC." + this.getClass().getCanonicalName());
            final Double area = FeatureCollectionUtil.getArea(obs.getFeatureCollection());
            IntersectionFeatureCollection isfc = new IntersectionFeatureCollection(1, obsTime, obs.getFeatureCollection(), area);
            tmp.add(isfc);
        }
        this.output = tmp;
        System.out.println("Exiting test." + this.getClass().getCanonicalName());
    }

    @ComplexDataInput(identifier = "inputcollection",
            title = "Ein Testinput.", abstrakt = "None.",
            binding = ObeservationFeatureCollectionListBinding.class)
    public void setInput(ObservationFeatureCollectionList in) {
        this.input = in;
    }

    @ComplexDataOutput(identifier = "outputcollection",
            title = "Ein Testoutput.", abstrakt = "None.",
            binding = IntersectionFeatureCollectionListBinding.class)
    public IntersectionFeatureCollectionList getOutput() {
        return this.output;
    }
}
