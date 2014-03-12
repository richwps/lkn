package net.disy.wps.lkn.mpa.processes;

import java.io.File;
import net.disy.wps.lkn.mpa.types.IntegerList;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import net.disy.wps.n52.binding.IntegerListBinding;

@Algorithm(version = "0.0.1", title = "IntegerListTest", abstrakt = ".")
public class IntegerListTest extends AbstractAnnotatedAlgorithm {

    private IntegerList input;
    private File output;

    
    /**
     * Constructs a new WPS-Process MacrophyteAssesment.
     */
    public IntegerListTest() {
        super();
        //LOGGER.setLevel(Level.ALL);
    }

    @Execute
    public void runMPB() {
        System.err.println(this.input.get(1));
        
        /*File f = this.input.persist();
        this.output=f;*/
    }

    @ComplexDataInput(identifier = "theinput", title = "theinput", abstrakt = "None.", binding = IntegerListBinding.class)
    public void setInput(IntegerList in) {
        this.input = in;
    }

    @ComplexDataOutput(identifier = "theoutput", title = "theoutput", abstrakt = "None.", binding = IntegerListBinding.class)
    public IntegerList getOutput() {
        return this.input;
    }
}
