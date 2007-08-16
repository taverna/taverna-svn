package net.sourceforge.taverna.scuflworkers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

/**
 * This class is provides a simple set of test to verify that a LocalWorker
 * is functioning properly.  To use this class, simply instantiate it can call
 * the tests that you need.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public abstract class LocalWorkerTest extends TestCase{

    private LocalWorker worker;
    private boolean allOutputsRequired =false;
    private boolean allInputsRequired = false;

    /**
     * Constructor
     * @param worker   The LocalWorker instance to be tested.
     * @param allOutputsRequired  Determines if all outputValues are required to be in the output map.
     * @param allInputsRequired   Determines if all inputValues are required to be in the input map.
     */
    public LocalWorkerTest(LocalWorker worker, boolean allOutputsRequired, boolean allInputsRequired){
        this.worker = worker;
        this.allOutputsRequired = allOutputsRequired;
        this.allInputsRequired  = allInputsRequired;
    }
    
    /**
     * This test verifies that the number of inputs, and input types
     * are the same.  
     */
    public void testInputs(){
        String[] inputs = this.worker.inputNames();
        String[] inputTypes = this.worker.inputTypes();
        assertTrue("The inputNames and inputTypes were not equal",inputs.length==inputTypes.length);
        
    }
    
    /**
     * This test verifies that the number of outputs and output types
     * are the same.
     */
    public void testOutputs(){
        String[] outputs = this.worker.outputNames();
        String[] outputTypes = this.worker.outputTypes();
        assertTrue("The inputNames and inputTypes were not equal",outputs.length==outputTypes.length);       
    }
    
    /**
     * This method tests a given local worker by setting all of the input names to the values specified
     * in the inputValues array.  These values are loaded into the inputMap and the LocalWorker's execute
     * method is then called.
     * 
     * @param inputNames
     * @param inputValues
     * @throws Exception
     */
    public void testWithInputs(String[] inputNames, String inputValues[]) throws Exception{
        
        if (inputNames.length != inputValues.length){
            throw new Exception("The number of inputNames does not match the number of inputValues");
        }
        
        Map inputMap = new HashMap();

        
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        for(int i=0; i < inputNames.length; i++){           
            inAdapter.putString(inputNames[i],inputValues[i]);
        }
        
        testWithInputMap(inputMap);       
    }
    
    /**
     * This method tests the LocalWorker when it's passed an input map of values.
     * It verifies that the input map only contains keys specified in the inputNames
     * array of the LocalWorker.  It also verifies that all output values are also
     * present, and that the output map is neither null, nor empty.
     * @param inputMap
     */
    public void testWithInputMap(Map inputMap)throws Exception{
        Map outputMap = this.worker.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        if (allInputsRequired){
            String[] inputNames = this.worker.inputNames();
            Set keys = inputMap.keySet();
            assertTrue("The inputMap contains a different number of keys than those specified in the inputNames array.", keys.size() == inputNames.length);
            for (int i=0; i < inputNames.length; i++){
                assertTrue("",keys.contains(inputNames[i]));
            }
        }
        
        
        assertNotNull("The output map was null",outputMap);
        assertTrue("The output map was empty",!outputMap.isEmpty());
        
        if (this.allOutputsRequired){
          String[] outputNames = this.worker.outputNames();
          Set outKeys = outputMap.keySet();
          for(int i=0; i < outputNames.length; i++){
             assertTrue("The output map does not contain the key: " + outputNames[i],outKeys.contains(outputNames[i])); 
             Object currObj = outputMap.get(outputNames[i]);
             assertNotNull("The output object: " + outputNames[i] + " was null",currObj);
             assertTrue("The output object is not a DataThing", currObj instanceof DataThing);
          }
        }
    }
    

}
