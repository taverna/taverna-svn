/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.sequencefile.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.help.CSH;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.activities.sequencefile.FileFormat;
import net.sf.taverna.t2.activities.sequencefile.SequenceFileActivity;
import net.sf.taverna.t2.activities.sequencefile.SequenceFileActivityConfigurationBean;
import net.sf.taverna.t2.activities.sequencefile.SequenceType;
import net.sf.taverna.t2.activities.sequencefile.actions.SequenceFileActivityConfigurationAction;
import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

/**
 * Configuration panel for the SequenceFileActivity.
 * 
 * @author David Withers
 */
public class SequenceFileConfigurationPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private SequenceFileActivity activity;

	private JPanel titlePanel, contentPanel, buttonPanel;

	private JLabel titleLabel, titleIcon, fileFormatLabel, sequenceTypeLabel;

	private DialogTextArea titleMessage;

	private ButtonGroup fileFormatButtonGroup, sequenceTypeButtonGroup;

	private JRadioButton raw, fasta, nbrf, embl, swissprot, genbank, genpep, pdb, refseq, gcg, gff;

	private JRadioButton rna, dna, protein;

	private JButton actionOkButton, actionCancelButton;

	private SequenceFileActivityConfigurationBean oldConfiguration, newConfiguration;

	public SequenceFileConfigurationPanel(SequenceFileActivity activity) {
		this.activity = activity;
		initialise();
	}

	private void initialise() {
		CSH.setHelpIDString(this, this.getClass().getCanonicalName());
		oldConfiguration = activity.getConfiguration();
		newConfiguration = new SequenceFileActivityConfigurationBean(oldConfiguration);

		// title
		titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBackground(Color.WHITE);
		addDivider(titlePanel, SwingConstants.BOTTOM, true);

		titleLabel = new JLabel(SequenceFileActivityConfigurationAction.CONFIGURE);
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13.5f));
		titleIcon = new JLabel("");
		titleMessage = new DialogTextArea("Select the file format and sequence type");
		titleMessage.setMargin(new Insets(5, 10, 10, 10));
		titleMessage.setFont(titleMessage.getFont().deriveFont(11f));
		titleMessage.setEditable(false);
		titleMessage.setFocusable(false);

		// sequence type
		sequenceTypeLabel = new JLabel("Sequence Type");
		addDivider(sequenceTypeLabel, SwingConstants.TOP, false);

		sequenceTypeButtonGroup = new ButtonGroup();
		rna = new JRadioButton("RNA");
		dna = new JRadioButton("DNA");
		protein = new JRadioButton("Protein");

		sequenceTypeButtonGroup.add(rna);
		sequenceTypeButtonGroup.add(dna);
		sequenceTypeButtonGroup.add(protein);

		if (newConfiguration.getSequenceType().equals(SequenceType.rna)) {
			rna.setSelected(true);
		} else if (newConfiguration.getSequenceType().equals(SequenceType.dna)) {
			dna.setSelected(true);
		} else if (newConfiguration.getSequenceType().equals(SequenceType.protein)) {
			protein.setSelected(true);
		}

		rna.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newConfiguration.setSequenceType(SequenceType.rna);
			}
		});
		dna.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newConfiguration.setSequenceType(SequenceType.dna);
			}
		});
		protein.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newConfiguration.setSequenceType(SequenceType.protein);
			}
		});

		// file format
		fileFormatLabel = new JLabel("File Format");

		fileFormatButtonGroup = new ButtonGroup();
		raw = new JRadioButton("raw");
		fasta = new JRadioButton("FASTA");
		nbrf = new JRadioButton("NBRF");
		embl = new JRadioButton("EMBL");
		swissprot = new JRadioButton("Swiss-Prot");
		genbank = new JRadioButton("GenBank");
		genpep = new JRadioButton("GenPep");
		pdb = new JRadioButton("PDB");
		refseq = new JRadioButton("RefSeq");
		gcg = new JRadioButton("GCG");
		gff = new JRadioButton("GFF");

		fileFormatButtonGroup.add(raw);
		fileFormatButtonGroup.add(fasta);
		fileFormatButtonGroup.add(nbrf);
		fileFormatButtonGroup.add(embl);
		fileFormatButtonGroup.add(swissprot);
		fileFormatButtonGroup.add(genbank);
		fileFormatButtonGroup.add(genpep);
		fileFormatButtonGroup.add(pdb);
		fileFormatButtonGroup.add(refseq);
		fileFormatButtonGroup.add(gcg);
		fileFormatButtonGroup.add(gff);

		raw.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newConfiguration.setFileFormat(FileFormat.raw);
				rna.setEnabled(true);
				dna.setEnabled(true);
				protein.setEnabled(true);
			}
		});
		fasta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newConfiguration.setFileFormat(FileFormat.fasta);
				rna.setEnabled(true);
				dna.setEnabled(true);
				protein.setEnabled(true);
			}
		});
		nbrf.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newConfiguration.setFileFormat(FileFormat.nbrf);
				rna.setEnabled(true);
				dna.setEnabled(true);
				protein.setEnabled(true);
			}
		});
		embl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newConfiguration.setFileFormat(FileFormat.embl);
				rna.setEnabled(true);
				dna.setEnabled(true);
				if (protein.isSelected()) {
					dna.doClick();
				}
				protein.setEnabled(false);
			}
		});
		swissprot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newConfiguration.setFileFormat(FileFormat.swissprot);
				protein.setEnabled(true);
				protein.doClick();
				rna.setEnabled(false);
				dna.setEnabled(false);
			}
		});
		genbank.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newConfiguration.setFileFormat(FileFormat.genbank);
				rna.setEnabled(true);
				dna.setEnabled(true);
				if (protein.isSelected()) {
					dna.doClick();
				}
				protein.setEnabled(false);
			}
		});
		genpep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newConfiguration.setFileFormat(FileFormat.genpep);
				protein.setEnabled(true);
				protein.doClick();
				rna.setEnabled(false);
				dna.setEnabled(false);
			}
		});
		pdb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newConfiguration.setFileFormat(FileFormat.pdb);
				protein.setEnabled(true);
				protein.doClick();
				rna.setEnabled(false);
				dna.setEnabled(false);
			}
		});
		refseq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newConfiguration.setFileFormat(FileFormat.refseq);
				rna.setEnabled(true);
				dna.setEnabled(true);
				if (protein.isSelected()) {
					dna.doClick();
				}
				protein.setEnabled(false);
			}
		});
		gcg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newConfiguration.setFileFormat(FileFormat.gcg);
				rna.setEnabled(true);
				dna.setEnabled(true);
				protein.setEnabled(true);
			}
		});
		gff.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newConfiguration.setFileFormat(FileFormat.gff);
				rna.setEnabled(true);
				dna.setEnabled(true);
				protein.setEnabled(true);
			}
		});

		if (newConfiguration.getFileFormat().equals(FileFormat.raw)) {
			raw.doClick();
		} else if (newConfiguration.getFileFormat().equals(FileFormat.fasta)) {
			fasta.doClick();
		} else if (newConfiguration.getFileFormat().equals(FileFormat.nbrf)) {
			nbrf.doClick();
		} else if (newConfiguration.getFileFormat().equals(FileFormat.embl)) {
			embl.doClick();
		} else if (newConfiguration.getFileFormat().equals(FileFormat.swissprot)) {
			swissprot.doClick();
		} else if (newConfiguration.getFileFormat().equals(FileFormat.genbank)) {
			genbank.doClick();
		} else if (newConfiguration.getFileFormat().equals(FileFormat.genpep)) {
			genpep.doClick();
		} else if (newConfiguration.getFileFormat().equals(FileFormat.pdb)) {
			pdb.doClick();
		} else if (newConfiguration.getFileFormat().equals(FileFormat.refseq)) {
			refseq.doClick();
		} else if (newConfiguration.getFileFormat().equals(FileFormat.gcg)) {
			gcg.doClick();
		} else if (newConfiguration.getFileFormat().equals(FileFormat.gff)) {
			gff.doClick();
		}

		// buttons
		actionOkButton = new JButton();
		actionOkButton.setFocusable(false);

		actionCancelButton = new JButton();
		actionCancelButton.setFocusable(false);

		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		addDivider(buttonPanel, SwingConstants.TOP, true);

		layoutPanel();
	}

	private void layoutPanel() {
		setPreferredSize(new Dimension(450, 400));
		setLayout(new BorderLayout());

		contentPanel = new JPanel(new GridBagLayout());
		add(contentPanel, BorderLayout.CENTER);

		// title
		titlePanel.setBorder(new CompoundBorder(titlePanel.getBorder(), new EmptyBorder(10, 10, 0,
				10)));
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.add(titleLabel, BorderLayout.NORTH);
		titlePanel.add(titleIcon, BorderLayout.WEST);
		titlePanel.add(titleMessage, BorderLayout.CENTER);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;

		// file format
		c.insets = new Insets(10, 10, 10, 10);
		contentPanel.add(fileFormatLabel, c);

		c.insets = new Insets(0, 25, 0, 10);
		c.gridwidth = 1;
		contentPanel.add(fasta, c);
		c.gridx = 1;
		contentPanel.add(raw, c);
		c.gridx = 0;
		contentPanel.add(nbrf, c);
		c.gridx = 1;
		contentPanel.add(embl, c);
		c.gridx = 0;
		contentPanel.add(swissprot, c);
		c.gridx = 1;
		contentPanel.add(genbank, c);
		c.gridx = 0;
		contentPanel.add(genpep, c);
		c.gridx = 1;
		contentPanel.add(pdb, c);
		c.gridx = 0;
		contentPanel.add(refseq, c);
		c.gridx = 1;
		contentPanel.add(gcg, c);
		c.gridx = 0;
		contentPanel.add(gff, c);

		c.gridwidth = GridBagConstraints.REMAINDER;

		// sequence type
		c.insets = new Insets(10, 10, 10, 10);
		contentPanel.add(sequenceTypeLabel, c);

		c.insets = new Insets(0, 25, 0, 10);
		contentPanel.add(rna, c);
		contentPanel.add(dna, c);
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		contentPanel.add(protein, c);

		buttonPanel.add(actionCancelButton);
		buttonPanel.add(actionOkButton);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	public void setOkAction(Action okAction) {
		actionOkButton.setAction(okAction);
	}

	public void setCancelAction(Action cancelAction) {
		actionCancelButton.setAction(cancelAction);
	}

	public SequenceFileActivityConfigurationBean getConfiguration() {
		return newConfiguration;
	}

	public boolean isConfigurationChanged() {
		return !newConfiguration.equals(oldConfiguration);
	}

	/**
	 * Adds a light gray or etched border to the top or bottom of a JComponent.
	 * 
	 * @param component
	 */
	protected void addDivider(JComponent component, final int position, final boolean etched) {
		component.setBorder(new Border() {
			private final Color borderColor = new Color(.6f, .6f, .6f);

			public Insets getBorderInsets(Component c) {
				if (position == SwingConstants.TOP) {
					return new Insets(5, 0, 0, 0);
				} else {
					return new Insets(0, 0, 5, 0);
				}
			}

			public boolean isBorderOpaque() {
				return false;
			}

			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				if (position == SwingConstants.TOP) {
					if (etched) {
						g.setColor(borderColor);
						g.drawLine(x, y, x + width, y);
						g.setColor(Color.WHITE);
						g.drawLine(x, y + 1, x + width, y + 1);
					} else {
						g.setColor(Color.LIGHT_GRAY);
						g.drawLine(x, y, x + width, y);
					}
				} else {
					if (etched) {
						g.setColor(borderColor);
						g.drawLine(x, y + height - 2, x + width, y + height - 2);
						g.setColor(Color.WHITE);
						g.drawLine(x, y + height - 1, x + width, y + height - 1);
					} else {
						g.setColor(Color.LIGHT_GRAY);
						g.drawLine(x, y + height - 1, x + width, y + height - 1);
					}
				}
			}

		});
	}

	@SuppressWarnings("serial")
	public static void main(String[] args) throws ActivityConfigurationException {
		final JFrame frame = new JFrame();
		SequenceFileActivity activity = new SequenceFileActivity();
		activity.configure(new SequenceFileActivityConfigurationBean());
		final SequenceFileConfigurationPanel config = new SequenceFileConfigurationPanel(activity);
		config.setOkAction(new AbstractAction("Finish") {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println(config.getConfiguration().getFileFormat());
				System.out.println(config.getConfiguration().getSequenceType());
				frame.setVisible(false);
				frame.dispose();
			}
		});
		config.setCancelAction(new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent arg0) {
				frame.setVisible(false);
				frame.dispose();
			}
		});
		frame.add(config);
		frame.pack();
		frame.setVisible(true);
	}

}
