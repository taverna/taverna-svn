package net.sf.taverna.t2.drizzle.util;

public interface PropertiedTreeObjectNode<O> extends PropertiedTreeNode<O> {
	void setObject(final O object);
	O getObject();
}
