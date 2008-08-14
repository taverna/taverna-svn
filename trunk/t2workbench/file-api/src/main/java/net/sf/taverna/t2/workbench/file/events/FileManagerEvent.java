package net.sf.taverna.t2.workbench.file.events;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.workbench.file.FileManager;

/**
 * An event given to {@link FileManager} observers registered using
 * {@link Observable#addObserver(net.sf.taverna.t2.lang.observer.Observer)}.
 * 
 * @see AbstractDataflowEvent
 * @see ClosedDataflowEvent
 * @see OpenedDataflowEvent
 * @see SavedDataflowEvent
 * @see SetCurrentDataflowEvent
 * @author Stian Soiland-Reyes
 * 
 */
public class FileManagerEvent {

}
