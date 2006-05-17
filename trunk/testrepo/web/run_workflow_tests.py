#!/usr/bin/env python
import pkg_resources
pkg_resources.require("TurboGears")

import turbogears
import cherrypy
cherrypy.lowercase_api = True

import os
import sys
try:
    from cStringIO import StringIO
except ImportError:    
    from StringIO import StringIO

import traceback

import logging as log

from repository.taverna.runner import WorkflowExecuter
from repository.taverna.baclava import make_input_elem
from elementtree import ElementTree as ET
import threading

# first look on the command line for a desired config file,
# if it's not on the command line, then
# look for setup.py in this directory. If it's not there, this script is
# probably installed
#if len(sys.argv) > 1:
#    turbogears.update_config(configfile=sys.argv[1], 
#        modulename="repository.config")
base = os.path.dirname(__file__)
if os.path.exists(os.path.join(base, "setup.py")):
    turbogears.update_config(configfile=os.path.join(base, "dev.cfg"),
        modulename="repository.config")
else:
    turbogears.update_config(configfile=os.path.join(base, "prod.cfg"),
        modulename="repository.config")

from repository.model import WorkflowTestRun, WorkflowTest


def run_test(test):
    inputs = {}
    for input in test.inputs:
        inputs[input.port] = input.data
    TIMEOUT=15
    input_doc = make_input_elem(inputs)
    log.info("Executing workflow test %s" % test.id)
    try:
        executed = WorkflowExecuter(test.workflow.data, inputs,
                                    timeout=TIMEOUT)
    except Exception, e:    
        log.warning("Could not execute workflow %s" % test.id, e)
        trace = StringIO()
        traceback.print_exc(file=trace)
        WorkflowTestRun(workflow_test=test,
                    return_code=None,
                    stderr=trace.getvalue(),
                    stdout="",
                    report=None,
                    workflow=test.workflow.data,
                    inputdoc=ET.tostring(input_doc),
                    outputdoc=executed.outputdoc)
        
        return
    # Log the test run
    WorkflowTestRun(workflow_test=test,
                    return_code=executed.return_code,
                    stdout=executed.stdout,
                    stderr=executed.stderr,
                    report=executed.report,
                    workflow=test.workflow.data,
                    inputdoc=ET.tostring(input_doc),
                    outputdoc=executed.outputdoc)


def main():
    for test in WorkflowTest.select():
        run_test(test)
        turbogears.database.commit_all()



if __name__ == "__main__":
    main()


