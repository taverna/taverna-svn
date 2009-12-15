/*******************************************************************************
 * Copyright (C) 2009 Ingo Wassink of University of Twente, Netherlands and
 * The University of Manchester   
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

/**
 * @author Ingo Wassink
 * @author Ian Dunlop
 * @author Alan R Williams
 */
/*
 * CVS
 * $Author: sowen70 $
 * $Date: 2008/09/04 13:41:08 $
 * $Revision: 1.2 $
 * University of Twente, Human Media Interaction Group
 */
package net.sf.taverna.t2.activities.rshell;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Class containing the conversion of java type stirngs to classes
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

	private static final List<SemanticTypes> inputSymanticTypes;
	
	private static final List<SemanticTypes> outputSymanticTypes;

	/**
	 * The enumeration type for symantic port types
	 */
    public static enum SemanticTypes {
        BOOL("Logical", PLAIN_TEXT_MIME_TYPE, String.class, 0),
        DOUBLE("Numeric", PLAIN_TEXT_MIME_TYPE, Double.class, 0),
        INTEGER("Integer", PLAIN_TEXT_MIME_TYPE, Integer.class, 0),
        REXP("R-expression", JAVA_SERIALIZED_OBJECT_MIME_TYPE, Object.class, 0),
        STRING("String", PLAIN_TEXT_MIME_TYPE, String.class, 0),
        BOOL_LIST("Logical vector", PLAIN_TEXT_LIST_MIME_TYPE, String.class, 1),
        DOUBLE_LIST("Numeric vector", PLAIN_TEXT_LIST_MIME_TYPE, Double.class, 1),
        INTEGER_LIST("Integer vector", PLAIN_TEXT_LIST_MIME_TYPE, Integer.class, 1),
        STRING_LIST("String vector", PLAIN_TEXT_LIST_MIME_TYPE, String.class, 1),
        PNG_FILE("PNG-image", PNG_IMAGE_MIME_TYPE, byte[].class, 0, true),
        TEXT_FILE("Text-file", TEXT_FILE_MIME_TYPE, String.class, 0, true);
        // PDF is currently not supported by Taverna
        // PDF("PDF-file", PDF_APPLICATION_MIME_TYPE, true)
        ;

        public final String description;

        public final String syntacticType;
        
        public final boolean isFile;

		private final Class semanticClass;

		private final int depth;

        SemanticTypes(String description, String syntacticType, Class semanticClass, int depth) {
            this(description, syntacticType, semanticClass, depth, false);
        }
        
        SemanticTypes(String description, String syntacticType, Class semanticClass, int depth, boolean isFile) {
            this.description = description;
            this.syntacticType = syntacticType;
			this.semanticClass = semanticClass;
			this.depth = depth;
            this.isFile = isFile;
        }

		/**
		 * @return the semanticClass
		 */
		public Class getSemanticClass() {
			return semanticClass;
		}

		/**
		 * @return the depth
		 */
		public int getDepth() {
			return depth;
		}
		

    }
    
	static {
		inputSymanticTypes = new ArrayList<SemanticTypes>(Arrays.asList(SemanticTypes.values()));		
//		inputSymanticTypes.remove(SemanticTypes.PDF_FILE);
		inputSymanticTypes.remove(SemanticTypes.PNG_FILE);
		inputSymanticTypes.remove(SemanticTypes.REXP);
		
		outputSymanticTypes = new ArrayList<SemanticTypes>(Arrays.asList(SemanticTypes.values()));
		outputSymanticTypes.remove(SemanticTypes.REXP);
   }

	/**
	 * @return the inputSymanticTypes
	 */
	public static SemanticTypes[] getInputSymanticTypes() {
		return inputSymanticTypes.toArray(new SemanticTypes[]{});
	}

	/**
	 * @return the outputSymanticTypes
	 */
	public static SemanticTypes[] getOutputSymanticTypes() {
		SemanticTypes[] result = outputSymanticTypes.toArray(new SemanticTypes[]{});
		return (result);
	};
}
