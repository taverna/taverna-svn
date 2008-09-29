package net.sourceforge.taverna.scuflworkers.io;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor reads an image file and places it in the output hash map.
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 * 
 * @tavinput filename The name of the binary image to be read (either a GIF,
 *           JPEG or PNG)
 * @tavoutput image A byte array containing the image data.
 * @tavoutput height The height of the image in pixels
 * @tavoutput width The width of the image in pixels
 */
public class ImageReader implements LocalWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		HashMap outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		String fileName = inAdapter.getString("filename");

		// do some error checking
		if (fileName == null || fileName.equals("")) {
			throw new TaskExecutionException("The 'filename' port must contain a value.");
		}
		File file = new File(fileName);

		if (!file.exists()) {
			throw new TaskExecutionException("The file: " + fileName + " does not exist.");
		}

		if (!file.isFile()) {
			throw new TaskExecutionException("The file: " + fileName + " is not a file.");
		}

		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();

			BufferedImage image = ImageIO.read(file);
			ImageIO.write(image, getImageFormatName(file), stream);
			byte[] imageBytes = stream.toByteArray();

			outputMap.put("image", new DataThing(imageBytes));
			outAdapter.putInt("height", image.getHeight());
			outAdapter.putInt("width", image.getWidth());

		} catch (IOException e) {
			throw new TaskExecutionException(e);
		}

		return outputMap;
	}

	private String getImageFormatName(File file) {
		String format = null;
		String ext = getExtension(file);
		if ("jpeg".equalsIgnoreCase(ext) || "jpg".equalsIgnoreCase(ext)) {
			format = "JPEG";
		} else if ("png".equalsIgnoreCase(ext)) {
			format = "PNG";
		} else if ("gif".equalsIgnoreCase(ext)) {
			format = "GIF";
		}
		return format;
	}

	private String getExtension(File file) {
		String ext = null;
		String name = file.getName();
		int index = name.lastIndexOf(".");
		ext = name.substring(index);
		return ext;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "filename" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
	 */
	public String[] outputNames() {
		return new String[] { "image", "height", "width" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "'image/*'", "'text/plain'", "'text/plain'" };
	}

}
