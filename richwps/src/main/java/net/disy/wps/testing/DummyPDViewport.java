package net.disy.wps.testing;

import java.util.Arrays;

import net.disy.ogc.wps.v_1_0_0.util.WpsUtils;
import net.disy.ogc.wpspd.v_1_0_0.ObjectFactory;
import net.disy.ogc.wpspd.v_1_0_0.ViewportType;
import net.disy.ogc.wpspd.v_1_0_0.WpspdUtils;
import net.disy.wps.n52.binding.PDViewportBinding;
import net.opengis.gml.v_3_1_1.EnvelopeType;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

@Algorithm(
		version = "1.0.0",
		title = "PDViewport Testprozess",
		abstrakt ="Das ist ein PDViewport-Testprozess auf einem 52North WPS-Server"
		)
public class DummyPDViewport extends AbstractAnnotatedAlgorithm {

    public DummyPDViewport() {
        super();
    }
    private ViewportType outputPDViewport;
    
    private final ObjectFactory objectFactory = WpspdUtils.createObjectFactory();
    final net.opengis.gml.v_3_1_1.ObjectFactory gmlObjectFactory = new net.opengis.gml.v_3_1_1.ObjectFactory();
    
    @ComplexDataOutput(identifier ="PDViewport", abstrakt="Das ist ein PDViewport", binding = PDViewportBinding.class)
    public ViewportType getPDViewport() {
    	return this.outputPDViewport;
    }
    
    @Execute
    public void runPDViewportTest() {
    	
    	final ViewportType viewport = objectFactory.createViewportType();
    	final EnvelopeType envelope = gmlObjectFactory.createEnvelopeType();

    	// Set properties for PD-Viewport
    	viewport.setPresentationDirectiveTitle(WpsUtils.createLanguageStringType("Das ist ein PDViewport Test"));
    	viewport.setAbstract(WpsUtils.createLanguageStringType("Das ist die Beschreibung des PDViewports"));
    	// Assign envelope to PD-Viewport
    	viewport.setEnvelope(gmlObjectFactory.createEnvelope(envelope));
    	// Set properties for Envelope
        envelope.setLowerCorner(gmlObjectFactory.createDirectPositionType());
        envelope.setUpperCorner(gmlObjectFactory.createDirectPositionType());
        envelope.setSrsName("urn:ogc:def:crs:EPSG::31467"); //$NON-NLS-1$
        envelope.getLowerCorner().setValue(Arrays.<Double> asList(3450000.0, 5850000.0));
        envelope.getUpperCorner().setValue(Arrays.<Double> asList(3460000.0, 5670000.0));
    	
    	this.outputPDViewport = viewport;
    }
}