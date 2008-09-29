package org.embl.ebi.escience.baclava;

import junit.framework.TestCase;

/**
 * Test for SyntacticType.
 *
 * @author Matthew Pocock
 */
public class SyntacticTypeTest extends TestCase
{
  public void testValueOfRaw()
  {
    String mimeType = "myType";
    SyntacticType type = SyntacticType.valueOf("'" + mimeType + "'");
    assertTrue("Type is " + mimeType,
               type.getMimeTypeList().contains(mimeType));
    assertTrue("Type is atom", type.isAtomic());
    assertFalse("Type is not a collection", type.isCollection());
  }

  public void testValueOfSet()
  {
    String mimeType = "myType";
    SyntacticType type = SyntacticType.valueOf("s('" + mimeType + "')");
    assertTrue("Type is " + mimeType,
               type.getMimeTypeList().contains(mimeType));
    assertFalse("Type is not atomic", type.isAtomic());
    assertTrue("Type is a collection", type.isCollection());
    assertTrue("Set Collection", type.getCollectionType() == SyntacticType.SET);
    assertTrue("Collection element is atomic",
               type.getElementType().isAtomic());
  }

  public void testValueOfList()
  {
    String mimeType = "myType";
    SyntacticType type = SyntacticType.valueOf("l('" + mimeType + "')");
    assertTrue("Type is " + mimeType,
               type.getMimeTypeList().contains(mimeType));
    assertFalse("Type is not atomic", type.isAtomic());
    assertTrue("Type is a collection", type.isCollection());
    assertTrue("List Collection", type.getCollectionType() == SyntacticType.LIST);
    assertTrue("Collection element is atomic",
               type.getElementType().isAtomic());
  }

  public void testValueOfListList()
  {
    String mimeType = "myType";
    SyntacticType type = SyntacticType.valueOf("l(l('" + mimeType + "'))");
    assertTrue("Type is " + mimeType,
               type.getMimeTypeList().contains(mimeType));
    assertFalse("Type is not atomic", type.isAtomic());
    assertTrue("Type is a collection", type.isCollection());
    assertTrue("List Collection", type.getCollectionType() ==
                                  SyntacticType.LIST);
    assertEquals("Element type is list", SyntacticType.LIST,
                 type.getElementType().getCollectionType());
  }

  // todo: add tests for eroneously formed types

  public void testConstructors()
  {
    SyntacticType atomic = new SyntacticType("element");
    SyntacticType list = new SyntacticType(atomic, SyntacticType.LIST);

    assertEquals("List of elements", atomic, list.getElementType());
  }

  public void testToStringAtomic()
  {
    String mimeType = "myType";
    SyntacticType type = new SyntacticType(mimeType);
    assertEquals("Type name is correct", "'" + mimeType + "'", type.toString());
  }

  public void testToStringList()
  {
    String mimeType = "myType";
    SyntacticType type = new SyntacticType(
            new SyntacticType(mimeType), SyntacticType.LIST);
    assertEquals("List name is correct",
                 "l('" + mimeType + "')", type.toString());
  }

  public void testToStringSet()
  {
    String mimeType = "myType";
    SyntacticType type = new SyntacticType(new SyntacticType(mimeType), SyntacticType.SET);
    assertEquals("Set name is correct",
                 "s('" + mimeType + "')", type.toString());
  }

  public void testStringRoundTrip()
  {
    String atomic = "'myType'";
    SyntacticType type = SyntacticType.valueOf(atomic);
    assertEquals("Atomic types round-trip", atomic, type.toString());
  }

  public void testListRoundTrip()
  {
    String list = "l('myType')";
    SyntacticType type = SyntacticType.valueOf(list);
    assertEquals("List types round-trip", list, type.toString());
  }

  public void testEqualsAtom()
  {
    String atomic = "'myType'";
    SyntacticType type1 = SyntacticType.valueOf(atomic);
    SyntacticType type2 = SyntacticType.valueOf(atomic);
    assertEquals("Atoms are equal", type1, type2);
  }

  public void testEqualsCollection()
  {
    String atomic = "l('myType')";
    SyntacticType type1 = SyntacticType.valueOf(atomic);
    SyntacticType type2 = SyntacticType.valueOf(atomic);
    assertEquals("Lists are equal", type1, type2);
  }
}
