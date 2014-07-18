package net.disy.wps.lkn.processes;

import net.disy.wps.lkn.utils.FeatureCollectionUtil;
import static net.disy.wps.lkn.utils.ReportingAreaUtils.ATTRIB_DISTR;
import static net.disy.wps.lkn.utils.ReportingAreaUtils.ATTRIB_TEMPLATE;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

@Algorithm(version = "0.0.1", title = "SelectReportingArea", abstrakt = ".")
public class SelectReportingArea extends AbstractAnnotatedAlgorithm {

    private SimpleFeatureCollection reportingareas;
    private String area;
    private SimpleFeatureCollection output;

    public SelectReportingArea() {
        super();
    }

    @Execute
    public void run() {
        String[] keys = {ATTRIB_DISTR, ATTRIB_TEMPLATE};
        String[] values = {this.area, ""};
        SimpleFeatureCollection outputCollection
                = FeatureCollectionUtil.extractEquals(this.reportingareas, keys, values);
        this.output = outputCollection;
    }

    @ComplexDataInput(identifier = "reportingareas",
            title = "reporting areas.", abstrakt = "None.",
            binding = GTVectorDataBinding.class)
    public void setReportingAreas(final FeatureCollection<?, ?> in) {
        this.reportingareas = (SimpleFeatureCollection) in;
    }

    @LiteralDataInput(identifier = "area",
            title = "area identifier {NF/DI}.", abstrakt = "None.",
            binding = LiteralStringBinding.class)
    public void setArea(final String in) {
        this.area = in;
    }

    @ComplexDataOutput(identifier = "reportingarea",
            title = "reportingarea.", abstrakt = "None.",
            binding = GTVectorDataBinding.class)
    public FeatureCollection getOutput() {
        return this.output;
    }
}
