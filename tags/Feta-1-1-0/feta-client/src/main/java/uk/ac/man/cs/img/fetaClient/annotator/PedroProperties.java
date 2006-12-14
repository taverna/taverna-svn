/*
 * PedroProperties.java
 *
 * Created on January 25, 2005, 10:49 AM
 */

package uk.ac.man.cs.img.fetaClient.annotator;

import java.io.File;
import java.io.FileWriter;

import uk.ac.man.cs.img.fetaClient.queryGUI.taverna.FetaClientProperties;

/**
 * 
 * @author alperp
 */
public class PedroProperties {

	// Pedro needs this property file to exist in order to start up
	// so we dynamically generate it
	private static final String propSchemaName = "pedro.session.schema";

	private static final String propMainConfName = "pedro.session.mainConfiguration";

	private static final String propFileExtName = "pedro.session.fileExtensionsToLaunch";

	private static final String propFavFilesName = "pedro.session.favouritesFile";

	private static final String propIconDirName = "pedro.session.iconDirectory";

	private static final String propResourceDirName = "pedro.session.resourceDirectory";

	private static final String propLibDirName = "pedro.session.libraryDirectory";

	private static final String propHelpDirName = "pedro.session.helpDirectory";

	private static final String propDocDirName = "pedro.session.documentDirectory";

	private static final String propSchemaValue = "services" + "/" + "model"
			+ "/" + "common.xsd";

	private static final String propMainConfValue = "services" + "/" + "config"
			+ "/" + "ConfigurationFile.xml";

	private static final String propFileExtValue = "services" + "/" + "config"
			+ "/" + "FileExtensionsToLaunch.xml";

	private static final String propFavFilesValue = "services" + "/" + "config"
			+ "/" + "FavouriteFiles.xml";

	private static final String propIconDirValue = "img";

	private static final String propResourceDirValue = "services" + "/"
			+ "resources";

	private static final String propLibDirValue = "services" + "/" + "lib";

	private static final String propHelpDirValue = "help";

	private static final String propDocDirValue = "services" + "/" + "doc";

	private File pedroPropFile;

	/**
	 * Creates temporay PedroProperties file with respect to Taverna Home
	 * directory
	 */
	public PedroProperties() {

		try {

			String tavernaHomeDirPath = FetaClientProperties.getPropertiesDir()
					+ File.separator;
			String pluginsPath = tavernaHomeDirPath + "plugins"
					+ File.separator;
			String pedroHomeDirPath = tavernaHomeDirPath + "plugins"
					+ File.separator + "pedro" + File.separator;
			pedroPropFile = new File(pedroHomeDirPath + "pedro.properties");
			FileWriter out = new FileWriter(pedroPropFile);
			out.write(propSchemaName + " = "
					+ pedroHomeDirPath.replace('\\', '/') + propSchemaValue
					+ "\n");
			out.write(propMainConfName + " = "
					+ pedroHomeDirPath.replace('\\', '/') + propMainConfValue
					+ "\n");
			out.write(propFileExtName + " = "
					+ pedroHomeDirPath.replace('\\', '/') + propFileExtValue
					+ "\n");
			out.write(propFavFilesName + " = "
					+ pedroHomeDirPath.replace('\\', '/') + propFavFilesValue
					+ "\n");
			out.write(propIconDirName + " = "
					+ pedroHomeDirPath.replace('\\', '/') + propIconDirValue
					+ "\n");
			out.write(propResourceDirName + " = "
					+ pedroHomeDirPath.replace('\\', '/')
					+ propResourceDirValue + "\n");
			out.write(propLibDirName + " = "
					+ pedroHomeDirPath.replace('\\', '/') + propLibDirValue
					+ "\n");
			// out.write(propLibDirName + " = " + pluginsPath.replace('\\','/')
			// +"\n");
			out.write(propHelpDirName + " = "
					+ pedroHomeDirPath.replace('\\', '/') + propHelpDirValue
					+ "\n");
			out.write(propDocDirName + " = "
					+ pedroHomeDirPath.replace('\\', '/') + propDocDirValue
					+ "\n");

			out.close();

		} catch (java.io.IOException ioExp) {
			ioExp.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	public File getPropertiesFile() {
		return pedroPropFile;
	}

}
