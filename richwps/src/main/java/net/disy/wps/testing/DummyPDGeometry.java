package net.disy.wps.testing;

import javax.xml.bind.JAXBElement;

import net.disy.ogc.wps.v_1_0_0.util.WpsUtils;
import net.disy.ogc.wpspd.v_1_0_0.GeometryType;
import net.disy.ogc.wpspd.v_1_0_0.ObjectFactory;
import net.disy.ogc.wpspd.v_1_0_0.WpspdUtils;
import net.disy.wps.n52.binding.PDGeometryBinding;
import net.opengis.gml.v_3_1_1.AbstractGeometryType;
import net.opengis.se.v_1_1_0.FillType;
import net.opengis.se.v_1_1_0.PolygonSymbolizerType;
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
import com.vividsolutions.jts.geom.Polygon;

@Algorithm(
		version = "1.0.0",
		title = "PDGeometry Testprozess",
		abstrakt ="Das ist ein PDGeometry-Testprozess auf einem 52North WPS-Server"
		)
public class DummyPDGeometry extends AbstractAnnotatedAlgorithm {

    public DummyPDGeometry() {
        super();
    }
    private GeometryType outputPDGeometry;
    
    private final ObjectFactory objectFactory = WpspdUtils.createObjectFactory();
    private final GeometryFactory geometryFactory = GML311ToJTSConstants.DEFAULT_GEOMETRY_FACTORY;
    private final JTSToGML311GeometryConverter geometryConverter = new JTSToGML311GeometryConverter();
    private final net.opengis.se.v_1_1_0.ObjectFactory seObjectFactory = new net.opengis.se.v_1_1_0.ObjectFactory();
    
    @ComplexDataOutput(identifier ="PDGeometry", title="PDGeometryOutput", abstrakt="Das ist eine pd:Geometry!", binding = PDGeometryBinding.class)
    public GeometryType getPDGeometry() {
    	return this.outputPDGeometry;
    }
    
    @Execute
    public void runPDGeometryTest() {
    	
    	final GeometryType geometry = objectFactory.createGeometryType();


    	// Polygon Geometry and Symbolizer
    	final Polygon polygon =  geometryFactory.createPolygon(
    			geometryFactory.createLinearRing(new Coordinate[]{
    					new Coordinate(3450000.0, 5850000.0),
    					new Coordinate(3470000.0, 5850000.0),
    					new Coordinate(3465000.0, 5870000.0),
    					new Coordinate(3450000.0, 5850000.0)
    			}), null
    			);
    	
    	@SuppressWarnings("unchecked")
    	final JAXBElement<AbstractGeometryType> pointGeometry = (JAXBElement<AbstractGeometryType>) geometryConverter
    	          .createElement(polygon);   	
    	
    	AbstractGeometryType geometryType =  geometryConverter.createGeometryType(polygon);
    	geometryType.setSrsName("urn:ogc:def:crs:EPSG::31467");
    	pointGeometry.setValue(geometryType);
    	
    	final PolygonSymbolizerType polygonSymbolizerType = seObjectFactory.createPolygonSymbolizerType();
        final JAXBElement<PolygonSymbolizerType> polygonSymbolizer = seObjectFactory.createPolygonSymbolizer(polygonSymbolizerType);
    	
        @SuppressWarnings("unchecked")
		final JAXBElement<SymbolizerType> symbolizer = (JAXBElement<SymbolizerType>) (Object) polygonSymbolizer;
        
        final FillType fillType = seObjectFactory.createFillType();
        polygonSymbolizerType.setFill(fillType);
        final StrokeType strokeType = seObjectFactory.createStrokeType();
        polygonSymbolizerType.setStroke(strokeType);
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

    	this.outputPDGeometry = geometry;
    }
}