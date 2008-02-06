Taverna Remote Execution service client
=======================================

Authors: Stian Soiland, David Withers
Copyright: 2006-2008 University of Manchester, UK
Version: 0.4.0
URL: http://taverna.sourceforge.net/
Contact: taverna-hackers@lists.sourceforge.net
Licence: LGPL 3 (See LICENCE or http://www.gnu.org/licenses/lgpl.html)

This is a Python library for submitting Taverna[1] workflows to the Taverna
Remote Execution service[2]. This requires the Remote Execution service that
has already been installed and configured, and with a client username and
password already registered by the administrator of the service. We 
recommend first testing the service using the Remote Execution service
from the Taverna workbench[3].



Installation
------------

Run "python setup.py install" as an administrator. This should also download 
and install any required 3rd party libraries.


Usage
-----
Here's the simplest usecase. Given a Taverna workflow in "workflow.xml" (that produces
a value at the port "myOutput") and a Taverna Remote Execution service [2] running 
at http://localhost:8080/tavernaservice/v1/ you can try::

	>>> SERVICE = "http://localhost:8080/tavernaservice/v1/"
	>>> workflow = open("workflow.xml").read()
	>>> import tavernaclient.client
	>>> service = tavernaclient.client.TavernaService(SERVICE, "johndoe", "s3cret")
	>>> results = service.executeSync(workflow)
	>>> print results["myOutput"]


Note that the call to executeSync() will block until the workflow has been executed
on the server, and in the current remote execution service this can unfortunately
take about 2 minutes. If you want to do this asynchronously, try::
	
	>>> import time
	>>> workflowURL = service.uploadWorkflow(workflow)
	>>> jobURL = service.submitJob(workflowURL)
	>>> while not service.isFinished(jobURL):
	...      # Do something else
	...      time.sleep(1)
	...
	>>> if service.getJobStatus(jobURL) == tavernaclient.client.Status.COMPLETE:
	...    results = service.getJobOutputs(jobURL)
	...    print results["myOutput"]


You can also supply inputs as a dictionary::

	>>> inputs = {}
	>>> inputs["gene"] = ["MY_GENE", "HIS_GENE"]
	>>> inputs["database"] = "kegg"
	>>> results = service.executeSync(workflow, inputs=inputs)

or::

	>>> inputsURL = service.uploadData(inputs)
	>>> jobURL = service.submitJob(workflowURL, inputsURL)


See the pydoc documentation of tavernaclient.client for more information, try::

	>>> help(tavernaclient.client)


Included are also lower level libraries tavernaclient.baclava for parsing 
and creating Baclava data documents, and tavernaclient.scufl for extracting basic 
information about a workflow, such as which input and output ports it defines.



References
----------

[1] http://taverna.sourceforge.net/
[2] http://www.mygrid.org.uk/usermanual1.7/remote_execution_server.html
[3] http://www.mygrid.org.uk/usermanual1.7/remote_execution.html

