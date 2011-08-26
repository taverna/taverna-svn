package net.sf.taverna.t2.workflowmodel;

/**
 * A simple generic class to hold a pair of same type objects. Used by various
 * Edit implementations that operate on pairs of Processors amongst other
 * things.
 * 
 * @author Tom Oinn
 * 
 * @param <T>
 *            Type of the pair of contained objects
 */
public class OrderedPair<T> {

	private T a, b;

	/**
	 * Build a new ordered pair with the specified objects.
	 * 
	 * @throws RuntimeException
	 *             if either a or b are null
	 * @param a
	 * @param b
	 */
	public OrderedPair(T a, T b) {
		if (a == null || b == null) {
			throw new RuntimeException(
					"Cannot construct ordered pair with null arguments");
		}
		this.a = a;
		this.b = b;
	}

	/**
	 * Return object a
	 */
	public T getA() {
		return this.a;
	}

	/**
	 * Return object b
	 */
	public T getB() {
		return this.b;
	}

	/**
	 * A pair of objects (a,b) is equal to another pair (c,d) if and only if a,
	 * b, c and d are all the same type and the condition (a.equals(c) &
	 * b.equals(d)) is true.
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof OrderedPair) {
			OrderedPair<?> op = (OrderedPair<?>) other;
			return (a.equals(op.getA()) && b.equals(op.getB()));
		} else {
			return false;
		}
	}

}
