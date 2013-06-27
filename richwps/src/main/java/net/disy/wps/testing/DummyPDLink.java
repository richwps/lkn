package net.disy.wps.testing;

import net.disy.ogc.wps.v_1_0_0.util.WpsUtils;
import net.disy.ogc.wpspd.v_1_0_0.LinkType;
import net.disy.ogc.wpspd.v_1_0_0.ObjectFactory;
import net.disy.ogc.wpspd.v_1_0_0.WpspdUtils;
import net.disy.wps.n52.binding.PDLinkBinding;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

@Algorithm(
		version = "1.0.0",
		title = "PDLink Testprozess",
		abstrakt ="Das ist ein PDLink-Testprozess auf einem 52North WPS-Server"
		)
public class DummyPDLink extends AbstractAnnotatedAlgorithm {

    public DummyPDLink() {
        super();
    }
    private LinkType outputPDLink;
    
    private final ObjectFactory objectFactory = WpspdUtils.createObjectFactory();
    
    @ComplexDataOutput(identifier ="PDLink", title="PDLinkOutput", abstrakt="Das ist ein pd:Link!", binding = PDLinkBinding.class)
    public LinkType getPDLink() {
    	return this.outputPDLink;
    }
    
    @Execute
    public void runPDTest() {
    	
    	final LinkType link = objectFactory.createLinkType();

    	link.setPresentationDirectiveTitle(WpsUtils.createLanguageStringType("Das ist ein pd:Link Test"));
    	link.setAbstract(WpsUtils.createLanguageStringType("Beschreibung des pd:Links"));
    	link.setHref("http://www.52north.org/");
    	link.setType("simple");
    	link.setShow("new");
    	
    	this.outputPDLink = link;
    }
}