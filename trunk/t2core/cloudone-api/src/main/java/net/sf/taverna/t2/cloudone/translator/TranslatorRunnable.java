package net.sf.taverna.t2.cloudone.translator;

import net.sf.taverna.t2.cloudone.ReferenceScheme;

public interface TranslatorRunnable extends Runnable {

	public void run();

	public ReferenceScheme getReferenceScheme();

	public boolean isFinished();

	public Exception getException();

}