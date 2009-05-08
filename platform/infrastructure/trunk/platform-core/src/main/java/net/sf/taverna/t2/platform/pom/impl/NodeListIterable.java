/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
 * A wrapper around a {@link NodeList} to make it {@link Iterable}.
 * 
 * @author Stian Soiland-Reyes
 */
package net.sf.taverna.t2.platform.pom.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Convenience helper wrapper around NodeList to allow iteration over NodeList
 * 
 * @author Stian Soiland-Reyes
 * @author Tom Oinn
 * 
 */
public class NodeListIterable implements Iterable<Node> {
	private final NodeList nodeList;

	public NodeListIterable(NodeList nodeList) {
		this.nodeList = nodeList;
	}

	public Iterator<Node> iterator() {
		return new NodeListIterator();
	}

	private class NodeListIterator implements Iterator<Node> {
		private int nextPosition = 0;

		public boolean hasNext() {
			synchronized (nodeList) {
				return nextPosition < nodeList.getLength();
			}
		}

		public Node next() throws NoSuchElementException {
			Node result;
			synchronized (nodeList) {
				result = nodeList.item(nextPosition);
				if (result == null) {
					throw new NoSuchElementException();
				}
				nextPosition++;
			}
			return result;
		}

		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}
}
