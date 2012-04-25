/*
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
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: SOAPResponsePrimitiveParser.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007/11/28 16:05:45 $
 *               by   $Author: sowen70 $
 * Created on 05-May-2006
 *****************************************************************/
package net.sf.taverna.wsdl.soap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.soap.SOAPElement;
import net.sf.taverna.wsdl.parser.TypeDescriptor;


/**
 * SOAPResponseParser responsible for parsing soap responses that map to outputs
 * that can be directly represented with Primitive types (i.e. int, String,
 * String[]).
 * 
 * @author sowen
 * 
 */
@SuppressWarnings("unchecked")
public class SOAPResponsePrimitiveParser implements SOAPResponseParser {

    private List<String> outputNames;

    public SOAPResponsePrimitiveParser(List<TypeDescriptor> outputDescriptors) {
        outputNames=new ArrayList<String>();
        for (TypeDescriptor desc : outputDescriptors) {
            outputNames.add(desc.getName());
        }
    }

    /**
     * Parses each SOAPBodyElement for the primitive type, and places it in the
     * output Map
     */
    @Override
    public Map parse(List<SOAPElement> response) throws Exception {
        Map result = new HashMap();
        int c = 0;

        SOAPElement responseElement = response.get(0);

        for (Iterator<SOAPElement> paramIterator = responseElement.getChildElements(); paramIterator.hasNext();) {
            SOAPElement param = paramIterator.next();
            Object value = param.getTextContent();
            if (outputNames.contains(param.getLocalName())) {
                result.put(param.getElementName(), ObjectConverter.convertObject(value));
            } else {
                result.put(outputNames.get(c), value);
            }
            c++;
        }

        return result;
    }
}
