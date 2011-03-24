package uk.ac.manchester.cs.elico.converter;

import ch.uzh.ifi.ddis.ida.api.DataRequirement;/*
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
 * Date: Feb 28, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class InputIOObject {

    private String filePath;
    private DataRequirement dataRequirement;
    private String baseURL = "http://www.e-lico.eu/";
    private String basePrefix = "metadata";

    public InputIOObject() {

    }

    public InputIOObject(String filePath, DataRequirement dataRequirement) {


        this.filePath = filePath;
        this.dataRequirement = dataRequirement;
    }

    public InputIOObject(String chosenRepositoryPath) {
        filePath = chosenRepositoryPath;
    }

    public DataRequirement getDataRequirement() {
        return dataRequirement;
    }

    public void setDataRequirement(DataRequirement dataRequirement) {
        this.dataRequirement = dataRequirement;
    }

    public String getFilePath() {

        return filePath;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getBasePrefix() {
        return basePrefix;
    }

    public void setBasePrefix(String basePrefix) {
        this.basePrefix = basePrefix;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}