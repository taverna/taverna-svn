/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.taverna.t2.matlabactivity;

import javax.swing.JButton;
import javax.swing.JFrame;
import net.sf.taverna.t2.activities.matlab.MatActivity;
import net.sf.taverna.t2.activities.matlab.MatActivityConfigurationBean;
import net.sf.taverna.t2.matlabactivity.views.MatActivityViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.MimeTypeConfig;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author user
 */
public class MatActivityViewerTest {

    @Test
    public void testMain() {
        // TODO Auto-generated method stub

        JFrame frame = new JFrame();

        MatActivity matActivity = new MatActivity();
        MatActivityConfigurationBean bean = new MatActivityConfigurationBean();
        try {
            matActivity.configure(bean);
        } catch (ActivityConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ContextualViewFactory viewFactoryForBeanType = ContextualViewFactoryRegistry.
                getInstance().getViewFactoryForObject(matActivity);
        assertNotNull("The beanshsell view factory should not be null",
                viewFactoryForBeanType);
        assertTrue("Was not a  Beanshell view factory",
                viewFactoryForBeanType instanceof MatActivityViewFactory);
        ContextualView viewType = viewFactoryForBeanType.getView(matActivity);
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
