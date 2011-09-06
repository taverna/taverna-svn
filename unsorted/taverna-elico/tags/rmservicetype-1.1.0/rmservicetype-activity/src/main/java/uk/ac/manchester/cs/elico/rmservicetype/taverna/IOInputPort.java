package uk.ac.manchester.cs.elico.rmservicetype.taverna;
import java.util.ArrayList;
import java.util.List;
/*

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
 * Date: Mar 9, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class IOInputPort implements IOObjectPort {

    private String className;
    private String portName;
    private List<String> fileLocations;
    private int numberOfPorts = 1;
     
    static String INPUTPORT = "INPUTPORT";

    public IOInputPort(String className, String portName, String fileLocation) {
        this.className = className;
        this.portName = portName.replace(" ", "_");
        this.fileLocations = new ArrayList<String>();
        
        if (fileLocation == null || fileLocation.isEmpty()) {
        	
        	this.fileLocations.add("");
        
        } else {
        	
        	this.fileLocations.add(0, fileLocation);
        	
        }
           
    }

    public String getPortType() {
        return INPUTPORT;
    }

    public void setClassName(String s) {
        className = s;
    }

    public void setPortName(String s) {
        portName = s.replace(" ", "_");
    }
    
    public void setFileLocationAt(int i, String s) {
    	
    	if (s.isEmpty()) {
    		 
    		 if (i >= fileLocations.size()) {
    	    	fileLocations.add(i, "");
    			//[debug]System.out.println(" adding " + fileLocations.get(i));

    	     } else {
    	    	 fileLocations.set(i, "");
    	    	//[debug]System.out.println(" setting " + fileLocations.get(i));

    	     }
    		 
    	} else {
    		
    		if (i >= fileLocations.size()) {
    	    	fileLocations.add(i, s);
    	    	//[debug]System.out.println(" adding " + fileLocations.get(i));

    	    } else {
    	    	 fileLocations.set(i, s);
    	    	//[debug]System.out.println(" setting " + fileLocations.get(i));

    	    }
    	}
            }

    public String getPortClass() {
        return className;
    }

    public String getPortName() {
        return portName;
    }

    public List<String> getFileLocations() {
        return fileLocations;
    }

    public boolean isInputPort() {
        return true;
    }

    public boolean isOutputPort() {
        return false;
    }

	public String getFileLocationAt(int i) {
		
		return fileLocations.get(i);
	}

	public void setNumberOfPorts(int numberOfPorts) {
		this.numberOfPorts = numberOfPorts;
	}

	public int getNumberOfPorts() {
		return numberOfPorts;
	}

	public void removeFileLocation(String location) {
		fileLocations.remove(location);
	}

}
