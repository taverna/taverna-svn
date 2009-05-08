/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
/*
 * CVS
 * $Author: davidwithers $
 * $Date: 2009/04/17 10:29:57 $
 * $Revision: 1.1 $
 * University of Twente, Human Media Interaction Group
 */
package net.sf.taverna.t2.activities.rshell;

/**
 * Class containing the conversion of java type stirngs to classes
 * 
 * @author Ingo Wassink
 * 
 */
public abstract class RshellPortTypes {
    private static final String JAVA_SERIALIZED_OBJECT_MIME_TYPE = "'application/x-java-serialized-object'";

    private static final String PLAIN_TEXT_MIME_TYPE = "'text/plain'";

    private static final String PLAIN_TEXT_LIST_MIME_TYPE = "l('text/plain')";

    private static final String PNG_IMAGE_MIME_TYPE = "'image/png'";
    
    private static final String TEXT_FILE_MIME_TYPE = "'text/plain'";

    // PDF is currently not supported by Taverna
    // private static final String PDF_APPLICATION_MIME_TYPE =
    // "'application/pdf'";

    /**
	 * The enumeration type for symantic port types
	 */
    public static enum SymanticTypes {
        BOOL("boolean", PLAIN_TEXT_MIME_TYPE), DOUBLE("double",
                PLAIN_TEXT_MIME_TYPE), INTEGER("integer", PLAIN_TEXT_MIME_TYPE), REXP(
                "R-expression", JAVA_SERIALIZED_OBJECT_MIME_TYPE), STRING(
                "string", PLAIN_TEXT_MIME_TYPE), DOUBLE_LIST("double[]",
                PLAIN_TEXT_LIST_MIME_TYPE), INTEGER_LIST("integer[]",
                PLAIN_TEXT_LIST_MIME_TYPE), STRING_LIST("string[]",
                PLAIN_TEXT_LIST_MIME_TYPE), PNG_FILE("PNG-image",
                PNG_IMAGE_MIME_TYPE, true),
                TEXT_FILE("Text-file", TEXT_FILE_MIME_TYPE, true);
        // PDF is currently not supported by Taverna
        // PDF("PDF-file", PDF_APPLICATION_MIME_TYPE, true)
        ;

        public final String description;

        public final String syntacticType;
        
        public final boolean isFile;

        SymanticTypes(String description, String syntacticType) {
            this(description, syntacticType, false);
        }
        
        SymanticTypes(String description, String syntacticType, boolean isFile) {
            this.description = description;
            this.syntacticType = syntacticType;
            this.isFile = isFile;
        }
    };
}
