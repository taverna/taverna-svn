This is a self-contained distribution of the S-OGSA server code that can be deployed locally for testing. 
It comes with a simple client class SOGSAClient that you can use to test the SBS interface functionality - see below.

** configuration **
the src/main/resources folder contains several config files. Most of the entries are fine, however you will need to change at least the following:

- AnzoServer.properties.derby:  
edit org.openanzo.repository.database.url=jdbc:derby:/Users/Paolo/scratch/anzoDerby;create=true;upgrade=true
it can point to any local folder (it will be created if non-existent)

- SBSclient.properties:  
contains refs to a test RDF file and test SPARQL queries.

The easiest way to build this distribution is through maven -- we use eclipse with the maven plugin here but this is not a requirement.

In case you are not familiar: 
 You can download maven 2 for Windows from their site, then move to the root folder of RESTful-SBS and do a 
 mvn install
 
 


Testing:
  You can use the supplied SOGSAClient to do all the magic for you or you can use something 
like cURL to post, get, put, delete bindings.

For example, to post a new binding use something like:

 curl -d "rdf=........add your own rdf here......&entityKey=abcde" http://localhost:25000/sbs
 
 now, you can also post files using
 
 curl -F "rdf=@filename&entityKey=abcde" http://localhost:25000/sbs  BUT i haven't got it to work yet
 
 when you succesfully post it returns the url for the binding which you can get by doing, for 
 example,
 curl  http://localhost:25000/sbs/abcde
 
 notice that bindings are given a unique identifier based on what you supplied as the "entityKey"
 
 REMEMBER - change the org.openanzo.repository.database.url=jdbc:derby:/Users/Ian/scratch/anzoDerby;create=true;upgrade=true
 to point to the correct database (not mine!!)