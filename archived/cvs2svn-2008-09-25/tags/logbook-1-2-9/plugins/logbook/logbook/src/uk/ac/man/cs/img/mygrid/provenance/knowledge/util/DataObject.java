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
 * Filename           $RCSfile: DataObject.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:13 $
 *               by   $Author: stain $
 * Created on 16-Aug-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.util;

import java.util.HashSet;
import java.util.Set;

/**
 * @author dturi
 * @version $Id: DataObject.java,v 1.1 2007-12-14 12:49:13 stain Exp $
 */
public class DataObject {

    String dataLSID;

    Set inputNames = new HashSet();

    Set outputNames = new HashSet();

    Set derivedFrom = new HashSet();

    Set wrappers = new HashSet();

    Set children = new HashSet();

    boolean dataCollection;

    /**
     * @param dataLSID
     *  
     */
    public DataObject(String dataLSID) {
        this.dataLSID = dataLSID;
    }

    public boolean isDataCollection() {
        return dataCollection;
    }

    public void setDataCollection(boolean dataCollection) {
        this.dataCollection = dataCollection;
    }

    public void addInputName(DataName inputName) {
        inputNames.add(inputName);
    }

    public void addOutputName(DataName outputName) {
        outputNames.add(outputName);
    }

    public void addDerivedFrom(String origin) {
        derivedFrom.add(origin);
    }

    public void addWrapper(String wrapper) {
        wrappers.add(wrapper);
    }

    public void addChild(String child) {
        children.add(child);
    }

    public Set getChildren() {
        return children;
    }

    public String getDataLSID() {
        return dataLSID;
    }

    public Set getDerivedFrom() {
        return derivedFrom;
    }

    /**
     * @return a Set of {@link DataName}s
     */
    public Set getInputNames() {
        return inputNames;
    }

    /**
     * @return a Set of {@link DataName}s
     */
    public Set getOutputNames() {
        return outputNames;
    }

    public Set getWrappers() {
        return wrappers;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (isDataCollection()) {
            sb.append("Data Collection = ");
            sb.append(dataLSID);
            sb.append(" {");
            sb.append("\n\t");
            sb.append("Children = " + children.toString());
        } else {
            sb.append("Atomic Data = ");
            sb.append(dataLSID);
            sb.append(" {");
            sb.append("\n\t");
            sb.append("Wrappers = " + wrappers.toString());
        }
        sb.append("\n\t");
        sb.append("Input Names = " + inputNames.toString());
        sb.append("\n\t");
        sb.append("Output Names = " + outputNames.toString());
        sb.append("\n\t");
        sb.append("Derived from = " + derivedFrom.toString());
        sb.append("\n}");
        return sb.toString();
    }
}
