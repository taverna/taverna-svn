/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

// Utility Imports
import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.scufl.Processor;
/**
 * Represents an alternate processor to be used
 * in case of failures in the primary. Contains
 * the Processor object to use as an alternate
 * and two Map objects, these act as translations
 * between the input and output names of the original
 * and substitute processor.<p>
 * For example, suppose the original processor has
 * inputs as follows :<p>
 * <table cellpadding="2" cellspacing="2" border="1">
 * <tr bgcolor="#eeeeff"><td>Name</td><td>Description</td><td>Type</td></tr>
 * <tr><td>inseq</td><td>Input sequence</td><td>'text/plain'</td></tr>
 * <tr><td>format</td><td>Desired format</td><td>'text/plain'</td></tr>
 * </table><p>
 * and the substitute has a similar set of inputs but with slightly
 * different names :<p>
 * <table cellpadding="2" cellspacing="2" border="1">
 * <tr bgcolor="#eeeeff"><td>Name</td><td>Description</td><td>Type</td></tr>
 * <tr><td>seq</td><td>Input sequence</td><td>'text/plain'</td></tr>
 * <tr><td>sformat</td><td>Desired format</td><td>'text/plain'</td></tr>
 * </table><p>
 * In this case the inputMapping object would contain the following :<p>
 * <table cellpadding="2" cellspacing="2" border="1">
 * <tr bgcolor="#eeeeff"><td>Key</td><td>Value</td></tr>
 * <tr><td>inseq</td><td>seq</td></tr>
 * <tr><td>format</td><td>sformat</td></tr>
 * </table><p>
 * This allows the task implementation to connect the appropriate inputs
 * to the alternate processor. A similar map would exist for the outputs,
 * again with the original or primary name as the key and alternate name
 * as the value.
 */
public class AlternateProcessor {
    
    private Processor alternate;
    private Map inputMapping = new HashMap();
    private Map outputMapping = new HashMap();
    
    public AlternateProcessor(Processor alternate) {
	this.alternate = alternate;
    }
    
    /**
     * Return the alternate processor object. This Processor
     * object will not be bound to the same workflow model
     * instance as the alternate, it will most of the time
     * not be bound to one at all. It is only intended to
     * be used to provide the alternate functionality and
     * not as the primary description. For this reason it is
     * certainly recommended to only use substitute processors
     * which are at least vaguely equivalent in operation, but
     * of course there's no way we either should or could 
     * enforce this. Use with care!
     */
    public Processor getProcessor() {
	return this.alternate;
    }
    
    /**
     * Get the input mappings, a Map object with
     * keys being the original or primary input names
     * and values being the corresponding names of inputs
     * on the alternate processor this holder class
     * contains.
     */
    public Map getInputMapping() {
	return this.inputMapping;
    }

    /**
     * Get the output mappings, a Map object with
     * keys being the original or primary output names
     * and values being the corresponding names of outputs
     * on the alternate processor this holder class
     * contains.
     */
    public Map getOutputMapping() {
	return this.outputMapping;
    }

}
