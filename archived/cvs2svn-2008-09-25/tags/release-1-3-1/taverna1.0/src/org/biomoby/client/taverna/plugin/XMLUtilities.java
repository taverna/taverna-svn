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
	 * TODO - place brief description here.
	 * <p>
	 * <b>PRE:</b>
	 * <p>
	 * <b>POST:</b>
	 * 
	 * @param xml
	 * @return
	 * @throws MobyException
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
	 * TODO - place brief description here.
	 * <p>
	 * <b>PRE:</b>
	 * <p>
	 * <b>POST:</b>
	 * 
	 * @param elementToWrap
	 * @return
	 */
	public static Element createMobyDataElementWrapper(Element elementToWrap) {
		Element element = null;
		Document doc = new Document();
		doc.setBaseURI(MobyObjectClassNSImpl.MOBYNS.getURI());
		// got a document, now need to create mobyData
		element = new Element("MOBY", MobyObjectClassNSImpl.MOBYNS);
		doc.setRootElement(element);
		Element mobyContent = new Element("mobyContent",
				MobyObjectClassNSImpl.MOBYNS);
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
	public static String getMobyElement(Element xml, String objectType,
			String articleName, String[] namespaces, String mobyEndpoint) throws MobyException {
		MobyObjectClassNSImpl moc = new MobyObjectClassNSImpl(mobyEndpoint);
		Element e = null;
		try {
		e = jDomUtilities.getElement("Simple", xml,
				new String[] { "articleName="
						+ ((articleName == null) ? "" : articleName) });
		} catch (NullPointerException npe) {
			// couldnt find the element try looking for the actual element
			List list = jDomUtilities.listChildren(xml, objectType, new ArrayList());
			// if you find one, return the first one
			if (list.size() > 0)
				return moc.toString(createMobyDataElementWrapper(moc
						.toSimple((Element)list.get(0), articleName)));
			else
				throw new MobyException("Couldn't find the element named " + objectType + " with article name " + articleName + System.getProperty("line.separator") + ". Please check with service provider to ensure that the moby service outputs what it is advertised to output.");
		}
		// TODO check namespaces, etc.
		if (e != null) {
			e = jDomUtilities.getElement(objectType, e, new String[] {"articleName="});
			if (e != null)
				return moc.toString(createMobyDataElementWrapper(moc
						.toSimple(e, articleName)));
		}
		return "<?xml version=\'1.0\' encoding=\'UTF-8\'?><moby:MOBY xmlns:moby=\'http://www.biomoby.org/moby\' xmlns=\'http://www.biomoby.org/moby\'><moby:mobyContent moby:authority=\'\'><moby:mobyData moby:queryID=\'a1\'/></moby:mobyContent></moby:MOBY>";
	}

	public static Document createDomDocument() {
		Document d = new Document();
		d.setBaseURI(MobyObjectClassNSImpl.MOBYNS.getURI());
		return d;
	}

	/**
	 * TODO - place brief description here.
	 * <p>
	 * <b>PRE:</b>
	 * <p>
	 * <b>POST:</b>
	 * 
	 * @param documentElement
	 * @param objectType
	 * @param articleName
	 * @param mobyEndpoint
	 * @return
	 */
	public static String getMobyCollection(Element documentElement,
			String objectType, String articleName, String mobyEndpoint) {
		MobyObjectClassNSImpl mo = new MobyObjectClassNSImpl(mobyEndpoint);
		Document doc = XMLUtilities.createDomDocument();
		Element root = new Element("MOBY", MobyObjectClassNSImpl.MOBYNS);
		Element content = new Element("mobyContent",
				MobyObjectClassNSImpl.MOBYNS);
		Element mobyData = new Element("mobyData", MobyObjectClassNSImpl.MOBYNS);
		Element mobyCollection = new Element("Collection",
				MobyObjectClassNSImpl.MOBYNS);
		mobyData.setAttribute("queryID", "a1", MobyObjectClassNSImpl.MOBYNS);
		root.addContent(content);
		doc.addContent(root);
		content.addContent(mobyData);
		mobyData.addContent(mobyCollection);
		Element e = documentElement.getChild("mobyContent",
				MobyObjectClassNSImpl.MOBYNS);
		if (e != null) {
			e = e.getChild("mobyData", MobyObjectClassNSImpl.MOBYNS);
			if (e != null) {
				List children = e.getChildren("Collection",
						MobyObjectClassNSImpl.MOBYNS);
				for (Iterator x = children.iterator(); x.hasNext();) {
					Object o = x.next();
					if (o instanceof Element) {
						Element child = (Element) o;
						if (child.getAttributeValue("articleName",
								MobyObjectClassNSImpl.MOBYNS).equals(
								articleName)) {
							e = child;
							mobyCollection.setAttribute((Attribute) child
									.getAttribute("articleName",
											MobyObjectClassNSImpl.MOBYNS)
									.clone());
						}
					}
				}
			}
		}
		List list = e.getChildren("Simple", MobyObjectClassNSImpl.MOBYNS);
		for (Iterator x = list.iterator(); x.hasNext();) {
			Element child = (Element) x.next();
			List objects = child.getChildren(objectType,
					MobyObjectClassNSImpl.MOBYNS);
			// iterate through the list adding them to the mobyCollection TODO
			Iterator iter = objects.iterator();
			while (iter.hasNext()) {
				Element simple = new Element("Simple",
						MobyObjectClassNSImpl.MOBYNS);
				Element _c = (Element) iter.next();
				iter.remove();
				simple.addContent(_c.detach());
				mobyCollection.addContent(simple);
			}
		}
		return mo.toString(root);
	}

	public static String getMobySimplesFromCollection(String xml,
			String mobyEndpoint) {
		if (xml == null)
			return null;
		// variables needed
		MobyObjectClassNSImpl mo = new MobyObjectClassNSImpl(mobyEndpoint);
		Element documentElement = XMLUtilities.getDOMDocument(xml)
				.getRootElement();
		int invocationCount = 1;
		// element data
		Element root = new Element("MOBY", MobyObjectClassNSImpl.MOBYNS);
		Element content = new Element("mobyContent",
				MobyObjectClassNSImpl.MOBYNS);
		root.addContent(content);

		Element de = documentElement.getChild("mobyContent",
				MobyObjectClassNSImpl.MOBYNS);
		if (de != null) {
			List mobyDatas = de.getChildren("mobyData",
					MobyObjectClassNSImpl.MOBYNS);
			if (mobyDatas == null)
				mobyDatas = de.getChildren("mobyData");
			if (mobyDatas == null || mobyDatas.size() == 0)
				return null;
			for (Iterator iter = mobyDatas.iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (obj instanceof Element) {
					Element e = (Element) obj;
					if (e != null) {
						List children = e.getChildren("Collection",
								MobyObjectClassNSImpl.MOBYNS);
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
								for (Iterator iter2 = simples.iterator(); iter2
										.hasNext();) {
									Object element = iter2.next();
									iter2.remove();
									if (element instanceof Element) {
										Element sim = (Element) element;
										Element mobyData = new Element(
												"mobyData",
												MobyObjectClassNSImpl.MOBYNS);
										mobyData.setAttribute("queryID", "a"
												+ invocationCount++,
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

	public static List createMobySimpleListFromCollection(String xml,
			String mobyEndpoint) {
		
		ArrayList arrayList = new ArrayList();
		if (xml == null)
			return arrayList;
		// variables needed
		MobyObjectClassNSImpl mo = new MobyObjectClassNSImpl(mobyEndpoint);
		Element documentElement = XMLUtilities.getDOMDocument(xml)
				.getRootElement();

		Element de = documentElement.getChild("mobyContent",
				MobyObjectClassNSImpl.MOBYNS);
		if (de != null) {
			List mobyDatas = de.getChildren("mobyData",
					MobyObjectClassNSImpl.MOBYNS);
			if (mobyDatas == null)
				mobyDatas = de.getChildren("mobyData");
			if (mobyDatas == null || mobyDatas.size() == 0)
				return arrayList;
			for (Iterator iter = mobyDatas.iterator(); iter.hasNext();) {
				Object obj = iter.next();
				if (obj instanceof Element) {
					Element e = (Element) obj;
					if (e != null) {
						List children = e.getChildren("Collection",
								MobyObjectClassNSImpl.MOBYNS);
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
								for (Iterator iter2 = simples.iterator(); iter2
										.hasNext();) {
									Object element = iter2.next();
									iter2.remove();
									if (element instanceof Element) {
//										 element data
										Element sim = (Element) element;
										Element mobyData = new Element("mobyData",MobyObjectClassNSImpl.MOBYNS);
										Element root = new Element("MOBY", MobyObjectClassNSImpl.MOBYNS);
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
	 * @param elements list is a list of items to append to a moby message
	 * @return a string representing the moby message
	 */
	public static Element createMultipleInvocationMessageFromList(List elements) {
		Element root = null;
		Document doc = new Document();
		doc.setBaseURI(MobyObjectClassNSImpl.MOBYNS.getURI());
		// got a document, now need to create mobyData
		root = new Element("MOBY", MobyObjectClassNSImpl.MOBYNS);
		doc.setRootElement(root);
		Element mobyContent = new Element("mobyContent",
				MobyObjectClassNSImpl.MOBYNS);
		int count = 0;
		for (Iterator iter = elements.iterator(); iter.hasNext();) {
			String str = (String) iter.next();
			Document d = getDOMDocument(str);
			Element mobyData = new Element("mobyData", MobyObjectClassNSImpl.MOBYNS);
			mobyData.setAttribute("queryID", "a"+count++, MobyObjectClassNSImpl.MOBYNS);
			mobyData.addContent(d.getRootElement().getChild("mobyContent", MobyObjectClassNSImpl.MOBYNS).getChild("mobyData", MobyObjectClassNSImpl.MOBYNS).cloneContent());
			mobyContent.addContent(mobyData);
		}
		root.addContent(mobyContent);
		return root;
	}
	// ////////////
	static String s = "<?xml version=\'1.0\' encoding=\'UTF-8\'?><moby:MOBY xmlns:moby=\'http://www.biomoby.org/moby\' xmlns=\'http://www.biomoby.org/moby\'><moby:mobyContent moby:authority=\'illuminae.com\'><moby:serviceNotes>This is a wrapper around the publicly available GMHMM server at http://opal.biology.gatech.edu/GeneMark/eukhmm.cgi</moby:serviceNotes>\r\n" + 
			"        <moby:mobyData moby:queryID=\'a1\'>\r\n" + 
			"            <moby:Simple moby:articleName=\'html_output\'><text-html namespace=\'\' id=\'\'><![CDATA[<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\r\n" + 
			"<!DOCTYPE html\r\n" + 
			"	PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\r\n" + 
			"	 \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\r\n" + 
			"<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en-US\" xml:lang=\"en-US\"><head><title>Eukaryotic GeneMark.hmm</title>\r\n" + 
			"<link rev=\"made\" href=\"mailto:john%40amber.biology.gatech.edu\" />\r\n" + 
			"</head><body bgcolor=\"white\"><br><font face=\"Verdana,Arial,Helvetica\"><b><font size=+1>Eukaryotic GeneMark.hmm</font></b> <font size=-2><a href=\"eukhmm.cgi\">(Reload this page)</a></font></font><table callpadding=\"0\"><tr valign=\"top\"><td><font face=\"Verdana,Arial,Helvetica\"><b>Reference:</b></font></td><td><font face=\"Verdana,Arial,Helvetica\">Borodovsky M. and Lukashin A. (unpublished)<br></td></tr><table><table callpadding=\"0\"><tr valign=\"top\"><td><font face=\"Verdana,Arial,Helvetica\"><tr><td colspan=\"2\"><font face=\"Verdana,Arial,Helvetica\"><a href=\"plant_accuracy.html\">Accuracy comparison</a></td></tr></table><tr><td colspan=\"2\"><font face=\"Verdana,Arial,Helvetica\"><br><font color=\"red\">UPDATE November 2004. </font>Eukaryotic GeneMark.hmm models have been updated for the following species:<br> O. sativa (Rice)<br>A. thaliana<br>C. elegans<br>C. reinhardtii<br>D. melanogaster<br><br><font size=\"-1\"><a href=\"eukhmm_history.html\">Listing of previous updates</a></font></font></td></tr></table><hr /><h1>Result of last submittal:</h1><H4><A NAME=\"hmm\">GeneMark.hmm Listing</A> </H4><H4>Go to: <A HREF=\"#prot\">GeneMark.hmm Protein Translations</A></H4><H4>Go to: <A HREF=\"#sub\">Job Submittal</A></H4>\r\n" + 
			"<PRE>\r\n" + 
			"Eukariotyc GeneMark.hmm version 3.3\r\n" + 
			"Sequence name: Thu Oct 13 12:01:01 EDT 2005\r\n" + 
			"Sequence length: 1009 bp\r\n" + 
			"G+C content: 56.79%\r\n" + 
			"Matrices file: /home/genmark/euk_ghm.matrices/athaliana_hmm3.0mod\r\n" + 
			"Thu Oct 13 12:01:02 2005\r\n" + 
			"\r\n" + 
			"\r\n" + 
			"Predicted genes/exons\r\n" + 
			"\r\n" + 
			"Gene Exon Strand Exon           Exon Range         Exon      Start/End\r\n" + 
			"  #    #         Type                             Length       Frame\r\n" + 
			"\r\n" + 
			"\r\n" + 
			"  1   1  -  Single            174        941        768          3 1\r\n" + 
			"\r\n" + 
			">gene_1|GeneMark.hmm|255_aa\r\n" + 
			"MASSSAVNPRTILGIPLDSGRCSVAGNGIIYPRDVSRDPGEDCGLLGDITAQAGYKAGHT\r\n" + 
			"VDSILAIHQAVKGASRITLASSTNSVSSSAHHGGLHSGAPVGGAGAGGVVHGGQVSLLQG\r\n" + 
			"LGQLPIGLCPAPACDVAGRVVSQDGSRLWQNTQLDIAVEGSTLGQAQHGDVVTCSHVVAV\r\n" + 
			"PGWMHHDLLHADVLLGAIVLAQVVVSQHHTEGHLAIHTVSRCHHPLLFDEGASTGVIPGT\r\n" + 
			"SRLVLEGNLRGPRIL\r\n" + 
			"\r\n" + 
			"</PRE><BR>\r\n" + 
			"<H4><A NAME=\"prot\">GeneMark.hmm Protein Translations</A></H4><H4>Go to: <A HREF=\"#hmm\">GeneMark.hmm Listing</A></H4>\r\n" + 
			"<H4>Go to: <A HREF=\"#sub\">Job Submittal</A></H4><BR>\r\n" + 
			"<PRE>\r\n" + 
			"</PRE><HR>\r\n" + 
			"<form method=\"post\" action=\"/GeneMark/eukhmm.cgi\" enctype=\"multipart/form-data\">\r\n" + 
			"<table border=\"0\" width=\"100%\"><tr><td bgcolor=\"#A0B8C8\"><font face=\"Verdana,Arial,Helvetica\">Input Sequence</font></td></tr><tr><td><font face=\"Verdana,Arial,Helvetica\">Title (optional):\r\n" + 
			"<a href=\"eukhmm_instructions.html#title\"><img src=\"images/info.gif\" height=10 width=10 border=0></a><br><input type=\"text\" name=\"title\"  size=\"77\" maxlength=\"80\" /><br><br>Sequence:<a href=\"eukhmm_instructions.html#title\"><img src=\"images/info.gif\" height=10 width=10 border=0></a><br><textarea name=\"sequence\" rows=\"10\" cols=\"62\">&gt;gi|163483|gb|M80838.1|BOVPANPRO B.taurus prepreproelastase I mRNA, complete cds GGGCGTGGCAACATGCTGCGCTTGCTGGTGTTCACCTCTCTCGTCCTTTATGGACACAGCACCCAGGACTTTCCAGAAACCAACGCCCGGGTAGTTGGAGGGACTGCGGTCTCAAAGAATTCTTGGCCCTCTCAGATTTCCCTCCAGTACAAGTCTGGAAGTTCCTGGTATCACACCTGTGGAGGCACCCTCATCAAACAGAAGTGGGTGATGACAGCGGCTCACTGTGTGGATAGCCAAATGACCTTCCGTGTGGTGCTGGGAGACCACAACCTGAGCCAGAACGATGGCACCGAGCAGTACATCAGCGTGCAGAAGATCGTGGTGCATCCATCCTGGAACAGCAACAACGTGGCTGCAGGTTACGACATCGCCGTGCTGCGCCTGGCCCAGAGTGCTACCCTCAACAGCTATGTCCAGCTGGGTGTTCTGCCACAGTCGGGAACCATCCTGGCTAACAACACGCCCTGCTACATCACAGGCTGGGGCAGGACTAAGACCAATGGGCAGCTGGCCCAGACCCTGCAGCAGGCTTACCTGCCCTCCGTGGACTACGCCACCTGCTCCAGCTCCTCCTACTGGGGCTCCACTGTGAAGACCACCATGGTGTGCGCTGGAGGAGACGGAGTTCGTGCTGGATGCCAGGGTGATTCTGGAGGCCCCCTTCACTGCTTGGTGAATGGCCAGTATGCTGTCCACGGTGTGACCAGCTTTGTATCCAGCCTGGGCTGTAATGTCTCCAAGAAGCCCACAGTCTTCACCCGGGTCTCTGCTTACATCTCTTGGATAAATAATGCCATTGCCAGCAACTGAACATCTTCCTGAGTCCAGTGGTATTCCCAAGATGGTTCTGGGATTGACAGCAGAACTTGAGGCCATCAAGGAAAAAACCAGTCTAAGAGACTATTGAGCCAGATGTGGAAAAGCAAATAAAATCGAATATATGT</textarea><br><br>Sequence File upload:<a href=\"eukhmm_instructions.html#title\"><img src=\"images/info.gif\" height=10 width=10 border=0></a><br><input type=\"file\" name=\"seq_file\"  size=\"50\" maxlength=\"120\" /><br><br><font face=\"Verdana,Arial,Helvetica\">Species:<a href=\"eukhmm_instructions.html#species#title\"><img src=\"images/info.gif\" height=10 width=10 border=0></a><select name=\"org\" size=\"1\">\r\n" + 
			"<option value=\"H.sapiens\">H.sapiens</option>\r\n" + 
			"<option value=\"C.elegans\">C.elegans</option>\r\n" + 
			"<option value=\"D.melanogaster\">D.melanogaster</option>\r\n" + 
			"<option selected=\"selected\" value=\"A.thaliana\">A.thaliana</option>\r\n" + 
			"<option value=\"C.reinhardtii\">C.reinhardtii</option>\r\n" + 
			"<option value=\"G.gallus\">G.gallus</option>\r\n" + 
			"<option value=\"O.sativa\">O.sativa</option>\r\n" + 
			"<option value=\"Z.mays\">Z.mays</option>\r\n" + 
			"<option value=\"T.aestivum\">T.aestivum</option>\r\n" + 
			"<option value=\"H.vulgare\">H.vulgare</option>\r\n" + 
			"<option value=\"M.musculus\">M.musculus</option>\r\n" + 
			"</select><p></font><tr><td bgcolor=\"#A0B8C8\"><font face=\"Verdana,Arial,Helvetica\">Output Options</font></td></tr><tr><td valign=\"top\" width=\"100%\"><font face=\"Verdana,Arial,Helvetica\">Email Address: <font size=\"-1\">(required for graphical output or sequences longer than 400000 bp)</font><a href=\"eukhmm_instructions.html#graph\"><img src=\"images/info.gif\" height=10 width=10 border=0></a><br><input type=\"text\" name=\"address\"  size=\"31\" maxlength=\"100\" /><br><br /><input type=\"checkbox\" name=\"pdf\" value=\"1\" checked=\"checked\" /> Generate PDF graphics (screen)<br><input type=\"checkbox\" name=\"postscript\" value=\"1\" /> Generate PostScript graphics (email)<a href=\"eukhmm_instructions.html#graph\"><img src=\"images/info.gif\" height=10 width=10 border=0></a><br /><input type=\"checkbox\" name=\"gmlst\" value=\"1\" /> Print GeneMark 2.4 predictions in addition to GeneMark.hmm predictions<a href=\"eukhmm_instructions.html#graph\"><img src=\"images/info.gif\" height=10 width=10 border=0></a><br /><input type=\"checkbox\" name=\"protein\" value=\"1\" checked=\"checked\" /> Translate predicted genes into protein<a href=\"eukhmm_instructions.html#graph\"><img src=\"images/info.gif\" height=10 width=10 border=0></a></font><br><br></td></tr><tr><td bgcolor=\"#A0B8C8\"><table border=\"0\"><tr><td width=\"50%\"><font face=\"Verdana,Arial,Helvetica\">Run</font></td><td><input type=\"reset\" name=\"Default\" value=\"Default\" />&nbsp;<input type=\"submit\" name=\"Action\" value=\"Start GeneMark.hmm\" /></td></tr></table></td></tr></table><div><input type=\"hidden\" name=\".cgifields\" value=\"protein\"  /><input type=\"hidden\" name=\".cgifields\" value=\"postscript\"  /><input type=\"hidden\" name=\".cgifields\" value=\"org\"  /><input type=\"hidden\" name=\".cgifields\" value=\"gmlst\"  /><input type=\"hidden\" name=\".cgifields\" value=\"pdf\"  /></div></form><HR><font face=\"Verdana,Arial,Helvetica\">\r\n" + 
			"Web pages maintained by\r\n" + 
			"<A HREF=\"mailto:gte851w@prism.gatech.edu\">GeneMark administrator, <i>gte851w@prism.gatech.edu</i></A>. Please send any suggestions for improvements or problems to the web page maintainer.</font></font> \r\n" + 
			"</body></html>\r\n" + 
			"\r\n" + 
			"]]></text-html></moby:Simple>\r\n" + 
			"        </moby:mobyData>\r\n" + 
			"        </moby:mobyContent></moby:MOBY>";

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
		//System.out.println(XMLUtilities.createMobySimpleListFromCollection(s, null));
		Element sim = jDomUtilities.getElement("Simple",doc.getRootElement(),new String[]{"articleName=html_output"});
		System.out.println(mo.toString(sim));
		
		Element element = jDomUtilities.getElement("text-html",sim,new String[]{"articleName="});
		System.out.println(mo.toString(element));
		
	}
}
