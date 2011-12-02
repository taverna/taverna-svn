package net.sf.taverna.portal.wireit.event;

import net.sf.taverna.portal.wireit.exception.WireItRunException;

/**
 * Interface for any module, or more likely inner class of a module which accepts the output from an upstream module.
 * @author Christian
 */
public interface OutputListener {
    
    /**
     * Receives the Object on to any upstream module.
     * <p>
     * This could cuase the module to execute.
     * Any log information is written to the outputBuilder.
     * @param output Information being passed from one module to another.
     * @param outputBuilder Logging buffer. 
     * @throws WireItRunException Something has gone wrong. This could be caused by exectution 
     *    or even one of the downstream modules.
     */
    public void outputReady(Object output, StringBuilder outputBuilder) throws WireItRunException;
}
