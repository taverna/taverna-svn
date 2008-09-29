package net.sf.taverna.t2.plugin.pretest;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

public class HealthReportCellRenderer implements TreeCellRenderer {

	private TreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();
	private Map<Status,Icon> iconMap = new HashMap<Status, Icon>();
	
	public HealthReportCellRenderer() {
		ImageIcon icon = new ImageIcon(HealthReportCellRenderer.class.getResource("/icons/ok.png"));
		iconMap.put(Status.OK, icon);
		icon = new ImageIcon(HealthReportCellRenderer.class.getResource("/icons/warning.png"));
		iconMap.put(Status.WARNING, icon);
		icon = new ImageIcon(HealthReportCellRenderer.class.getResource("/icons/severe.png"));
		iconMap.put(Status.SEVERE, icon);
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		Component result=null;
		if (value instanceof HealthReport) {
			HealthReport report = (HealthReport)value;
			String message = report.getMessage()!=null && report.getMessage().trim().length()>0 ? ":"+report.getMessage() : "";
			result = new JLabel(report.getSubject()+message,iconForStatus(report.getStatus()),JLabel.LEFT);
		}
		else {
			result = defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		}
		result.setBackground(tree.getBackground());
		return result;
	}

	private Icon iconForStatus(Status status) {
		return iconMap.get(status);
	}

}
