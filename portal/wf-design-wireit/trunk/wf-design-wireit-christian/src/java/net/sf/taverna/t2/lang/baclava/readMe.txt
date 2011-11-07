This is code taken from net.sf.taverna.t2.lang.results

I did a copy to remove
ResultsUtils.java and BaclavaDocumentHandler as they have external dependecies and DataThingFactor does not depend on them.

There has been np code change here!

Ideal would be to split the net.sf.taverna.t2.lang.results module so it can be included rather than having copied code.