Feta Engine is a service discovery component that allows search over XML/RDF based 
service descriptions that conform to the myGrid-BioMoby service schema and that are 
annotated with a domain ontology. The engine supports RDF(S) reasoning
during query evaluation hence enables generalization and specialization of search 
requests. More information on the architecture of Feta Engine and its working
mechanisms can be found here: 
http://twiki.mygrid.org.uk/twiki/bin/view/Mygrid/ServiceDiscovery

Querying of  service descriptions acccessed by the Feta Engine 
is provided through the Feta Engine web-service interface. This 
must be deployed in a web-server.  Operations on the web-service
access  the inner RDF model of service descriptions inside the Feta Engine.


In order for Feta to be ontology aware, the domain ontologies used in the 
generation of semantic descriptions need to be introduced to Feta Engine through its 
configuration file



The process of installing  the  Feta Engine  consists  of  

	- Deciding on a location for the file that will hold the index of services registered to Feta.
	- Modifying Feta Engine's configuration file so that it points out to this file location, and to 
the necessary  ontologies
	- Building the Feta Engine web-service and  deploying  it on Tomcat. 


Please read the build.txt for details on how to build Feta Engine from source