/*
 * IQueryProvider.java
 *
 * Created on 02 December 2005, 16:15
 */

package uk.ac.man.cs.img.fetaEngine.command;

import uk.ac.man.cs.img.fetaEngine.webservice.CannedQueryType;

/**
 * 
 * @author Pinar
 */
public interface IQueryProvider {

	public String getQueryforCommand(CannedQueryType commandType,
			String paramValue);

}
