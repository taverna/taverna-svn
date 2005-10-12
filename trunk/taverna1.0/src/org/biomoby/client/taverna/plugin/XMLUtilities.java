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
	 */
	public static String getMobyElement(Element xml, String objectType,
			String articleName, String[] namespaces, String mobyEndpoint) {
		MobyObjectClassNSImpl moc = new MobyObjectClassNSImpl(mobyEndpoint);
		Element e = jDomUtilities.getElement(objectType, xml,
				new String[] { "articleName="
						+ ((articleName == null) ? "" : articleName) });
		// TODO check namespaces, etc.
		if (e != null) {
			return moc.toString(createMobyDataElementWrapper(moc
					.toSimple(e, "")));
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
	// ////////////
	static String s = "<?xml version=\'1.0\' encoding=\'UTF-8\'?><moby:MOBY xmlns:moby=\'http://www.biomoby.org/moby\' xmlns=\'http://www.biomoby.org/moby\'><moby:mobyContent moby:authority=\'illuminae.com\'><moby:serviceNotes>This takes the HTML output from the public GeneMark HMM server and extracts the fasta protein content from it as a collection of FASTA_AA objets</moby:serviceNotes>\r\n"
			+ "        <moby:mobyData moby:queryID=\'a1\'>\r\n"
			+ "            <moby:Collection moby:articleName=\'fasta_output\'>\r\n"
			+ "                <moby:Simple><FASTA_AA namespace=\'\' id=\'\'><![CDATA[>gene_1|GeneMark.hmm|690_aa\r\n"
			+ "MGRSHFVIFIILLNLLQILDPSFSLPLCTDSRAPFQQKTPLAFCSYNGTSCCNSTDDKQL\r\n"
			+ "QTQFNAMNISDPGCASLVKSVICAMCDKFSAELFRTDSVPRELPILCNSTTSENSNKSSQ\r\n"
			+ "TKNDFCSKVWTTCQNVSIISSPFAASVKSNSTKLTDLWKSQIDFCNEFGGASGVGSVCFA\r\n"
			+ "GEPVSLNSTTPISPPGGLCLEKIGNGSYINMVAHPDGSSRAFFSNQQGKIWLATIPAVDS\r\n"
			+ "GKLLDLDESSPFLDLIDEVHFDTELGMMGIAFHPKFSQNGRFFVSFNCDKQAWPGCGGRC\r\n"
			+ "SCNSDIDCDPSKLPSDSGSQPCQYQAVIAEFTASGSQPTQAKTASPKEVRRIFTMGLPFT\r\n"
			+ "GHHGGQILFGPRDGYLYFMMGDGGGIGDPYNFSQNKKSLLGKIIRLDIDSTSSVEEITKL\r\n"
			+ "GLWGNYSIPKDNPYAEDKELQPEIWALGMRNPWRCSFDSARPSYFMCADVGQDKFEEVNI\r\n"
			+ "ISKGGNYGWNEYEGPYLYTPSKSPGGNKSMSSINPIFPVMGYNHSDVNKNGGSASITGGY\r\n"
			+ "FYRSMTDPCMHGRYLFADLYAGFMWAGTENPEDSGTFNTSQISFNCAQKSPIDCTSVPGS\r\n"
			+ "SVPALGYIFSYGEDNNKDMYILASSGVYRVVRPSRCKYTCAKENSSAVDDDIPSSPPPAS\r\n"
			+ "PPSAAIMLTGSYSNFVVILMSLILMLTSWL\r\n"
			+ "\r\n"
			+ "]]></FASTA_AA></moby:Simple>\r\n"
			+ "<moby:Simple><FASTA_AA namespace=\'\' id=\'\'><![CDATA[>gene_2|GeneMark.hmm|553_aa\r\n"
			+ "MTSNDGFSGILKSAKEAFEVGKKFWKELELYKKEVGSIVESNKTEECPHSISISGSEFLG\r\n"
			+ "KGRMMVLPCGLTLGSHITVVGKPRRAHQERDPKISLLREGQFLMVSQFMMELQGLKTVDG\r\n"
			+ "EDPPRILHFNPRLSGDWSGKPMIEQNTCYRMQWGTAQRCDGWRSRDDEETVDGQVKCEKW\r\n"
			+ "IRDNDTNHSEQSKASWWLNRLVGRKKKVDFDWPFPFSEDRLFVLTLSAGFEGYHVNVDGR\r\n"
			+ "HVTSFPYRIGFALEDATGLSLNGDIDVDSVFAASLPTTHPSFAPQRHLDMSNRWKTPPLL\r\n"
			+ "DQPVDLFIGILSAGNHFAERMAIRRSWLQHQLIKSSNVVARFFVALHARKDINVELKKEA\r\n"
			+ "QFFGDIVIVPFMDNYDLVVLKTVAICEYGVHVAFAKNIMKCDDDTFVRVDAVIKEINKIP\r\n"
			+ "ENRSLYVGNINYYHKPLRNGKWAVTYEEWPEEDYPPYANGPGYIISSAIANFVVSEFDNH\r\n"
			+ "KLKLFKMEDVSMGMWVEKFNSSSRPVQYVHSLKFSQSGCVDDYYTAHYQSPRQMICMWNK\r\n"
			+ "LQQLGRPQCCNMR\r\n"
			+ "\r\n"
			+ "]]></FASTA_AA></moby:Simple>\r\n"
			+ "\r\n"
			+ "            </moby:Collection>\r\n"
			+ "            <moby:Collection moby:articleName=\'fasta_output\'>\r\n"
			+ "                <moby:Simple><FASTA_AA namespace=\'\' id=\'\'><![CDATA[>gene_1|GeneMark.hmm|690_aa\r\n"
			+ "MGRSHFVIFIILLNLLQILDPSFSLPLCTDSRAPFQQKTPLAFCSYNGTSCCNSTDDKQL\r\n"
			+ "QTQFNAMNISDPGCASLVKSVICAMCDKFSAELFRTDSVPRELPILCNSTTSENSNKSSQ\r\n"
			+ "TKNDFCSKVWTTCQNVSIISSPFAASVKSNSTKLTDLWKSQIDFCNEFGGASGVGSVCFA\r\n"
			+ "GEPVSLNSTTPISPPGGLCLEKIGNGSYINMVAHPDGSSRAFFSNQQGKIWLATIPAVDS\r\n"
			+ "GKLLDLDESSPFLDLIDEVHFDTELGMMGIAFHPKFSQNGRFFVSFNCDKQAWPGCGGRC\r\n"
			+ "SCNSDIDCDPSKLPSDSGSQPCQYQAVIAEFTASGSQPTQAKTASPKEVRRIFTMGLPFT\r\n"
			+ "GHHGGQILFGPRDGYLYFMMGDGGGIGDPYNFSQNKKSLLGKIIRLDIDSTSSVEEITKL\r\n"
			+ "GLWGNYSIPKDNPYAEDKELQPEIWALGMRNPWRCSFDSARPSYFMCADVGQDKFEEVNI\r\n"
			+ "ISKGGNYGWNEYEGPYLYTPSKSPGGNKSMSSINPIFPVMGYNHSDVNKNGGSASITGGY\r\n"
			+ "FYRSMTDPCMHGRYLFADLYAGFMWAGTENPEDSGTFNTSQISFNCAQKSPIDCTSVPGS\r\n"
			+ "SVPALGYIFSYGEDNNKDMYILASSGVYRVVRPSRCKYTCAKENSSAVDDDIPSSPPPAS\r\n"
			+ "PPSAAIMLTGSYSNFVVILMSLILMLTSWL\r\n"
			+ "\r\n"
			+ "]]></FASTA_AA></moby:Simple>\r\n"
			+ "<moby:Simple><FASTA_AA namespace=\'\' id=\'\'><![CDATA[>gene_2|GeneMark.hmm|553_aa\r\n"
			+ "MTSNDGFSGILKSAKEAFEVGKKFWKELELYKKEVGSIVESNKTEECPHSISISGSEFLG\r\n"
			+ "KGRMMVLPCGLTLGSHITVVGKPRRAHQERDPKISLLREGQFLMVSQFMMELQGLKTVDG\r\n"
			+ "EDPPRILHFNPRLSGDWSGKPMIEQNTCYRMQWGTAQRCDGWRSRDDEETVDGQVKCEKW\r\n"
			+ "IRDNDTNHSEQSKASWWLNRLVGRKKKVDFDWPFPFSEDRLFVLTLSAGFEGYHVNVDGR\r\n"
			+ "HVTSFPYRIGFALEDATGLSLNGDIDVDSVFAASLPTTHPSFAPQRHLDMSNRWKTPPLL\r\n"
			+ "DQPVDLFIGILSAGNHFAERMAIRRSWLQHQLIKSSNVVARFFVALHARKDINVELKKEA\r\n"
			+ "QFFGDIVIVPFMDNYDLVVLKTVAICEYGVHVAFAKNIMKCDDDTFVRVDAVIKEINKIP\r\n"
			+ "ENRSLYVGNINYYHKPLRNGKWAVTYEEWPEEDYPPYANGPGYIISSAIANFVVSEFDNH\r\n"
			+ "KLKLFKMEDVSMGMWVEKFNSSSRPVQYVHSLKFSQSGCVDDYYTAHYQSPRQMICMWNK\r\n"
			+ "LQQLGRPQCCNMR\r\n"
			+ "\r\n"
			+ "]]></FASTA_AA></moby:Simple>\r\n"
			+ "\r\n"
			+ "            </moby:Collection>\r\n"
			+ "        </moby:mobyData>\r\n"
			+ "        <moby:mobyData moby:queryID=\'a2\'>\r\n"
			+ "            <moby:Collection moby:articleName=\'fasta_output\'>\r\n"
			+ "                <moby:Simple><FASTA_AA namespace=\'\' id=\'\'><![CDATA[>gene_1|GeneMark.hmm|690_aa\r\n"
			+ "MGRSHFVIFIILLNLLQILDPSFSLPLCTDSRAPFQQKTPLAFCSYNGTSCCNSTDDKQL\r\n"
			+ "QTQFNAMNISDPGCASLVKSVICAMCDKFSAELFRTDSVPRELPILCNSTTSENSNKSSQ\r\n"
			+ "TKNDFCSKVWTTCQNVSIISSPFAASVKSNSTKLTDLWKSQIDFCNEFGGASGVGSVCFA\r\n"
			+ "GEPVSLNSTTPISPPGGLCLEKIGNGSYINMVAHPDGSSRAFFSNQQGKIWLATIPAVDS\r\n"
			+ "GKLLDLDESSPFLDLIDEVHFDTELGMMGIAFHPKFSQNGRFFVSFNCDKQAWPGCGGRC\r\n"
			+ "SCNSDIDCDPSKLPSDSGSQPCQYQAVIAEFTASGSQPTQAKTASPKEVRRIFTMGLPFT\r\n"
			+ "GHHGGQILFGPRDGYLYFMMGDGGGIGDPYNFSQNKKSLLGKIIRLDIDSTSSVEEITKL\r\n"
			+ "GLWGNYSIPKDNPYAEDKELQPEIWALGMRNPWRCSFDSARPSYFMCADVGQDKFEEVNI\r\n"
			+ "ISKGGNYGWNEYEGPYLYTPSKSPGGNKSMSSINPIFPVMGYNHSDVNKNGGSASITGGY\r\n"
			+ "FYRSMTDPCMHGRYLFADLYAGFMWAGTENPEDSGTFNTSQISFNCAQKSPIDCTSVPGS\r\n"
			+ "SVPALGYIFSYGEDNNKDMYILASSGVYRVVRPSRCKYTCAKENSSAVDDDIPSSPPPAS\r\n"
			+ "PPSAAIMLTGSYSNFVVILMSLILMLTSWL\r\n"
			+ "\r\n"
			+ "]]></FASTA_AA></moby:Simple>\r\n"
			+ "<moby:Simple><FASTA_AA namespace=\'\' id=\'\'><![CDATA[>gene_2|GeneMark.hmm|553_aa\r\n"
			+ "MTSNDGFSGILKSAKEAFEVGKKFWKELELYKKEVGSIVESNKTEECPHSISISGSEFLG\r\n"
			+ "KGRMMVLPCGLTLGSHITVVGKPRRAHQERDPKISLLREGQFLMVSQFMMELQGLKTVDG\r\n"
			+ "EDPPRILHFNPRLSGDWSGKPMIEQNTCYRMQWGTAQRCDGWRSRDDEETVDGQVKCEKW\r\n"
			+ "IRDNDTNHSEQSKASWWLNRLVGRKKKVDFDWPFPFSEDRLFVLTLSAGFEGYHVNVDGR\r\n"
			+ "HVTSFPYRIGFALEDATGLSLNGDIDVDSVFAASLPTTHPSFAPQRHLDMSNRWKTPPLL\r\n"
			+ "DQPVDLFIGILSAGNHFAERMAIRRSWLQHQLIKSSNVVARFFVALHARKDINVELKKEA\r\n"
			+ "QFFGDIVIVPFMDNYDLVVLKTVAICEYGVHVAFAKNIMKCDDDTFVRVDAVIKEINKIP\r\n"
			+ "ENRSLYVGNINYYHKPLRNGKWAVTYEEWPEEDYPPYANGPGYIISSAIANFVVSEFDNH\r\n"
			+ "KLKLFKMEDVSMGMWVEKFNSSSRPVQYVHSLKFSQSGCVDDYYTAHYQSPRQMICMWNK\r\n"
			+ "LQQLGRPQCCNMR\r\n"
			+ "\r\n"
			+ "]]></FASTA_AA></moby:Simple>\r\n"
			+ "\r\n"
			+ "            </moby:Collection>\r\n"
			+ "            <moby:Collection moby:articleName=\'fasta_output\'>\r\n"
			+ "                <moby:Simple><FASTA_AA namespace=\'\' id=\'\'><![CDATA[>gene_1|GeneMark.hmm|690_aa\r\n"
			+ "MGRSHFVIFIILLNLLQILDPSFSLPLCTDSRAPFQQKTPLAFCSYNGTSCCNSTDDKQL\r\n"
			+ "QTQFNAMNISDPGCASLVKSVICAMCDKFSAELFRTDSVPRELPILCNSTTSENSNKSSQ\r\n"
			+ "TKNDFCSKVWTTCQNVSIISSPFAASVKSNSTKLTDLWKSQIDFCNEFGGASGVGSVCFA\r\n"
			+ "GEPVSLNSTTPISPPGGLCLEKIGNGSYINMVAHPDGSSRAFFSNQQGKIWLATIPAVDS\r\n"
			+ "GKLLDLDESSPFLDLIDEVHFDTELGMMGIAFHPKFSQNGRFFVSFNCDKQAWPGCGGRC\r\n"
			+ "SCNSDIDCDPSKLPSDSGSQPCQYQAVIAEFTASGSQPTQAKTASPKEVRRIFTMGLPFT\r\n"
			+ "GHHGGQILFGPRDGYLYFMMGDGGGIGDPYNFSQNKKSLLGKIIRLDIDSTSSVEEITKL\r\n"
			+ "GLWGNYSIPKDNPYAEDKELQPEIWALGMRNPWRCSFDSARPSYFMCADVGQDKFEEVNI\r\n"
			+ "ISKGGNYGWNEYEGPYLYTPSKSPGGNKSMSSINPIFPVMGYNHSDVNKNGGSASITGGY\r\n"
			+ "FYRSMTDPCMHGRYLFADLYAGFMWAGTENPEDSGTFNTSQISFNCAQKSPIDCTSVPGS\r\n"
			+ "SVPALGYIFSYGEDNNKDMYILASSGVYRVVRPSRCKYTCAKENSSAVDDDIPSSPPPAS\r\n"
			+ "PPSAAIMLTGSYSNFVVILMSLILMLTSWL\r\n"
			+ "\r\n"
			+ "]]></FASTA_AA></moby:Simple>\r\n"
			+ "<moby:Simple><FASTA_AA namespace=\'\' id=\'\'><![CDATA[>gene_2|GeneMark.hmm|553_aa\r\n"
			+ "MTSNDGFSGILKSAKEAFEVGKKFWKELELYKKEVGSIVESNKTEECPHSISISGSEFLG\r\n"
			+ "KGRMMVLPCGLTLGSHITVVGKPRRAHQERDPKISLLREGQFLMVSQFMMELQGLKTVDG\r\n"
			+ "EDPPRILHFNPRLSGDWSGKPMIEQNTCYRMQWGTAQRCDGWRSRDDEETVDGQVKCEKW\r\n"
			+ "IRDNDTNHSEQSKASWWLNRLVGRKKKVDFDWPFPFSEDRLFVLTLSAGFEGYHVNVDGR\r\n"
			+ "HVTSFPYRIGFALEDATGLSLNGDIDVDSVFAASLPTTHPSFAPQRHLDMSNRWKTPPLL\r\n"
			+ "DQPVDLFIGILSAGNHFAERMAIRRSWLQHQLIKSSNVVARFFVALHARKDINVELKKEA\r\n"
			+ "QFFGDIVIVPFMDNYDLVVLKTVAICEYGVHVAFAKNIMKCDDDTFVRVDAVIKEINKIP\r\n"
			+ "ENRSLYVGNINYYHKPLRNGKWAVTYEEWPEEDYPPYANGPGYIISSAIANFVVSEFDNH\r\n"
			+ "KLKLFKMEDVSMGMWVEKFNSSSRPVQYVHSLKFSQSGCVDDYYTAHYQSPRQMICMWNK\r\n"
			+ "LQQLGRPQCCNMR\r\n"
			+ "\r\n"
			+ "]]></FASTA_AA></moby:Simple>\r\n"
			+ "\r\n"
			+ "            </moby:Collection>\r\n"
			+ "        </moby:mobyData>\r\n"
			+ "        </moby:mobyContent></moby:MOBY>";

	public static void main(String[] args) throws Exception {

		System.out.println(XMLUtilities.createMobySimpleListFromCollection(s, null));
//		System.out.println(XMLUtilities.getMobyCollection(XMLUtilities
//				.getDOMDocument(s).getRootElement(), "FASTA_AA",
//				"fasta_output", ""));
	}
}
