import urllib2
import time
from elementtree import ElementTree
import baclava

class TavernaServiceException(Exception):
    pass


class STATUS:
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
    FINISHED = (COMPLETE, CANCELLED, DESTROYED, FAILED)
    
class TavernaService:
    def __init__(self, url, user, password):
        self.realm = 'Taverna service'
        self.url = url
        self.user = user
        self.password = password
        self.addAuthHandler()
        
    def addAuthHandler(self):
        auth_handler = urllib2.HTTPBasicAuthHandler()
        auth_handler.add_password(self.realm, self.url, self.user, self.password)
        opener = urllib2.build_opener(auth_handler)
        urllib2.install_opener(opener)    
    
    def getCapabilities(self):
        request = urllib2.Request(self.url)
        request.add_header("Accept", "application/vnd.taverna.rest+xml")
        result = urllib2.urlopen(request)
        return ElementTree.parse(result).getroot()
        
    def getUserURL(self):
        capabilities = self.getCapabilities()
        currentUser = capabilities.find("{http://taverna.sf.net/service}currentUser")
        currentUserURL = currentUser.attrib["{http://www.w3.org/1999/xlink}href"]

        request = urllib2.Request(currentUserURL)
        request.add_header("Accept", "application/vnd.taverna.rest+xml")
        result = urllib2.urlopen(request)
        return result.url
     
    def getUser(self):
        userURL = self.getUserURL()
        request = urllib2.Request(userURL)
        request.add_header("Accept", "application/vnd.taverna.rest+xml")
        result = urllib2.urlopen(request)
        return ElementTree.parse(result).getroot()    
        
    def getUserCollectionURL(self, collection):
        user = self.getUser()
        collections = user.find("{http://taverna.sf.net/service}" + collection)
        return collections.attrib["{http://www.w3.org/1999/xlink}href"]
        
    def uploadToUserCollection(self, url, data, contentType):
        request = urllib2.Request(url, data)
        request.add_header("Accept", "application/vnd.taverna.rest+xml")
        request.add_header("Content-Type", contentType)
        try:
            result = urllib2.urlopen(request)
            # Expected 201 Created
            raise TavernaServiceException("Expected 201 Created when uploading workflow")
        except urllib2.HTTPError, error:
            # Control through exceptions!
            if (error.code != 201): # 201 Created
                 raise error
            return error.headers["Location"]
        
    def uploadWorkflow(self, workflowXML):
        workflowsURL = self.getUserCollectionURL("workflows")
        return self.uploadToUserCollection(workflowsURL, workflowXML, 
                               "application/vnd.taverna.scufl+xml")
     
    def createJobDocument(self, workflowURL, dataURL=None):
          jobElem = ElementTree.Element("{http://taverna.sf.net/service}job")
          workflowElem = ElementTree.SubElement(jobElem, "{http://taverna.sf.net/service}workflow")
          workflowElem.attrib["{http://www.w3.org/1999/xlink}href"] = workflowURL
          return ElementTree.tostring(jobElem)

    def submitJob(self, jobDocument):
        jobsURL = self.getUserCollectionURL("jobs")
        return self.uploadToUserCollection(jobsURL, jobDocument, 
                                           "application/vnd.taverna.rest+xml")
    
    def createDataDocument(self, dictionary):
        dataElem = baclava.make_input_elem(dictionary)
        return ElementTree.tostring(dataElem)
     
    def parseDataDocument(self, dataDocument):
        results = {}
        for (key, value) in baclava.parse(dataDocument).iteritems():
            results[key] = value.data
        return results
     
    def uploadData(self, baclavaDoc):
        datasURL = self.getUserCollectionURL("datas")
        return self.uploadToUserCollection(datasURL, baclavaDoc, 
                               "application/vnd.taverna.baclava+xml")

    def getRestDocument(self, url):
        request = urllib2.Request(url)
        request.add_header("Accept", "application/vnd.taverna.rest+xml")
        result = urllib2.urlopen(request)
        return ElementTree.parse(result).getroot()  

    def getJobStatus(self, jobURL):
        jobDocument = self.getRestDocument(jobURL)
        status = jobDocument.find("{http://taverna.sf.net/service}status")
        statusURL = status.attrib["{http://www.w3.org/1999/xlink}href"]
        return status.text
    
    def isFinished(self, jobURL):
        status = self.getJobStatus(jobURL)
        return status in STATUS.FINISHED
    
    def waitForJob(self, jobURL, timeOut=60, refresh=0.5):            
        until = time.time() + timeOut
        while(not self.isFinished(jobURL) and until > time.time()):
            time.sleep(refresh)
        return self.getJobStatus(jobURL)
        
        