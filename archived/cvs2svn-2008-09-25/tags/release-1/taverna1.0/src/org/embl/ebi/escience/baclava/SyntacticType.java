package org.embl.ebi.escience.baclava;

import java.util.*;

/**
 * A Syntactic type representing what a DataThing is composed from.
 * The recognised collection types are Set, List, Tree and Partial Order. The
 * atomic types are represented by MIME types.
 *
 * @author Matthew Pocock
 */
public class SyntacticType
{
  public static final Collection SET = new Collection("Set", "s");
  public static final Collection LIST = new Collection("List", "l");
  public static final Collection PARTIAL_ORDER = new Collection("Partial Order", "p");

  public static final SyntacticType NULL = new SyntacticType("null") {
    public String toString()
    {
      return "'null'";
    }
  };

  /**
   * Convert a string version of a syntactic type into a SyntacticType instance.
   * This should round-trip with toString().
   *
   * @param val   the string representation
   * @return      a SyntacticType representing val
   * @throws IllegalArgumentException
   */
  public static SyntacticType valueOf(String val)
          throws IllegalArgumentException
  {
    Stack typeStack = new Stack();
    SyntacticType type = null;

    for(int i = 0; i < val.length(); i++) {
      char c = val.charAt(i);

      switch (c) {
        case 's':
          // set
          typeStack.push(SET);
          i++;
          if(val.charAt(i) != '(') {
            throw new IllegalArgumentException(
                    "Unabel to parse " + val + " at index " + i);
          }
          break;

        case 'l':
          // list
          typeStack.push(LIST);
          i++;
          if (val.charAt(i) != '(') {
            throw new IllegalArgumentException("Unabel to parse " + val +
                                               " at index " +
                                               i);
          }
          break;

        case 'p':
          // partial order
          typeStack.push(PARTIAL_ORDER);
          i++;
          if (val.charAt(i) != '(') {
            throw new IllegalArgumentException("Unabel to parse " + val +
                                               " at index " +
                                               i);
          }
          break;

        case '\'':
          // quoted mime type - read untill '
          i++;
          int start = i;
          while(val.charAt(i) != '\'') {
            i++;
          }
          String name = val.substring(start, i);
          if(i == start || "null".equals(name)) {
            type = SyntacticType.NULL;
          } else {
            String[] parts = name.split(",");
            type = new SyntacticType(Arrays.asList(parts));
          }
          break;

        case ')':
          // closing elipse
          Collection coll = (Collection) typeStack.pop();
          type = new SyntacticType(type, coll);
          break;

        default:
          throw new IllegalArgumentException(
                  "Unable to parse due to illegal character: '" + c +
                  "' at " + i + " in " + val);
      }
    }

    if(!typeStack.empty()) {
      throw new IllegalArgumentException("Unballanced brackets in " + val);
    }

    return type;
  }

  /**
   * Create a new syntactic type given an example object. If the object is a
   * collection, then a collection type will be created. If it is empty, then
   * the element type will be null. Otherwise, the element type will be guessed
   * using the first element of the collection.
   *
   * @param example an example Object
   * @throws IllegalArgumentException if the example can't be processed
   */
  public static SyntacticType introspect(Object example)
  {
    if (example instanceof java.util.Collection) {
      // work out the collection type
      SyntacticType elType;
      Collection colType;
      if (example instanceof Set) {
        colType = SET;
      } else if (example instanceof List) {
        colType = LIST;
      } else {
        colType = PARTIAL_ORDER;
      }

      // and the element type
      Iterator i = ((java.util.Collection) example).iterator();
      if (!i.hasNext()) {
        elType = SyntacticType.NULL;
      } else {
        elType = introspect(i.next());
      }

      return new SyntacticType(elType, colType);
    } else {
      return new SyntacticType(MimeTypeGuesser.DEFAULT.guessMimeType(example));
    }
  }

  private final SyntacticType elementType;
  private final Collection collectionType;
  private final List mimeTypeList; // List<String>

  /**
   * Create a new syntactic type which is a collection.
   *
   * @param elementType     the type of each element in the collection
   * @param collectionType  the type of the collection (List, Set etc.)
   * @throws NullPointerException if either argument is null
   */
  public SyntacticType(SyntacticType elementType,
                 Collection collectionType)
  {
    if(elementType == null) {
      throw new NullPointerException("Element type can't be null");
    }
    if (collectionType == null) {
      throw new NullPointerException("Collection type can't be null");
    }

    this.elementType = elementType;
    this.collectionType = collectionType;
    this.mimeTypeList = null;
  }

  /**
   * Create a new synctactic type from a list of mimetypes. This will be atomic.
   *
   * @param mimeTypes  a List of Strings giving the mimetypes to use
   * @throws NullPointerException if mimeTypes is null
   */
  public SyntacticType(List mimeTypes)
  {
    if(mimeTypes == null) {
      throw new NullPointerException("Mimetypes list must not be null");
    }

    this.elementType = null;
    this.collectionType = null;
    this.mimeTypeList = new ArrayList(mimeTypes);
  }

  /**
   * Create a new syntacitc type from a mime type. This will be atomic.
   *
   * @param mimeType  the mimetype to use
   * @throws NullPointerException  if mimeType is null
   */
  public SyntacticType(String mimeType)
  {
    if(mimeType == null) {
      throw new NullPointerException("Mimetype must not be null");
    }

    this.elementType = null;
    this.collectionType = null;
    this.mimeTypeList = Arrays.asList(new String[] { mimeType });
  }

  public boolean isCollection()
  {
    return collectionType != null;
  }

  public boolean isAtomic()
  {
    return mimeTypeList != null;
  }

  public SyntacticType getElementType()
  {
    return elementType;
  }

  public Collection getCollectionType()
  {
    return collectionType;
  }

  /**
   * Returns the element type this SyntacticType is over. If this is a
   * collection then it will get the element type of the members of the
   * collection. If this is an atomic type it will return the type of the
   * atom directly.
   *
   * @return the element type
   */
  public List getMimeTypeList()
  {
    if(isAtomic()) {
      return mimeTypeList;
    } else {
      return getElementType().getMimeTypeList();
    }
  }

  public static SyntacticType collectionOf(SyntacticType type, Collection collection)
  {
    return new SyntacticType(type, collection);
  }

  public String toString()
  {
    if(isCollection()) {
      return collectionType.getSymbol() + "(" + elementType.toString() + ")";
    } else if(isAtomic()) {
      StringBuffer sbuf = new StringBuffer();
      sbuf.append("'");
      for(Iterator i = mimeTypeList.iterator(); i.hasNext(); ) {
        sbuf.append(i.next());
      }
      sbuf.append("'");
      return sbuf.toString();
    } else {
      throw new AssertionError("Must be a collection or atomic");
    }
  }

  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SyntacticType)) {
      return false;
    }

    final SyntacticType syntacticType = (SyntacticType) o;

    if (collectionType != null
        ? !collectionType.equals(syntacticType.collectionType)
        : syntacticType.collectionType != null) {
      return false;
    }
    if (elementType != null
        ? !elementType.equals(syntacticType.elementType)
        : syntacticType.elementType != null) {
      return false;
    }
    if (mimeTypeList != null
        ? !mimeTypeList.equals(syntacticType.mimeTypeList)
        : syntacticType.mimeTypeList != null) {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    int result;
    result = (elementType != null ? elementType.hashCode() : 0);
    result = 29 * result +
             (collectionType != null ? collectionType.hashCode() : 0);
    result = 29 * result + (mimeTypeList != null ? mimeTypeList.hashCode() : 0);
    return result;
  }

  /**
   * Type-safe Enum for collection types.
   *
   * @author Matthew Pocock
   */
  public static class Collection
  {
    private final String name;
    private final String symbol;

    public Collection(String name, String symbol)
    {
      this.name = name;
      this.symbol = symbol;
    }

    public String getName()
    {
      return name;
    }

    public String getSymbol()
    {
      return symbol;
    }

    public String toString()
    {
      return name;
    }
  }
}
