/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.tools.apiconsumer;

import com.sun.javadoc.*;

/**
 * Provides a summary of the specified ClassDoc as HTML.
 * This class does not provide the &lt;html&gt; and &lt;body&gt;
 * tags, it returns the contents of the &lt;body&gt; tag.
 * @author Tom Oinn
 */
public class HTMLClassSummary {

    public static String getSummary(ClassDoc classdoc) {
	
	StringBuffer sb = new StringBuffer();
	
	sb.append("<h2>"+classdoc.typeName()+"</h2>\n");
	if (classdoc.commentText().equals("") == false) {
	    sb.append("<blockquote>"+classdoc.commentText()+"</blockquote>");
	}
	if (classdoc.isAbstract() == false) {
	    sb.append("<h3>Constructors</h3>");
	    ConstructorDoc[] constructors = classdoc.constructors();
	    sb.append(getExecutableMemberSummary(constructors));
	}
	sb.append("<h3>Methods</h3>");
	MethodDoc[] methods = classdoc.methods();
	sb.append(getExecutableMemberSummary(methods));

	return sb.toString();
    }

    private static String getExecutableMemberSummary(ExecutableMemberDoc[] members) {
	StringBuffer sb = new StringBuffer();
	sb.append("<blockquote>");
	for (int i = 0; i < members.length; i++) {
	    Parameter[] params = members[i].parameters();
	    sb.append("<font color=\"blue\">");
	    if (members[i].isPublic()) {
		sb.append("public ");
	    }
	    else if (members[i].isProtected()) {
		sb.append("protected ");
	    }
	    else if (members[i].isPrivate()) {
		sb.append("private ");
	    }
	    if (members[i].isStatic()) {
		sb.append("static ");
	    }
	    sb.append("</font>");
	    if (members[i] instanceof MethodDoc) {
		// Add the return signature in
		sb.append("<font color=\"green\">");
		MethodDoc md = (MethodDoc)members[i];
		sb.append(md.returnType().typeName()+md.returnType().dimension());
		sb.append("</font> ");
	    }
	    sb.append(members[i].name()+"(");
	    for (int j = 0; j < params.length; j++) {
		Parameter param = params[j];
		sb.append("<font color=\"green\">"+param.type().typeName()+param.type().dimension()+"</font> <font color=\"purple\">"+param.name()+"</font>");
		if (j < params.length-1) {
		    sb.append(", ");
		}
	    }
	    sb.append(")");
	    if (i < members.length-1) {
		sb.append("<br>");
	    }
	}
	sb.append("</blockquote>");
	return sb.toString();
    } 

}
