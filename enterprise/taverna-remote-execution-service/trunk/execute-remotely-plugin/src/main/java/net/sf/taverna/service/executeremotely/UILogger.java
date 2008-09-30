package net.sf.taverna.service.executeremotely;

/**
 * Interface for user interface logging. If no user interface is attached, use
 * {@link DummyUILogger} which does nothing.
 * 
 * @author Stian Soiland
 */
public interface UILogger {
	public void log(Exception ex);

	public void log(String msg);

	public class DummyUILogger implements UILogger {
		public void log(Exception ex) {
		}

		public void log(String msg) {
		}
	}
}