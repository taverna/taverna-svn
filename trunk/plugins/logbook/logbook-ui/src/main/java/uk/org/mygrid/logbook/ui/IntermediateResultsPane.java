package uk.org.mygrid.logbook.ui;

import java.awt.Dimension;

import javax.swing.JTabbedPane;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflui.ResultItemPanel;

import uk.org.mygrid.logbook.util.Utils;

public class IntermediateResultsPane extends JTabbedPane {

    private JTabbedPane intermediateOutputs;

    private JTabbedPane intermediateInputs;

    public IntermediateResultsPane() {

        this.setMinimumSize(new Dimension(500, 150));
        intermediateOutputs = new JTabbedPane();
        intermediateInputs = new JTabbedPane();
        // intermediateResults.add("Graph", new JScrollPane(workflowEditor));
        this.add("Intermediate inputs", intermediateInputs);
        this.add("Intermediate outputs", intermediateOutputs);

    }

    public void clear() {

        intermediateInputs.removeAll();
        intermediateOutputs.removeAll();

    }

    public void addOutput(DataThing d, String name) {

        ResultItemPanel ripOutput = new ResultItemPanel(d);

        name = Utils.outputLocalName(name);

        intermediateOutputs.add(name, ripOutput);
    }

    public void addInput(DataThing d, String name) {

        ResultItemPanel ripInput = new ResultItemPanel(d);

        name = Utils.inputLocalName(name);

        intermediateInputs.add(name, ripInput);
    }

}
