package net.sf.taverna.t2.cloudone.datamanager;

/**
 * <p>
 * Thrown on attempt to register a malformed list. A list is considered well
 * formed if and only if it's empty or all of its children are well-formed and
 * of the same depth. The depth of the well-formed list will be 1 higher than
 * it's children.
 * </p>
 * <p>
 * Example of a malformed list:
 * </p>
 *
 * <pre>
 * 		List&lt;Object&gt; list1 = ...
 * 		List&lt;List&lt;Object&gt;&gt; deepList1 = new ArrayList&lt;List&lt;Object&gt;&gt;();
 * 		deepList1.add(list1);
 *
 * 		List&lt;Object&gt; list2 = ...
 * 		List&lt;List&lt;Object&gt;&gt; deepList2 = new ArrayList&lt;List&lt;Object&gt;&gt;();
 * 		deepList2.add(list2);
 *
 * 		List&lt;List&lt;List&lt;Object&gt;&gt;&gt; deeperList = new ArrayList&lt;List&lt;List&lt;Object&gt;&gt;&gt;();
 * 		deeperList.add(deepList2);
 *
 * 		List&lt;List&lt;?&gt;&gt; malformedList = new ArrayList&lt;List&lt;?&gt;&gt;();
 * 		malformedList.add(deepList1);
 * 		malformedList.add(deeperList);
 * </pre>
 *
 * <p>
 * <code>malformedList</code> above is malformed because it's first child,
 * <code>deepList1</code> is of depth 2 (it contains a list of elements), but
 * it's second child, <code>deeperList</code> is of depth 3 (it contains a
 * list of lists of elements). Any list containing malformedList would also be
 * malformed.
 * </p>
 *
 * @author Stian Soiland
 * @author Ian Dunlop
 *
 */
public class MalformedListException extends ListException {

	private static final long serialVersionUID = 8825742389472964931L;

	public MalformedListException() {
		super();
	}

	public MalformedListException(String message) {
		super(message);
	}

	public MalformedListException(String message, Throwable cause) {
		super(message, cause);
	}

	public MalformedListException(Throwable cause) {
		super(cause);
	}

}
