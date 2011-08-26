/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.biomoby.shared.MobyException;
import org.biomoby.shared.mobyxml.jdom.MobyObjectClassNSImpl;
import org.biomoby.shared.mobyxml.jdom.jDomUtilities;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XMLUtilities {

	/**
	 * 
	 * <p>
	 * <b>PRE:</b>
	 * <p>
	 * <b>POST:</b>
	 * 
	 * @param xml
	 *            a stirng of xml to create a DOM document for
	 * @return a Document representing the string of xml, or null if there was a
	 *         problem with the input XML
	 */
	public static Document getDOMDocument(String xml) {
		SAXBuilder builder = new SAXBuilder();
		// Create the document
		Document doc = null;
		try {
			doc = builder.build(new StringReader(xml));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
	}

	/**
	 * 
	 * <p>
	 * <b>PRE:</b>
	 * <p>
	 * <b>POST:</b>
	 * 
	 * @param elementToWrap
	 *            an Element object that you would like to wrap in the following
	 * 
	 * <pre>
	 *  &lt;MOBY&gt;&lt;mobyContent&gt;&lt;mobyData&gt;
	 * </pre>
	 * 
	 * tags.
	 * @return the input element wrapped in the tags specified above.
	 */
	public static Element createMobyDataElementWrapper(Element elementToWrap) {
		Element element = null;
		Document doc = new Document();
		doc.setBaseURI(MobyObjectClassNSImpl.MOBYNS.getURI());
		// got a document, now need to create mobyData
		element = new Element("MOBY", MobyObjectClassNSImpl.MOBYNS);
		doc.setRootElement(element);
		Element mobyContent = new Element("mobyContent", MobyObjectClassNSImpl.MOBYNS);
		Element mobyData = new Element("mobyData", MobyObjectClassNSImpl.MOBYNS);
		mobyData.setAttribute("queryID", "a1", MobyObjectClassNSImpl.MOBYNS);
		mobyContent.addContent(mobyData);
		mobyData.addContent(elementToWrap.detach());
		element.addContent(mobyContent);
		return element;
	}

	/**
	 * 
	 * Takes in a jDom Element and performs a search for tag with a given
	 * objectType, articleName and possible namespaces
	 * <p>
	 * <b>PRE: xml is valid XML and was returned by a Moby service</b>
	 * <p>
	 * <b>POST: The sought after element is returned.</b>
	 * 
	 * @param xml -
	 *            The element to conduct the search on.
	 * @param objectType -
	 *            The tag name of the element to search for.
	 * @param articleName -
	 *            The article name of the object
	 * @param namespaces -
	 *            The list of possible namespaces or null for any.
	 * @return String representation of the Element in question with tag name
	 *         objectType, article name articleName, and list of possible
	 *         namespaces namespaces.
	 * @throws MobyException
	 */
	public static String getMobyElement(Element xml, String objectType, String articleName,
			String[] namespaces, String mobyEndpoint) throws MobyException {
		MobyObjectClassNSImpl moc = new MobyObjectClassNSImpl(mobyEndpoint);
		Element e = null;
		try {
			e = jDomUtilities.getElement("Simple", xml, new String[] { "articleName="
					+ ((articleName == null) ? "" : articleName) });
		} catch (NullPointerException npe) {
			// couldnt find the element try looking for the actual element
			List list = jDomUtilities.listChildren(xml, objectType, new ArrayList());
			// if you find one, return the first one
			if (list.size() > 0)
				return moc.toString(createMobyDataElementWrapper(moc.toSimple(
						(Element) list.get(0), articleName)));
			else
				throw new MobyException(
						"Couldn't find the element named "
								+ objectType
								+ " with article name "
								+ articleName
								+ System.getProperty("line.separator")
								+ ". Please check with service provider to ensure that the moby service outputs what it is advertised to output.");
		}
		// TODO check namespaces, etc.
		if (e != null) {
			e = jDomUtilities.getElement(objectType, e, new String[] { "articleName=" });
			if (e != null)
				return moc.toString(createMobyDataElementWrapper(moc.toSimple(e, articleName)));
		}
		return "<?xml version=\'1.0\' encoding=\'UTF-8\'?><moby:MOBY xmlns:moby=\'http://www.biomoby.org/moby\' xmlns=\'http://www.biomoby.org/moby\'><moby:mobyContent moby:authority=\'\'><moby:mobyData moby:queryID=\'a1\'/></moby:mobyContent></moby:MOBY>";
	}

	/**
	 * 
	 * @return a new DOM Document
	 */
	public static Document createDomDocument() {
		Document d = new Document();
		d.setBaseURI(MobyObjectClassNSImpl.MOBYNS.getURI());
		return d;
	}

	/**
	 * <p>
	 * <b>PRE:</b>
	 * <p>
	 * <b>POST:</b>
	 * 
	 * @param documentElement
	 *            the full moby message that you would like to extract the
	 *            collection from
	 * @param objectType
	 *            the objectType in the collection that you would like to
	 *            extract
	 * @param articleName
	 *            the name of the collection
	 * @param mobyEndpoint
	 *            the endpoint of the mobycentral registry
	 * @return a full moby message that contains the collection if it exists,
	 *         otherwise null is returned.
	 */
	public static String getMobyCollection(Element documentElement, String objectType,
			String articleName, String mobyEndpoint) {
		MobyObjectClassNSImpl mo = new MobyObjectClassNSImpl(mobyEndpoint);
		Document doc = XMLUtilities.createDomDocument();
		Element root = new Element("MOBY", MobyObjectClassNSImpl.MOBYNS);
		Element content = new Element("mobyContent", MobyObjectClassNSImpl.MOBYNS);
		Element mobyData = new Element("mobyData", MobyObjectClassNSImpl.MOBYNS);
		Element mobyCollection = new Element("Collection", MobyObjectClassNSImpl.MOBYNS);
		mobyData.setAttribute("queryID", "a1", MobyObjectClassNSImpl.MOBYNS);
		root.addContent(content);
		doc.addContent(root);
		content.addContent(mobyData);
		mobyData.addContent(mobyCollection);
		Element e = documentElement.getChild("mobyContent", MobyObjectClassNSImpl.MOBYNS);
		if (e == null)
			e = documentElement.getChild("mobyContent");
		if (e != null)
			if (e.getChild("mobyData", MobyObjectClassNSImpl.MOBYNS) != null) {
				e = e.getChild("mobyData", MobyObjectClassNSImpl.MOBYNS);
			} else {
				e = e.getChild("mobyData");
			}
		if (e != null) {
			List children = e.getChildren("Collection", MobyObjectClassNSImpl.MOBYNS);
			if (children.isEmpty())
				children = e.getChildren("Collection");
			for (Iterator x = children.iterator(); x.hasNext();) {
				Object o = x.next();
				if (o instanceof Element) {
					Element child = (Element) o;
					if (child.getAttributeValue("articleName", MobyObjectClassNSImpl.MOBYNS) != null) {

						if (child.getAttributeValue("articleName", MobyObjectClassNSImpl.MOBYNS)
								.equals(articleName)) {
							e = child;
							mobyCollection.setAttribute((Attribute) child.getAttribute(
									"articleName", MobyObjectClassNSImpl.MOBYNS).clone());
						}
					} else if (child.getAttributeValue("articleName") != null) {

						if (child.getAttributeValue("articleName").equals(articleName)) {
							e = child;
							mobyCollection.setAttribute((Attribute) child.getAttribute(
									"articleName").clone());
						}
					}
				}
			}
		}
		if (e == null)
			return null;
		List list = e.getChildren("Simple", MobyObjectClassNSImpl.MOBYNS);
		for (Iterator x = list.iterator(); x.hasNext();) {
			Element child = (Element) x.next();
			List objects = child.getChildren(objectType, MobyObjectClassNSImpl.MOBYNS);
			// iterate through the list adding them to the mobyCollection TODO
			Iterator iter = objects.iterator();
			while (iter.hasNext()) {
				Element simple = new Element("Simple", MobyObjectClassNSImpl.MOBYNS);
				Element _c = (Element) iter.next();
				iter.remove();
				simple.addContent(_c.detach());
				mobyCollection.addContent(simple);
			}
		}
		return mo.toString(root);
	}

	/**
	 * 
	 * @param xml
	 *            the string of xml containing the collection that you would
	 *            like to extract the simples from
	 * @param mobyEndpoint
	 *            the endpoint of the mobycentral registry that you are using
	 * @return a moby message that contains simples without the collection
	 */
	public static String getMobySimplesFromCollection(String xml, String mobyEndpoint) {
		if (xml == null)
			return null;
		// variables needed
		MobyObjectClassNSImpl mo = new MobyObjectClassNSImpl(mobyEndpoint);
		Element documentElement = XMLUtilities.getDOMDocument(xml).getRootElement();
		int invocationCount = 1;
		// element data
		Element root = new Element("MOBY", MobyObjectClassNSImpl.MOBYNS);
		Element content = new Element("mobyContent", MobyObjectClassNSImpl.MOBYNS);
		root.addContent(content);

		Element de = documentElement.getChild("mobyContent", MobyObjectClassNSImpl.MOBYNS);
		if (de != null) {
			List mobyDatas = de.getChildren("mobyData", MobyObjectClassNSImpl.MOBYNS);
			if (mobyDatas == null)
				mobyDatas = de.getChildren("mobyData");
			if (mobyDatas == null || mobyDatas.size() == 0)
				return null;
			for (Iterator iter = mobyDatas.iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (obj instanceof Element) {
					Element e = (Element) obj;
					if (e != null) {
						List children = e.getChildren("Collection", MobyObjectClassNSImpl.MOBYNS);
						if (children.size() == 0)
							children = e.getChildren("Collection");
						if (children.size() == 0)
							return null;
						for (Iterator x = children.iterator(); x.hasNext();) {
							Object o = x.next();
							if (o instanceof Element) {
								Element child = (Element) o;
								List simples = child.getChildren("Simple",
										MobyObjectClassNSImpl.MOBYNS);
								if (simples.size() == 0)
									simples = e.getChildren("Simple");
								if (simples.size() == 0)
									return null;
								for (Iterator iter2 = simples.iterator(); iter2.hasNext();) {
									Object element = iter2.next();
									iter2.remove();
									if (element instanceof Element) {
										Element sim = (Element) element;
										Element mobyData = new Element("mobyData",
												MobyObjectClassNSImpl.MOBYNS);
										mobyData.setAttribute("queryID", "a" + invocationCount++,
												MobyObjectClassNSImpl.MOBYNS);
										mobyData.addContent(sim.detach());
										content.addContent(mobyData);
									}
								}
							}
						}
					}
				}
			}
		}
		if (de == null)
			return null;
		return mo.toString(root);
	}

	/**
	 * TODO better to return the simples with the name obtained from the
	 * collection or to return only a list of simples from the collection given
	 * by name
	 * 
	 * @param xml
	 *            the string of xml that you would like to extract the simples
	 *            from
	 * @param mobyEndpoint
	 *            the endpoint of the registry that you are using
	 * @param articleName
	 *            the article name that you would like to give the simples
	 * @return a list of simples obtained from the collection renamed to
	 *         articleName
	 */
	public static List createMobySimpleListFromCollection(String xml, String mobyEndpoint,
			String articleName) {

		ArrayList arrayList = new ArrayList();
		if (xml == null)
			return arrayList;
		// variables needed
		MobyObjectClassNSImpl mo = new MobyObjectClassNSImpl(mobyEndpoint);
		Element documentElement = XMLUtilities.getDOMDocument(xml).getRootElement();

		Element de = documentElement.getChild("mobyContent", MobyObjectClassNSImpl.MOBYNS);
		if (de != null) {
			List mobyDatas = de.getChildren("mobyData", MobyObjectClassNSImpl.MOBYNS);
			if (mobyDatas == null)
				mobyDatas = de.getChildren("mobyData");
			if (mobyDatas == null || mobyDatas.size() == 0)
				return arrayList;
			for (Iterator iter = mobyDatas.iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (obj instanceof Element) {
					Element e = (Element) obj;
					if (e != null) {
						List children = e.getChildren("Collection", MobyObjectClassNSImpl.MOBYNS);
						if (children.size() == 0)
							children = e.getChildren("Collection");
						if (children.size() == 0)
							return arrayList;
						for (Iterator x = children.iterator(); x.hasNext();) {
							Object o = x.next();
							if (o instanceof Element) {
								Element child = (Element) o;
								List simples = child.getChildren("Simple",
										MobyObjectClassNSImpl.MOBYNS);
								if (simples.size() == 0)
									simples = e.getChildren("Simple");
								if (simples.size() == 0)
									return arrayList;
								for (Iterator iter2 = simples.iterator(); iter2.hasNext();) {
									Object element = iter2.next();
									iter2.remove();
									if (element instanceof Element) {
										// element data
										Element sim = (Element) element;
										if (!articleName.equals("")) {
											sim.setAttribute("articleName", articleName);
										}
										Element mobyData = new Element("mobyData",
												MobyObjectClassNSImpl.MOBYNS);
										Element root = new Element("MOBY",
												MobyObjectClassNSImpl.MOBYNS);
										Element content = new Element("mobyContent",
												MobyObjectClassNSImpl.MOBYNS);
										root.addContent(content);
										content.addContent(mobyData);
										mobyData.setAttribute("queryID", "a1",
												MobyObjectClassNSImpl.MOBYNS);
										mobyData.addContent(sim.detach());
										arrayList.add(mo.toString(root));
									}
								}
							}
						}
					}
				}
			}
		}
		return arrayList;
	}

	/**
	 * 
	 * @param elements
	 *            list is a list of items to append to a moby message
	 * @return an element that contains the list of elements as multiple
	 *         invokations
	 */
	public static Element createMultipleInvocationMessageFromList(List elements) {
		Element root = null;
		Document doc = new Document();
		doc.setBaseURI(MobyObjectClassNSImpl.MOBYNS.getURI());
		// got a document, now need to create mobyData
		root = new Element("MOBY", MobyObjectClassNSImpl.MOBYNS);
		doc.setRootElement(root);
		Element mobyContent = new Element("mobyContent", MobyObjectClassNSImpl.MOBYNS);
		int count = 0;
		for (Iterator iter = elements.iterator(); iter.hasNext();) {
			String str = (String) iter.next();
			Document d = getDOMDocument(str);
			Element mobyData = new Element("mobyData", MobyObjectClassNSImpl.MOBYNS);
			mobyData.setAttribute("queryID", "a" + count++, MobyObjectClassNSImpl.MOBYNS);
			Element mc = d.getRootElement().getChild("mobyContent", MobyObjectClassNSImpl.MOBYNS);
			if (mc == null) {
				mc = d.getRootElement().getChild("mobyContent");
			}
			Element md = mc.getChild("mobyData", MobyObjectClassNSImpl.MOBYNS);
			if (md == null) {
				md = mc.getChild("mobyData");
			}
			mobyData.addContent(md.cloneContent());
			mobyContent.addContent(mobyData);
		}
		root.addContent(mobyContent);
		return root;
	}

	// ////////////
	static String s = "<moby:MOBY xmlns:moby=\"http://www.biomoby.org/moby\">\r\n"
			+ "        <moby:mobyContent>\r\n"
			+ "          <moby:mobyData moby:queryID=\"a0\">\r\n"
			+ "            <moby:Simple moby:articleName=\"outputString\">\r\n"
			+ "              <moby:String moby:id=\"\" moby:namespace=\"\">\r\n" + "j\r\n"
			+ "              </moby:String>\r\n" + "            </moby:Simple>\r\n"
			+ "          </moby:mobyData>\r\n"
			+ "          <moby:mobyData moby:queryID=\"a1\">\r\n"
			+ "            <moby:Simple moby:articleName=\"outputString\">\r\n"
			+ "              <moby:String moby:id=\"\" moby:namespace=\"\">\r\n" + "u\r\n"
			+ "              </moby:String>\r\n" + "            </moby:Simple>\r\n"
			+ "          </moby:mobyData>\r\n"
			+ "          <moby:mobyData moby:queryID=\"a2\">\r\n"
			+ "            <moby:Simple moby:articleName=\"outputString\">\r\n"
			+ "              <moby:String moby:id=\"\" moby:namespace=\"\">\r\n" + "l\r\n"
			+ "              </moby:String>\r\n" + "            </moby:Simple>\r\n"
			+ "          </moby:mobyData>\r\n"
			+ "          <moby:mobyData moby:queryID=\"a3\">\r\n"
			+ "            <moby:Simple moby:articleName=\"outputString\">\r\n"
			+ "              <moby:String moby:id=\"\" moby:namespace=\"\">\r\n" + "i\r\n"
			+ "              </moby:String>\r\n" + "            </moby:Simple>\r\n"
			+ "          </moby:mobyData>\r\n"
			+ "          <moby:mobyData moby:queryID=\"a4\">\r\n"
			+ "            <moby:Simple moby:articleName=\"outputString\">\r\n"
			+ "              <moby:String moby:id=\"\" moby:namespace=\"\">\r\n" + "e\r\n"
			+ "              </moby:String>\r\n" + "            </moby:Simple>\r\n"
			+ "          </moby:mobyData>\r\n"
			+ "          <moby:mobyData moby:queryID=\"a5\">\r\n"
			+ "            <moby:Simple moby:articleName=\"outputString\">\r\n"
			+ "              <moby:String moby:id=\"\" moby:namespace=\"\">\r\n" + "t\r\n"
			+ "              </moby:String>\r\n" + "            </moby:Simple>\r\n"
			+ "          </moby:mobyData>\r\n"
			+ "          <moby:mobyData moby:queryID=\"a6\">\r\n"
			+ "            <moby:Simple moby:articleName=\"outputString\">\r\n"
			+ "              <moby:String moby:id=\"\" moby:namespace=\"\">\r\n" + "a\r\n"
			+ "              </moby:String>\r\n" + "            </moby:Simple>\r\n"
			+ "          </moby:mobyData>\r\n" + "        </moby:mobyContent>\r\n"
			+ "      </moby:MOBY>";

	public static void main(String[] args) throws Exception {
		MobyObjectClassNSImpl mo = new MobyObjectClassNSImpl(null);
		SAXBuilder builder = new SAXBuilder();
		// Create the document
		Document doc = null;
		try {
			doc = builder.build(new StringReader(s));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//
		System.out.println(XMLUtilities.getSingleInvokationsFromMultipleInvokationMessage(doc
				.getRootElement()));
		System.out.println(XMLUtilities.areAllSimples(XMLUtilities
				.getSingleInvokationsFromMultipleInvokationMessage(doc.getRootElement())));
		List list = XMLUtilities.getSingleInvokationsFromMultipleInvokationMessage(doc
				.getRootElement());
		Element newMobyData = new Element("mobyData", mo.MOBYNS);
		newMobyData.setAttribute("queryID", "amalgamated", mo.MOBYNS);
		Element collectionE = new Element("Collection", mo.MOBYNS);
		collectionE.setAttribute("articleName", "array2", mo.MOBYNS);
		newMobyData.addContent(collectionE);
		for (Iterator it = list.iterator(); it.hasNext();) {
			Element md = (Element) it.next();
			System.out.println("md.toString()\n" + mo.toString(md));

				if (!md.getChildren("Simple").isEmpty()
						|| !md.getChildren("Simple", mo.MOBYNS).isEmpty()) {

					List simples = md.getChildren("Simple");
					if (simples.isEmpty())
						simples = md.getChildren("Simple", mo.MOBYNS);

					for (Iterator sIt = simples.iterator(); sIt.hasNext();) {
						Element sElement = (Element) sIt.next();
						System.out.println("sElement.toString()\n" + mo.toString(sElement));
						collectionE.addContent(sElement.cloneContent());
					}
				}
		}
		String ss = mo.toString(newMobyData);
		System.out.println(ss);
	}

	/**
	 * 
	 * @param documentElement
	 *            the moby xml message to check whether or not there are
	 *            multiple invokations in the message
	 * @return true if a message has > 1 mobyData element, false otherwise
	 */
	public static boolean isMultipleInvokationMessage(Element documentElement) {
		List list = new ArrayList();
		jDomUtilities.listChildren(documentElement, "mobyData", list);
		if (list != null)
			if (list.size() > 1)
				return true;
		return false;
	}

	/**
	 * Call this method only after you have ensured that a multiple invokations
	 * exist!
	 * 
	 * @param documentElement
	 *            the moby xml message to extract a list of invokation messages
	 * @return a list of single invokations
	 */
	public static List getSingleInvokationsFromMultipleInvokationMessage(Element documentElement) {
		List list = new ArrayList();
		jDomUtilities.listChildren(documentElement, "mobyData", list);
		if (list != null)
			return list;
		return new ArrayList();
	}

	/**
	 * 
	 * @param invocations
	 *            a list of mobyData blocks to check whether there are only
	 *            Simples in the invokations
	 * @return true if all the invokations contain only Simples, false
	 *         otherwise.
	 */
	public static boolean areAllSimples(List invocations) {
		for (Iterator it = invocations.iterator(); it.hasNext();) {
			Element element = (Element) it.next();
			if (element.getChild("Collection") != null
					|| element.getChild("Collection", MobyObjectClassNSImpl.MOBYNS) != null)
				return false;
		}
		return true;
	}
}
