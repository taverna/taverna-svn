package net.sf.taverna.t2.cloudone.entity.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.io.IOUtils;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;

public class DataDocumentPanel extends AbstractEntityPanel {
	
	private DataDocumentIdentifier id;
	private DataDocument doc;
	private final DataManager dataManager;
	
	public DataDocumentPanel(DataManager dataManager, DataDocumentIdentifier id) throws RetrievalException, NotFoundException {
		this.dataManager = dataManager;
		this.id = id;
		doc = (DataDocument) dataManager.getEntity(id);
		buildPanel();
	}

	@Override
	public JComponent createHeader() {
		
		return createLabel("DataDocument", id);
	}
	
	@Override
	public JComponent createDetails() {
		JPanel refPanel = new JPanel();
		refPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 1;
		c.weightx = 0.1;
		c.fill = GridBagConstraints.HORIZONTAL;
		for (ReferenceScheme<?> scheme: doc.getReferenceSchemes()) {
			try {
				if (scheme.getCharset() != null) {
					InputStream stream = null;
					stream = scheme.dereference(dataManager);
					byte[] b = new byte[1024];
					int bytesRead = stream.read(b, 0, 1024);
					stream.close();
					String blobString = new String(b, 0, bytesRead, scheme.getCharset());
					JLabel refSchemeType = new JLabel(scheme.getClass().getSimpleName() + " " + scheme.toString());
					refPanel.add(refSchemeType, c);
					JLabel blobType = new JLabel("<html><a href='#'>(download)</a>"+ blobString.substring(0,20));
					refPanel.add(blobType);
					continue;
				}
			} catch (DereferenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Show simple version instead
			JLabel refSchemeType = new JLabel(scheme.getClass().getSimpleName() + " " + scheme.toString());
			refPanel.add(refSchemeType, c);			
		}
		return refPanel;
	}

}
