/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreePropertyValueNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeRootNode;
import net.sf.taverna.t2.drizzle.util.PropertyKey;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author alanrw
 *
 */
public class PropertiedTreeNodeImplTest {

	private PropertiedTreeNode<ExampleObject> testImpl;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testImpl = createNode();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	private PropertiedTreeRootNode<ExampleObject> createNode() {
		return new PropertiedTreeRootNodeImpl<ExampleObject> ();
	}
	
	private PropertiedTreeObjectNode<ExampleObject> createObjectNode(final ExampleObject object) {
		PropertiedTreeObjectNode<ExampleObject> result =
			new PropertiedTreeObjectNodeImpl<ExampleObject>();
		result.setObject(object);
		return result;
	}
	
	private PropertiedTreePropertyValueNode<ExampleObject> createPropertyNode(final PropertyKey key) {
		PropertiedTreePropertyValueNode<ExampleObject> result =
			new PropertiedTreePropertyValueNodeImpl<ExampleObject>();
		result.setKey(key);
		return result;
	}
	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeNodeImpl#PropertiedTreeNodeImpl()}.
	 */
	@Test
	public final void testPropertiedTreeNodeImpl() {
		assertEquals("getChildCount", 0, testImpl.getChildCount());
		assertNull("getParent", testImpl.getParent());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeNodeImpl#addChild(net.sf.taverna.t2.drizzle.util.PropertiedTreeNode)}.
	 */
	@Test
	public final void testAddChild() {
		try {
			testImpl.addChild(null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
			// This is OK
		}
		PropertiedTreeNode<ExampleObject> child1 = createNode();
		PropertiedTreeNode<ExampleObject> child2 = createNode();
		PropertiedTreeNode<ExampleObject> child3 = createNode();
		testImpl.addChild(child1);
		assertEquals(1, testImpl.getChildCount());
		assertEquals(child1, testImpl.getChild(0));

		try {
			testImpl.addChild(child1);
			fail("IllegalArgumentException expected");
		}
		catch (IllegalArgumentException e) {
			// This is OK
		}
		testImpl.addChild(child2);
		assertEquals(2, testImpl.getChildCount());
		assertEquals(child1, testImpl.getChild(0));
		assertEquals(child2, testImpl.getChild(1));
		
		testImpl.addChild(child3);
		assertEquals(3, testImpl.getChildCount());
		assertEquals(child1, testImpl.getChild(0));
		assertEquals(child2, testImpl.getChild(1));
		assertEquals(child3, testImpl.getChild(2));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeNodeImpl#getAllObjects()}.
	 */
	@Test
	public final void testGetAllObjects() {
		ExampleObject object1 = new ExampleObject();
		ExampleObject object2 = new ExampleObject();
		ExampleObject object3 = new ExampleObject();
		
		Set<ExampleObject> allObjects = testImpl.getAllObjects();
		assertEquals(0, allObjects.size());
		
		PropertiedTreeNode<ExampleObject> child1 = createNode();
		testImpl.addChild(child1);
		PropertiedTreeObjectNode<ExampleObject> child2 = createObjectNode(object1);
		testImpl.addChild(child2);
		
		allObjects = testImpl.getAllObjects();
		assertEquals(1, allObjects.size());
		assertTrue(allObjects.contains(object1));
		
		PropertiedTreeObjectNode<ExampleObject> grandchild1 = createObjectNode(object2);
		child1.addChild(grandchild1);
		PropertiedTreeObjectNode<ExampleObject> grandchild2 = createObjectNode(object3);
		child1.addChild(grandchild2);
		allObjects = testImpl.getAllObjects();
		assertEquals(3, allObjects.size());
		assertTrue(allObjects.contains(object1));
		assertTrue(allObjects.contains(object2));
		assertTrue(allObjects.contains(object3));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeNodeImpl#getAncestorWithKey(net.sf.taverna.t2.drizzle.util.PropertyKey)}.
	 */
	@Test
	public final void testGetAncestorWithKey() {
		try {
			testImpl.getAncestorWithKey(null);
			fail("NullPointerException expected");
		}
		catch (NullPointerException e) {
			// This is OK
		}
		PropertyKey key1 = new ExampleKey();
		PropertyKey key2 = new ExampleKey();
		PropertyKey key3 = new ExampleKey();
		PropertyKey key4 = new ExampleKey();
		
		PropertiedTreePropertyValueNode<ExampleObject> child = createPropertyNode(key1);
		testImpl.addChild(child);
		
		PropertiedTreePropertyValueNode<ExampleObject> grandchild = createPropertyNode(key2);
		child.addChild(grandchild);
		
		PropertiedTreePropertyValueNode<ExampleObject> greatgrandchild = createPropertyNode(key3);
		grandchild.addChild(greatgrandchild);
		
		PropertiedTreeObjectNode<ExampleObject> leaf = createObjectNode(new ExampleObject());
		greatgrandchild.addChild(leaf);
		
		assertEquals(child, leaf.getAncestorWithKey(key1));
		assertEquals(grandchild, leaf.getAncestorWithKey(key2));
		assertEquals(greatgrandchild, leaf.getAncestorWithKey(key3));
		assertNull(leaf.getAncestorWithKey(key4));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeNodeImpl#getChild(int)}.
	 */
	@Test
	public final void testGetChild() {
		// Done under addChild except for
		assertNull(testImpl.getChild(0));
		assertNull(testImpl.getChild(7));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeNodeImpl#getChildCount()}.
	 */
	@Test
	public final void testGetChildCount() {
		// Done under addChild except for
		assertEquals(0, testImpl.getChildCount());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeNodeImpl#getDepth()}.
	 */
	@Test
	public final void testGetDepth() {
		assertEquals(0, testImpl.getDepth());
		
		PropertiedTreeRootNode<ExampleObject> child = createNode();
		testImpl.addChild(child);
		
		PropertiedTreeRootNode<ExampleObject> grandchild = createNode();
		child.addChild(grandchild);
		
		PropertiedTreeRootNode<ExampleObject> greatgrandchild = createNode();
		grandchild.addChild(greatgrandchild);
		
		PropertiedTreeRootNode<ExampleObject> leaf = createNode();
		greatgrandchild.addChild(leaf);
		
		assertEquals(1, child.getDepth());
		assertEquals(2, grandchild.getDepth());
		assertEquals(3, greatgrandchild.getDepth());
		assertEquals(4, leaf.getDepth());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeNodeImpl#getIndexOfChild(net.sf.taverna.t2.drizzle.util.PropertiedTreeNode)}.
	 */
	@Test
	public final void testGetIndexOfChild() {
		try {
			testImpl.getIndexOfChild(null);
			fail ("NullPointerException expected");
		}
		catch (NullPointerException e) {
			// This is OK
		}
		PropertiedTreeNode<ExampleObject> child1 = createNode();
		PropertiedTreeNode<ExampleObject> child2 = createNode();
		PropertiedTreeNode<ExampleObject> child3 = createNode();
		PropertiedTreeNode<ExampleObject> child4 = createNode();
		testImpl.addChild(child1);
		testImpl.addChild(child2);
		testImpl.addChild(child3);
		
		assertEquals(0, testImpl.getIndexOfChild(child1));
		assertEquals(1, testImpl.getIndexOfChild(child2));
		assertEquals(2, testImpl.getIndexOfChild(child3));
		assertEquals(-1, testImpl.getIndexOfChild(child4));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeNodeImpl#getParent()}.
	 */
	@Test
	public final void testGetParent() {
		assertNull(testImpl.getParent());
		PropertiedTreeNode<ExampleObject> child1 = createNode();
		testImpl.addChild(child1);
		assertEquals(testImpl, child1.getParent());
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeNodeImpl#getPath()}.
	 */
	@Test
	public final void testGetPath() {
		PropertiedTreeRootNode<ExampleObject> child = createNode();
		testImpl.addChild(child);
		
		PropertiedTreeRootNode<ExampleObject> grandchild = createNode();
		child.addChild(grandchild);
		
		PropertiedTreeRootNode<ExampleObject> greatgrandchild = createNode();
		grandchild.addChild(greatgrandchild);
		
		PropertiedTreeRootNode<ExampleObject> leaf = createNode();
		greatgrandchild.addChild(leaf);

		PropertiedTreeNode<ExampleObject>[] path = testImpl.getPath();
		assertEquals(1, path.length);
		assertEquals(testImpl, path[0]);
		
		path = child.getPath();
		assertEquals(2, path.length);
		assertEquals(testImpl, path[0]);
		assertEquals(child, path[1]);

		path = grandchild.getPath();
		assertEquals(3, path.length);
		assertEquals(testImpl, path[0]);
		assertEquals(child, path[1]);
		assertEquals(grandchild, path[2]);

		path = leaf.getPath();
		assertEquals(5, path.length);
		assertEquals(testImpl, path[0]);
		assertEquals(child, path[1]);
		assertEquals(grandchild, path[2]);
		assertEquals(greatgrandchild, path[3]);
		assertEquals(leaf, path[4]);
}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeNodeImpl#getPathList()}.
	 */
	@Test
	public final void testGetPathList() {
		PropertiedTreeRootNode<ExampleObject> child = createNode();
		testImpl.addChild(child);
		
		PropertiedTreeRootNode<ExampleObject> grandchild = createNode();
		child.addChild(grandchild);
		
		PropertiedTreeRootNode<ExampleObject> greatgrandchild = createNode();
		grandchild.addChild(greatgrandchild);
		
		PropertiedTreeRootNode<ExampleObject> leaf = createNode();
		greatgrandchild.addChild(leaf);

		List<PropertiedTreeNode<ExampleObject>> pathList = testImpl.getPathList();
		assertEquals(1, pathList.size());
		assertEquals(testImpl, pathList.get(0));
		
		pathList = child.getPathList();
		assertEquals(2, pathList.size());
		assertEquals(testImpl, pathList.get(0));
		assertEquals(child, pathList.get(1));

		pathList = grandchild.getPathList();
		assertEquals(3, pathList.size());
		assertEquals(testImpl, pathList.get(0));
		assertEquals(child, pathList.get(1));
		assertEquals(grandchild, pathList.get(2));

		pathList = leaf.getPathList();
		assertEquals(5, pathList.size());
		assertEquals(testImpl, pathList.get(0));
		assertEquals(child, pathList.get(1));
		assertEquals(grandchild, pathList.get(2));
		assertEquals(greatgrandchild, pathList.get(3));
		assertEquals(leaf, pathList.get(4));
	}

	/**
	 * Test method for {@link net.sf.taverna.t2.drizzle.util.impl.PropertiedTreeNodeImpl#removeAllChildren()}.
	 */
	@Test
	public final void testRemoveAllChildren() {
		
		testImpl.removeAllChildren();
		assertEquals(0, testImpl.getChildCount());
		
		PropertiedTreeNode<ExampleObject> child1 = createNode();
		PropertiedTreeNode<ExampleObject> child2 = createNode();
		PropertiedTreeNode<ExampleObject> child3 = createNode();
		testImpl.addChild(child1);
		testImpl.addChild(child2);
		testImpl.addChild(child3);
		
		assertEquals(3, testImpl.getChildCount());
		testImpl.removeAllChildren();
		assertEquals(0, testImpl.getChildCount());
		assertNull(child1.getParent());
	}

}
