package net.disy.wps.lkn.mpa.processes;

import java.io.File;
import net.disy.wps.lkn.mpa.types.IntegerList;

import net.disy.wps.n52.binding.MPBResultBinding;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import net.disy.wps.lkn.utils.MSRLD5Utils;
import net.disy.wps.lkn.mpa.types.ObservationFeatureCollectionList;

@Algorithm(version = "0.0.1", title = "MSRLD5selection", abstrakt = ".")
public class MSRLD5selection extends AbstractAnnotatedAlgorithm {

    // Logger fuer Debugging erzeugen
    // use with wps 3.2.0
    protected static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MSRLD5selection.class);
    //use with wps 3.1.0
    //protected static org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(MacrophyteAssesment.class);

    /**
     * MSRLD05 algea and seagras.
     */
    private SimpleFeatureCollection inputMSRLD5;
    /**
     * The relevantTopoYear for the assessment.
     */
    private Integer inputAssesmentYear;

    /**
     * Dealing with MSRLD5 measurements.
     */
    private MSRLD5Utils msrld5;
   

    private File outputA;
    private File outputB;
    private File outputC;

    /**
     * Constructs a new WPS-Process MacrophyteAssesment.
     */
    public MSRLD5selection() {
        super();
        //LOGGER.setLevel(Level.ALL);
    }

    @Execute
    /**
     * Selektion von MSRLD5-FeatureCollections algen und seegras anhand des
     * MSRLD5-Attributs OBSV_PARAMNAME. COV_OP = Algen. COV_ZS = Seegras.
     */
    public void runMPB() {
        this.msrld5 = new MSRLD5Utils(this.inputMSRLD5);
       
        
    
    
    
        ObservationFeatureCollectionList relevantAlgea = msrld5.getRelevantObservationsByParameterAndYear(MSRLD5Utils.ATTRIB_OBS_PARAMNAME_OP, this.inputAssesmentYear);
        final int amountAlgaeObservations = relevantAlgea.size();
        
        ObservationFeatureCollectionList relevantSeagras;
        relevantSeagras = msrld5.getRelevantObservationsByParameterAndYear(MSRLD5Utils.ATTRIB_OBS_PARAMNAME_ZS, this.inputAssesmentYear);
        final int amountSeagrasObservations = relevantSeagras.size();

        // Validierung der vorherigen Selektion
        // Test: Gibt es zwei Eintraege in der msrld5Parameters? (Seegras und
        // Algen
        if (relevantAlgea == null || relevantSeagras == null) {
            throw new RuntimeException(
                    "Es sind nicht genuegend Parameter zur Bewertung vorhanden!");
        }

        if (amountSeagrasObservations != amountAlgaeObservations) {
            throw new RuntimeException(
                    "Es sind nicht genuegend Parameter zur Bewertung vorhanden!");
        }

        IntegerList relevantYears;        
        // Test: Entsprechen sich die Jahre von Seegras- und Algen-Datensaetze?
        for (int i = 0; i < amountSeagrasObservations; i++) {
            int seagrasyear = relevantSeagras.get(i).getDateTime().getYear();
            int algeayear = relevantAlgea.get(i).getDateTime().getYear();
            if (seagrasyear != algeayear) {
                throw new RuntimeException(
                        "Die relevanten Jahre der"
                        + "beiden Parameter 'Algen' und 'Seegras' entsprechen sich nicht!");
            }
        }

        relevantYears = new IntegerList();

        for (int i = 0; i < amountSeagrasObservations; i++) {
            int seagrasyear = relevantSeagras.get(i).getDateTime().getYear();
            //Hinzufuegen/Merken eines der beiden Jahre.   
            relevantYears.add(seagrasyear);
        }

        File f1 = relevantAlgea.persist();
        File f2 = relevantSeagras.persist();
        File f3 = relevantSeagras.persist();
        this.outputA = f1;
        this.outputB = f2;
        this.outputC = f3;
    }

    @ComplexDataInput(identifier = "msrl-d5", title = "MSRL D5 Daten", abstrakt = "MSRL D5 Daten, die Algen- und Seegras- Polygone enthalten.", binding = GTVectorDataBinding.class)
    public void setMSRLD5(final FeatureCollection<?, ?> inputCollection) {
        this.inputMSRLD5 = (SimpleFeatureCollection) inputCollection;
    }

    @LiteralDataInput(identifier = "bewertungsjahr", title = "Bewertungsjahr", abstrakt = "Bewertungsjahr, von dem die durchzufuehrende Bewertung ausgeht.", binding = LiteralStringBinding.class)
    public void setAssesmentYear(String assesmentYear) {
        this.inputAssesmentYear = Integer.parseInt(assesmentYear);
    }

    @ComplexDataOutput(identifier = "relevantAlgea", title = "XML-Rohdaten Datei", abstrakt = "XML-Datei mit Rohdaten der Bewertung", binding = MPBResultBinding.class)
    public File getOutputA() {
        return this.outputA;
    }

    @ComplexDataOutput(identifier = "relevantSeagras", title = "XML-Rohdaten Datei", abstrakt = "XML-Datei mit Rohdaten der Bewertung", binding = MPBResultBinding.class)
    public File getOutputB() {
        return this.outputB;
    }

    @ComplexDataOutput(identifier = "relevantYears", title = "XML-Rohdaten Datei", abstrakt = "XML-Datei mit Rohdaten der Bewertung", binding = MPBResultBinding.class)
    public File getOutputC() {
        return this.outputC;
    }
}