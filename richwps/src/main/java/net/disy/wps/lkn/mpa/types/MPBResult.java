package net.disy.wps.lkn.mpa.types;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import net.disy.wps.lkn.utils.FeatureCollectionUtil;

import net.disy.wps.lkn.utils.MPAUtils;


import org.joda.time.DateTime;

/**
 * Diese Klasse repraesentiert das Gesamtergebnis eines Bewertungsvorgangs
 *
 * @author woessner
 * @see net.disy.wps.n52.binding.MPBResultBinding
 */
@XmlRootElement(name = "MPBResult")
public class MPBResult {

    private DateTime timestamp;
    private Integer bewertungsjahr;
    private ArrayList<MPBAreaResult> areaResults = new ArrayList<MPBAreaResult>();

    public MPBResult() {
        timestamp = DateTime.now();
        MPBEvalMatrix nfMatrix = new MPBEvalMatrix(MPAUtils.NORDFRIESLAND);
        areaResults.add(new MPBAreaResult(timestamp, bewertungsjahr,
                MPAUtils.NORDFRIESLAND, nfMatrix));
        MPBEvalMatrix diMatrix = new MPBEvalMatrix(MPAUtils.DITHMARSCHEN);
        areaResults.add(new MPBAreaResult(timestamp, bewertungsjahr,
                MPAUtils.DITHMARSCHEN, diMatrix));
    }

    /**
     * Initialisiert die statischen Bewertungsmatrizen
     */
    private void initEvalMatrices() {
        for (int i = 0; i < areaResults.size(); i++) {
            if (areaResults.get(i).getEvalMatrix() == null) {
                areaResults.get(i).setEvalMatrix(
                        new MPBEvalMatrix(areaResults.get(i).getGebiet()));
            }
        }
    }

    public DateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(DateTime ts) {
        this.timestamp = ts;
    }

    /**
     * Gibt das Ergebnis fuer Nordfriesland zur�ck
     *
     * @return
     */
    public MPBAreaResult getNFResult() {
        for (int i = 0; i < areaResults.size(); i++) {
            if (areaResults.get(i).getGebiet() == MPAUtils.NORDFRIESLAND) {
                return areaResults.get(i);
            }
        }
        return null;
    }

    /**
     * Gibt das Ergebnis fuer Dithmarschen zur�ck
     *
     * @return
     */
    public MPBAreaResult getDIResult() {
        for (int i = 0; i < areaResults.size(); i++) {
            if (areaResults.get(i).getGebiet() == MPAUtils.DITHMARSCHEN) {
                return areaResults.get(i);
            }
        }
        return null;
    }

    @XmlElement(name = "Bewertungsjahr")
    public Integer getBewertungsjahr() {
        return this.bewertungsjahr;
    }

    /**
     * Setzt das Bewertungsjahr fuer alle Berichtsgebiete
     *
     * @param bewertungsjahr
     */
    public void setBewertungsjahr(Integer bewertungsjahr) {
        this.bewertungsjahr = bewertungsjahr;
        getAreaResult(MPAUtils.NORDFRIESLAND).setBewertungsjahr(bewertungsjahr);
        getAreaResult(MPAUtils.DITHMARSCHEN).setBewertungsjahr(bewertungsjahr);
    }

    /**
     * Gibt ein bestimmtes Teilergebnis fuer das angegebene Berichtsgebiet
     * zurueck
     *
     * @param gebiet
     * @return
     */
    public MPBAreaResult getAreaResult(Integer gebiet) {
        switch (gebiet) {
            case MPAUtils.NORDFRIESLAND:
                return getNFResult();
            case MPAUtils.DITHMARSCHEN:
                return getDIResult();
        }
        return null;
    }

    @XmlElement(name = "MPBAreaResult")
    public ArrayList<MPBAreaResult> getAreaResults() {
        return areaResults;
    }

    /**
     * Fuegt den Teilergebnissen einen neuen MPBResultRecord hinzu
     *
     * @param rec
     */
    public void addRecord(MPBResultRecord rec) {
        for (int i = 0; i < areaResults.size(); i++) {
            areaResults.get(i).addRecord(rec.clone());
        }
    }

    /**
     * Berechnet fuer jedes Teilergebniss die Bewertungsparameter
     */
    public void calculateParameters() {
        initEvalMatrices();
        for (int i = 0; i < areaResults.size(); i++) {
            areaResults.get(i).calculateParameters();
        }
    }

    /**
     * Gibt den mittleren EQR-Wert fuer Nordfriesland zurueck
     *
     * @return Mittlerer EQR-Wert
     */
    public String getMeanEqrOfNF() {
        return FeatureCollectionUtil.toDecimalStr(getAreaResult(MPAUtils.NORDFRIESLAND)
                .getMeanEQR());
    }

    /**
     * Gibt den mittleren EQR-Wert fuer Dithmarschen zurueck
     *
     * @return Mittlerer EQR-Wert
     */
    public String getMeanEqrOfDI() {
        return FeatureCollectionUtil.toDecimalStr(getAreaResult(MPAUtils.DITHMARSCHEN)
                .getMeanEQR());
    }

    /**
     * Gibt die String-Repraesentation des mittleren EQR-Wertes fuer
     * Nordfriesland zurueck
     *
     * @return String-Repraesentation des mittleren EQR-Wertes
     */
    public String getMeanEqrStringOfNF() {
        return getAreaResult(MPAUtils.NORDFRIESLAND).getMeanEQREvalString();
    }

    /**
     * Gibt die String-Repraesentation des mittleren EQR-Wertes fuer
     * Dithmarschen zurueck
     *
     * @return String-Repraesentation des mittleren EQR-Wertes
     */
    public String getMeanEqrStringOfDI() {
        return getAreaResult(MPAUtils.DITHMARSCHEN).getMeanEQREvalString();
    }

    /**
     * Gibt den CSV-String der Rohdaten-Ergebnisse zurueck
     *
     * @param metadata
     * @return
     */
    public String getRawRecordsString(boolean metadata) {
        initEvalMatrices();
        // Sollte fuer beide AreaResults gleich sein!
        return getAreaResult(MPAUtils.DITHMARSCHEN)
                .getRawRecordsString(metadata);
    }

    /**
     * Gibt den CSV-String der Bewertungsergebnisse zurueck
     *
     * @param metadata
     * @return
     */
    public String getEvalRecordsString(boolean metadata) {
        String resultStr = "";
        initEvalMatrices();
        if (metadata) {
            resultStr = resultStr + "Makrophytenbewertung ausgehend von "
                    + this.bewertungsjahr + "\n";
            resultStr = resultStr + "Ausgabe der Bewertungsergebnisse";
            resultStr = resultStr + "Erstellt am " + this.timestamp.toString()
                    + "\n\n";
            resultStr = resultStr
                    + "Ergebnismatrix fuer das Berichtsgebiet Nordfriesland"
                    + "\n\n";
        }

        resultStr += getAreaResult(MPAUtils.NORDFRIESLAND)
                .getEvalRecordsString();
        resultStr = resultStr
                + FeatureCollectionUtil.toDecimalStr(getAreaResult(MPAUtils.NORDFRIESLAND)
                        .getMeanEQR()) + ","
                + getAreaResult(MPAUtils.NORDFRIESLAND).getMeanEQREvalString()
                + "\n";

        if (metadata) {
            resultStr = resultStr + "\n"
                    + "Ergebnismatrix fuer das Berichtsgebiet Dithmarschen"
                    + "\n\n";
        }
        resultStr += getAreaResult(MPAUtils.DITHMARSCHEN).getEvalRecordsString();
        resultStr = resultStr
                + FeatureCollectionUtil.toDecimalStr(getAreaResult(MPAUtils.DITHMARSCHEN)
                        .getMeanEQR()) + ","
                + getAreaResult(MPAUtils.DITHMARSCHEN).getMeanEQREvalString()
                + "\n";

        return resultStr;
    }

    /**
     * Erzeugt einen Textbaustein fuer eine Trendaussauge fuer die
     * Seegras-Entwicklung aus den vier vom Bewertungsjahr ausgehend letzten
     * gesamten Seegrasvorkommen
     *
     * @param evalArea
     * @return
     */
    public String getSeegrasTrend(Integer evalArea) {
        String trendStr;
        MPBAreaResult areaResult = getAreaResult(evalArea);
        Double zs0 = null;
        Double zs1 = null;
        Double zs2 = null;
        Double zs3 = null;

        if (evalArea == MPAUtils.NORDFRIESLAND) {
            zs0 = areaResult.getRecordByYear(bewertungsjahr).ZS_totalareaNF;
            zs1 = areaResult.getRecordByYear(bewertungsjahr - 1).ZS_totalareaNF;
            zs2 = areaResult.getRecordByYear(bewertungsjahr - 2).ZS_totalareaNF;
            zs3 = areaResult.getRecordByYear(bewertungsjahr - 3).ZS_totalareaNF;
        } else if (evalArea == MPAUtils.DITHMARSCHEN) {
            zs0 = areaResult.getRecordByYear(bewertungsjahr).ZS_totalareaDI;
            zs1 = areaResult.getRecordByYear(bewertungsjahr - 1).ZS_totalareaDI;
            zs2 = areaResult.getRecordByYear(bewertungsjahr - 2).ZS_totalareaDI;
            zs3 = areaResult.getRecordByYear(bewertungsjahr - 3).ZS_totalareaDI;
        }

        if (Math.abs(zs0 - zs1) / zs0 <= 0.15
                && Math.abs(zs1 - zs2) / zs1 <= 0.20 && zs0 < 2 * zs3) {
            trendStr = " stabiler Zustand";
        } else if (zs3 < zs2 && zs2 < zs1 && zs1 < zs0) {
            trendStr = " stetiger Zuwachs";
        } else if (Math.abs(zs2 - zs1) <= Math.abs(zs1 - zs0)
                && 1.5 * zs1 < zs0) {
            trendStr = " expandierender Zuwachs";
        } else if (zs2 < zs1 && zs1 < zs0) {
            trendStr = " stabiler Zuwachs";
        } else if (Math.abs(zs2 - zs1) * 1.5 < Math.abs(zs1 - zs0) && zs1 < zs0) {
            trendStr = " massiver Zuwachs";
        } else if (Math.abs(zs3 - zs2) > Math.abs(zs2 - zs1)
                && Math.abs(zs2 - zs1) <= Math.abs(zs1 - zs0) && zs0 > zs3) {
            trendStr = "e stabilisierende positive Entwicklung";
        } else if (zs3 > zs2 && zs2 > zs1 && zs1 > zs0) {
            trendStr = "e stetige Abnahme";
        } else if (zs2 > zs1 && zs1 > zs0) {
            trendStr = "e stabile Abnahme";
        } else if (Math.abs(zs3 - zs2) >= Math.abs(zs2 - zs1)
                && Math.abs(zs2 - zs1) >= Math.abs(zs1 - zs0)) {
            trendStr = "e deutliche Abnahme";
        } else if (Math.abs(zs2 - zs1) * 1.5 > Math.abs(zs1 - zs0) || zs1 > zs0) {
            trendStr = " auffallender Rueckgang";
        } else if (Math.abs(zs3 - zs2) <= Math.abs(zs2 - zs1)
                && Math.abs(zs2 - zs1) > Math.abs(zs1 - zs0)) {
            trendStr = "e stabilisierende negative Entwicklung";
        } else {
            trendStr = "nichts besonderes";
        }

        return trendStr;
    }

    /**
     * Erzeugt einen Textbaustein fuer eine Trendaussauge fuer die
     * EQR-Entwicklung aus den vier vom Bewertungsjahr ausgehend letzten
     * gewichteten EQR-Werten
     *
     * @param evalArea
     * @return Textbaustein fuer Trendaussage
     */
    public String getEQRTrend(Integer evalArea) {
        String trendStr;

        MPBAreaResult areaResult = getAreaResult(evalArea);
        Double eqr0 = null;
        Double eqr1 = null;
        Double eqr2 = null;
        Double eqr3 = null;

        if (evalArea == MPAUtils.NORDFRIESLAND) {
            eqr0 = areaResult.getRecordByYear(bewertungsjahr).getWeightedEQR();
            eqr1 = areaResult.getRecordByYear(bewertungsjahr - 1)
                    .getWeightedEQR();
            eqr2 = areaResult.getRecordByYear(bewertungsjahr - 2)
                    .getWeightedEQR();
            eqr3 = areaResult.getRecordByYear(bewertungsjahr - 3)
                    .getWeightedEQR();
        } else if (evalArea == MPAUtils.DITHMARSCHEN) {
            eqr0 = areaResult.getRecordByYear(bewertungsjahr).getWeightedEQR();
            eqr1 = areaResult.getRecordByYear(bewertungsjahr - 1)
                    .getWeightedEQR();
            eqr2 = areaResult.getRecordByYear(bewertungsjahr - 2)
                    .getWeightedEQR();
            eqr3 = areaResult.getRecordByYear(bewertungsjahr - 3)
                    .getWeightedEQR();
        }

        if (Math.abs(eqr0 - eqr1) <= 0.005 && Math.abs(eqr1 - eqr2) <= 0.005) {
            trendStr = " bleiben fast auf dem gleichen Niveau innerhalb der letzten Jahre.";
        } else if ((eqr0 - eqr1) / eqr0 >= 0.25) {
            trendStr = " nehmen gegenueber den Vorjahren und besonders im Bewertungsjahr selbst deutlich zu.";
        } else if (eqr3 < eqr2
                && eqr2 < eqr1
                && eqr1 < eqr0
                && Math.abs(Math.abs(eqr0 - eqr1) - Math.abs(eqr1 - eqr2)) <= 0.005
                && Math.abs(Math.abs(eqr1 - eqr2) - Math.abs(eqr2 - eqr3)) <= 0.005) {
            trendStr = " nehmen stetig linear zu.";
        } else if (eqr2 < eqr1
                && eqr1 < eqr0
                && Math.abs(Math.abs(eqr0 - eqr1) - Math.abs(eqr1 - eqr2)) <= 0.005) {
            trendStr = " zeigen einen leichten linearen Zuwachs.";
        } else if (eqr3 < eqr2 && eqr2 < eqr1 && eqr1 < eqr0) {
            trendStr = " nehmen stetig zu und zeigen seit 3 Jahren einen etwa stabil verlaufenden Zuwachs.";
        } else if (eqr2 < eqr1 && eqr1 < eqr0) {
            trendStr = " nehmen leicht zu und zeigen seit 2 Jahren einen etwa stabil verlaufenden Zuwachs.";
        } else if ((eqr1 - eqr0) / eqr0 >= 0.25) {
            trendStr = " nehmen gegenueber den Vorjahren und besonders im Bewertungsjahr selbst deutlich ab.";
        } else if (eqr3 > eqr2
                && eqr2 > eqr1
                && eqr1 > eqr0
                && Math.abs(Math.abs(eqr0 - eqr1) - Math.abs(eqr1 - eqr2)) <= 0.005
                && Math.abs(Math.abs(eqr1 - eqr2) - Math.abs(eqr2 - eqr3)) <= 0.005) {
            trendStr = " nehmen stetig linear ab.";
        } else if (eqr2 > eqr1
                && eqr1 > eqr0
                && Math.abs(Math.abs(eqr0 - eqr1) - Math.abs(eqr1 - eqr2)) <= 0.005) {
            trendStr = " zeigen eine leichte lineare Abnahme.";
        } else if (eqr3 > eqr2 && eqr2 > eqr1 && eqr1 > eqr0) {
            trendStr = " nehmen stetig ab und zeigen seit 3 Jahren eine etwa stabil verlaufende Minderung.";
        } else if (eqr2 > eqr1 && eqr1 > eqr0) {
            trendStr = " nehmen leicht ab und zeigen seit 2 Jahren eine etwa stabil verlaufende Minderung.";
        } else {
            trendStr = "liegen ausserhalb des Trends und weisen somit keine aussagekraeftigen Zusammenhaenge auf.";
        }

        return trendStr;
    }

    public File persist() {
        File f = null;
        try {
            JAXBContext context = JAXBContext.newInstance(MPBResult.class);
            Marshaller m = context.createMarshaller();
            f = File.createTempFile("MPB", "Result");

            m.marshal(this, f);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return f;
    }
}
