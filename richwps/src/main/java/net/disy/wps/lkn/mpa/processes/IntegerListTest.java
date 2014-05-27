package net.disy.wps.lkn.mpa.processes;

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
    
    /**
     * Constructs a new WPS-Process MacrophyteAssesment.
     */
    public IntegerListTest() {
        super();
    }

    @Execute
    public void runMPB() {
        System.err.println(this.input.getArray());
        System.err.println(this.input.get(1));
    }

    @ComplexDataInput(identifier = "inputyears",
            title = "Ein Testinput.", abstrakt = "None.", binding = IntegerListBinding.class, minOccurs = 1, maxOccurs = 1)
    public void setInput(IntegerList in) {
        this.input = in;
    }

    @ComplexDataOutput(identifier = "outputyears",
            title = "Ein Testoutput.", abstrakt = "None.", binding = IntegerListBinding.class)
    public IntegerList getOutput() {
        return this.input;
    }
}
