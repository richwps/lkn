package net.disy.wps.lkn.mpa.processes;


import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import net.disy.wps.lkn.mpa.types.ObservationFeatureCollectionList;
import net.disy.wps.n52.binding.ObeservationFeatureCollectionListBinding;

@Algorithm(version = "0.0.1", title = "ObservationFeatureCollectionListTest", abstrakt = ".")
public class ObservationFeatureCollectionListTest extends AbstractAnnotatedAlgorithm {

    private ObservationFeatureCollectionList input;
    
    /**
     * Constructs a new WPS-Process MacrophyteAssesment.
     */
    public ObservationFeatureCollectionListTest() {
        super();
    }

    @Execute
    public void runMPB() {
        System.out.println(this.input.getPayload().size());
    }

    @ComplexDataInput(identifier = "inputcollection",
            title = "Ein Testinput.", abstrakt = "None.", binding = ObeservationFeatureCollectionListBinding.class)
    public void setInput(ObservationFeatureCollectionList in) {
        this.input = in;
    }

    @ComplexDataOutput(identifier = "outputcollection",
            title = "Ein Testoutput.", abstrakt = "None.", binding = ObeservationFeatureCollectionListBinding.class)
    public ObservationFeatureCollectionList getOutput() {
        return this.input;
    }
}
