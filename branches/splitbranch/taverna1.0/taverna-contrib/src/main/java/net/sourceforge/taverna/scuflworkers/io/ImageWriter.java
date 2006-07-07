package net.sourceforge.taverna.scuflworkers.io;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

//TODO: finish ImageWriter
/**
 * This processor writes images out to a file.
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 * 
 * @tavinput image An image byte array.
 * @tavoutput complete A dummy value indicating that the write operation is
 *            complete.
 */
public class ImageWriter implements LocalWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		Map outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		DataThing imageThing = (DataThing) inputMap.get("image");
		byte[] imageArray = (byte[]) imageThing.getDataObject();

		// get the image height
		String heightStr = inAdapter.getString("height");
		if (heightStr == null || heightStr.equals("")) {
			throw new TaskExecutionException("The 'height' cannot be null");
		}
		int height = Integer.parseInt(heightStr);

		// get the image width
		String widthStr = inAdapter.getString("width");
		if (widthStr == null || widthStr.equals("")) {
			throw new TaskExecutionException("The 'width' cannot be null");
		}
		int width = Integer.parseInt(widthStr);

		// get the output file
		String outputFileStr = inAdapter.getString("outputfile");
		if (outputFileStr == null || outputFileStr.equals("")) {
			throw new TaskExecutionException("The 'outputfile' cannot be null");
		}
		File outputFile = new File(outputFileStr);

		// Create a buffered image in which to draw
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		String imageType = inAdapter.getString("imageType");
		try {
			bufferedImage = ImageIO.read(new ByteArrayInputStream(imageArray));
			ImageIO.write(bufferedImage, imageType, outputFile);
			outAdapter.putString("complete", "true");
		} catch (IOException e) {
			throw new TaskExecutionException(e);
		}

		return outputMap;
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "image", "height", "width", "imageType", "outputfile" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'image/*'", "'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
	 */
	public String[] outputNames() {
		return new String[] { "complete" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "'text/plain'" };
	}

}
