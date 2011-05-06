/*******************************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.ui.servicepanel;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.events.AbstractProviderEvent;
import net.sf.taverna.t2.servicedescriptions.events.AbstractProviderNotification;
import net.sf.taverna.t2.servicedescriptions.events.PartialServiceDescriptionsNotification;
import net.sf.taverna.t2.servicedescriptions.events.ProviderErrorNotification;
import net.sf.taverna.t2.servicedescriptions.events.RemovedProviderEvent;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionProvidedEvent;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionRegistryEvent;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeModel;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeNode;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

import org.apache.log4j.Logger;

/**
 * A panel of available services
 * 
 * @author Stian Soiland-Reyes
 * 
 */
@SuppressWarnings("serial")
public class ServicePanel extends JPanel implements UIComponentSPI {

	private static Logger logger = Logger.getLogger(ServicePanel.class);

	private static final int INITIAL_BLANK_OUT_COUNTER = 2;

	public int blankOutCounter = 0;

	public static final String AVAILABLE_SERVICES = "Available services";
	public static final String MATCHING_SERVIES = "Matching services";
	public static final String NO_MATCHING_SERVICES = "No matching services";
	public static final String MOBY_OBJECTS = "MOBY Objects";
	
	/**
	 * A Comparable constant to be used with buildPathMap
	 */
	private static final String SERVICES = "4DA84170-7746-4817-8C2E-E29AF8B2984D";

	private static final int STATUS_LINE_MESSAGE_MS = 600;

	private TreeUpdaterThread updaterThread;

	private RootFilterTreeNode root = new RootFilterTreeNode(AVAILABLE_SERVICES);
	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	private ServiceTreePanel serviceTreePanel;

	private JLabel statusLine;

	private FilterTreeModel treeModel;

	protected ServiceDescriptionRegistryObserver serviceDescriptionRegistryObserver = new ServiceDescriptionRegistryObserver();

	protected Timer statusUpdateTimer;

	protected Object updateLock = new Object();
	
	private static ServicePathElementComparator servicePathElementComparator = new ServicePathElementComparator();

	public ServicePanel(ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
		serviceDescriptionRegistry
				.addObserver(serviceDescriptionRegistryObserver);
		initialise();
		TitledBorder border = new TitledBorder("Service panel");
		border.setTitleJustification(TitledBorder.CENTER);
		this.setBorder(border);
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Service panel";
	}

	public void onDisplay() {
	}

	public void onDispose() {
	}

	public void providerStatus(ServiceDescriptionProvider provider,
			String message) {
		logger.info(message + " " + provider);
		final String htmlMessage = "<html><small>" + message + " [" + provider
				+ "]</small></html>";

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				blankOutCounter = INITIAL_BLANK_OUT_COUNTER;
				statusLine.setText(htmlMessage);
				statusLine.setVisible(true);
			}
		});
	}

	protected void initialise() {
		removeAll();
		setLayout(new BorderLayout());
		treeModel = new FilterTreeModel(root);
		serviceTreePanel = new ServiceTreePanel(treeModel, serviceDescriptionRegistry);
		serviceTreePanel.setAvailableObjectsString(AVAILABLE_SERVICES);
		serviceTreePanel.setMatchingObjectsString(MATCHING_SERVIES);
		serviceTreePanel.setNoMatchingObjectsString(NO_MATCHING_SERVICES);
		add(serviceTreePanel);
		statusLine = new JLabel();
		add(statusLine, BorderLayout.SOUTH);
		if (statusUpdateTimer != null) {
			statusUpdateTimer.cancel();
		}
		statusUpdateTimer = new Timer("Clear status line", true);
		statusUpdateTimer.scheduleAtFixedRate(new UpdateStatusLineTask(), 0,
				STATUS_LINE_MESSAGE_MS);
		updateTree();
	}

	protected void updateTree() {
		synchronized (updateLock) {
			if (updaterThread != null && updaterThread.isAlive()) {
				return;
			}
			updaterThread = new TreeUpdaterThread();
			updaterThread.start();
		}
	}

	protected static class ServicePathElementComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			if ((o1 instanceof String) && (o2 instanceof String)) {
				if (o1.equals(ServiceDescription.SERVICE_TEMPLATES)) {
					return -1;
				}
				if (o2.equals(ServiceDescription.SERVICE_TEMPLATES)) {
					return 1;
				}
				if (o1.equals(ServiceDescription.LOCAL_SERVICES)) {
					return -1;
				}
				if (o2.equals(ServiceDescription.LOCAL_SERVICES)) {
					return 1;
				}
				if (o1.equals(MOBY_OBJECTS)) {
					return -1;
				}
				if (o2.equals(MOBY_OBJECTS)) {
					return 1;
				}
			}
			return o1.toString().compareToIgnoreCase(o2.toString());
		}
	}

	public class TreeUpdaterThread extends Thread {

		private boolean aborting = false;

		private TreeUpdaterThread() {
			super("Updating service panel");
			setDaemon(true);
		}

		public void abort() {
			aborting = true;
			interrupt();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			Map<Comparable, Map> pathMap = buildPathMap();
			populateChildren(root, pathMap);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						synchronized (serviceTreePanel) {
							serviceTreePanel.runFilter();
						}
						} catch (InterruptedException e) {
						// TODO Auto-generated catch block
							logger.error("", e);
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						logger.error("", e);
					}
				}
			});
		}

		@SuppressWarnings("unchecked")
		protected Map<Comparable, Map> buildPathMap() {
			Map<Comparable, Map> paths = new TreeMap<Comparable, Map>();
			for (ServiceDescription serviceDescription : serviceDescriptionRegistry
					.getServiceDescriptions()) {
				if (aborting) {
					return paths;
				}
				Map currentPath = paths;
				Map pathEntry = paths;
				for (Object pathElem : serviceDescription.getPath()) {
					pathEntry = (Map) currentPath.get(pathElem);
					if (pathEntry == null) {
						pathEntry = new TreeMap();
						currentPath.put(pathElem, pathEntry);
					}
					currentPath = pathEntry;
				}
				TreeMap<String, Set<ServiceDescription>> services = (TreeMap<String, Set<ServiceDescription>>) pathEntry
						.get(SERVICES);
				if (services == null) {
					services = new TreeMap<String, Set<ServiceDescription>>();
					pathEntry.put(SERVICES, services);
				}
				String serviceDescriptionName = serviceDescription.getName();
				if (!services.containsKey(serviceDescriptionName)) {
					Set<ServiceDescription> serviceSet = new HashSet<ServiceDescription>();
					services.put(serviceDescriptionName, serviceSet);
				}
				services.get(serviceDescriptionName).add(serviceDescription);
			}
			return paths;
		}

		@SuppressWarnings("unchecked")
		protected void populateChildren(FilterTreeNode node, Map pathMap) {
			if (aborting) {
				return;
			}
			if (node == root) {
				// Clear top root
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (aborting) {
							return;
						}
						serviceTreePanel.setFilter(null);
						root.removeAllChildren();
					}
				});
			}

			TreeSet<Comparable> paths = new TreeSet<Comparable>(servicePathElementComparator);
			TreeMap<String, Set<ServiceDescription>> services = (TreeMap<String, Set<ServiceDescription>>) pathMap
			.get(SERVICES);
			if (services == null) {
				services = new TreeMap<String, Set<ServiceDescription>>();
			}
			paths.addAll(pathMap.keySet());
			paths.addAll(services.keySet());
			
			for (Comparable pathElement : paths) {
				if (aborting) {
					return;
				}
				if (pathElement.equals(SERVICES)) {
					continue;
				}
				Set<FilterTreeNode> childNodes = new HashSet<FilterTreeNode> ();
				if (services.containsKey(pathElement)) {
					for (ServiceDescription sd : services.get(pathElement)) {
						childNodes.add (new ServiceFilterTreeNode(sd));
					}
				} else {
					childNodes.add(new PathElementFilterTreeNode((String)pathElement));
				}
				SwingUtilities
						.invokeLater(new AddNodeRunnable(node, childNodes));
				if ((pathMap.containsKey(pathElement)) && !childNodes.isEmpty()){
					populateChildren(childNodes.iterator().next(), (Map) pathMap.get(pathElement));
				}
			}
//			if (!services.isEmpty()) {
//				Collections.sort(services, serviceComparator);
//				for (String serviceName : services.keySet()) {
//					if (aborting) {
//						return;
//					}
//					if (pathMap.containsKey(serviceName)) {
//						continue;
//					}
//					SwingUtilities.invokeLater(new AddNodeRunnable(node,
//							new ServiceFilterTreeNode(services.get(serviceName))));
//				}
//			}
		}
		

		public class AddNodeRunnable implements Runnable {
			private final Set< FilterTreeNode> nodes;
			private final FilterTreeNode root;

			public AddNodeRunnable(FilterTreeNode root, Set<FilterTreeNode> nodes) {
				this.root = root;
				this.nodes = nodes;
			}

			public void run() {
				if (aborting) {
					return;
				}
				for (FilterTreeNode n : nodes) {
					root.add(n);
				}
			}
		}
	}

	public static class RemoveNodeRunnable implements Runnable {
		private final FilterTreeNode root;

		public RemoveNodeRunnable(FilterTreeNode root) {
			this.root = root;
		}

		public void run() {
			root.removeFromParent();
		}
	}

	private final class ServiceDescriptionRegistryObserver implements
			Observer<ServiceDescriptionRegistryEvent> {
		Set<ServiceDescriptionProvider> alreadyComplainedAbout = new HashSet<ServiceDescriptionProvider>();

		public void notify(Observable<ServiceDescriptionRegistryEvent> sender,
				ServiceDescriptionRegistryEvent message) throws Exception {
			if (message instanceof ProviderErrorNotification) {
				final ProviderErrorNotification pen = (ProviderErrorNotification) message;
				reportServiceProviderError(pen);
			} else if ((message instanceof ServiceDescriptionProvidedEvent)
					|| (message instanceof RemovedProviderEvent)) {
				AbstractProviderEvent ape = (AbstractProviderEvent) message;
				alreadyComplainedAbout.remove(ape.getProvider());
			}

			if (message instanceof AbstractProviderNotification) {
				AbstractProviderNotification abstractProviderNotification = (AbstractProviderNotification) message;
				providerStatus(abstractProviderNotification.getProvider(),
						abstractProviderNotification.getMessage());
			}
			if (message instanceof PartialServiceDescriptionsNotification) {
				// TODO: Support other events
				// and only update relevant parts of tree, or at least select
				// the recently added provider
				updateTree();
			}
			if (message instanceof RemovedProviderEvent) {
				updateTree();
			}
		}

		private void reportServiceProviderError(
				final ProviderErrorNotification pen) {
			ServiceDescriptionProvider provider = pen.getProvider();

			if (serviceDescriptionRegistry
					.getDefaultServiceDescriptionProviders().contains(provider)) {
				return;
			}
			if (alreadyComplainedAbout.contains(provider)) {
				return;
			}
			alreadyComplainedAbout.add(provider);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(ServicePanel.this, pen
							.getMessage()
							+ "\n" + pen.getProvider(), "Import service error",
							JOptionPane.ERROR_MESSAGE);
				}
			});
		}
	}


	private final class UpdateStatusLineTask extends TimerTask {
		@Override
		public void run() {
			if (blankOutCounter < 0 || blankOutCounter-- > 0) {
				// Only clear it once
				return;
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (blankOutCounter < 0) {
						statusLine.setVisible(false);
					}
				}
			});
		}
	}
}
