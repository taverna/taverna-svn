package net.sf.taverna.t2.cloudone.entity.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class DataDocumentPanel extends AbstractEntityPanel {

	static final long serialVersionUID = 1L;
	private DataDocumentIdentifier id;
	private DataDocument doc;
	private final DataManager dataManager;
	private static Logger logger = Logger.getLogger(DataDocumentPanel.class);

	public DataDocumentPanel(DataManager dataManager, DataDocumentIdentifier id)
			throws RetrievalException, NotFoundException {
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
		for (final ReferenceScheme<?> scheme : doc.getReferenceSchemes()) {
			try {
				if (scheme.getCharset() != null) {
					InputStream stream = null;
					stream = scheme.dereference(dataManager);
					byte[] b = new byte[1024];
					int bytesRead = stream.read(b, 0, 1024);
					stream.close();
					MagicMatch match;
					match = Magic.getMagicMatch(b);
					String mimeType = match.getMimeType();
					String blobString = new String(b, 0, bytesRead, scheme
							.getCharset());
					JLabel refSchemeType = new JLabel(scheme.getClass()
							.getSimpleName()
							+ " " + scheme.toString());
					refPanel.add(refSchemeType, c);
					JLabel blobType = new JLabel(
							"<html><a href='#'>(download)</a>" + " ("
									+ mimeType + ") "
									+ blobString.substring(0, 20));
					blobType.addMouseListener(new MouseAdapter() {

						public void mouseClicked(MouseEvent e) {
							JFileChooser fc = new JFileChooser();
							Preferences prefs = Preferences
									.userNodeForPackage(DataDocumentPanel.class);
							String curDir = prefs.get("currentDir", System
									.getProperty("user.home"));
							fc.setCurrentDirectory(new File(curDir));
							int returnVal = fc
									.showSaveDialog(DataDocumentPanel.this);
							if (returnVal != JFileChooser.APPROVE_OPTION) {
								return;
							}
							prefs.put("currentDir", fc.getCurrentDirectory()
									.toString());
							File file = fc.getSelectedFile();

							try {
								InputStream stream = null;
								stream = scheme.dereference(dataManager);
								FileOutputStream fos = new FileOutputStream(
										file);
								IOUtils.copy(stream, fos);
								stream.close();
								fos.close();
							} catch (IOException ioe) {
								JOptionPane
										.showMessageDialog(
												DataDocumentPanel.this,
												"Problem saving data : \n"
														+ ioe.getMessage(),
												"Exception!",
												JOptionPane.ERROR_MESSAGE);
							} catch (DereferenceException e1) {
								logger.warn("Problem saving data", e1);
							}
						}
					});
					refPanel.add(blobType, c);
					continue;
				}
			} catch (DereferenceException e) {
				logger.warn("Cannot dereference " + id, e);
			} catch (IOException e) {
				logger.warn("Problem reading data stream" , e);
			} catch (MagicParseException e) {
				logger.warn("Could not find mime type from " + id, e);
			} catch (MagicMatchNotFoundException e) {
				logger.warn("Mime type of " + e + " not recognised", e);
			} catch (MagicException e) {
				logger.warn("Problem with finding mime type", e);
			}
			// Show simple version instead
			JLabel refSchemeType = new JLabel(scheme.getClass().getSimpleName()
					+ " " + scheme.toString());
			refPanel.add(refSchemeType, c);
		}
		return refPanel;
	}
}
