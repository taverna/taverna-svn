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

package net.sf.taverna.ocula.example;

import java.util.*;

/**
 * A PhoneBook contains a set of Person objects with contact details
 * @author Tom Oinn
 */
public class PhoneBook {
    
    private List people;

    /**
     * Create a new PhoneBook object with a few Person entries
     * hardcoded in to test the example
     */
    public PhoneBook() {
	this.people = new ArrayList();
	people.add(new Person("Andy","England","123453453"));
	people.add(new Person("Bob","Latvia","23232342"));
	people.add(new Person("Claire","England","2346456"));
	people.add(new Person("Danielle","France","345356456"));
    }

    /**
     * Get all the Person objects within this PhoneBook
     */
    public Person[] getPeople() {
	return (Person[])this.people.toArray(new Person[0]);
    }

}
