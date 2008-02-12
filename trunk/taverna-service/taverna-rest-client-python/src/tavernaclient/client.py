# -*- coding: utf-8 -*-

"""
Taverna Remote Execution service client
=======================================

Authors: Stian Soiland, David Withers
Copyright: 2008 University of Manchester, UK
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

"""

import urllib2
import time

from elementtree import ElementTree

from tavernaclient import baclava
from ns import Namespace


class _Namespaces:
    """Name spaces used by various XML documents.
    """
    xscufl = Namespace("http://org.embl.ebi.escience/xscufl/0.1alpha")
    baclava = Namespace("http://org.embl.ebi.escience/baclava/0.1alpha")
    service = Namespace("http://taverna.sf.net/service")
    xlink = Namespace("http://www.w3.org/1999/xlink")


class _MimeTypes:
    """Mime types used by the rest protocol.
    
    See net.sf.taverna.service.interfaces.TavernaConstants.java
    """
    # For most of the rest documents
    rest = "application/vnd.taverna.rest+xml"
    # For Taverna workflows
    scufl = "application/vnd.taverna.scufl+xml"
    # For Taverna's Baclava data documents
    baclava = "application/vnd.taverna.baclava+xml"
    #�For Taverna's internal progress reports
    report = "application/vnd.taverna.report+xml"
    # For Taverna's console
    console = "text/plain"


DEFAULT_TIMEOUT = 5 * 60 # in seconds
DEFAULT_REFRESH = 0.5 # in seconds

class TavernaServiceError(Exception):
    """Base class for Taverna service errors.
    """

class NotCompleteError(TavernaServiceError):
    """Job did not complete. 
    Thrown by executeSync()
    """ 
    def __init__(self, jobURL, status):
        msg = "Job " + jobURL + " not complete, status: " + status
        super(NotCompleteError, self).__init__(msg)

class CouldNotCreateError(TavernaServiceError):
    """Could not create resource. 
    """
    def __init__(self, url):
        msg = "Expected 201 Created when uploading " + url
        super(CouldNotCreateError, self).__init__(msg)

class Status:
    """Status messages that can be returned from TavernaService.getJobStatus().
    
    If isFinished(status) is true, this means the job is finished, 
    either successfully (COMPLETE), unsuccessfully (CANCELLED, FAILED), or 
    that the job is no longer in the database (DESTROYED).
    
    When a job has just been created it will be in status NEW, after that 
    it will immediately be on a queue and in the state QUEUED. Once the 
    job has been picked up by a worker it will be in INITIALISING, this 
    state might include the startup time of the worker and while downloading 
    the workflow and input data to the worker. The state PAUSED is not 
    currently used. The FAILING state can occur if the workflow engine
    crashed, after clean-up or if the workflow itself failed, the state 
    will be FAILED.
    
    The job might at any time be set to the state CANCELLING by the user, 
    which will stop execution of the workflow, leading to the state 
    CANCELLED.
    
    If the workflow execution completed the state will be set to COMPLETE, 
    after which the workflow result data should be available by using
    _getJobOutputsDoc().
    
    If data about the job has been lost (probably because it's too old 
    or has been deleted by the user), the state will be DESTROYED.
    """
    NEW = "NEW"
    QUEUED = "QUEUED"
    INITIALISING = "INITIALISING"
    PAUSED = "PAUSED"
    FAILING = "FAILING"
    CANCELLING = "CANCELLING"
    CANCELLED = "CANCELLED"
    COMPLETE = "COMPLETE"
    FAILED = "FAILED"
    DESTROYED = "DESTROYED"
    _FINISHED = (COMPLETE, CANCELLED, DESTROYED, FAILED)
    _ALL = (NEW, QUEUED, INITIALISING, PAUSED, FAILING, 
           CANCELLING, CANCELLED, COMPLETE, FAILED, DESTROYED)
    
    @classmethod
    def isFinished(cls, status):
        """Return True if the status is a finished status.
        This would normally include COMPLETE, CANCELLED, DESTROYED and FAILED.
        """
        return status in cls._FINISHED
    
    @classmethod
    def isValidStatus(cls, status):
        """Check if a string is a valid status."""
        return status in cls._ALL
    
class TavernaService(object):
    """Client library for accessing a Taverna Remote execution service.
    
    Since the service is a rest interface, this library reflects that to 
    a certain degree and many of the methods return URLs to be used by 
    other methods.
    
    The main methods of interest are - in order of a normal execution:
    
        executeSync() -- Given a scufl document or the URL for a previously 
            uploaded workflow, and data as a dictionary or URL for previously
            uploaded data, submit job for execution, wait for completion 
            (or a timeout) and retrieve results. This is a blocking 
            convenience method that can be used instead of the methods below.
    
        uploadWorkflow() -- Given a scufl document as a string, upload the
            workflow to the server for later execution. Return the URL for the
            created workflow resource that can be used with submitJob()
            
        uploadData()-- Given a dictionary of input values to a
            workflow run, upload the data to the user's collection.
            Return the URL for the created data resource that can be used with 
            submitJob()
        
        submitJob() -- Given the URL for a workflow resource and optionally
            the URL for a input data resource, submit the a to the server 
            to be executed. Return the URL to the created job resource.
        
        getJobStatus() -- Get the status of the job. Return one of the values from
            Status.
            
        isFinished() -- Return True if the job is in a finished state. Note 
            that this also includes failed states.

        weitForJob() -- Wait until job has finished execution, or a maximum
            timeout is exceeded.
                       
        getJobOutputs() -- Get the outputs produced by job.  Return a  
            dictionary which values are strings, lists of strings, 
            or deeper lists.
            
    Most or all of these methods might in addition to stated exceptions also raise
    urllib2.HTTPError if anything goes wrong in communicating with the service.
    """
    
    """Default realm for authentication"""
    realm = 'Taverna service'
        
    def __init__(self, url, username, password):
        """Construct a Taverna remote execution service client accessing the service
        at the given base URL.  
        
        Note that this constructor will not attempt to verify the URL or the 
        credentials. To verify, call _getUserURL() which requires authentication.
        
        url -- The base URL for the service, normally ending in /v1/, for example:
            "http://myserver.com:8080/tavernaService/v1/"
        
        username -- The username of a user that has been previously created or 
            registered in the web interface of the service.
            
        password -- The password of the user. Note that the password will be sent
            over the wire using unencrypted HTTP Basic Auth, unless the URL starts
            with "https".
        """
        self.url = url
        self.username = username
        self.password = password
        self._addAuthHandler()
        
    def _addAuthHandler(self):
        """Add a globally bound HTTP Basic Auth handler bound for the 
        URL and _REALM of this service. 
        
        Note that this might affect other users of urllib2 accessing the 
        same URLs, including other instances of the TavernaService.
        """
        auth_handler = urllib2.HTTPBasicAuthHandler()
        auth_handler.add_password(self.realm, self.url, self.username, self.password)
        opener = urllib2.build_opener(auth_handler)
        urllib2.install_opener(opener)    

    def _createDataDoc(self, dictionary):
        """Create a Baclava data document to be uploaded with _uploadDataDoc(). 
        
        Return the Baclava data document as XML. This data document can be parsed using
        _parseDataDoc()
        
        dictionary -- A dictionary where the keys are strings, matching the names of input
            ports of the workflow to run. The values can be strings, lists of strings, or deeper
            lists. 
        """
        dataElem = baclava.make_input_elem(dictionary)
        return ElementTree.tostring(dataElem) 
    
    def _createJobDoc(self, workflowURL, inputsURL=None):
        """Create a job document for submission with submitJob().

        Return the job document as XML.
        
        workflowURL -- The URL of a workflow previously uploaded using
            uploadWorkflow()
        
        inputsURL -- The (optional) URL of a input document previously
            uploaded using _uploadDataDoc()
        
        """
        jobElem = ElementTree.Element(_Namespaces.service.job)
        workflowElem = ElementTree.SubElement(jobElem, _Namespaces.service.workflow)
        workflowElem.attrib[_Namespaces.xlink.href] = workflowURL
        
        if inputsURL is not None:
            inputsElem = ElementTree.SubElement(jobElem, _Namespaces.service.inputs)
            inputsElem.attrib[_Namespaces.xlink.href] = inputsURL
        
        return ElementTree.tostring(jobElem)
    
    def _getCapabilities(self):
        """Get the capabilities document as an ElementTree object. 
        
        This document contains the links to the main collections of the service.
        """
        request = urllib2.Request(self.url)
        request.add_header("Accept", _MimeTypes.rest)
        result = urllib2.urlopen(request)
        return ElementTree.parse(result).getroot()
        
    def _getJobOutputsDoc(self, jobURL):
        """Get the output document for a job.
        
        Return the Baclava output document as an ElementTree object, or None
        if the job didn't have an output document (yet). This document can be
        parsed using _parseDataDoc().
        
        jobURL -- The URL to a job resource previously created using
            submitJob().
        """
        outputsURL = self._getJobOutputsURL(jobURL)
        if outputsURL is None:
            return None
        return self._retrieveXMLDoc(outputsURL, mimeType=_MimeTypes.baclava)    
    
    def _getJobOutputsURL(self, jobURL):
        """Get the URL to the output document for a job.
        
        It generally only makes sense to call this function if
        getJobStatus() == Status.COMPLETED, but no check is enforced here.
        
        Return the URL to a data document produced by the job, or None if the
        job has not (yet) produced any output.
        
        jobURL -- The URL to a job resource previously created using
            submitJob().
        """
        jobDocument = self._retrieveXMLDoc(jobURL)
        outputsElem = jobDocument.find(_Namespaces.service.outputs)
        if outputsElem is None:
            return None
        return outputsElem.attrib[_Namespaces.xlink.href]    
    
    def _retrieveXMLDoc(self, url, mimeType=_MimeTypes.rest):
        """Retrieve an XML document from the given URL.
        
        Return the ElementTree root element of the retrieved document.
        
        url -- The URL to a resource retrievable as an XML document
        
        mimeType -- The mime-type to request using the Accept header, by default
            _MimeTypes.rest
        """
        request = urllib2.Request(url)
        request.add_header("Accept", mimeType)
        result = urllib2.urlopen(request)
        return ElementTree.parse(result).getroot()  
        
    def _getUser(self):
        """Get the user document as an ElementTree object. 
        
        This document contains the links to the user owned collections, 
        such as where to upload workflows and jobs.
        """
        userURL = self._getUserURL()
        request = urllib2.Request(userURL)
        request.add_header("Accept", _MimeTypes.rest)
        result = urllib2.urlopen(request)
        return ElementTree.parse(result).getroot()    
    
    def _getUserCollectionURL(self, collection):
        """Get the URL to a user-owned collection. 
        
        collectionType -- The collection, either "workflows" or "datas"
        """
        user = self._getUser()
        collections = user.find(_Namespaces.service.get(collection))
        return collections.attrib[_Namespaces.xlink.href]
    
    def _getUserURL(self):
        """Get the URL for the current user's home on the server.
        """
        capabilities = self._getCapabilities()
        currentUser = capabilities.find(_Namespaces.service.currentUser)
        currentUserURL = currentUser.attrib[_Namespaces.xlink.href]

        request = urllib2.Request(currentUserURL)
        request.add_header("Accept", _MimeTypes.rest)
        result = urllib2.urlopen(request)
        return result.url
    
    def _parseDataDoc(self, xml=None, elem=None):
        """Parse a Baclava data document as returned from _getJobOutputsDoc().
        
        Return a dictionary where the keys are strings, matching the names of 
            ports of the workflow. The values can be strings, lists of strings, or deeper
            lists. 
            
        xml -- A Baclava data document as XML. This data document can be created
            using _createDataDoc()
            
        elem -- A Baclava data document as an ElementTree element.
        """
        results = {}
        for (key, value) in baclava.parse(xml, elem).iteritems():
            results[key] = value.data
        return results

    def _submitJobDoc(self, jobDocument):
        """Submit a job to be queued for execution on the server.
        
        Return the URL to the job resource.
        
        jobDocument -- A job document created with _createJobDoc() specifying
            the workflow to run with which inputs.
            
        Raises:
            CouldNotCreateError -- If the service returned 200 OK instead of
                creating the resource
        """
        jobsURL = self._getUserCollectionURL("jobs")
        return self._uploadToCollection(jobsURL, jobDocument, 
                                           _MimeTypes.rest)    
    
    def _uploadDataDoc(self, baclavaDoc):
        """Upload a Baclava data document to the current user's collection.
        
        Return the URL of the created data resource.
        
        baclavaDoc -- A Baclava data document created with _createDataDoc()
        
        Raises:
            CouldNotCreateError -- If the service returned 200 OK instead of
                creating the resource
        """
        datasURL = self._getUserCollectionURL("datas")
        return self._uploadToCollection(datasURL, baclavaDoc, 
                               _MimeTypes.baclava)
        
    def _uploadToCollection(self, url, data, contentType):
        """Upload data by POST-ing to given URL. 
        
        Return the URL of the created resource if the request succeeded with
        201 Created.
        
        Raises:
            CouldNotCreateError -- If the service returned 200 OK instead of
                creating the resource
            urllib2.HTTPError -- If any other HTTP result code (including errors) 
                was returned
        
        url -- The URL of the collection of where to POST, 
            normally retrieved using _getUserCollectionURL().
        
        data -- The data to upload as a string
        
        contentType -- The MIME type of the data to upload. Typically the value
            of one of the _MimeTypes constants. For data uploaded to the "datas" user 
            collection this would be _MimeTypes.baclava, and for workflow to the "
            workflows" collection, _MimeTypes.scufl. Any other XML documents from 
            the _Namespaces.service namespace has the mime type _MimeTypes.rest
        """
        request = urllib2.Request(url, data)
        request.add_header("Accept", _MimeTypes.rest)
        request.add_header("Content-Type", contentType)
        try:
            urllib2.urlopen(request)
            raise CouldNotCreateError(url)
        except urllib2.HTTPError, error:
            # Control through exceptions!
            if error.code != 201: # Was not 201 Created
                raise error
            return error.headers["Location"]
    


    def executeSync(self, workflowXML=None, workflowURL=None, inputs=None, 
                    timeOut=DEFAULT_TIMEOUT, refresh=DEFAULT_REFRESH):
        """Execute a workflow and wait until it's finished. 
        
        This will block until the workflow has been executed by the server, and
        return the result of the workflow run.
        
        Return the parsed output document as a dictionary where the keys are 
        strings, matching the names of output ports of the workflow. The 
        values can be strings, lists of strings, or deeper lists. If the workflow
        did not produce any output, None might be returned instead.
        
        workflowXML -- The workflow as a Taverna scufl XML string. This *or* the 
            workflowURL parameter is required.
        
        workflowURL -- The URL to a workflow previously uploaded using 
            uploadWorkflow(). This *or* the workflowXML parameter is required.
            
        inputs -- The (optional) inputs to the workflow, either as a Baclava 
            XML document (string), or as a dictionary where the keys are 
            strings, matching the names of input ports of the workflow. The 
            values can be strings, lists of strings, or deeper lists. 
        
        timeOut -- The maximum number of seconds (as a float) to wait for job.
            The default value is DEFAULT_TIMEOUT.
        
        refresh -- In seconds (as a float), how often to check the job's 
            status while waiting. The default value is DEFAULT_REFRESH.
        
        Raises:
            NotCompleteError -- If the job did not complete, for instance because
                the timeout was reached before completion.
            
            urllib2.HTTPError -- If any step in submitting or requesting the status and
                result of the job failed.
        """
        if workflowXML is None and workflowURL is None:
            raise TypeError("workflowXML or worklowURL must be given")
        if workflowXML is not None and workflowURL is not None:
            raise TypeError("Only one of workflowXML and workflowURL can be given")
        if workflowXML is not None:
            workflowURL = self.uploadWorkflow(workflowXML)
        
        if inputs and getattr(inputs, "__getitem__", None): # is of duck type dict()
            inputsURL = self.uploadData(inputs)
        else:
            inputsURL = inputs
        
        jobURL = self.submitJob(workflowURL, inputsURL)
        status = self.waitForJob(jobURL, timeOut, refresh)
        
        if status != Status.COMPLETE:
            raise NotCompleteError(jobURL, status) 
        outputs = self.getJobOutputs(jobURL)
        return outputs
    
    def getJobStatus(self, jobURL):
        """Get the status of a previously submitted job.
        
        Return the status as a string, one of the values from Status.
        
        jobURL -- The URL to a job resource previously created using
            submitJob().
        """
        jobDocument = self._retrieveXMLDoc(jobURL)
        status = jobDocument.find(_Namespaces.service.status)
        # TODO: For future checks, use: 
        #statusURL = status.attrib[_Namespaces.xlink.href]
        return status.text
    
    def getJobReport(self, jobURL):
        """Get the job's internal progress report. This might be available
        while the job is running.
        
        Return the internal progress report as an ElementTree object.
        
                jobURL -- The URL to a job resource previously created using
            submitJob().
        """
        jobDocument = self._retrieveXMLDoc(jobURL)
        reportElem = jobDocument.find(_Namespaces.service.report)
        reportURL = reportElem.attrib[_Namespaces.xlink.href]
        # TODO: Cache reportURL per job
        return self._retrieveXMLDoc(reportURL, _MimeTypes.report)
        
    
    def getJobOutputs(self, jobURL):
        """Get the outputs of a job.
        
        Return the job outputs as a dictionary where the keys are strings, 
        matching the names of output ports of the workflow. The values can be 
        strings, lists of strings, or deeper lists. If no outputs exists, 
        None is returned instead.
        
        jobURL -- The URL to a job resource previously created using
            submitJob().
        """
        jobOutputs = self._getJobOutputsDoc(jobURL)
        if jobOutputs is None:
            return None
        return self._parseDataDoc(elem=jobOutputs)
        
    def isFinished(self, jobURL):
        """Check if a job has finished in one way or another. 
        
        Note that the job might have finished unsuccessfully. To check 
        if a job is actually complete, check::
        
            getJobStatus(jobURL) == Status.COMPLETE.
        
        Return True if the job is in a finished state, that is that the
        Status.isFinished(getJobStatus()) is True.
        
        jobURL -- The URL to a job resource previously created using
            submitJob().
        """
        status = self.getJobStatus(jobURL)
        return Status.isFinished(status)

    def submitJob(self, workflowURL, inputsURL=None):
        """Submit a job to be queued for execution on the server.
        
        Return the URL to the created job resource.
        
        workflowURL -- The URL of a workflow previously uploaded using
            uploadWorkflow()
        
        inputsURL -- The (optional) URL of a input resource previously
            uploaded using uploadData()
            
        Raises:
            CouldNotCreateError -- If the service returned 200 OK instead of
                creating the resource    
        """
        jobDocument = self._createJobDoc(workflowURL, inputsURL)
        return self._submitJobDoc(jobDocument)

    def uploadData(self, dictionary):
        """Upload data to be used with submitJob().
        
        Return the URL to the created data resource.
        
        dictionary -- A dictionary where the keys are strings, matching the names of input
            ports of the workflow to run. The values can be strings, lists of strings, or deeper
            lists.
            
        Raises:
            CouldNotCreateError -- If the service returned 200 OK instead of
                creating the resource    
        """
        inputs = self._createDataDoc(dictionary)
        return self._uploadDataDoc(inputs)

                
    def uploadWorkflow(self, workflowXML):
        """Upload a workflow XML document to the current users' collection.
        
        Return the URL of the created workflow resource.
        
        workflowXML -- The Taverna scufl workflow as a string
        
        Raises:
            CouldNotCreateError -- If the service returned 200 OK instead of
                creating the resource
        """
        workflowsURL = self._getUserCollectionURL("workflows")
        return self._uploadToCollection(workflowsURL, workflowXML, 
                               _MimeTypes.scufl)
    
    def waitForJob(self, jobURL, timeOut=DEFAULT_TIMEOUT, refresh=DEFAULT_REFRESH):
        """Wait (blocking) for a job to finish, or until a maximum timeout 
        has been reached.
        
        Return the status of the job, one of the values from Status. If the
        timeout was reached, the status might be one that is not finished, 
        check with Status.isFinished(). The workflow will only have
        successfully completed if the status returned is equal to 
        Status.COMPLETE.
        
        jobURL -- The URL to a job resource previously created using
            submitJob().
        
        timeOut -- The maximum number of seconds (as a float) to wait for job.
            The default value is DEFAULT_TIMEOUT.
        
        refresh -- In seconds (as a float), how often to check the job's 
            status while waiting. The default value is DEFAULT_REFRESH.
        """           
        now = time.time()
        until = now + timeOut
        while until > now and not self.isFinished(jobURL):
            now = time.time() # isFinished() might have taken a while
            time.sleep(max(min(refresh, until-now), 0))
            now = time.time() # after the sleep
        return self.getJobStatus(jobURL)

