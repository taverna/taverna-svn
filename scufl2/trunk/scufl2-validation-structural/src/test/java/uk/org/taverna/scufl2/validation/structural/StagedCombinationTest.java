package uk.org.taverna.scufl2.validation.structural;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.iterationstrategy.CrossProduct;
import uk.org.taverna.scufl2.api.iterationstrategy.DotProduct;
import uk.org.taverna.scufl2.api.iterationstrategy.IterationStrategyStack;
import uk.org.taverna.scufl2.api.iterationstrategy.PortNode;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;


public class StagedCombinationTest {
	
	private InputProcessorPort a;
	private InputProcessorPort b;
	
	private CrossProduct getCross(int depthA, int depthB) {
		a = new InputProcessorPort();
		a.setName("a");
		a.setDepth(0);
		CrossProduct cp = new CrossProduct();
		PortNode nipn1 = new PortNode(cp, a);
		nipn1.setDesiredDepth(depthA);
		
		b = new InputProcessorPort();
		b.setName("b");
		b.setDepth(0);
		PortNode nipn2 = new PortNode(cp, b);
		nipn2.setDesiredDepth(depthB);

		return cp;
	}
	
	private DotProduct getDot(int depthA, int depthB) {
		a = new InputProcessorPort();
		a.setName("a");
		a.setDepth(0);
		DotProduct dp = new DotProduct();
		PortNode nipn1 = new PortNode(dp, a);
		nipn1.setDesiredDepth(depthA);
		
		b = new InputProcessorPort();
		b.setName("b");
		b.setDepth(0);
		PortNode nipn2 = new PortNode(dp, b);
		nipn2.setDesiredDepth(depthB);

		return dp;
	}
	
	/**
	 * Test whether Paul's example of iterating with dot product then cross
	 * product can typecheck in a single staged iteration. This was an example
	 * where the user had two lists of folders (a1, a2, b1, b2, c1, c2) and
	 * wanted to compare all the contents of each 'a' folder with the other 'a'
	 * folder and so on, doing a dot match to only compare a1 and a2 then a
	 * cross product join within each pair to compare all contents of a1 with
	 * all contents of a2. This appears to work!
	 */
	@Test
	public void testStagedCombinationOfDotAndCross() {
		Processor p = new Processor();
		IterationStrategyStack iss = new IterationStrategyStack(p);
		iss.add(getDot(1, 1));	
		iss.add(getCross(0, 0));
		
		StructuralValidator sv = new StructuralValidator();
		sv.getValidatorState().setProcessor(p);
		Map<InputProcessorPort, Integer> tempDepths = new HashMap<InputProcessorPort, Integer>();
		tempDepths.put(a,2);
		tempDepths.put(b,2);

		assertEquals(Integer.valueOf(3), sv.calculateResultWrappingDepth(tempDepths));
	}

}
