package net.sf.taverna.workbench.ui.beanshell.views;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.swing.JButton;
import javax.swing.JFrame;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.beanshell.views.BeanshellActivityViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactoryRegistry;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.MimeTypeConfig;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

public class BeanshellViewer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		JFrame frame = new JFrame();
		
		BeanshellActivity beanshellActivity = new BeanshellActivity();
		BeanshellActivityConfigurationBean bean = new BeanshellActivityConfigurationBean();
		try {
			beanshellActivity.configure(bean);
		} catch (ActivityConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ActivityViewFactory viewFactoryForBeanType = ActivityViewFactoryRegistry.getInstance().getViewFactoryForBeanType(beanshellActivity);
		assertNotNull("The beanshsell view factory should not be null",viewFactoryForBeanType);
		assertTrue("Was not a  Beanshell view factory", viewFactoryForBeanType instanceof BeanshellActivityViewFactory);
		ContextualView viewType = viewFactoryForBeanType.getView(beanshellActivity);
		JButton button = new JButton();
		button.addActionListener(viewType.getConfigureAction(null));
		frame.add(viewType);
		frame.add(button);
		frame.setVisible(true);
		
		
		JFrame mimeFrame = new JFrame();
		MimeTypeConfig mimeConf = new MimeTypeConfig();
		mimeFrame.add(mimeConf);
		mimeFrame.setTitle("mime");
		mimeFrame.setVisible(true);
		
		
		
	}

}
