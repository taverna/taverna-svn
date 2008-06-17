package net.sf.taverna.t2.workbench.ui.activitypalette;

import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactoryRegistry;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

/**
 * Demonstrates the T2 activity palette functionality. Shows how drag and drop
 * operations from the {@link ActivityTree} will work along with a
 * {@link DropTarget} on the receiving component
 * 
 * @author Ian Dunlop
 * 
 */
public class ActivityPaletteTester {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setLayout(new BorderLayout());
		final JPanel viewPanel = new JPanel();
		frame.add(viewPanel, BorderLayout.SOUTH);

		final JTextArea textArea = new JTextArea();
		textArea.setText("Drop stuff here");
		textArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		textArea.setVisible(true);
		textArea.setDragEnabled(true);

		textArea.setDropTarget(new DropTarget() {

			@Override
			public synchronized void drop(DropTargetDropEvent dtde) {
				try {
					ActivityAndBeanWrapper transferData = null;
					try {
						transferData = (ActivityAndBeanWrapper) dtde
								.getTransferable()
								.getTransferData(
										new DataFlavor(
												DataFlavor.javaJVMLocalObjectMimeType
														+ ";class=net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper"));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					Activity activity = transferData.getActivity();
					activity.configure(transferData.getBean());
					System.out.println("activity: "
							+ activity.getClass().getName());
					ActivityViewFactory factory = ActivityViewFactoryRegistry
							.getInstance().getViewFactoryForBeanType(
									(Activity<?>) activity);
					ActivityContextualView view = factory.getView(activity);
					viewPanel.removeAll();
					viewPanel.add(view);
					viewPanel.revalidate();

				} catch (UnsupportedFlavorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ActivityConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
		frame.add(textArea, BorderLayout.LINE_START);

		ActivityPaletteComponent palette = new ActivityPaletteComponent();
		frame.add(palette, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(300, 600);
		frame.setVisible(true);

	}

}
