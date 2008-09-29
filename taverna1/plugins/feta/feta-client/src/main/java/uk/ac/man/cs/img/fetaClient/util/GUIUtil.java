/*
 * GUIUtil.java
 *
 * Created on February 24, 2005, 4:00 PM
 */

package uk.ac.man.cs.img.fetaClient.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.embl.ebi.escience.scuflui.shared.UIUtils;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;
import org.w3c.dom.Document;

import uk.ac.man.cs.img.fetaClient.annotator.AnnotatorLauncher;

/**
 * 
 * @author alperp
 */
public class GUIUtil {
	public static void launchAnnotator(String descLoc) {

		try {
			InputStream fetaStream = null;

			if ((descLoc != null) && (descLoc.length() > 0)) {
				try {
					URL url = new URL(descLoc);
					URLConnection connection = url.openConnection();
					fetaStream = connection.getInputStream();

				} catch (Exception e) {
					e.printStackTrace();
					fetaStream = new FileInputStream(new File(descLoc));
				}
			} else {
				fetaStream = null;
			}
			launchAnnotator(fetaStream);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void launchAnnotator(InputStream fetaStream) {

		AnnotatorLauncher pedroLauncher;
		try {
			if (fetaStream != null) {
				pedroLauncher = new AnnotatorLauncher(fetaStream);
			} else {
				pedroLauncher = new AnnotatorLauncher();
			}
			// if (Workbench.workbench != null) {
			// UIUtils.createFrame(Workbench.workbench.model,
			// (ScuflUIComponent)pedroLauncher, 100,100, 30, 30);
			UIUtils.createFrame(pedroLauncher.getModel(),
					(UIComponentSPI) pedroLauncher, 100, 100, 30, 30);

			// }
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void launchAnnotator(org.w3c.dom.Document fetaDOM) {
		try {

			ByteArrayInputStream fetaStream;
			if (fetaDOM != null) {
				OutputFormat format = new OutputFormat((Document) fetaDOM);
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				XMLSerializer serializer = new XMLSerializer(outStream, format);
				serializer.asDOMSerializer();
				serializer.serialize(fetaDOM);
				fetaStream = new ByteArrayInputStream(outStream.toByteArray());
			} else {
				fetaStream = null;
			}
			AnnotatorLauncher pedroLauncher;

			if (fetaStream != null) {
				pedroLauncher = new AnnotatorLauncher(fetaStream);
			} else {
				pedroLauncher = new AnnotatorLauncher();
			}
			// if (Workbench.workbench != null) {
			// UIUtils.createFrame(Workbench.workbench.model,
			// (ScuflUIComponent)pedroLauncher, 100,100, 30, 30);
			UIUtils.createFrame(pedroLauncher.getModel(),
					(UIComponentSPI) pedroLauncher, 100, 100, 30, 30);
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
