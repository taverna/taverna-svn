package net.sf.taverna.t2.activities.localworker.views;

import java.awt.Frame;

import javax.swing.Action;

import net.sf.taverna.activities.localworker.actions.LocalworkerActivityConfigurationAction;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.localworker.LocalworkerActivity;
import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.annotationbeans.HostInstitution;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

public class LocalworkerActivityContextualView extends
		HTMLBasedActivityContextualView<BeanshellActivityConfigurationBean> {

	public LocalworkerActivityContextualView(Activity<?> activity) {
		super(activity);
	}

	@Override
	protected String getRawTableRowsHtml() {
		String html = "<tr><th>Input Port Name</th><th>Depth</th></tr>";
		for (ActivityInputPortDefinitionBean bean : getConfigBean()
				.getInputPortDefinitions()) {
			html = html + "<tr><td>" + bean.getName() + "</td><td>"
					+ bean.getDepth() + "</td></tr>";
		}
		html = html
				+ "<tr><th>Output Port Name</th><th>Depth</th><th>Granular Depth</th></tr>";
		for (ActivityOutputPortDefinitionBean bean : getConfigBean()
				.getOutputPortDefinitions()) {
			html = html + "<tr></td>" + bean.getName() + "</td><td>"
					+ bean.getDepth() + "</td><td>" + bean.getGranularDepth()
					+ "</td></tr>";
		}
		return html;
	}

	@Override
	protected String getViewTitle() {
		if (checkAnnotations()) {
			// this is a user defined localworker so use the correct name
			return "User Defined Local Worker";
		} else {

			return "Local Worker";
		}
	}

	private boolean checkAnnotations() {
		for (AnnotationChain chain : getActivity().getAnnotations()) {
			for (AnnotationAssertion<?> assertion : chain.getAssertions()) {
				Object detail = assertion.getDetail();
				System.out.println(detail.getClass().getName());
				if (detail instanceof HostInstitution) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new LocalworkerActivityConfigurationAction(
				(LocalworkerActivity) getActivity(), owner);
	}

}
