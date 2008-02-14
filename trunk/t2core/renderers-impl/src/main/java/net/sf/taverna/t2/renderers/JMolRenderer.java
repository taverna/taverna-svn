package net.sf.taverna.t2.renderers;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolSimpleViewer;
import org.jmol.api.JmolViewer;
import org.jmol.viewer.Viewer;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

/**
 * Renders using the Jmol software for chemical structures
 * 
 * @author Tom Oinn
 */
public class JMolRenderer implements Renderer {

	public JMolRenderer() {
	}

	public boolean isTerminal() {
		return true;
	}

	public boolean canHandle(DataFacade facade,
			EntityIdentifier entityIdentifier, String mimeType) {
		Object resolve = null;
		try {
			resolve = facade.resolve(entityIdentifier);
		} catch (RetrievalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (resolve instanceof String) {
			if (mimeType.matches(".*chemical/x-pdb.*")
					|| mimeType.matches(".*chemical/x-mdl-molfile.*")
					|| mimeType.matches(".*chemical/x-cml.*")) {
				return true;
			}
		}

		return false;
	}

	public boolean canHandle(String mimeType) {
		if (mimeType.matches(".*chemical/x-pdb.*")
				|| mimeType.matches(".*chemical/x-mdl-molfile.*")
				|| mimeType.matches(".*chemical/x-cml.*")) {
			return true;
		}

		return false;
	}

	static final String proteinScriptString = "wireframe off; spacefill off; select protein; cartoon; colour structure; select ligand; spacefill; colour cpk; select dna; spacefill 0.4; wireframe on; colour cpk;";

	static final String scriptString = "select *; spacefill 0.4; wireframe 0.2; colour cpk;";

	public JComponent getComponent(EntityIdentifier entityIdentifier,
			DataFacade dataFacade) {
		JMolPanel panel = new JMolPanel();
		String coordinateText = null;
		try {
			coordinateText = (String) dataFacade.resolve(entityIdentifier,
					String.class);
		} catch (RetrievalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JmolSimpleViewer viewer = panel.getViewer();
		viewer.openStringInline(coordinateText);
		if (((JmolViewer) viewer).getAtomCount() > 300) {
			viewer.evalString(proteinScriptString);
		} else {
			viewer.evalString(scriptString);
		}
		return panel;
		// return null;
	}

	class JMolPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		JmolSimpleViewer viewer;
		JmolAdapter adapter;

		JMolPanel() {
			adapter = new SmarterJmolAdapter(null);
			viewer = Viewer.allocateJmolSimpleViewer(this, adapter);
			// viewer = JmolSimpleViewer.allocateSimpleViewer(this, adapter);
		}

		public JmolSimpleViewer getViewer() {
			return viewer;
		}

		final Dimension currentSize = new Dimension();
		final Rectangle rectClip = new Rectangle();

		public void paint(Graphics g) {
			getSize(currentSize);
			g.getClipBounds(rectClip);
			viewer.renderScreenImage(g, currentSize, rectClip);
		}
	}
}
