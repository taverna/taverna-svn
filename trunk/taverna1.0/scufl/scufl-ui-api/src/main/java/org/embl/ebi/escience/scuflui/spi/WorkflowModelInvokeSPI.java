package org.embl.ebi.escience.scuflui.spi;

import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflui.InputsNotMatchingException;

/**
 * Interface for classes that in addition to the WorkflowModelViewSPI behaviour
 * also want to be notified when ModelMap.INVOKE_WORKFLOW has been set.
 * <p>
 * attachToModel(ScuflModel) will be called either when ModelMap.INVOKE_WORKFLOW
 * or ModelMap.CURRENT_WORKFLOW has been set. If the class needs to
 * differensiate between the two it can inspect the ModelMap.
 * 
 * @see org.embl.ebi.escience.scuflui.shared.ModelMap
 * @see WorkflowModelViewSPI
 * @author Stian Soiland
 * 
 */
public interface WorkflowModelInvokeSPI extends WorkflowModelViewSPI {
    public void setWorkflowInputs(Map<String, DataThing> inputs)
            throws InputsNotMatchingException;
}