/**
 * A wrapper around a {@link NodeList} to make it {@link Iterable}.
 * 
 * @author Stian Soiland-Reyes
 */
package net.sf.taverna.raven.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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