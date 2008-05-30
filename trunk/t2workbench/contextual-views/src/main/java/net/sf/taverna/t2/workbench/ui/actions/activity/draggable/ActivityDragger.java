package net.sf.taverna.t2.workbench.ui.actions.activity.draggable;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactoryRegistry;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

public class ActivityDragger extends JFrame {

	private DataFlavor dataFlavor;

	public ActivityDragger() {

		try {
			dataFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType
							+ ";class=net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		setLayout(new GridBagLayout());
		final JTextField dragTextArea = new JTextField("hello there");
		dragTextArea.setBorder(javax.swing.BorderFactory.createTitledBorder(
				null, null,
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		StringConstantConfigurationBean bean1 = new StringConstantConfigurationBean();
		bean1.setValue("hello there");
		StringConstantActivity activity1 = new StringConstantActivity();
		try {
			activity1.configure(bean1);
		} catch (ActivityConfigurationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		StringConstantTransferHandler handler1 = new StringConstantTransferHandler(
				bean1, activity1);
		dragTextArea.setTransferHandler(handler1);
		dragTextArea.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				System.out.println("clicked1");
				StringConstantTransferHandler transferHandler = (StringConstantTransferHandler)dragTextArea.getTransferHandler();
				ActivityViewFactory viewFactoryForBeanType = ActivityViewFactoryRegistry
						.getInstance().getViewFactoryForBeanType(transferHandler.getActivity());
				ActivityContextualView viewType = viewFactoryForBeanType
						.getView(transferHandler.getActivity());
				viewType.setVisible(true);
			}

			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				System.out.println("entered1");
			}

			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				System.out.println("exited1");
			}

			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				System.out.println("pressed1");
			}

			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				System.out.println("released1");
				
			}

		});
		dragTextArea.setDropTarget(new DropTarget() {

			@Override
			public synchronized void drop(DropTargetDropEvent dtde) {
				try {
					List transferData = (List) dtde.getTransferable()
							.getTransferData(dataFlavor);
					StringConstantConfigurationBean bean = (StringConstantConfigurationBean) transferData
							.get(0);
					String value = bean.getValue();
					System.out.println("value: " + value);
					if (value != null) {
						dragTextArea.setText(value);
						StringConstantTransferHandler th = (StringConstantTransferHandler) dragTextArea
								.getTransferHandler();
						th.setBean(bean);
					}
					Activity activity = (Activity) transferData.get(1);
					if (activity != null) {
						StringConstantTransferHandler th = (StringConstantTransferHandler) dragTextArea
								.getTransferHandler();
						th.setActivity(activity);
						activity.configure(bean);
					}
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

		dragTextArea.setDragEnabled(true);

		final JTextField dropTextArea = new JTextField("how are you doing");
		dropTextArea.setBorder(javax.swing.BorderFactory.createTitledBorder(
				null, null,
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new java.awt.Font("Lucida Grande", 1, 12)));
		StringConstantConfigurationBean bean2 = new StringConstantConfigurationBean();
		bean2.setValue("how are you doing");
		StringConstantActivity activity2 = new StringConstantActivity();
		try {
			activity2.configure(bean2);
		} catch (ActivityConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		StringConstantTransferHandler handler2 = new StringConstantTransferHandler(
				bean2, activity2);
		dropTextArea.setTransferHandler(handler2);
		dropTextArea.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				System.out.println("clicked2");
				StringConstantTransferHandler transferHandler = (StringConstantTransferHandler)dropTextArea.getTransferHandler();
				ActivityViewFactory viewFactoryForBeanType = ActivityViewFactoryRegistry
						.getInstance().getViewFactoryForBeanType(transferHandler.getActivity());
				ActivityContextualView viewType = viewFactoryForBeanType
						.getView(transferHandler.getActivity());
				viewType.setVisible(true);		
			}

			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				System.out.println("entered2");
			}

			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				System.out.println("exited2");
			}

			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				System.out.println("pressed2");
			}

			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				System.out.println("released2");
						
			}

		});
		dropTextArea.setDragEnabled(true);
		dropTextArea.setDropTarget(new DropTarget() {

			@Override
			public synchronized void drop(DropTargetDropEvent dtde) {
				try {
					List transferData = (List) dtde.getTransferable()
							.getTransferData(dataFlavor);
					StringConstantConfigurationBean bean = (StringConstantConfigurationBean) transferData
							.get(0);
					String value = bean.getValue();
					System.out.println("value: " + value);
					if (value != null) {
						dropTextArea.setText(value);
						StringConstantTransferHandler th = (StringConstantTransferHandler) dropTextArea
								.getTransferHandler();
						th.setBean(bean);
					}
					Activity activity = (Activity) transferData.get(1);
					if (activity != null) {
						StringConstantTransferHandler th = (StringConstantTransferHandler) dropTextArea
								.getTransferHandler();
						th.setActivity(activity);
						activity.configure(bean);
					}
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

		GridBagConstraints outerConstraint = new GridBagConstraints();
		outerConstraint.anchor = GridBagConstraints.FIRST_LINE_START;
		outerConstraint.gridx = 0;
		outerConstraint.gridy = 0;
		outerConstraint.weightx = 0;
		outerConstraint.weighty = 0;
		outerConstraint.fill = GridBagConstraints.BOTH;

		add(dragTextArea, outerConstraint);
		outerConstraint.gridy = 1;
		add(dropTextArea, outerConstraint);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ActivityDragger activityDragger = new ActivityDragger();
		activityDragger.setSize(500, 500);
		activityDragger.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		activityDragger.setVisible(true);
	}

}
