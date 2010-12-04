package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.lang.ui.DialogTextArea;



import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerActivityConfigurationBean;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity;

public class RapidMinerConfigurationView extends JPanel {

	private RapidMinerActivityConfigurationBean oldConfiguration;
	private RapidMinerActivityConfigurationBean newConfiguration;

	private JPanel titlePanel, contentPanel, buttonPanel, page1, page2;
	
	private CardLayout cardLayout = new CardLayout();
	
	private JLabel titleLabel, titleIcon;
	
	private DialogTextArea titleMessage;
	
	public RapidMinerConfigurationView(RapidMinerExampleActivity activity) {
		
		
		oldConfiguration = activity.getConfiguration();
		newConfiguration = oldConfiguration;
		initialise();
		layoutPanel();
	}

	private void initialise() {

		titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBackground(Color.WHITE);
		addDivider(titlePanel, SwingConstants.BOTTOM, true);
		
		// title
		titleLabel = new JLabel("Operator : " + newConfiguration.getOperatorName());
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13.5f));
		
		titleIcon = new JLabel("");
		titleMessage = new DialogTextArea("Please chose whether you want to explicitly set the operator output location");
		titleMessage.setMargin(new Insets(5, 10, 10, 10));
		titleMessage.setFont(titleMessage.getFont().deriveFont(11f));
		titleMessage.setEditable(false);
		titleMessage.setFocusable(false);
	}
	
	private void layoutPanel() {
		// TODO Auto-generated method stub
		setPreferredSize(new Dimension(450, 400));
		setLayout(new BorderLayout());
		
		page1 = new JPanel(new GridBagLayout());
		page2 = new JPanel(new GridBagLayout());
		
		contentPanel = new JPanel(cardLayout);
		contentPanel.add(page1, "page1");
		contentPanel.add(page2, "page2");
		add(contentPanel, BorderLayout.CENTER);
		
		// title
		// title
		titlePanel.setBorder(new CompoundBorder(titlePanel.getBorder(), new EmptyBorder(10, 10, 0, 10)));
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.add(titleLabel, BorderLayout.NORTH);
		titlePanel.add(titleIcon, BorderLayout.WEST);
		titlePanel.add(titleMessage, BorderLayout.CENTER);

		
		
	}

	public RapidMinerConfigurationView(LayoutManager arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public RapidMinerConfigurationView(boolean arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public RapidMinerConfigurationView(LayoutManager arg0, boolean arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
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

}
