package idaservicetype.idaservicetype.ui.converter;

import org.semanticweb.owl.io.OWLOntologyInputSource;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;/*
 * Copyright (C) 2007, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/**
 * Author: Simon Jupp<br>
 * Date: Jan 14, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class OntologyStreamReader implements OWLOntologyInputSource {

	Reader in = null;
	InputSource inSource = null;

	public OntologyStreamReader(String output) {
		in = new StringReader(output);
		inSource = new InputSource();
		inSource.setCharacterStream(in);
	}


	public InputStream getInputStream() {
		return inSource.getByteStream();
	}

	public URI getPhysicalURI() {
		URI uri = URI.create("");

		return uri;
	}

	public Reader getReader() {
		return in;
	}

	public boolean isInputStreamAvailable() {
		try {
			if (inSource.getByteStream().available() == 1) return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public boolean isReaderAvailable() {
		try {
			return in.ready();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}