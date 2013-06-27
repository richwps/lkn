package net.disy.wps.testing;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

@Algorithm(
		version = "1.1.0",
		title = "Multiplikation berechnen",
		abstrakt = "Dieser Prozess multipliziert zwei Zahlen miteinander."		
		)
public class MultiplicationCalc extends AbstractAnnotatedAlgorithm {

    public MultiplicationCalc() {
        super();
    }

    private Double zahl1, zahl2;
    private Double ergebnis;
    
    @LiteralDataInput(identifier = "zahl1", title = "Zahl 1", abstrakt = "Erste Zahl", binding = LiteralDoubleBinding.class)
    public void setZahl1(Double zahl1) {
    	this.zahl1 = zahl1;
    }
    
    @LiteralDataInput(identifier = "zahl2", title = "Zahl 2", abstrakt = "Zweite Zahl", binding = LiteralDoubleBinding.class)
    public void setZahl2(Double zahl2) {
    	this.zahl2 = zahl2;
    }
    
    @LiteralDataOutput(identifier = "multiErgebnis", title = "Multiplikationsergebnis", abstrakt = "Ergebnis der Multiplikation zweier Zahlen", binding = LiteralDoubleBinding.class)
    public Double getErgebnis() {
        return ergebnis;
    }

    @Execute
    public void runMultiplication() {
    	this.ergebnis = this.zahl1 * this.zahl2;
    }
}