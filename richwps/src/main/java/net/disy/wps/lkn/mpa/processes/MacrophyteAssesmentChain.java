package net.disy.wps.lkn.mpa.processes;

import java.util.Collections;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import net.disy.wps.lkn.mpa.types.*;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.RepositoryManager;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.disy.wps.n52.binding.IntegerListBinding;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;

@Algorithm(version = "0.0.1", title = "MacrophyteAssesmentChain", abstrakt = "Prozess zur Bewertung der Berichtsgebiete Nordfriesland und Dithmarschen anhand von MSRL-D5 Daten")
public class MacrophyteAssesmentChain extends AbstractAnnotatedAlgorithm {

    // Logger fuer Debugging erzeugen
    // use with wps 3.2.0
    protected static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MacrophyteAssesmentChain.class);
    //use with wps 3.1.0
    //protected static org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(MacrophyteAssesment.class);

    /**
     * Reporting Areas.
     */
    private SimpleFeatureCollection inputReportingAreas;
    /**
     * Topography.
     */
    private SimpleFeatureCollection inputTopography;
    /**
     * MSRLD05 Algea and Seagras..
     */
    private SimpleFeatureCollection inputMSRLD5;
    /**
     * The relevantTopoYear for the assesment.
     */
    private Integer inputAssesmentYear;

    private SimpleFeatureCollection outputCollection = FeatureCollections.newCollection();

    /**
     * Constructs a new WPS-Process MacrophyteAssesment.
     */
    public MacrophyteAssesmentChain() {
        super();
        //LOGGER.setLevel(Level.ALL);
    }

    private List<IData> wrappLiteral(String payload) {
        return Collections.singletonList((IData) new LiteralStringBinding(payload));
    }

    private List<IData> wrappSFC(SimpleFeatureCollection sfc) {
        GTVectorDataBinding gt = new GTVectorDataBinding((FeatureCollection) sfc);
        return Collections.singletonList((IData) gt);
    }

    private List<IData> wrappIntegerList(IntegerList data) {
        return Collections.singletonList((IData) new IntegerListBinding(data));
    }

    private List<IData> wrappOBLC(ObservationFeatureCollectionList data) {
        return Collections.singletonList((IData) new net.disy.wps.n52.binding.ObeservationFeatureCollectionListBinding(data));
    }
      private List<IData> wrappISLC(IntersectionFeatureCollectionList data) {
        return Collections.singletonList((IData) new net.disy.wps.n52.binding.IntersectionFeatureCollectionListBinding(data));
    }

    private SimpleFeatureCollection unwrapSFC(Object data) {
        return (SimpleFeatureCollection) ((GTVectorDataBinding) data).getPayload();
    }

    private ObservationFeatureCollectionList unwrappOBFCL(Object data) {
        return ((net.disy.wps.n52.binding.ObeservationFeatureCollectionListBinding) data).getPayload();
    }

    private IntersectionFeatureCollectionList unwrappISFCL(Object data) {
        return ((net.disy.wps.n52.binding.IntersectionFeatureCollectionListBinding) data).getPayload();
    }

    private IntegerList unwrappIntegerList(Object data) {
        return ((net.disy.wps.n52.binding.IntegerListBinding) data).getPayload();
    }

    @Execute
    public void run() {
        RepositoryManager rm = RepositoryManager.getInstance();
        SimpleFeatureCollection reportingAreasNF = null;
        SimpleFeatureCollection reportingAreasDI = null;
        IntegerList relevantYears = null;
        ObservationFeatureCollectionList relevantAlgea = null;
        ObservationFeatureCollectionList relevantSeagras = null;
        ObservationFeatureCollectionList relevantTopographies = null;
        IntegerList relevantTopographyYears = null;
        IntegerList existingTopographyYears = null;
        IntersectionFeatureCollectionList intersectionTidelandsReportingAreasNF = null;
        IntersectionFeatureCollectionList intersectionTidelandsReportingAreasDI = null;

        System.out.println("Starting msrl nfselection");
        try {
            IAlgorithm nfselection = rm.getAlgorithm(net.disy.wps.lkn.processes.SelectReportingArea.class.getCanonicalName());
            Map<String, List<IData>> nfselectioninputs = new HashMap<String, List<IData>>();
            nfselectioninputs.put("area", wrappLiteral("NF"));
            nfselectioninputs.put("reportingareas", wrappSFC(this.inputReportingAreas));

            Map outputs = nfselection.run(nfselectioninputs);
            reportingAreasNF = unwrapSFC(outputs.get("reportingarea"));

        } catch (ExceptionReport ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

        System.out.println("Starting msrl diselection");
        try {
            IAlgorithm diselection = rm.getAlgorithm(net.disy.wps.lkn.processes.SelectReportingArea.class.getCanonicalName());
            Map<String, List<IData>> diselectioninputs = new HashMap<String, List<IData>>();
            diselectioninputs.put("area", wrappLiteral("DI"));
            diselectioninputs.put("reportingareas", wrappSFC(this.inputReportingAreas));
            Map outputs = diselection.run(diselectioninputs);
            reportingAreasDI = unwrapSFC(outputs.get("reportingarea"));
        } catch (ExceptionReport ex) {
            Logger.getLogger(MacrophyteAssesmentChain.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        System.out.println("Starting msrl selection");
        try {
            IAlgorithm diselection = rm.getAlgorithm(net.disy.wps.lkn.mpa.processes.MSRLD5selection.class.getCanonicalName());
            Map<String, List<IData>> msrlselectioninputs = new HashMap<String, List<IData>>();
            msrlselectioninputs.put("msrl-d5", wrappSFC(this.inputMSRLD5));
            msrlselectioninputs.put("bewertungsjahr", this.wrappLiteral(this.inputAssesmentYear + ""));
            Map outputs = diselection.run(msrlselectioninputs);
            relevantAlgea = unwrappOBFCL(outputs.get("relevantAlgea"));
            relevantSeagras = unwrappOBFCL(outputs.get("relevantSeagras"));
            relevantYears = unwrappIntegerList(outputs.get("relevantYears"));
        } catch (ExceptionReport ex) {
            Logger.getLogger(MacrophyteAssesmentChain.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        System.out.println("Starting topography selection");
        try {
            IAlgorithm diselection = rm.getAlgorithm(net.disy.wps.lkn.mpa.processes.SelectTopography.class.getCanonicalName());

            Map<String, List<IData>> diselectioninputs = new HashMap<String, List<IData>>();
            diselectioninputs.put("topography", wrappSFC(this.inputTopography));
            diselectioninputs.put("relevantYears", wrappIntegerList(relevantYears));;
            Map outputs = diselection.run(diselectioninputs);
            relevantTopographies = unwrappOBFCL(outputs.get("relevantTopographies"));
            relevantTopographyYears = unwrappIntegerList(outputs.get("relevantTopographyYears"));
            existingTopographyYears = unwrappIntegerList(outputs.get("existingTopographyYears"));
        } catch (ExceptionReport ex) {
            Logger.getLogger(MacrophyteAssesmentChain.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        System.out.println("Starting intersection 1");
        try {
            IAlgorithm diintersect = rm.getAlgorithm(net.disy.wps.lkn.mpa.processes.Intersect.class.getCanonicalName());
            Map<String, List<IData>> diselectioninputs = new HashMap<String, List<IData>>();
            diselectioninputs.put("reportingAreas", wrappSFC(reportingAreasDI));
            diselectioninputs.put("topography", wrappOBLC(relevantTopographies));
            diselectioninputs.put("relevantTopographyYears", wrappIntegerList(relevantTopographyYears));
            Map outputs = diintersect.run(diselectioninputs);
            intersectionTidelandsReportingAreasDI = unwrappISFCL(outputs.get("intersections"));
        } catch (ExceptionReport ex) {
            Logger.getLogger(MacrophyteAssesmentChain.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();

        }

        System.out.println("Starting intersection 2");
        try {
            IAlgorithm nfintersect = rm.getAlgorithm(net.disy.wps.lkn.mpa.processes.Intersect.class.getCanonicalName());
            Map<String, List<IData>> diselectioninputs = new HashMap<String, List<IData>>();
            diselectioninputs.put("reportingAreas", wrappSFC(reportingAreasNF));
            diselectioninputs.put("topography", wrappOBLC(relevantTopographies));
            diselectioninputs.put("relevantTopographyYears", wrappIntegerList(relevantTopographyYears));
            Map outputs = nfintersect.run(diselectioninputs);
            intersectionTidelandsReportingAreasNF = unwrappISFCL(outputs.get("intersections"));
        } catch (ExceptionReport ex) {
            Logger.getLogger(MacrophyteAssesmentChain.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }

        System.out.println("Starting Characteristics ");
        try {
            IAlgorithm characteristics = rm.getAlgorithm(net.disy.wps.lkn.mpa.processes.Characteristics.class.getCanonicalName());
            Map<String, List<IData>> diselectioninputs = new HashMap<String, List<IData>>();
            diselectioninputs.put("relevantYears",  wrappIntegerList(relevantYears));
            diselectioninputs.put("existingTopographyYears", wrappIntegerList(existingTopographyYears));
            diselectioninputs.put("intersectionTidelandsReportingAreasNF", wrappISLC(intersectionTidelandsReportingAreasNF));
            diselectioninputs.put("intersectionTidelandsReportingAreasDI", wrappISLC(intersectionTidelandsReportingAreasDI));
            diselectioninputs.put("relevantSeagras", wrappOBLC(relevantSeagras));
            diselectioninputs.put("relevantAlgea", wrappOBLC(relevantAlgea));
            diselectioninputs.put("reportingAreasNF", wrappSFC(reportingAreasNF));
            diselectioninputs.put("reportingAreasDI", wrappSFC(reportingAreasDI));
            Map outputs = characteristics.run(diselectioninputs);
            this.outputCollection = unwrapSFC(outputs.get("mpbResultGml"));
        } catch (ExceptionReport ex) {
            Logger.getLogger(MacrophyteAssesmentChain.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        
        System.out.println("Done :) ");
    }

    @ComplexDataInput(identifier = "berichtsgebiete", title = "Berichtsgebiete", abstrakt = "Berichtsgebiete die die Werte 'DI' und 'NF' im Attribut 'DISTR' enthalten.", binding = GTVectorDataBinding.class)
    public void setReportingArea(final FeatureCollection<?, ?> evalAreaCollection) {
        this.inputReportingAreas = (SimpleFeatureCollection) evalAreaCollection;
    }

    @ComplexDataInput(identifier = "topographie", title = "Topographie", abstrakt = "Topographie Layer", minOccurs = 1, maxOccurs = 1, binding = GTVectorDataBinding.class)
    public void setTopography(final FeatureCollection<?, ?> topoCollection) {
        this.inputTopography = (SimpleFeatureCollection) topoCollection;
    }

    @ComplexDataInput(identifier = "msrl-d5", title = "MSRL D5 Daten", abstrakt = "MSRL D5 Daten, die Algen- und Seegras- Polygone enthalten.", binding = GTVectorDataBinding.class)
    public void setMSRLD5(final FeatureCollection<?, ?> inputCollection) {
        this.inputMSRLD5 = (SimpleFeatureCollection) inputCollection;
    }

    @LiteralDataInput(identifier = "bewertungsjahr", title = "Bewertungsjahr", abstrakt = "Bewertungsjahr, von dem die durchzufuehrende Bewertung ausgeht.", binding = LiteralStringBinding.class)
    public void setAssesmentYear(String assesmentYear) {
        this.inputAssesmentYear = Integer.parseInt(assesmentYear);
    }

    // Achtung: Output Type muss hier FeatureCollection sein (nicht
    // SimpleFeatureCollection)!
    @ComplexDataOutput(identifier = "mpbResultGml", title = "Bewertete Berichtsgebiete", abstrakt = "FeatureCollection der bewerteten Berichtsgebiete", binding = GTVectorDataBinding.class)
    public FeatureCollection getResultGml() {
        return this.outputCollection;
    }
}
