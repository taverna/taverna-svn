package net.sf.taverna.raven.launcher;


public class Launcher {

	public static void main(String[] args) {
		Launcher launcher = new Launcher();
		launcher.launchArgs(args);
	}

	public void launchArgs(String[] args) {
		ApplicationConfig config = ApplicationConfig.getInstance();
		System.out
				.println("Application name is " + config.getApplicationName());
		System.out.println("Application title is "
				+ config.getApplicationTitle());
	}
}
