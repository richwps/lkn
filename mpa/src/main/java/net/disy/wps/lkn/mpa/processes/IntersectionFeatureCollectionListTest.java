package net.disy.wps.lkn.mpa.processes;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollectionList;
import net.disy.wps.n52.binding.IntersectionFeatureCollectionListBinding;

@Algorithm(version = "0.0.1", title = "IntersectionFeatureCollectionListTest", abstrakt = ".")
/**
 * ObservationFeatureCollectionListTest for testing parsers and generators.
 */
public class IntersectionFeatureCollectionListTest extends AbstractAnnotatedAlgorithm {

    private IntersectionFeatureCollectionList input;
    private IntersectionFeatureCollectionList output;

    /**
     * Constructs a new WPS-Process ObservationFeatureCollectionListTest.
     */
    public IntersectionFeatureCollectionListTest() {
        super();
    }

    @Execute
    public void runMPB() {
        System.out.println("Entering test." + this.getClass().getCanonicalName());
        int size = this.input.getPayload().size();
        System.out.println("IntersectionFeatureCollectionListTest incoming "
                + "collection has " + size + " elements.");
        size = this.input.getPayload().get(0).getFeatureCollection().size();
        System.out.println("IntersectionFeatureCollectionListTest incoming "
                + "simplefeaturecollection has " + size + " elements.");
        this.output = this.input;
        System.out.println("Exiting test." + this.getClass().getCanonicalName());
    }

    @ComplexDataInput(identifier = "inputcollection",
            title = "Ein Testinput.", abstrakt = "None.",
            binding = IntersectionFeatureCollectionListBinding.class)
    public void setInput(IntersectionFeatureCollectionList in) {
        this.input = in;
    }

    @ComplexDataOutput(identifier = "outputcollection",
            title = "Ein Testoutput.", abstrakt = "None.",
            binding = IntersectionFeatureCollectionListBinding.class)
    public IntersectionFeatureCollectionList getOutput() {
        return this.output;
    }
}
