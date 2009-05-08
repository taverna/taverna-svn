package net.sf.taverna.t2.invocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines a hierarchical process identifier, used throughout the enactor
 * 
 * @author Tom Oinn
 * 
 */
public class ProcessIdentifier implements Comparable<ProcessIdentifier> {

	private List<String> idList = new ArrayList<String>();
	private String stringForm = null;

	/**
	 * Construct a new process identifier from a colon separated string. If the
	 * supplied string is null or the empty string this is interpreted as the
	 * root process identifier.
	 * 
	 * @param identifierString
	 */
	public ProcessIdentifier(String identifierString) {
		if (identifierString == null || identifierString.equals("")) {
			// 
		} else {
			String[] identifierStringArray = identifierString.split(":");
			if (identifierStringArray.length == 0) {
				idList.add(identifierString);
			}
			configure(identifierStringArray);
		}
	}

	/**
	 * Construct a new process identifier from an array of local identifier
	 * strings.
	 * 
	 * @param identifierStringArray
	 */
	public ProcessIdentifier(String[] identifierStringArray) {
		configure(identifierStringArray);
	}

	/**
	 * Return the process identifier as a colon separated string
	 */
	@Override
	public synchronized String toString() {
		if (stringForm == null) {
			StringBuffer sb = new StringBuffer();
			boolean first = true;
			for (String element : idList) {
				if (!first) {
					sb.append(":");
					first = false;
				}
				sb.append(element);
			}
			stringForm = sb.toString();
		}
		return stringForm;
	}

	/**
	 * Create a process identifier with the specified parent and new local name
	 * 
	 * @param identifierStringArray
	 */
	public ProcessIdentifier createChild(String child) {
		ProcessIdentifier result = new ProcessIdentifier(this);
		result.idList.add(child);
		result.stringForm = null;
		return result;
	}

	/**
	 * Create a process identifier representing the parent of the supplied
	 * identifier, throws a process identifier exception if the supplied
	 * identifier is the root process identifier
	 * 
	 * @param other
	 */
	public ProcessIdentifier getParent() {
		if (idList.isEmpty()) {
			throw new ProcessIdentifierException(
					"Can't get the parent of the root process identifier");
		} else {
			ProcessIdentifier parent = new ProcessIdentifier(this);
			parent.idList.remove(parent.idList.size() - 1);
			parent.stringForm = null;
			return parent;
		}
	}

	/**
	 * Return the last item in the local process name list
	 * 
	 * @return
	 */
	public String getDeepestItem() {
		if (idList.isEmpty()) {
			throw new ProcessIdentifierException(
					"Can't get the last item of the root process identifier");
		}
		return idList.get(idList.size() - 1);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ProcessIdentifier) {
			ProcessIdentifier p = (ProcessIdentifier) other;
			if (toString().equals(p.toString())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	private ProcessIdentifier(ProcessIdentifier other) {
		this.idList.addAll(other.idList);
	}

	public ProcessIdentifier() {
		//
	}

	private final void configure(String[] identifierStringArray) {
		Collections.addAll(idList, identifierStringArray);
	}

	public int compareTo(ProcessIdentifier o) {
		return toString().compareTo(o.toString());
	}

	public String[] asArray() {
		return idList.toArray(new String[] {});
	}

}
