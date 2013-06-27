package net.disy.wps.testing;

import javax.xml.bind.JAXBElement;

import net.disy.ogc.wps.v_1_0_0.util.WpsUtils;
import net.disy.ogc.wpspd.v_1_0_0.Geometry;
import net.disy.ogc.wpspd.v_1_0_0.GeometryType;
import net.disy.ogc.wpspd.v_1_0_0.MarkerType;
import net.disy.ogc.wpspd.v_1_0_0.MessageType;
import net.disy.ogc.wpspd.v_1_0_0.ObjectFactory;
import net.disy.ogc.wpspd.v_1_0_0.WpspdUtils;
import net.disy.wps.n52.binding.PDMarkerBinding;
import net.opengis.gml.v_3_1_1.AbstractGeometryType;
import net.opengis.se.v_1_1_0.FillType;
import net.opengis.se.v_1_1_0.GraphicType;
import net.opengis.se.v_1_1_0.MarkType;
import net.opengis.se.v_1_1_0.PointSymbolizerType;
import net.opengis.se.v_1_1_0.StrokeType;
import net.opengis.se.v_1_1_0.SvgParameterType;
import net.opengis.se.v_1_1_0.SymbolizerType;

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
		version = "1.0.0",
		title = "PDMarker Testprozess",
		abstrakt ="Das ist ein PDMarker-Testprozess auf einem 52North WPS-Server"
		)
public class DummyPDMarker extends AbstractAnnotatedAlgorithm {

    public DummyPDMarker() {
        super();
    }
    private MarkerType outputPDMarker;
    
    private final ObjectFactory objectFactory = WpspdUtils.createObjectFactory();
    private final GeometryFactory geometryFactory = GML311ToJTSConstants.DEFAULT_GEOMETRY_FACTORY;
    private final JTSToGML311GeometryConverter geometryConverter = new JTSToGML311GeometryConverter();
    private final net.opengis.se.v_1_1_0.ObjectFactory seObjectFactory = new net.opengis.se.v_1_1_0.ObjectFactory();
    
    @ComplexDataOutput(identifier ="PDMarker", title="PDMarkerOutput", abstrakt="Das ist eine pd:Marker!", binding = PDMarkerBinding.class)
    public MarkerType getPDMarker() {
    	return this.outputPDMarker;
    }
    
    @Execute
    public void runPDMarkerTest() {
    	
    	final GeometryType geometry = objectFactory.createGeometryType();
    	final MarkerType marker = objectFactory.createMarkerType();

    	// Point Geometry and Symbolizer
    	final Point point = geometryFactory.createPoint(new Coordinate(3470000.0, 5870000.0));
    	@SuppressWarnings("unchecked")
    	final JAXBElement<AbstractGeometryType> pointGeometry = (JAXBElement<AbstractGeometryType>) geometryConverter
    	          .createElement(point);   	
    	
    	AbstractGeometryType geometryType =  geometryConverter.createGeometryType(point);
    	geometryType.setSrsName("urn:ogc:def:crs:EPSG::31467");
    	pointGeometry.setValue(geometryType);
    	
    	final PointSymbolizerType pointSymbolizerType = seObjectFactory.createPointSymbolizerType();
        final JAXBElement<PointSymbolizerType> pointSymbolizer = seObjectFactory.createPointSymbolizer(pointSymbolizerType);
    	
        @SuppressWarnings("unchecked")
		final JAXBElement<SymbolizerType> symbolizer = (JAXBElement<SymbolizerType>) (Object) pointSymbolizer;
        
        // Graphic for SymbolizerType
        final GraphicType graphicType = seObjectFactory.createGraphicType();
        pointSymbolizerType.setGraphic(graphicType);
        final MarkType markType = seObjectFactory.createMarkType();
        
        graphicType.getExternalGraphicOrMark().add(markType);
        final FillType fillType = seObjectFactory.createFillType();
        markType.setFill(fillType);
        final StrokeType strokeType = seObjectFactory.createStrokeType();
        markType.setStroke(strokeType);
        {
            final SvgParameterType svgParameterType = seObjectFactory.createSvgParameterType();
            svgParameterType.setName("fill");
            svgParameterType.getContent().add("#ff0000");
            fillType.getSvgParameter().add(svgParameterType);
        }
        {
            final SvgParameterType svgParameterType = seObjectFactory.createSvgParameterType();
            svgParameterType.setName("stroke");
            svgParameterType.getContent().add("#0000ff");
            strokeType.getSvgParameter().add(svgParameterType);
        }
        {
            final SvgParameterType svgParameterType = seObjectFactory.createSvgParameterType();
            svgParameterType.setName("stroke-width");
            svgParameterType.getContent().add("2");
            strokeType.getSvgParameter().add(svgParameterType);
        }
        
        
    	geometry.setPresentationDirectiveTitle(WpsUtils.createLanguageStringType("Das ist ein pd:Geometry Test"));
    	geometry.setAbstract(WpsUtils.createLanguageStringType("Beschreibung der pd:Geometry"));
        geometry.setGeometry(pointGeometry);
        geometry.setSymbolizer(symbolizer);

        MessageType msgType = objectFactory.createMessageType();
        msgType.setIdentifier(WpsUtils.createCodeType("Nachricht"));
        msgType.setAbstract(WpsUtils.createLanguageStringType("Das ist die Beschreibung der DummyNachricht"));
        msgType.setType("info");
        msgType.setContent("Hallo, ich bin die Nachricht des Markers");
        
        
     	// Set properties for PD-Marker
        marker.setPresentationDirectiveTitle(WpsUtils.createLanguageStringType("Das ist ein WPS-PD Marker Test"));
        marker.setAbstract(WpsUtils.createLanguageStringType("Das ist die Beschreibung des WPS-PD Markers"));
        Geometry geom = new Geometry(geometry);
        marker.setGeometry(geom);
        marker.setMessage(objectFactory.createMessage(msgType));
        
    	this.outputPDMarker = marker;
    }
}