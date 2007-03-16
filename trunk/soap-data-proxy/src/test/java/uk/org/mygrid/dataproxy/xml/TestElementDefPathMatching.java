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
 * Filename           $RCSfile: TestElementDefPathMatching.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-03-16 15:28:59 $
 *               by   $Author: sowen70 $
 * Created on 16 Mar 2007
 *****************************************************************/
package uk.org.mygrid.dataproxy.xml;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestElementDefPathMatching {

	@Test
	public void testDirectMatch() throws Exception
	{
		ElementDef a = new ElementDef("name","namespace","*/name","operation");
		
		assertTrue(a.checkPath("name"));
		assertFalse(a.checkPath("bob"));
	}
	
	@Test
	public void testSimplePath() throws Exception 
	{
		ElementDef a = new ElementDef("name","namespace","*/start/*/end","operation");
		
		assertTrue(a.checkPath("start/middle/end"));
		assertTrue(a.checkPath("beginning/start/middle/end"));
		assertTrue(a.checkPath("start/bob/end"));
		assertTrue(a.checkPath("start/end"));
		assertFalse(a.checkPath("beginning/middle/end"));		
	}
	
	public void testOpenEndedPath() throws Exception
	{
		ElementDef a = new ElementDef("name","namespace","*/start/*","operation");
		assertTrue(a.checkPath("start/bob/monkey"));
		assertTrue(a.checkPath("start/bob"));
		assertTrue(a.checkPath("start/bob/monkey/parrot"));
		assertTrue(a.checkPath("cheese/start/bob/monkey/parrot"));
		assertTrue(a.checkPath("chicken/cheese/start/bob/monkey/parrot"));
		assertTrue(a.checkPath("/start"));
		
		assertFalse(a.checkPath("start"));
		assertFalse(a.checkPath("starta"));
		assertFalse(a.checkPath("astarta"));
		assertFalse(a.checkPath("astart"));
		assertFalse(a.checkPath("bob/monkey/parrot"));
		
	}
	
	@Test
	public void testMultiLevelsOfWildcard() throws Exception {
		ElementDef a = new ElementDef("name","namespace","*/start/*/*/bob","*");
		
		assertTrue(a.checkPath("start/bob"));
		assertTrue(a.checkPath("start/middle/bob"));
		assertTrue(a.checkPath("start/middle/end/bob"));
		
		assertTrue(a.checkPath("prestart/start/middle/end/bob"));
		
		assertFalse(a.checkPath("astart/bob"));
		assertFalse(a.checkPath("startbob"));
	}
}
