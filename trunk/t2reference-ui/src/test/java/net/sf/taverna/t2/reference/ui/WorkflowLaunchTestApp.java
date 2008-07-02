package net.sf.taverna.t2.reference.ui;

import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

import org.springframework.context.ApplicationContext;

public class WorkflowLaunchTestApp {

	private static WorkflowLaunchPanel wlp;

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
				"inMemoryReferenceServiceTestContext.xml");
		final ReferenceService referenceService = (ReferenceService) appContext
				.getBean("t2reference.service.referenceService");
		final ReferenceContext refContext = null;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI(referenceService, refContext);
			}
		});
	}

	@SuppressWarnings("serial")
	private static void createAndShowGUI(ReferenceService referenceService,
			ReferenceContext referenceContext) {
		// Create and set up the window.
		JFrame frame = new JFrame("PreRegistrationTreeModel Demo 3");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		wlp = new WorkflowLaunchPanel(referenceService, referenceContext) {
			@Override
			public void handleLaunch(Map<String, T2Reference> workflowInputs) {
				System.out.println("Launch...");
				for (String inputName : workflowInputs.keySet()) {
					System.out.println(inputName + " = "
							+ workflowInputs.get(inputName).toString());
				}
			}
		};
		wlp.setOpaque(true); // content panes must be opaque

		// Add some inputs
		wlp.addInputTab("Single item", 0);
		wlp.addInputTab("List", 1);
		wlp.addInputTab("List of lists", 2);
		wlp.addInputTab("List of lists of lists", 3);
		wlp.addInputTab("Really deep list", 6);

		frame.setContentPane(wlp);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}
}
