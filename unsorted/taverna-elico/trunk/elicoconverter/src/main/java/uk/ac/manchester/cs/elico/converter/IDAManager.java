package uk.ac.manchester.cs.elico.converter;

import ch.uzh.ifi.ddis.ida.api.*;
import ch.uzh.ifi.ddis.ida.api.exception.IDAException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;/*
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
 * Date: Jan 31, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 *
 * This is the initialisation object and manages the creation of
 * IDA API objects and the setting of preferences
 */

public class IDAManager {

    Logger logger = LoggerFactory.getLogger(IDAManager.class);

    private IDAPreferences idaPrefs;

    private IDAInterface idaInterface;

    private Set<MainGoal> mainGoals;
    private Set<Task> tasks;

    private GoalFactory goalFactory;

    public IDAManager(String pathToFlora, String pathToTemporaryFolder) {
        setPreferences(pathToFlora, pathToTemporaryFolder);
        idaInterface = IDAFactory.getIDA();
    }

    


    public IDAManager() {
        mainGoals = new HashSet<MainGoal>();
        tasks = new HashSet<Task>();
        idaInterface = IDAFactory.getIDA();
    }

    public void startPlanner () throws IDAException {
        idaInterface.startPlanner(idaPrefs);
    }

    public void setPreferences (String pathToFlora, String pathToTemporaryFolder) {
        idaPrefs = new IDAPreferences(pathToFlora, pathToTemporaryFolder);
    }

    public IDAPreferences getPreferences() {
        return idaPrefs;
    }

    public IDAInterface getIDAInterface () {
        return idaInterface;
    }

    


    public void shutdownPlanner() throws IDAException {
        idaInterface.shutDownPlanner();
    }
    

    



    

}
