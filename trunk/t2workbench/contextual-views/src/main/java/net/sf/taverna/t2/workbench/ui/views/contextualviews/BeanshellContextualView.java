package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.StringConstantConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

import org.syntax.jedit.JEditTextArea;
import org.syntax.jedit.TextAreaDefaults;
import org.syntax.jedit.tokenmarker.JavaTokenMarker;

public class BeanshellContextualView extends
HTMLBasedActivityContextualView<BeanshellActivityConfigurationBean> {

	public BeanshellContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected String getRawTableRowsHtml() {
		// TODO Auto-generated method stub
		 String html = "<tr><th>Input Port Name</th><th>Depth</th></tr>";
		for (ActivityInputPortDefinitionBean bean:getConfigBean().getInputPortDefinitions()) {
			html = html + "<tr><td>" + 	bean.getName() + "</td><td>" + bean.getDepth() + "</td></tr>";
		}
		return html;
	}

	@Override
	protected String getViewTitle() {
		// TODO Auto-generated method stub
		return "Beanshell Contextual View";
	}

	@Override
	protected void setNewValues() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Action getConfigureAction() {
		// TODO Auto-generated method stub
		return new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				BeanshellConfigView beanshellConfigView = new BeanshellConfigView((BeanshellActivity)getActivity());
				JFrame frame = new JFrame();
				frame.add(beanshellConfigView);
				try {
				frame.setVisible(true);
				}catch(Exception e1) {
					System.out.println(e1.toString());
				}
			}
			
		};
	}


}
