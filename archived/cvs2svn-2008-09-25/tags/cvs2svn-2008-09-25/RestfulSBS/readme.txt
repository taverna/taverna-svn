This is a self-contained distribution of the S-OGSA server code that can be deployed locally for testing. 
Eventually, the server will be deployed on a stable server that the client can point to -- it can at IU or here in the UK

The distr. comes with a simple client class SOGSAClient that you can use to test the SBS interface functionality.

The client includes a std sequence of server calls:

- 1 create a new SB
- 2 add more RDF to it
- 3 query the SB using SPARQL
- 4 retrieve all avaiable SBs in the server
- 5 delete a SB

** configuration **
the src/main/resources folder contains several config files. Most of the entries are fine, however you will need to change at least the following:

- AnzoServer.properties.derby:  
edit org.openanzo.repository.database.url=jdbc:derby:/Users/Paolo/scratch/anzoDerby;create=true;upgrade=true
it can point to any local folder (it will be created if non-existent)

- SBSclient.properties:  
contains refs to files containing our example RDF and test SPARQL queries.

** building **
The easiest way to build this distribution is through maven -- we use eclipse with the maven plugin here but this is not a requirement.

In case you are not familiar: 
 You can download maven 2 for Windows from their site, then move to the root folder of RESTful-SBS and do a 
 mvn install
 
 that should take care of everything.
 
 ** running the server **
 you just execute SOGSAServer as a regular Java app. It will listen on  localhost
 
 ** running the client **
 once the server is up, just run SOGSAClient as a regular Java app
 
 The client 
  
 alternatively, you can use cURL to test the server:
  to post, get, put, delete bindings.

For example, to post a new binding use something like:

 curl -d "rdf=........add your own rdf here......&entityKey=abcde" http://localhost:25000/sbs
 
 now, you can also post files using
 
 curl -F "rdf=@filename&entityKey=abcde" http://localhost:25000/sbs  BUT i haven't got it to work yet
 
 when you succesfully post it returns the url for the binding which you can get by doing, for 
 example,
 curl  http://localhost:25000/sbs/abcde
 
 notice that bindings are given a unique identifier based on what you supplied as the "entityKey"
