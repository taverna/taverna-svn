package net.sf.taverna.t2.workbench.updatemanager;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.raven.plugins.PluginManager;
import net.sf.taverna.raven.plugins.ui.PluginManagerFrame;
import net.sf.taverna.raven.plugins.ui.UpdatesAvailableIcon;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

public class PluginMenuAction extends AbstractMenuAction {

	private static final String UPDATES_AND_PLUGINS = "Updates and plugins";
	
	@SuppressWarnings("serial")
	protected class SoftwareUpdates extends AbstractAction {

		public SoftwareUpdates() {
			super(UPDATES_AND_PLUGINS, UpdatesAvailableIcon.updateRecommendedIcon);
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
		}

		public void actionPerformed(ActionEvent e) {
			Component parent = null;
			if (e.getSource() instanceof Component) {
				parent = (Component) e.getSource();
			}
			final PluginManagerFrame pluginManagerUI = new PluginManagerFrame(
					PluginManager.getInstance());
			if (parent != null) {
				pluginManagerUI.setLocationRelativeTo(parent);
			}
			pluginManagerUI.setVisible(true);
		}
	}

	public PluginMenuAction() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#advanced"),
				100);
	}

	@Override
	protected Action createAction() {
		return new SoftwareUpdates();
	}

}
