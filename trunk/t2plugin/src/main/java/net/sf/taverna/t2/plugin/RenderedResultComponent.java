package net.sf.taverna.t2.plugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.renderers.Renderer;

/**
 * Displays the {@link Component} returned from a workflow result
 * {@link Renderer} within a {@link JPanel}. Use the
 * {@link RendererResultComponentFactory} to get an an isntance of this class
 * 
 * @author Ian Dunlop
 * 
 */
public class RenderedResultComponent extends JPanel {

	private Component resultComponent;

	private JPanel resultPanel;

	/**
	 * Create the {@link JPanel} which displays the results and place it in the
	 * centre in a {@link JScrollPane}
	 */
	protected RenderedResultComponent() {
		resultPanel = new JPanel();
		resultPanel.setToolTipText("To display: select a result from the \"Results Panel\", right click and select a renderer type");
		resultPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());
		add(new JLabel("Rendered Results"), BorderLayout.NORTH);
		add(new JScrollPane(resultPanel), BorderLayout.CENTER);
		setToolTipText("To display: select a result from the \"Results Panel\", right click and select a renderer type");
	}

	/**
	 * Change the {@link Renderer} {@link Component} which will be displayed and
	 * redraw it.
	 * 
	 * @param newResultComponent
	 */
	public void setResultComponent(Component newResultComponent) {
		if (resultComponent != null) {
			resultPanel.remove(resultComponent);
		}
		resultComponent = newResultComponent;
		resultComponent.setPreferredSize(new Dimension(600, 600));
		resultPanel.add(resultComponent);
		revalidate();
	}

}
