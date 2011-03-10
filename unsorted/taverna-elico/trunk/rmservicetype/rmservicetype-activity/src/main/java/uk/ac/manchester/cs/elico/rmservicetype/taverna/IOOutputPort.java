package uk.ac.manchester.cs.elico.rmservicetype.taverna;/*
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
public class IOOutputPort implements IOObjectPort {

    private String className;
    private String portName;
    private String fileLocation;


    static String OUTPUTPORT = "OUTPUTPORT";

    public IOOutputPort(String className, String portName, String fileLocation) {
        this.className = className;
        this.portName = portName.replace(" ", "_");
        this.fileLocation = fileLocation;
    }

    public String getPortType() {
        return OUTPUTPORT;
    }

    public void setClassName(String s) {
        className = s;
    }

    public void setPortName(String s) {
        portName = s.replace(" ", "_");
    }

    public void setFileLocation(String s) {
        fileLocation = s;
    }

    public String getClassName() {
        return className;
    }

    public String getPortName() {
        return portName;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public boolean isInputPort() {
        return false;
    }

    public boolean isOutputPort() {
        return true;
    }
}
