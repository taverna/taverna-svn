package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.idawizard.components;

import java.awt.event.ActionListener;/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Simon Jupp<br>
 * Date: Feb 23, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class WizardController implements ActionListener {

    private Wizard wizard;


    /**
     * This constructor accepts a reference to the Wizard component that created it,
     * which it uses to update the button components and access the WizardModel.
     * @param w A callback to the Wizard component that created this controller.
     */
    public WizardController(Wizard w) {
        wizard = w;
    }


    /**
     * Calling method for the action listener interface. This class listens for actions
     * performed by the buttons in the Wizard class, and calls methods below to determine
     * the correct course of action.
     * @param evt The ActionEvent that occurred.
     */
    public void actionPerformed(java.awt.event.ActionEvent evt) {

        if (evt.getActionCommand().equals(Wizard.CANCEL_BUTTON_ACTION_COMMAND))
            cancelButtonPressed();
        else if (evt.getActionCommand().equals(Wizard.BACK_BUTTON_ACTION_COMMAND))
            backButtonPressed();
        else if (evt.getActionCommand().equals(Wizard.NEXT_BUTTON_ACTION_COMMAND))
            nextButtonPressed();
    }


    public void cancel() {
        cancelButtonPressed();
    }


    private void cancelButtonPressed() {
        wizard.close(Wizard.CANCEL_RETURN_CODE);
    }


    public void next() {
        nextButtonPressed();
    }


    private void nextButtonPressed() {

        WizardModel model = wizard.getModel();
        WizardPanel descriptor = model.getCurrentPanelDescriptor();

        //  If it is a finishable panel, dispose down the dialog. Otherwise,
        //  get the ID that the current panel identifies as the next panel,
        //  and display it.

        Object nextPanelDescriptor = descriptor.getNextPanelDescriptor();

        if (nextPanelDescriptor instanceof WizardPanel.FinishIdentifier) {
            descriptor.aboutToHidePanel();
            wizard.close(Wizard.FINISH_RETURN_CODE);
        }
        else {
            wizard.setCurrentPanel(nextPanelDescriptor);
        }
    }


    private void backButtonPressed() {

        WizardModel model = wizard.getModel();
        WizardPanel descriptor = model.getCurrentPanelDescriptor();

        //  Get the descriptor that the current panel identifies as the previous
        //  panel, and display it.

        Object backPanelDescriptor = descriptor.getBackPanelDescriptor();
        wizard.setCurrentPanel(backPanelDescriptor);
    }


    public void resetButtonsToPanelRules() {

        //  Reset the buttons to support the original panel rules,
        //  including whether the next or back buttons are enabled or
        //  disabled, or if the panel is finishable.

        WizardModel model = wizard.getModel();
        WizardPanel descriptor = model.getCurrentPanelDescriptor();

        model.setCancelButtonText(Wizard.CANCEL_TEXT);

        //  If the panel in question has another panel behind it, enable
        //  the back button. Otherwise, disable it.

        model.setBackButtonText(Wizard.BACK_TEXT);

        if (descriptor.getBackPanelDescriptor() != null)
            model.setBackButtonEnabled(Boolean.TRUE);
        else
            model.setBackButtonEnabled(Boolean.FALSE);

        //  If the panel in question has one or more panels in front of it,
        //  enable the next button. Otherwise, disable it.

        if (descriptor.getNextPanelDescriptor() != null)
            model.setNextFinishButtonEnabled(Boolean.TRUE);
        else
            model.setNextFinishButtonEnabled(Boolean.FALSE);

        //  If the panel in question is the last panel in the series, change
        //  the Next button to Finish. Otherwise, set the text back to Next.

        if (descriptor.getNextPanelDescriptor() instanceof WizardPanel.FinishIdentifier) {
            model.setNextFinishButtonText(Wizard.FINISH_TEXT);
        }
        else {
            model.setNextFinishButtonText(Wizard.NEXT_TEXT);
        }
    }
}


