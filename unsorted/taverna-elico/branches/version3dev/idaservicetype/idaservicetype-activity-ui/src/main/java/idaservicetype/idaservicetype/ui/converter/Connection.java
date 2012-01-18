package idaservicetype.idaservicetype.ui.converter;

import ch.uzh.ifi.ddis.ida.api.IOObjectDescription;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.RapidMinerExampleActivity;

import java.util.LinkedList;
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
 * Date: Feb 2, 2011<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 */
public class Connection {

    private String ioObjectId;

    private RapidMinerExampleActivity producer;

    public String getOutputPort() {
        return outputPort;
    }//    private String  producesProperty;

    public void setOutputPort(String outputPort) {
        this.outputPort = outputPort;
    }

    private String outputPort;

    private DMWFProperty  producesProperty;

    private String location;

    private List<User> users = new LinkedList<User>();

    public class DMWFProperty {

        private String propertyName;
        private String propertyType;

        public DMWFProperty(String propertyName, String propertyType) {
            this.propertyName = propertyName;
            this.propertyType = propertyType;
        }

        public String getPropertyName() {

            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getPropertyType() {
            return propertyType;
        }

        public void setPropertyType(String propertyType) {
            this.propertyType = propertyType;
        }
    }

 	public class User {
		private final RapidMinerExampleActivity user;
//		private final String usesProperty;

         public String getInputPort() {
             return inputPort;
         }

         public void setInputPort(String inputPort) {
             this.inputPort = inputPort;
         }

         private String inputPort;

        private final DMWFProperty dmwfProperty;
		private User(RapidMinerExampleActivity operator, DMWFProperty usesProperty) {
			this.user = operator;
			this.dmwfProperty = usesProperty;
		}

		public DMWFProperty getUsesProperty() {
			return dmwfProperty;
		}
		public RapidMinerExampleActivity getOperator() {
			return user;
		}
	}
	

	public Connection(String id) {
		this.ioObjectId = id;
	}

//	public void addUser(RapidMinerExampleActivity operator, String roleName) {
//		users.add(new User(operator, roleName));
//	}

    public void addUser(RapidMinerExampleActivity operator, IOObjectDescription ioObjectDescription) {

        DMWFProperty dmwfProperty = new DMWFProperty(ioObjectDescription.getRMRoleName(), ioObjectDescription.getIOObjectTypeID());
		users.add(new User(operator, dmwfProperty));
	}

	public void setProducer(RapidMinerExampleActivity operator, IOObjectDescription ioObjectDescription) {
        DMWFProperty dmwfProperty = new DMWFProperty(ioObjectDescription.getRMRoleName(), ioObjectDescription.getIOObjectTypeID());
		this.producer = operator;
		this.producesProperty = dmwfProperty;
	}

	public String getIoObjectId() {
		return ioObjectId;
	}
	public List<User> getUsers() {
		return users;
	}
	public RapidMinerExampleActivity getProducer() {
		return producer;
	}
	public DMWFProperty getProducesProperty() {
		return producesProperty;
	}

    public void setLocation(String iooLocation) {
        this.location = iooLocation;
    }

    public String getLocation() {
        return location;
    }
   
}
