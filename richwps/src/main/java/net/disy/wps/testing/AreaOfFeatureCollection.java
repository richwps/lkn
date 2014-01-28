package net.disy.wps.testing;

import net.disy.wps.common.Util;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

@Algorithm(
		version = "1.0.0",
		title = "AreaOfFeatureCollection",
		abstrakt = "Berechnet die Gesamtflaeche der Geometrien einer FeatureCollection"
		)
public class AreaOfFeatureCollection extends AbstractAnnotatedAlgorithm {

    public AreaOfFeatureCollection() {
        super();
    }
    
    SimpleFeatureCollection inputSfc = FeatureCollections.newCollection();
    Double outputArea = 0.0;
    
    @ComplexDataInput(
    		identifier="inputFeatureCollection", title="FeatureCollection",
    		abstrakt="FeatureCollection deren Flaeche bestimmt werden soll", binding=GTVectorDataBinding.class)
    public void setInputCollection(FeatureCollection<?, ?> fc) {
    	this.inputSfc = (SimpleFeatureCollection) fc;
    }
    
    @LiteralDataOutput(
    		identifier = "area", title="Flaeche",
    		abstrakt="Gesamtflaeche der FeatureCollection", binding = LiteralDoubleBinding.class)
    public Double getArea() {
    	return this.outputArea;
    }
    
    @Execute
    public void runAreaOfFeatureCollection() {
    	this.outputArea = Util.getAreaFromFC(inputSfc);
    }
    
}