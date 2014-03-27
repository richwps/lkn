package net.disy.wps.lkn.mpa.processes;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import net.disy.wps.lkn.mpa.types.ObservationFeatureCollectionList;
import net.disy.wps.n52.binding.ObeservationFeatureCollectionListBinding;

@Algorithm(version = "0.0.1", title = "ObservationFeatureCollectionListTest", abstrakt = ".")
/**
 * ObservationFeatureCollectionListTest for testing parsers and generators.
 */
public class ObservationFeatureCollectionListTest extends AbstractAnnotatedAlgorithm {

    private ObservationFeatureCollectionList input;
    private ObservationFeatureCollectionList output;

    /**
     * Constructs a new WPS-Process ObservationFeatureCollectionListTest.
     */
    public ObservationFeatureCollectionListTest() {
        super();
    }

    @Execute
    public void runMPB() {
        System.out.println("Entering test." + this.getClass().getCanonicalName());
        int size = this.input.getPayload().size();
        System.out.println("ObservationFeatureCollectionListTest incoming "
                + "collection has " + size + " elements.");
        size = this.input.getPayload().get(0).getFeatureCollection().size();
        System.out.println("ObservationFeatureCollectionListTest incoming "
                + "simplefeaturecollection has " + size + " elements.");
        this.output = this.input;
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
            binding = ObeservationFeatureCollectionListBinding.class)
    public ObservationFeatureCollectionList getOutput() {
        return this.output;
    }
}
