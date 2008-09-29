/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */

package org.embl.ebi.escience.scuflworkers.soaplab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.shared.GraphColours;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.embl.ebi.escience.scuflui.shared.UIUtils;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;
import org.embl.ebi.escience.scuflworkers.ProcessorEditor;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

/**
 * Edits the polling properties for the soaplab processor
 * 
 * @author Tom Oinn
 */
public class PollingPropertiesEditor implements ProcessorEditor {

	public String getEditorDescription() {
		return "Configure polling properties...";
	}

	public ActionListener getListener(Processor theProcessor) {
		final SoaplabProcessor sp = (SoaplabProcessor) theProcessor;
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				UIUtils.createFrame(sp.getModel(), new PollingPropertiesPanel(
						sp), 100, 100, 280, 125);
			}
		};
	}

	public class PollingPropertiesPanel extends JPanel implements
			UIComponentSPI {

		SoaplabProcessor processor;

		Color col, col2;

		JTextField interval, intervalmax, backoff;

		public PollingPropertiesPanel(SoaplabProcessor sp) {
			processor = sp;
			setOpaque(false);
			setLayout(new BorderLayout());
			col = GraphColours.getColour(ProcessorHelper
					.getPreferredColour(processor), Color.WHITE);
			col2 = ShadedLabel.halfShade(col);
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridLayout(3, 4));
			buttonPanel.setOpaque(false);
			add(buttonPanel, BorderLayout.CENTER);
			add(Box.createRigidArea(new Dimension(5, 5)), BorderLayout.NORTH);
			add(Box.createRigidArea(new Dimension(5, 5)), BorderLayout.EAST);
			add(Box.createRigidArea(new Dimension(5, 5)), BorderLayout.WEST);
			add(Box.createRigidArea(new Dimension(5, 5)), BorderLayout.SOUTH);
			final JCheckBox isPolling = new JCheckBox("Polling? ", processor
					.isPollingDefined());
			JButton okayButton = new JButton("Apply");
			okayButton.setOpaque(false);
			// okayButton.setPreferredSize(new Dimension(50,25));
			interval = new JTextField(processor.getPollingInterval() + "");
			intervalmax = new JTextField(processor.getPollingIntervalMax() + "");
			backoff = new JTextField(processor.getPollingBackoff() + "");
			interval.setEnabled(processor.isPollingDefined());
			backoff.setEnabled(processor.isPollingDefined());
			intervalmax.setEnabled(processor.isPollingDefined());
			isPolling.setOpaque(false);
			buttonPanel.add(isPolling);
			buttonPanel.add(new JLabel("Interval"));
			buttonPanel.add(interval);
			buttonPanel.add(Box.createHorizontalGlue());
			buttonPanel.add(new JLabel("Maximum Interval"));
			buttonPanel.add(intervalmax);
			JPanel okayPanel = new JPanel(new BorderLayout());
			okayPanel.setOpaque(false);
			okayPanel.add(okayButton, BorderLayout.WEST);
			okayPanel.add(Box.createRigidArea(new Dimension(10, 10)),
					BorderLayout.CENTER);
			buttonPanel.add(okayPanel);
			buttonPanel.add(new JLabel("Backoff"));
			buttonPanel.add(backoff);
			buttonPanel.setPreferredSize(new Dimension(300, 80));
			buttonPanel.setMinimumSize(new Dimension(300, 80));
			buttonPanel.setMaximumSize(new Dimension(300, 80));

			setPreferredSize(new Dimension(0, 0));

			isPolling.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.DESELECTED) {
						interval.setEnabled(false);
						intervalmax.setEnabled(false);
						backoff.setEnabled(false);
						interval.setText("0");
						intervalmax.setText("0");
						backoff.setText("1.0");
					} else {
						interval.setEnabled(true);
						intervalmax.setEnabled(true);
						backoff.setEnabled(true);
						interval.setText("3000");
						intervalmax.setText("60000");
						backoff.setText("1.1");
					}
				}
			});

			okayButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						processor.setPolling(Integer.parseInt(interval
								.getText()), Double.parseDouble(backoff
								.getText()), Integer.parseInt(intervalmax
								.getText()));
					} catch (NumberFormatException nfe) {
						JOptionPane.showMessageDialog(null,
								"Fields must be valid numbers!\n"
										+ nfe.getMessage(), "Exception!",
								JOptionPane.ERROR_MESSAGE);
						interval.setText(processor.getPollingInterval() + "");
						intervalmax.setText(processor.getPollingIntervalMax()
								+ "");
						backoff.setText(processor.getPollingBackoff() + "");
					}
				}
			});

		}

		// Apply a graduated wheat coloured background
		protected void paintComponent(Graphics g) {
			final int width = getWidth();
			final int height = getHeight();
			Graphics2D g2d = (Graphics2D) g;
			Paint oldPaint = g2d.getPaint();
			g2d.setPaint(new GradientPaint(0, 0, col, width, 0, col2));
			g2d.fillRect(0, 0, width, height);
			g2d.setPaint(oldPaint);
			super.paintComponent(g);
		}

		public void onDisplay() {
			//
		}

		public void onDispose() {
			try {
				processor.setPolling(Integer.parseInt(interval.getText()),
						Double.parseDouble(backoff.getText()), Integer
								.parseInt(intervalmax.getText()));
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(null,
						"Fields must be valid numbers!\n" + nfe.getMessage(),
						"Exception!", JOptionPane.ERROR_MESSAGE);
			}
		}

		public String getName() {
			if (processor==null){
				return "Polling properties";
			}
			else{			
				return "Polling properties for " + processor.getName();
			}
		}

		public ImageIcon getIcon() {
			return ProcessorHelper.getPreferredIcon(processor);
		}
	}

}
