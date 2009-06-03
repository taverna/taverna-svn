// Copyright (C) 2008 The University of Manchester, University of Southampton and Cardiff University
package net.sf.taverna.t2.ui.perspectives.myexperiment.model;

import java.util.Comparator;

/*
 * @author Jiten Bhagat, modified by Sergejs Aleksejevs
 */
public class Tag extends Resource {
	
	private String tagName;
	
	private int count;
  
	
	public Tag()
	{
	  super();
	  this.setItemType(Resource.TAG);
	}
	
	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	
	
	public static class ReversePopularityComparator implements Comparator<Tag>
	{
	  public ReversePopularityComparator()
	  {
	    super();
	  }
	  
	  public int compare(Tag t1, Tag t2)
	  {
	    if (t1.getCount() == t2.getCount()) {
	      // in case of the same popularity, compare by tag name
	      return (t1.getTagName().compareTo(t2.getTagName()));
	    }
	    else {
	      // popularity isn't the same; arrange by popularity (more popular first)
	      return (t2.getCount() - t1.getCount());
	    }
	  }
	}
	
	
	public static class AlphanumericComparator implements Comparator<Tag>
  {
    public AlphanumericComparator()
    {
      super();
    }
    
    public int compare(Tag t1, Tag t2)
    {
      return (t1.getTagName().compareTo(t2.getTagName()));
    }
  }
	
	
  /**
   * A helper method to return a set of API elements that are
   * needed to satisfy request of a particular type - e.g. creating
   * a listing of resources or populating full preview, etc.
   * 
   * @param iRequestType A constant value from Resource class.
   * @return Comma-separated string containing values of required API elements.
   */
  public static String getRequiredAPIElements(int iRequestType)
  {
    String strElements = "";
    
    // cases higher up in the list are supersets of those that come below -
    // hence no "break" statements are required, because 'falling through' the
    // switch statement is the desired behaviour in this case
    switch (iRequestType) {
      case Resource.REQUEST_DEFAULT_FROM_API:
        strElements += ""; // no change needed - defaults will be used
    }
    
    return (strElements);
  }
	
}
