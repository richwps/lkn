package net.disy.wps.lkn.mpa.processes;

import static net.disy.wps.lkn.mpa.processes.MacrophyteAssessment.LOGGER;
import net.disy.wps.lkn.mpa.types.IntegerList;
import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollection;
import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollectionList;
import net.disy.wps.lkn.mpa.types.ObservationFeatureCollectionList;
import net.disy.wps.lkn.utils.FeatureCollectionUtil;
import net.disy.wps.lkn.utils.MPAUtils;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import net.disy.wps.n52.binding.IntegerListBinding;
import net.disy.wps.n52.binding.IntersectionFeatureCollectionListBinding;
import net.disy.wps.n52.binding.ObeservationFeatureCollectionListBinding;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.joda.time.DateTime;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;

@Algorithm(version = "0.0.1", title = "Intersect", abstrakt = ".")
public class Intersect extends AbstractAnnotatedAlgorithm {

    private IntegerList relevantTopographyYears;

    private SimpleFeatureCollection reporingAreas;
    private ObservationFeatureCollectionList relevantTopographies;

    private IntersectionFeatureCollectionList intersectionsTidelandReportingAreas;

    public Intersect() {
        super();
    }

    @Execute
    public void runMPB() {
        intersectionsTidelandReportingAreas = new IntersectionFeatureCollectionList();
        for (Integer relevantTopoYear : relevantTopographyYears) {

            final SimpleFeatureCollection intersecWattBGebiet
                    = MPAUtils.intersectReportingsareasAndTidelands(
                            reporingAreas,
                            relevantTopographies.getObsCollByYear(relevantTopoYear)
                            .getFeatureCollection());
            LOGGER.debug("Intersection-Result valid: "
                    + FeatureCollectionUtil.checkValid(intersecWattBGebiet));

            final DateTime obsTime = new DateTime(relevantTopoYear, 1, 1, 0, 0);
            final Double area = FeatureCollectionUtil.getArea(intersecWattBGebiet);

            final IntersectionFeatureCollection intersecColl = new IntersectionFeatureCollection(MPAUtils.DITHMARSCHEN, obsTime, intersecWattBGebiet, area);
            intersectionsTidelandReportingAreas.add(intersecColl);

        }//of for
    }

    @ComplexDataInput(identifier = "reportingAreas",
            title = "ingoing reportingareas.", abstrakt = "None.", binding = GTVectorDataBinding.class, minOccurs = 1)
    public void setReportingAreas(final FeatureCollection<?, ?> in) {
        this.reporingAreas = (SimpleFeatureCollection) in;
    }

    @ComplexDataInput(identifier = "topography",
            title = "ingoing topography.", abstrakt = "None.", binding = ObeservationFeatureCollectionListBinding.class, minOccurs = 1)
    public void setTopography(final ObservationFeatureCollectionList in) {
        this.relevantTopographies = in;
    }

    @ComplexDataInput(identifier = "relevantTopographyYears",
            title = "relevantTopographyYears.", abstrakt = "None.", binding = IntegerListBinding.class, minOccurs = 1)
    public void setRelevantYears(final IntegerList in) {
        this.relevantTopographyYears = in;
    }

    @ComplexDataOutput(identifier = "intersections",
            title = ".", abstrakt = "None.", binding = IntersectionFeatureCollectionListBinding.class)
    public IntersectionFeatureCollectionList getRelevantTopos() {
        return this.intersectionsTidelandReportingAreas;
    }
}
