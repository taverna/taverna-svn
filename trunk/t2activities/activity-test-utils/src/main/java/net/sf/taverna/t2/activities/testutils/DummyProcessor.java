package net.sf.taverna.t2.activities.testutils;

import java.util.Properties;
import org.embl.ebi.escience.scufl.Processor;

/**
 *
 * @author Stuart Owen
 */
public class DummyProcessor extends Processor {
    
        public DummyProcessor() throws Exception {
            super(null,"wsdl");
        }
        public Properties getProperties() {
            return new Properties();
        }	
}
