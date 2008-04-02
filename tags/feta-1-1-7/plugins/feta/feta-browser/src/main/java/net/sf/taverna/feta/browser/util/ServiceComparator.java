/**
 * 
 */
package net.sf.taverna.feta.browser.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import uk.org.mygrid.mygridmobyservice.ServiceDescription;

public class ServiceComparator implements Comparator<ServiceDescription> {
	private static ServiceComparator instance;

	public static <Type extends Comparable<Type>> int compareSets(Set<Type> s1,
			Set<Type> s2) {
		Iterator<Type> s1Iter = s1.iterator();
		Iterator<Type> s2Iter = s2.iterator();
		while (s1Iter.hasNext()) {
			if (!s2Iter.hasNext()) {
				return 1; // s2 smallest
			}
			Type o1Obj = s1Iter.next();
			Type o2Obj = s2Iter.next();
			int diff = o1Obj.compareTo(o2Obj);
			if (diff != 0) {
				return diff;
			}
		}
		if (s2Iter.hasNext()) {
			return -1; // s1 smallest
		} else {
			return 0; // equal for all elements
		}
	}

	public synchronized static ServiceComparator getInstance() {
		if (instance == null) {
			instance = new ServiceComparator();
		}
		return instance;
	}

	protected ServiceComparator() {
	}

	public int compare(ServiceDescription service1, ServiceDescription service2) {
		int diff = compareSets(service1.getHasInterfaceLocations(), service2
				.getHasInterfaceLocations());
		if (diff != 0) {
			return diff;
		}

		return compareSets(service1.getHasServiceNameTexts(), service2
				.getHasServiceNameTexts());
	}

}