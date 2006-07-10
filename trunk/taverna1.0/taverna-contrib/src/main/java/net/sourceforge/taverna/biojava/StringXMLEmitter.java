package net.sourceforge.taverna.biojava;

import java.io.StringWriter;

import org.biojava.bio.program.xml.BaseXMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class StringXMLEmitter extends DefaultHandler {

	private BaseXMLWriter oXMLWriter;

	private boolean tEmitQNames;

	private String oName;

	private StringWriter writer = new StringWriter();

	public StringXMLEmitter(StringWriter writer) {
		oXMLWriter = new BaseXMLWriter();
		tEmitQNames = true;
		this.writer = writer;
	}

	public StringXMLEmitter() {
		oXMLWriter = new BaseXMLWriter();
		tEmitQNames = true;
		this.writer.write("<?xml version=\"1.0\"?>");
	}

	public StringXMLEmitter(boolean flag) {
		oXMLWriter = new BaseXMLWriter();
		tEmitQNames = true;
		setEmitQNames(flag);
		this.writer.write("<?xml version=\"1.0\"?>");
	}

	public void characters(char ach[], int i, int j) throws SAXException {
		writer.write(oXMLWriter.writePCData(new String(ach, i, j)));
	}

	public void endElement(String string1, String string2, String string3) {
		writer.write(oXMLWriter.endElement());
	}

	private boolean isEmitQNames() {
		return tEmitQNames;
	}

	private void setEmitQNames(boolean flag) {
		tEmitQNames = flag;
	}

	public void startElement(String string1, String string2, String string3, Attributes attributes) {
		if (isEmitQNames())
			oName = string3;
		else
			oName = string2;
		if (attributes.getLength() != 0)
			writer.write(oXMLWriter.startElement(oName, attributes));
		else
			writer.write(oXMLWriter.startElement(oName));
	}

	/**
	 * @return Returns the writer.
	 */
	public StringWriter getWriter() {
		return writer;
	}

	/**
	 * @param writer
	 *            The writer to set.
	 */
	public void setWriter(StringWriter writer) {
		this.writer = writer;
	}
}