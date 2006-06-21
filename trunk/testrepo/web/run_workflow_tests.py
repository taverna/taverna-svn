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

base = os.path.dirname(__file__)
if os.path.exists(os.path.join(base, "setup.py")):
    turbogears.update_config(configfile=os.path.join(base, "dev.cfg"),
        modulename="repository.config")
else:
    turbogears.update_config(configfile=os.path.join(base, "prod.cfg"),
        modulename="repository.config")

from repository.model import WorkflowTestRun, WorkflowTest

from configobj import ConfigObj
taverna_config = ConfigObj(os.path.join(base, "taverna.cfg"))

def java_cmd(taverna, java):
    cmd = []
    cmd.append(taverna_config["java"]["versions"][java])

    cmd.append("-Xms%s" % taverna_config["java"]["options"]["start_memory"])
    cmd.append("-Xmx%s" % taverna_config["java"]["options"]["max_memory"])
    
    if taverna_config["java"]["options"]["server"].lower() not in (
            "false", "0", "no"):
        cmd.append("-server")
          
    if taverna_config["java"]["options"]["headless"].lower() not in (
            "false", "0", "no"):
        cmd.append("-Djava.awt.headless=true")

    cmd.append("-Dtaverna.main=%s" %
        taverna_config["taverna"]["versions"][taverna]["main"]) 

    cmd.append("-jar")
    cmd.append(taverna_config["taverna"]["versions"][taverna]["jar"])

    return cmd


def all_taverna_javas():
    """Find all taverna/java combinations.
    
    Yield tuples of (taverna, java) as to be called to java_cmd()
    """
    JAVAS = taverna_config["java"]["versions"].keys()
    for taverna, tav_dict in taverna_config["taverna"]["versions"].iteritems():
        javas = tav_dict.get("java", "").split() or JAVAS
        for java in javas:
            yield (taverna, java)

    

def run_test(test, taverna, java):
    inputs = {}
    for input in test.inputs:
        inputs[input.port] = input.data
    TIMEOUT=15
    input_doc = make_input_elem(inputs)
    log.info("Executing workflow test %s with taverna %s and java %s" % 
              (test.id, taverna, java))

    cmd = java_cmd(taverna, java)
    try:
        executed = WorkflowExecuter(test.workflow.data, inputs,
                                    timeout=TIMEOUT, cmd=cmd)
        stdout = executed.stdout
        stderr = executed.stderr
        report = executed.report
        return_code = executed.return_code
        outputdoc = executed.outputdoc
    except Exception, e:    
        log.warning("Could not execute workflow %s" % test.id, e)
        trace = StringIO()
        traceback.print_exc(file=trace)
        stderr = trace.getvalue(),
        stdout = ""
        report = ""
        returncode = -1
        outpurdoc = ""

    # Log the test run
    WorkflowTestRun(workflow_test=test,
                    return_code=return_code,
                    stdout=stdout,
                    stderr=stderr,
                    report=report,
                    workflow=test.workflow.data,
                    inputdoc=ET.tostring(input_doc),
                    outputdoc=outputdoc,
                    taverna=taverna, 
                    java=java)
    turbogears.database.commit_all()


def main():
    for test in WorkflowTest.select():
        for taverna, java in all_taverna_javas():
            run_test(test, taverna, java)



if __name__ == "__main__":
    main()


