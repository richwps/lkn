package net.disy.wps.testing;

import java.util.Arrays;

import javax.xml.bind.JAXBElement;

import net.disy.ogc.wps.v_1_0_0.util.WpsUtils;
import net.disy.ogc.wpspd.v_1_0_0.FeatureTypeDescriptionType;
import net.disy.ogc.wpspd.v_1_0_0.FeaturesType;
import net.disy.ogc.wpspd.v_1_0_0.Geometry;
import net.disy.ogc.wpspd.v_1_0_0.GeometryType;
import net.disy.ogc.wpspd.v_1_0_0.GroupType;
import net.disy.ogc.wpspd.v_1_0_0.Hints;
import net.disy.ogc.wpspd.v_1_0_0.LinkType;
import net.disy.ogc.wpspd.v_1_0_0.MarkerType;
import net.disy.ogc.wpspd.v_1_0_0.MessageType;
import net.disy.ogc.wpspd.v_1_0_0.ObjectFactory;
import net.disy.ogc.wpspd.v_1_0_0.StyledFeatureCollectionType;
import net.disy.ogc.wpspd.v_1_0_0.ViewportType;
import net.disy.ogc.wpspd.v_1_0_0.WpspdUtils;
import net.opengis.gml.v_3_1_1.AbstractGeometryType;
import net.opengis.gml.v_3_1_1.EnvelopeType;
import net.opengis.se.v_1_1_0.PointSymbolizerType;
import net.opengis.se.v_1_1_0.SymbolizerType;
import net.disy.wps.n52.binding.*;

import org.jvnet.ogc.gml.v_3_1_1.jts.GML311ToJTSConstants;
import org.jvnet.ogc.gml.v_3_1_1.jts.JTSToGML311GeometryConverter;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

@Algorithm(
		version = "0.0.5",
		title = "WPS-PD Testprozess"
		)
public class DummyPDTest extends AbstractAnnotatedAlgorithm {

    public DummyPDTest() {
        super();
    }

    private String message;
    private MessageType outputPDMessage;
    private LinkType outputPDLink;
    private ViewportType outputPDViewport;
    private MarkerType outputPDMarker;
    private GeometryType outputPDGeometry;
    private GroupType outputPDGroup;
    private StyledFeatureCollectionType outputPDSfc;
    
    private final ObjectFactory objectFactory = WpspdUtils.createObjectFactory();
    final net.opengis.gml.v_3_1_1.ObjectFactory gmlObjectFactory = new net.opengis.gml.v_3_1_1.ObjectFactory();
    private final GeometryFactory geometryFactory = GML311ToJTSConstants.DEFAULT_GEOMETRY_FACTORY;
    private final JTSToGML311GeometryConverter geometryConverter = new JTSToGML311GeometryConverter();
    private final net.opengis.se.v_1_1_0.ObjectFactory seObjectFactory = new net.opengis.se.v_1_1_0.ObjectFactory();
    
    @ComplexDataOutput(identifier ="PDLink", binding = PDLinkBinding.class)
    public LinkType getPDLink() {
    	return this.outputPDLink;
    }
    
    @ComplexDataOutput(identifier = "PDMessage", abstrakt="Das ist eine PDMessage", binding = PDMessageBinding.class)
    public MessageType getPDMessage() {
        return this.outputPDMessage;
    }
    
    @ComplexDataOutput(identifier ="PDViewport", abstrakt="Das ist ein PDViewport", binding = PDViewportBinding.class)
    public ViewportType getPDViewport() {
    	return this.outputPDViewport;
    }
    
    @ComplexDataOutput(identifier = "PDMarker", abstrakt="Das ist ein PDMarker", binding = PDMarkerBinding.class)
    public MarkerType getPDMarker() {
    	return this.outputPDMarker;
    }
    @ComplexDataOutput(identifier = "PDGeometry", abstrakt="Das ist eine PDGeometry", binding = PDGeometryBinding.class)
    public GeometryType getPDGeometry() {
    	return this.outputPDGeometry;
    }
    @ComplexDataOutput(identifier = "PDGroup", abstrakt="Das ist eine PDGroup", binding = PDGroupBinding.class)
    public GroupType getPDGroup() {
    	return this.outputPDGroup;
    }
    @ComplexDataOutput(identifier = "PDSfc", abstrakt="Das ist eine PDStyledFeatureCollection", binding = PDStyledFeatureCollectionBinding.class)
    public StyledFeatureCollectionType getPDSfc() {
    	return this.outputPDSfc;
    }
    
    @Execute
    public void runPDTest() {
    	
    	final LinkType link = objectFactory.createLinkType();
    	final MessageType message = objectFactory.createMessageType();
    	final ViewportType viewport = objectFactory.createViewportType();
    	final EnvelopeType envelope = gmlObjectFactory.createEnvelopeType();
    	final MarkerType marker = objectFactory.createMarkerType();
    	final GeometryType geometry = objectFactory.createGeometryType();
    	final GroupType group = objectFactory.createGroupType();
    	final StyledFeatureCollectionType sfc = objectFactory.createStyledFeatureCollectionType();
    	
    	// Point Geometry and Symbolizer
    	final Point point = geometryFactory.createPoint(new Coordinate(49, 8));
    	@SuppressWarnings("unchecked")
    	final JAXBElement<AbstractGeometryType> pointGeometry = (JAXBElement<AbstractGeometryType>) geometryConverter
    	          .createElement(point);
    	final PointSymbolizerType pointSymbolizerType = seObjectFactory.createPointSymbolizerType();
        final JAXBElement<PointSymbolizerType> pointSymbolizer = seObjectFactory.createPointSymbolizer(pointSymbolizerType);
        
        seObjectFactory.createPointSymbolizer(pointSymbolizerType);
        
        @SuppressWarnings("unchecked")
		final JAXBElement<SymbolizerType> symbolizer = (JAXBElement<SymbolizerType>) (Object) pointSymbolizer;
    	
        this.message = "Hallo Du!";
        
    	// Set properties for PD-Link
    	link.setPresentationDirectiveTitle(WpsUtils.createLanguageStringType("Das ist ein WPS-PD Link Test"));
    	link.setAbstract(WpsUtils.createLanguageStringType("Beschreibung des WPS-PD Links!"));
    	link.setHref("http://www.legato.net");
    	link.setType("simple");
    	link.setShow("new");
    	
    	// Set properties for PD-Message
    	message.setPresentationDirectiveTitle(WpsUtils.createLanguageStringType("Das ist ein WPS-PD Message Test"));
    	message.setAbstract(WpsUtils.createLanguageStringType("Das ist die Beschreibung der WPS-PD Message!"));
    	message.setContent(this.message);
    	message.setType("error");
    	
    	// Set properties for PD-Viewport
    	viewport.setPresentationDirectiveTitle(WpsUtils.createLanguageStringType("Das ist ein WPS-PD Viewport Test"));
    	viewport.setAbstract(WpsUtils.createLanguageStringType("Das ist die Beschreibung des WPS-PD Viewports"));
    	// Assign envelope to PD-Viewport
    	viewport.setEnvelope(gmlObjectFactory.createEnvelope(envelope));
    	// Set properties for Envelope
        envelope.setLowerCorner(gmlObjectFactory.createDirectPositionType());
        envelope.setUpperCorner(gmlObjectFactory.createDirectPositionType());
        envelope.setSrsName("urn:ogc:def:crs:EPSG::4326"); //$NON-NLS-1$
        envelope.getLowerCorner().setValue(Arrays.<Double> asList(49.007021, 8.402815));
        envelope.getUpperCorner().setValue(Arrays.<Double> asList(49.007023, 8.402813));
    	
        // Set properties for PD-Geometry
        geometry.setPresentationDirectiveTitle(WpsUtils.createLanguageStringType("Das ist ein WPS-PD Geometry Test"));
        geometry.setAbstract(WpsUtils.createLanguageStringType("Das ist die Beschreibung der WPS-PD Geometry"));
        geometry.setGeometry(pointGeometry);
        geometry.setSymbolizer(symbolizer);
        // ...
        
    	// Set properties for PD-Marker
        marker.setPresentationDirectiveTitle(WpsUtils.createLanguageStringType("Das ist ein WPS-PD Marker Test"));
        marker.setAbstract(WpsUtils.createLanguageStringType("Das ist die Beschreibung des WPS-PD Markers"));
        Geometry geom = new Geometry(geometry);
        marker.setGeometry(geom);
        // ...
        
        // Set properties for PD-Group
        group.setPresentationDirectiveTitle(WpsUtils.createLanguageStringType("Das ist ein WPS-PD Group Test"));
        group.setAbstract(WpsUtils.createLanguageStringType("Das ist die Beschreibung der WPS-PD Group"));
        group.getPresentationDirective().add(objectFactory.createLink(link));
        group.getPresentationDirective().add(objectFactory.createMessage(message));
        group.getPresentationDirective().add(objectFactory.createViewport(viewport));
        group.getPresentationDirective().add(objectFactory.createGeometry(geometry));
        group.getPresentationDirective().add(objectFactory.createMarker(marker));
        
        // Set properties for PD-StyledFeatureCollection
        FeaturesType ft = objectFactory.createFeaturesType();
        FeatureTypeDescriptionType ftdt = objectFactory.createFeatureTypeDescriptionType();
        Hints hints = objectFactory.createHints();
        
        sfc.setPresentationDirectiveTitle(WpsUtils.createLanguageStringType("Das ist ein WPS-PD StyledFeatureCollection Test"));
        sfc.setAbstract(WpsUtils.createLanguageStringType("Das ist eine Beschreibung der WPS-PD StyledFeatureCollection"));
        sfc.setFeatures(ft);
        sfc.setFeatureTypeDescription(ftdt);
        sfc.setHints(hints);
        
    	this.outputPDLink = link;
    	this.outputPDMessage = message;
    	this.outputPDViewport = viewport;
    	this.outputPDMarker = marker;
    	this.outputPDGeometry = geometry;
    	this.outputPDGroup = group;
    	this.outputPDSfc = sfc;
    }
}