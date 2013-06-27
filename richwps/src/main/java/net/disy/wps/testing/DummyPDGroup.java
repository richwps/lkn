package net.disy.wps.testing;

import javax.xml.bind.JAXBElement;

import net.disy.ogc.wps.v_1_0_0.util.WpsUtils;
import net.disy.ogc.wpspd.v_1_0_0.Geometry;
import net.disy.ogc.wpspd.v_1_0_0.GeometryType;
import net.disy.ogc.wpspd.v_1_0_0.GroupType;
import net.disy.ogc.wpspd.v_1_0_0.MarkerType;
import net.disy.ogc.wpspd.v_1_0_0.MessageType;
import net.disy.ogc.wpspd.v_1_0_0.ObjectFactory;
import net.disy.ogc.wpspd.v_1_0_0.WpspdUtils;
import net.disy.wps.n52.binding.PDGroupBinding;
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
		title = "PDGroup Testprozess",
		abstrakt ="Das ist ein PDGroupy-Testprozess auf einem 52North WPS-Server"
		)
public class DummyPDGroup extends AbstractAnnotatedAlgorithm {

    public DummyPDGroup() {
        super();
    }
    private GroupType outputPDGroup;
    
    private final ObjectFactory objectFactory = WpspdUtils.createObjectFactory();
    private final GeometryFactory geometryFactory = GML311ToJTSConstants.DEFAULT_GEOMETRY_FACTORY;
    private final JTSToGML311GeometryConverter geometryConverter = new JTSToGML311GeometryConverter();
    private final net.opengis.se.v_1_1_0.ObjectFactory seObjectFactory = new net.opengis.se.v_1_1_0.ObjectFactory();
    
    @ComplexDataOutput(identifier ="PDGroup", title="PDGroupOutput", abstrakt="Das ist eine pd:Group!", binding = PDGroupBinding.class)
    public GroupType getPDGroup() {
    	return this.outputPDGroup;
    }
    
    @Execute
    public void runPDGroupTest() {
    	
    	final GroupType group = objectFactory.createGroupType();
    	
    	final GeometryType geometryTypeA = objectFactory.createGeometryType();
    	final GeometryType geometryTypeB = objectFactory.createGeometryType();
    	final MarkerType markerType = objectFactory.createMarkerType();

    	// Point Geometry and Symbolizer
    	final Point pointA = geometryFactory.createPoint(new Coordinate(3460000.0, 5900000.0));
    	final Point pointB = geometryFactory.createPoint(new Coordinate(3480000.0, 5900000.0));
    	@SuppressWarnings("unchecked")
    	final JAXBElement<AbstractGeometryType> pointGeometryA = (JAXBElement<AbstractGeometryType>) geometryConverter
    	          .createElement(pointA);
    	@SuppressWarnings("unchecked")
		final JAXBElement<AbstractGeometryType> pointGeometryB = (JAXBElement<AbstractGeometryType>) geometryConverter
  	          .createElement(pointB); 
    	
    	AbstractGeometryType abstrGeometryTypeA =  geometryConverter.createGeometryType(pointA);
    	AbstractGeometryType abstrGeometryTypeB =  geometryConverter.createGeometryType(pointB);
    	abstrGeometryTypeA.setSrsName("urn:ogc:def:crs:EPSG::31467");
    	abstrGeometryTypeB.setSrsName("urn:ogc:def:crs:EPSG::31467");
    	pointGeometryA.setValue(abstrGeometryTypeA);
    	pointGeometryB.setValue(abstrGeometryTypeB);
    	
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
        
        
        geometryTypeA.setPresentationDirectiveTitle(WpsUtils.createLanguageStringType("Das ist ein pd:Geometry Test"));
        geometryTypeA.setAbstract(WpsUtils.createLanguageStringType("Beschreibung der pd:Geometry"));
        geometryTypeA.setGeometry(pointGeometryA);
        geometryTypeA.setSymbolizer(symbolizer);
        
        geometryTypeB.setPresentationDirectiveTitle(WpsUtils.createLanguageStringType("Das ist ein pd:Geometry Test"));
        geometryTypeB.setAbstract(WpsUtils.createLanguageStringType("Beschreibung der pd:Geometry"));
        geometryTypeB.setGeometry(pointGeometryB);
        geometryTypeB.setSymbolizer(symbolizer);
        
        MessageType msgType = objectFactory.createMessageType();
        msgType.setIdentifier(WpsUtils.createCodeType("Nachricht"));
        msgType.setAbstract(WpsUtils.createLanguageStringType("Das ist die Beschreibung der DummyNachricht"));
        msgType.setType("info");
        msgType.setContent("Hallo, ich bin die Nachricht des Markers");
        
        markerType.setPresentationDirectiveTitle(WpsUtils.createLanguageStringType("Das ist ein WPS-PD Marker Test"));
        markerType.setAbstract(WpsUtils.createLanguageStringType("Das ist die Beschreibung des WPS-PD Markers"));
        Geometry geom = new Geometry(geometryTypeB);
        markerType.setGeometry(geom);
        markerType.setMessage(objectFactory.createMessage(msgType));
        
        group.getPresentationDirective().add(objectFactory.createGeometry(geometryTypeA));
        group.getPresentationDirective().add(objectFactory.createMarker(markerType));

    	this.outputPDGroup = group;
    }
}