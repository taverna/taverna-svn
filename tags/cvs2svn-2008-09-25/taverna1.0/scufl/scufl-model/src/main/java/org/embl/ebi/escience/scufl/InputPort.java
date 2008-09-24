/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A port that consumes data on behalf of a processor
 * 
 * @author Tom Oinn
 */
public class InputPort extends Port implements Serializable, Transferable {

	final public static DataFlavor FLAVOR = new DataFlavor(InputPort.class,
			"Input Port");

	static DataFlavor[] flavors = { FLAVOR };

	/**
	 * Non deterministic single select mode for multiple input links where the
	 * first link to populate the port with data 'wins' and removes all other
	 * links to prevent blocking. This is the default behaviour mode for input
	 * ports
	 */
	public static int NDSELECT = 0;

	/**
	 * Combine mode for multiple input links where each distinct input from all
	 * upstream links is inserted into a list, ordering is currently not defined
	 */
	public static int MERGE = 1;

	private int mergeMode = InputPort.NDSELECT;

	private String defaultValue;

	private boolean isOptional = false;

	String[] cv = null;

	/**
	 * Return the current merge mode for this input port
	 */
	public int getMergeMode() {
		return this.mergeMode;
	}

	/**
	 * Set the merge mode for this input port, this value must currently be one
	 * of InputPort.NDSELECT or InputPort.MERGE
	 */
	public void setMergeMode(int mode) {
		this.mergeMode = mode;
	}

	/**
	 * Does this input port have a default value?
	 */
	public boolean hasDefaultValue() {
		return (this.defaultValue != null);
	}

	/**
	 * Get the default value for this port, or null if there is no default
	 */
	public String getDefaultValue() {
		return this.defaultValue;
	}

	/**
	 * Get the default value wrapped up in however many layers of collections
	 * are required.
	 */
	public Object getWrappedDefaultValue() {
		Object result = this.defaultValue;
		int portDepth = (getSyntacticType().split("\\'")[0].length()) / 2;
		while (portDepth > 0) {
			portDepth--;
			List newList = new ArrayList();
			newList.add(result);
			result = newList;
		}
		return result;
	}

	/**
	 * Set the default value for this port, set to null to remove.
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		fireModelEvent(new MinorScuflModelEvent(this, "Set default value"));
	}

	/**
	 * Set a controlled list of allowed values for the default
	 */
	public void setControlledVocabulary(String[] values) {
		this.cv = values;
	}

	/**
	 * Get the list of allowed default values
	 */
	public String[] getControlledVocabulary() {
		return this.cv;
	}

	/**
	 * Does this port have a controlled vocab for default values?
	 */
	public boolean hasControlledVocabulary() {
		return (this.cv != null);
	}

	/**
	 * Is this input optional?
	 */
	public boolean isOptional() {
		return this.isOptional;
	}

	/**
	 * Is this input attached to anything?
	 */
	public boolean isBound() {
		ScuflModel model = getProcessor().getModel();
		if (model == null) {
			return false;
		}
		DataConstraint[] dc = model.getDataConstraints();
		for (int i = 0; i < dc.length; i++) {
			if (dc[i].getSink() == this) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Set whether the input is optional (default value if never set is that the
	 * input is mandatory)
	 */
	public void setOptional(boolean optional) {
		this.isOptional = optional;
	}

	/**
	 * Implements transferable interface
	 */
	public Object getTransferData(DataFlavor df)
			throws UnsupportedFlavorException, IOException {
		if (df.equals(FLAVOR)) {
			return this;
		} else {
			throw new UnsupportedFlavorException(df);
		}
	}

	/**
	 * Implements transferable interface
	 */
	public boolean isDataFlavorSupported(DataFlavor df) {
		return df.equals(FLAVOR);
	}

	/**
	 * Implements transferable interface
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public InputPort(Processor processor, String name)
			throws DuplicatePortNameException, PortCreationException {
		super(processor, name);
	}

}
