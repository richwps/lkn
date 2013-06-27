package net.disy.wps.testing;

import net.disy.ogc.wps.v_1_0_0.util.WpsUtils;
import net.disy.ogc.wpspd.v_1_0_0.MessageType;
import net.disy.ogc.wpspd.v_1_0_0.ObjectFactory;
import net.disy.ogc.wpspd.v_1_0_0.WpspdUtils;
import net.disy.wps.n52.binding.PDMessageBinding;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

@Algorithm(
		version = "1.0.0",
		title = "PDMessage Testprozess",
		abstrakt ="Das ist ein PDMessage-Testprozess auf einem 52North WPS-Server"
		)
public class DummyPDMessage extends AbstractAnnotatedAlgorithm {

    public DummyPDMessage() {
        super();
    }
    private MessageType outputPDMessage;
    
    private final ObjectFactory objectFactory = WpspdUtils.createObjectFactory();
    
    @ComplexDataOutput(identifier ="PDMessage", title="PDMessageOutput", abstrakt="Das ist eine pd:Message!", binding = PDMessageBinding.class)
    public MessageType getPDMessage() {
    	return this.outputPDMessage;
    }
    
    @Execute
    public void runPDMessageTest() {
    	
    	final MessageType message = objectFactory.createMessageType();

    	message.setPresentationDirectiveTitle(WpsUtils.createLanguageStringType("Das ist ein pd:Message Test"));
    	message.setAbstract(WpsUtils.createLanguageStringType("Beschreibung der pd:Message"));
    	message.setContent("Dummy Error: Something went wrong with your 52North WPS-Server!");
    	message.setType("error");

    	this.outputPDMessage = message;
    }
}