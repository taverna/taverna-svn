package net.sourceforge.taverna.scuflworkers.io;

import java.util.HashMap;

import junit.framework.TestCase;
import net.sourceforge.taverna.baclava.DataThingAdapter;

/**
 * This class
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class DataRangeTaskTest extends TestCase {

    DataRangeTask task = new DataRangeTask();
    
    public void testExecute() {
        HashMap inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        
        
    }

    public void testConvertCoords() {
    }

}
