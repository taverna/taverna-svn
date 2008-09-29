/*
 *
 * Copyright (C) 2003 The University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 */
package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.jdom.output.XMLOutputter;

import uk.ac.man.cs.img.fetaClient.util.StringUtil;
import uk.ac.man.cs.img.fetaEngine.commons.ServiceType;
import uk.ac.man.cs.img.fetaEngine.webservice.CannedQueryType;
import uk.ac.man.cs.img.fetaEngine.webservice.FetaAdminPortType;
import uk.ac.man.cs.img.fetaEngine.webservice.FetaAdminPortTypeBindingStub;
import uk.ac.man.cs.img.fetaEngine.webservice.FetaCannedRequestType;
import uk.ac.man.cs.img.fetaEngine.webservice.FetaCompositeSearchRequestType;
import uk.ac.man.cs.img.fetaEngine.webservice.FetaLocator;
import uk.ac.man.cs.img.fetaEngine.webservice.FetaPortType;
import uk.ac.man.cs.img.fetaEngine.webservice.FetaPortTypeBindingStub;
import uk.ac.man.cs.img.fetaEngine.webservice.FetaSearchResponseType;

/**
 * @author alperp
 * 
 * 
 * 
 */

public class QueryHelper {

	private boolean incompleteServices;

	private DefaultTreeModel serviceTreeModel;

	private boolean isCacheDirty = false;

	private URL fetaEngineLocation;

	private URL fetaAdminLoc;

	private Map specElements;

	private Map specElementsReverseMap;

	public QueryHelper(URL fetaLoc, URL adminLoc) {

		super();

		fetaEngineLocation = fetaLoc;
		fetaAdminLoc = adminLoc;

		specElements = new HashMap();
		specElementsReverseMap = new HashMap();

		queryForAll(false);
		// just to build the spec table behind the scenes..
	}

	public void queryForAll(boolean visible) {
		updateCache();

		List cannedQueryList = new ArrayList();
		FetaCannedRequestType reqAll = new FetaCannedRequestType(
				CannedQueryType.GetAll, "");
		cannedQueryList.add(reqAll);
		if (visible) {
			clearTree();
			buildServiceList(executeRequest(cannedQueryList));
		} else {
			buildSpecTable(executeRequest(cannedQueryList));
		}
	}

	public void query(QueryCriteriaList queryList) {
		clearTree();
		updateCache();

		HashSet resultSet = new HashSet();
		List cannedQueryList = new ArrayList();

		for (Iterator iter = queryList.getQueryList().iterator(); iter
				.hasNext();) {
			QueryCriteriaModel query = (QueryCriteriaModel) iter.next();
			QueryCriteriaType type = query.getCriteriaType();
			if ((type == QueryCriteriaType.TASK_CRITERIA_TYPE)
					|| (type == QueryCriteriaType.METHOD_CRITERIA_TYPE)
					// ONTO-CHNG-PROPGTN || (type ==
					// QueryCriteriaType.APPLICATION_CRITERIA_TYPE)
					|| (type == QueryCriteriaType.RESOURCE_CRITERIA_TYPE)
					// ONTO-CHNG-PROPGTN || (type ==
					// QueryCriteriaType.RESOURCE_CONTENT_CRITERIA_TYPE)
					|| (type == QueryCriteriaType.INPUT_CRITERIA_TYPE)
					|| (type == QueryCriteriaType.OUTPUT_CRITERIA_TYPE)) {

				Object ontoTerm = (Object) query.getValue();

				String queryValue = (String) (((FetaOntologyTermModel) ontoTerm)
						.getID());
				/*
				 * String[] parts = queryValue.split("#"); if (parts.length ==
				 * 2) { queryValue = parts[1]; }
				 */
				FetaCannedRequestType reqT = new FetaCannedRequestType(
						CannedQueryType.fromString(type
								.getWebServiceCompatibleType()), queryValue);
				cannedQueryList.add(reqT);
			} else if (type == QueryCriteriaType.TYPE_CRITERIA_TYPE) {

				FetaCannedRequestType reqTy = new FetaCannedRequestType(
						CannedQueryType.fromString(type
								.getWebServiceCompatibleType()), ServiceType
								.getRDFLiteralEnumForString((String) query
										.getValue()));
				cannedQueryList.add(reqTy);

			} else if ((type == QueryCriteriaType.DESCRIPTION_CRITERIA_TYPE)
					|| (type == QueryCriteriaType.NAME_CRITERIA_TYPE)) {
				String enteredVal = StringUtil.trim((String) query.getValue());
				if (enteredVal.length() != 0) {

					FetaCannedRequestType reqD = new FetaCannedRequestType(
							CannedQueryType.fromString(type
									.getWebServiceCompatibleType()),
							(String) query.getValue());
					cannedQueryList.add(reqD);

				}
			}

		}

		buildServiceList(executeRequest(cannedQueryList));

	}

	public String getStoreContent(URL locationURL) {

		try {
			FetaAdminPortType port;
			port = new FetaLocator().getfetaAdmin(locationURL);
			((FetaAdminPortTypeBindingStub) port).setTimeout(2400000);
//			System.out.println("Got the service binding");
			String response = null;
			// ((FetaAdminPortTypeBindingStub)port).refresh();
			refreshEngine(locationURL);
			response = (String) ((FetaAdminPortTypeBindingStub) port)
					.getStoreContent();
			return response;
		} catch (Exception ex) {
			System.out.println("Error occured during executing query: ");
			ex.printStackTrace();
			return null;
		}

	}

	public void refreshEngine(URL locationURL) {
		/*
		 * try {
		 * 
		 * 
		 * FetaAdminPortType port; port = new
		 * FetaLocator().getfetaAdmin(locationURL);
		 * ((FetaAdminPortTypeBindingStub)port).setTimeout(2400000);
		 * System.out.println("Got the service binding");
		 * 
		 * ((FetaAdminPortTypeBindingStub)port).refresh();
		 *  } catch (Exception ex) { System.out.println("Error occured during
		 * executing Feta Engine Refresh Request: " ); ex.printStackTrace();
		 *  }
		 * 
		 */
	}

	public String freeFormQuery(URL locationURL, String freeFormQueryString) {
		try {

			FetaPortType port;
			port = new FetaLocator().getfeta(locationURL);
			((FetaPortTypeBindingStub) port).setTimeout(2400000);
//			System.out.println("Got the service binding");

			String resultRDF = ((FetaPortTypeBindingStub) port)
					.freeFormQuery(freeFormQueryString);
			return resultRDF;

		} catch (Exception ex) {
			System.out
					.println("Error occured during executing Feta Engine Free Form Query Request: ");
			ex.printStackTrace();
			return null;

		}
	}

	private String[] executeRequest(List cannedQueryList) {
		try {
			FetaPortType port;

			FetaCannedRequestType[] tmp = new FetaCannedRequestType[cannedQueryList
					.size()];

			for (int i = 0; i < cannedQueryList.size(); i++) {
				tmp[i] = (FetaCannedRequestType) cannedQueryList.get(i);
			}

			FetaCompositeSearchRequestType reqs = new FetaCompositeSearchRequestType(
					tmp);

			port = new FetaLocator().getfeta(this.fetaEngineLocation);
			((FetaPortTypeBindingStub) port).setTimeout(2400000);
//			System.out.println("Got the service binding");
			FetaSearchResponseType response = null;
			response = (FetaSearchResponseType) ((FetaPortTypeBindingStub) port)
					.inquire(reqs);

			if (response.getOperationURI() != null) {
//				System.out.println("New URI: " + response.getOperationURI()[0]);

			}

			return response.getOperationURI();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Error occured during executing query: "
					+ ex.getMessage());
			return null;
		}

	}

	private void buildServiceList(String[] operLSIDs) {

		incompleteServices = false;
		if (operLSIDs != null) {

			for (int i = 0; i < operLSIDs.length; i++) {
				try {
					String operID = (String) operLSIDs[i];
					String[] tokens = operID.split("\\$");
					if (tokens.length != 3) {
						Exception e = new Exception();
						throw e;
					}
					IServiceModelFiller descriptionWrapper;

					if (tokens[0].toLowerCase().endsWith("xml")) {
						descriptionWrapper = new PedroXMLWrapper(operID);
					} else {
						descriptionWrapper = new FetaRDFWrapper(operID);
					}

					DefaultMutableTreeNode resultingOperation = new DefaultMutableTreeNode(
							new BasicServiceModel(descriptionWrapper));
					serviceTreeModel.insertNodeInto(resultingOperation,
							(DefaultMutableTreeNode) (serviceTreeModel
									.getRoot()), 0);

				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (Exception exp) {
					exp.printStackTrace();
				}
			}// for
			serviceTreeModel.nodeStructureChanged((TreeNode) serviceTreeModel
					.getRoot());

		}// if
		else {
			JOptionPane.showMessageDialog(null,
					"No results were returned for your query.", "Information",
					JOptionPane.INFORMATION_MESSAGE);
		}
		TavernaFetaGUI.getInstance().clearFormPanel();
		TavernaFetaGUI.getInstance().switchToResultPanel();
	}

	private void buildSpecTable(String[] operLSIDs) {

		this.specElements = new HashMap();
		this.specElementsReverseMap = new HashMap();

		XMLOutputter xo = new XMLOutputter();

		if (operLSIDs != null) {
			for (int i = 0; i < operLSIDs.length; i++) {
				try {
					String operID = (String) operLSIDs[i];
					String[] tokens = operID.split("\\$");
					if (tokens.length != 3) {
						Exception e = new Exception();
						throw e;
					}
					IServiceModelFiller descriptionWrapper;

					if (tokens[0].toLowerCase().endsWith("xml")) {
						descriptionWrapper = new PedroXMLWrapper(operID);
					} else {
						descriptionWrapper = new FetaRDFWrapper(operID);
					}

					BasicServiceModel model = new BasicServiceModel(
							descriptionWrapper);

					String specElementStr = xo.outputString(model
							.getTavernaProcessorSpecAsElement());
					this.specElements.put(operID, specElementStr);

					if (specElementsReverseMap.containsKey(specElementStr)) {
						List fetaDescLoctionList = (List) specElementsReverseMap
								.get(specElementStr);
						fetaDescLoctionList.add(model
								.getServiceDescriptionLocation());
						specElementsReverseMap.put(specElementStr,
								fetaDescLoctionList);
					} else {
						List fetaDescLoctionList = new ArrayList();
						fetaDescLoctionList.add(model
								.getServiceDescriptionLocation());
						this.specElementsReverseMap.put(specElementStr,
								fetaDescLoctionList);
					}

				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (Exception exp) {
					exp.printStackTrace();
				}// catch
			}// for
		}// if
		else {
			return;
			// DO NOTHING
		}

	}

	public List reverseLookup(org.jdom.Element specElement) {

		if ((specElements == null) || (specElementsReverseMap == null)) {
			List cannedQueryList = new ArrayList();
			FetaCannedRequestType reqAll = new FetaCannedRequestType(
					CannedQueryType.GetAll, "");
			cannedQueryList.add(reqAll);
			this.buildSpecTable(executeRequest(cannedQueryList));

		}

		XMLOutputter xo = new XMLOutputter();
		if (specElements.containsValue(xo.outputString(specElement))) {
			return (List) specElementsReverseMap.get(xo
					.outputString(specElement));
		} else {
			return null;
		}

	}

	public void clearTree() {
		((DefaultMutableTreeNode) (serviceTreeModel.getRoot()))
				.removeAllChildren();
		serviceTreeModel.reload();

	}

	/**
	 * @return
	 */
	public DefaultTreeModel getTree() {
		return serviceTreeModel;
	}

	/**
	 * @param model
	 */
	public void setTree(DefaultTreeModel model) {
		serviceTreeModel = model;
	}

	public URL getFetaEngineLocation() {
		return this.fetaEngineLocation;
	}

	public boolean isInEngineScope(String pulishURL) {

		try {
			URL publishedDescURL = new URL(pulishURL);
			String hostname = publishedDescURL.getHost();
			// we apply a simple lookup procedure we check whether feta engine's
			// scope covers descriptions from this host.
			for (Iterator j = specElements.entrySet().iterator(); j.hasNext();) {
				Map.Entry entry = (Map.Entry) j.next();
				String operationURI = (String) entry.getKey();

				if (operationURI.indexOf(hostname) != -1) {
					System.out.println("Publication in scope of Feta Engine");
					this.isCacheDirty = true;
					return true;
				}

			}

		} catch (Exception exp) {
			return false;
		}
		return false;
	}

	public void updateCache() {
		if (this.isCacheDirty) {
			this.refreshEngine(this.fetaAdminLoc);
			this.specElements = null;
			this.specElementsReverseMap = null;

		}

	}

}
