package net.disy.wps.testing;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

@Algorithm(
		version = "1.1.0",
		title = "Multiplikationsergebnis schreiben",
		abstrakt = "Dieser Prozess erzeugt aus dem Ergebnis einer Multiplikation eine Aussage."
		)
public class MultiplicationString extends AbstractAnnotatedAlgorithm {

    public MultiplicationString() {
        super();
    }

    private Double inputNumber;
    private String outputString;
    
    @LiteralDataInput(identifier = "multiErgebnis", title = "Multiplikationsergebnis", abstrakt = "Ergebnis der Multiplikation aus dem MultiplicationCalc-Prozess",  binding = LiteralDoubleBinding.class)
    public void setNumber(Double inputNumber) {
    	this.inputNumber = inputNumber;
    }
    
    @LiteralDataOutput(identifier = "outputString", title = "Ergebnis-Aussage", abstrakt = "Aussage über das Ergebnis der Multiplikation", binding = LiteralStringBinding.class)
    public String getErgebnis() {
        return outputString;
    }
    
    @Execute
    public void runMultiplication() {
    	this.outputString = "Das Ergebnis der Multiplikation lautet: " + this.inputNumber.toString();
    }
}