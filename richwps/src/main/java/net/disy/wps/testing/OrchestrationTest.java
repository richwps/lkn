package net.disy.wps.testing;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

@Algorithm(
		version = "0.0.5",
		title = "Orchestration Testprozess"
		)
public class OrchestrationTest extends AbstractAnnotatedAlgorithm {

    public OrchestrationTest() {
        super();
    }

    private String message;
    private String outputMessage;
    
    
    @LiteralDataInput(identifier = "dummyMessage", binding = LiteralStringBinding.class)
    public void setMessage(String message) {
    	this.message = message;
    }
    
    @LiteralDataOutput(identifier ="outputMessage", binding = LiteralStringBinding.class)
    public String getString() {
    	return this.outputMessage;
    }
    
    
    @Execute
    public void runPDTest() {
    	this.outputMessage = message;
    	
    	//Orchestrator or = new Orchestrator();
    
    	    	
    }
}