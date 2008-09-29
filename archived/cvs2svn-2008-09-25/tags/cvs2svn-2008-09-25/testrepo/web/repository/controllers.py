import logging
from datetime import datetime
import cherrypy

import turbogears
from turbogears import controllers, expose, redirect
from turbogears import identity

from repository import json
from model import Workflow, WorkflowTest, WorkflowTestInput, WorkflowTestOutput, WorkflowTestRun
from taverna.scufl import Scufl
from taverna.runner import WorkflowExecuter
from taverna import baclava
from elementtree import ElementTree as ET

log = logging.getLogger("repository.controllers")

class Root(controllers.RootController):

    @expose(template="repository.templates.workflows")
    def index(self):
        workflows = [(wf.id, wf.name) for wf in
            Workflow.select(orderBy=Workflow.q.name)]  
        return dict(workflows=workflows)

    @expose(template="repository.templates.workflow")
    def workflow(self, id=None):
        if not id:
            raise redirect("/")
        id = int(id)
        # TODO: Check for non-existing
        workflow = Workflow.get(id)
        try:
            scufl = Scufl(workflow.data)
        except Exception, e:
            # FIXME: Log exception
            scufl = None
        return dict(id=id, workflow=workflow, scufl=scufl)

    @expose(template="repository.templates.run")
    @identity.require(identity.has_permission("execute"))
    def run(self, id=None):
        if not id:
            raise redirect("/")
        id = int(id)
        workflow = Workflow.get(id)
        # FIXME: Assumes no input is needed 
        TIMEOUT=10
        log.info("Executing workflow", id)
        executed = WorkflowExecuter(workflow.data, timeout=TIMEOUT)
        return dict(executed=executed, id=id,
                    name=workflow.name)

    @expose(template="repository.templates.run")
    @identity.require(identity.has_permission("execute"))
    def run_test(self, id=None):
        if not id:
            raise redirect("/")
        id = int(id)
        workflow_test = WorkflowTest.get(id)
        # FIXME: Assumes no input is needed 

        inputs = {}
        for input in workflow_test.inputs:
            inputs[input.port] = input.data
        
        TIMEOUT=30
        log.info("Executing workflow test %d", id)
        executed = WorkflowExecuter(workflow_test.workflow.data, inputs, 
                                    timeout=TIMEOUT)
        input_doc = baclava.make_input_elem(inputs)
        # Log the test run
        test_run = WorkflowTestRun(workflow_test=workflow_test,
                        return_code=executed.return_code, 
                        stdout=executed.stdout,
                        stderr=executed.stderr,
                        report=executed.report,
                        workflow=workflow_test.workflow.data,
                        inputdoc=ET.tostring(input_doc),
                        taverna="1.4",
                        java="1.5",
                        outputdoc=executed.outputdoc)
        raise redirect("/test_run/%d" % test_run.id)

    def score(self, run):
        passed = []
        if run.return_code == 0:
            passed.append("return_code") 
        if run.stdout == "":
            passed.append("stdout")
        if run.stderr == "":
            passed.append("stderr")
        if run.report:
            passed.append("report")
        if run.outputdoc:
            passed.append("outputdoc")
        errors = 5-len(passed)    
        return errors, passed

    @expose(template="repository.templates.runs")
    def runs(self):
        runs = []
        for run in WorkflowTestRun.select():
            errors, passed = self.score(run)
            runs.append((errors, passed, run.workflow_test.workflow.name, run))
        runs.sort()
        return dict(runs=runs)      
    
    @expose()
    def test_run_download(self, id, in_out, name, *path):
        id = int(id)
        run = WorkflowTestRun.get(id)
        if in_out == "input":
            doc = baclava.parse(run.inputdoc)
        elif in_out == "output":    
            doc = baclava.parse(run.outputdoc)
        else:
            raise "Unknown doc %s" % in_out
        data = doc[name]    
        result = data.data
        for index in path:
            index = int(index)
            result = result[index]
        mime_type = data.mime_types[-1]    
        # Make web browser attempt to show these things
        if mime_type == "image/*":
            mime_type = "image"
        cherrypy.response.headers["Content-Type"] = mime_type
        return result 
             
    @expose(template="repository.templates.testrun")
    def test_run(self, id):
        id = int(id)
        run = WorkflowTestRun.get(id)
        d = dict()
        d["id"] = id
        d["test"] = run.workflow_test
        errors,passed = self.score(run)
        d["errors"] = errors
        d["passed"] = passed
        d["name"] = run.workflow_test.workflow.name
        d["workflow_id"] = run.workflow_test.workflow.id
        d["test_id"] = run.workflow_test.id
        d["run_date"] = run.run_date or datetime.now()
        d["stdout"] = run.stdout
        d["stderr"] = run.stderr
        d["return_code"] = run.return_code
        d["report"] = run.report
        d["outputdoc"] = run.outputdoc
        d["inputdoc"] = run.inputdoc
        try:
            d["inputs"] = baclava.parse(run.inputdoc)
        except Exception, e:
            log.warn("Could not parse input document for test run %d: %s", id, e)
            d["inputs"] = {}
        try:
            d["outputs"] = baclava.parse(run.outputdoc)
        except Exception, e:
            log.warn("Could not parse output document for test run %d: %s", id, e)
            d["outputs"] = {}
        return d

    @expose(content_type="text/xml", format="xml")
    def download(self, id):
        # TODO: Check for non-existing
        id = int(id)
        workflow = Workflow.get(id)
        #return cherrypy.lib.cptools.serveFile("/etc/passwd",
        #    "text/xml", "attachment", workflow.name + ".xml")
        return workflow.data


    @expose(template="repository.templates.add_workflow")
    @identity.require(identity.has_permission("add"))
    def add_workflow(self):
        return dict(name="", description="")

    @expose(template="repository.templates.edit_workflow")
    @identity.require(identity.has_permission("edit"))
    def edit_workflow(self, id):
        id = int(id)
        workflow = Workflow.get(id)
        return dict(id=id, name=workflow.name,
                    description=workflow.description)

    @expose(template="repository.templates.edit_test")
    @identity.require(identity.has_permission("add_test"))
    def add_test(self, id):
        """Add a test to the workflow id given"""
        id = int(id)
        workflow = Workflow.get(id)
        scufl = Scufl(workflow.data)
        match_types = ["exact", "ignore", "regex"]
    
        return dict(id=None, workflow_id=id, name=workflow.name,
                    description=workflow.description, scufl=scufl,
                    workflow_test=None, sources={},
                    match_types = match_types,
                    out_types = {},
                    sinks={})

    @expose(template="repository.templates.edit_test")
    #@identity.require(identity.has_permission("edit_test"))
    def edit_test(self, id):
        id = int(id)
        workflow_test = WorkflowTest.get(id)
        workflow = workflow_test.workflow
        scufl = Scufl(workflow.data)
        sources = dict((s.port, s.data) for s in workflow_test.inputs)
        sinks = dict((s.port, s.data) for s in workflow_test.outputs)
        out_types = dict((s.port, s.type) for s in workflow_test.outputs)
        match_types = ["exact", "ignore", "regex"]
        return dict(id=id, workflow_id=workflow.id, name=workflow.name,
                    description=workflow.description, scufl=scufl,
                    workflow_test=workflow_test,
                    match_types=match_types,
                    out_types=out_types,
                    sources=sources, sinks=sinks)

    @expose(template="repository.templates.test")
    def test(self, id):
        id = int(id)
        test = WorkflowTest.get(id)
        return dict(id=id, test=test, workflow=test.workflow)
    
    
    @expose()
    @identity.require(identity.has_permission("add_test"))
    def new_test(self, workflow_id, submit, test_id=None, name="", **ports):
        if test_id:
            test = WorkflowTest.get(int(test_id))
        else:
            test = WorkflowTest(workflowID=int(workflow_id), name=name)
        inputs = {}
        outputs = {}
        types = {}
        
        for (port_name, port_data) in ports.items():
            if port_name.startswith("input_"):
                # Strip up our input_ (could also be part of real port_name)
                port_name = port_name[len("input_"):]
                inputs[port_name] = port_data 
            elif port_name.startswith("output_"):
                # Strip up our input_ (could also be part of real port_name)
                port_name = port_name[len("output_"):] 
                outputs[port_name] = port_data
            elif port_name.startswith("outtype_"):
                port_name = port_name[len("outtype_"):]
                types[port_name] = port_data
        
        turbogears.database.commit_all()
        for input in test.inputs:
            input.destroySelf()
        turbogears.database.commit_all()    
        for output in test.outputs:
            output.destroySelf()
        turbogears.database.commit_all()        

        for name,data in inputs.items():
            WorkflowTestInput(workflow_test=test, port=name,
                              data=data)
            
        for name,data in outputs.items():
            type = types.get(name)
            if not type: 
                if data:
                    type = "exact"
                else:
                    type = "ignore"     
            WorkflowTestOutput(workflow_test=test, port=name,
                                type=type, data=data)        
        turbogears.database.commit_all()     
        raise redirect("/test/%d" % test.id)
        

    @expose()
    @identity.require(identity.has_permission("add"))
    def new_workflow(self, upload_file, name="", description="", submit=None):
        """Save the uploaded workflow to disk"""
        data = upload_file.file.read()
        if not name:
            name = upload_file.filename.replace(".xml", "")
        workflow = Workflow(name=name, description=description,
                            data=data,
                            created_by=identity.current.user.id)
        log.info("Saved new workflow %d", workflow.id)
        raise redirect("/workflow/%d" % workflow.id)

    @expose()
    @identity.require(identity.has_permission("save"))
    def save_workflow(self, id, name="", description="", submit=None):
        workflow = Workflow.get(int(id))
        workflow.name = name
        workflow.description = description
        workflow.updated = datetime.now()
        log.info("Saved workflow %d", workflow.id)
        raise redirect("/workflow/%d" % workflow.id)

    @expose()
    @identity.require(identity.has_permission("delete"))
    def del_test(self, id):
        id = int(id)
        #workflow = Workflow.get(int(id))
        test = WorkflowTest.get(id)
        workflow = test.workflow
        turbogears.database.commit_all()
        for input in test.inputs:
            input.destroySelf()
        turbogears.database.commit_all()    
        for output in test.outputs:
            output.destroySelf()
        turbogears.database.commit_all()        
        test.destroySelf()
        turbogears.database.commit_all()        
        log.info("Deleted test %d", id)
        raise redirect("/workflow/%d" % workflow.id)

    @expose()
    @identity.require(identity.has_permission("delete"))
    def del_workflow(self, id):
        id = int(id)
        workflow = Workflow.get(id)
        workflow.destroySelf()
        log.info("Deleted workflow %d", id)
        raise redirect("/")

    @expose(template="repository.templates.register")
    def register(self, forward_url=None):
        pass

    @expose(template="repository.templates.login")
    def login(self, forward_url=None, previous_url=None, *args, **kw):

        if not identity.current.anonymous and identity.was_login_attempted():
            raise redirect(forward_url)

        forward_url=None
        previous_url= cherrypy.request.path

        if identity.was_login_attempted():
            msg=_("The credentials you supplied were not correct or "
                   "did not grant access to this resource.")
        elif identity.get_identity_errors():
            msg=_("You must provide your credentials before accessing "
                   "this resource.")
        else:
            msg=_("Please log in.")
            forward_url= cherrypy.request.headers.get("Referer", "/")
        cherrypy.response.status=403
        return dict(message=msg, previous_url=previous_url, logging_in=True,
                    original_parameters=cherrypy.request.params,
                    forward_url=forward_url)

    @expose()
    def logout(self):
        identity.current.logout()
        raise redirect("/")
    
    

