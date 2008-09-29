package net.sf.taverna.t2.workflowmodel;

import java.util.List;

import net.sf.taverna.t2.spi.SPIRegistry;

@SuppressWarnings("unchecked")
public class EditsRegistry extends SPIRegistry<Edits> {

	private static EditsRegistry instance;
	
	protected EditsRegistry() {
		super(Edits.class);
	}

	public static synchronized EditsRegistry getInstance() {
		if (instance == null) {
			instance = new EditsRegistry();
		}
		return instance;
	}

	public static Edits getEdits() {
		List<Edits> instances = getInstance().getInstances();
		Edits result = null;
		if (instances.size() == 0) {
			System.out.println("No Edits implementation defined");
		} else {
			if (instances.size() > 1)
				System.out
						.println("More that 1 Edits implementation defined, using the first");
			result=instances.get(0);
		}
		return result;
	}
}
