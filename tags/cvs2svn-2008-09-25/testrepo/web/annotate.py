#!/usr/bin/env python

import sys
import RDF


import template_script
from repository import model
from repository.taverna.scufl import Scufl
from repository.taverna.ns import XSCUFL
from repository.taverna import baclava

import logging as log
#log = logging.getLogger("annotate")


_options_string="""host='localhost', 
user='repository', password='VJBc5HHL', 
database='repository'"""

rdf_model = None

# Remember the trailing /!
URL_BASE = "http://www.mygrid.org.uk/testrepo/"

ns = RDF.NS(URL_BASE + "o/")
ns.rdfs = RDF.NS("http://www.w3.org/2000/01/rdf-schema#")
ns.rdf = RDF.NS("http://www.w3.org/1999/02/22-rdf-syntax-ns#")




def create_rdf_model():
    storage = RDF.Storage(storage_name="mysql", name="repository",
                          options_string=_options_string + ", new='true'")
def get_rdf_model():
    global rdf_model
    if rdf_model:
        return rdf_model
    storage = RDF.Storage(storage_name="mysql", name="repository",
                          options_string=_options_string)
    rdf_model = RDF.Model(storage) 
    return rdf_model

def print_rdf_model():
    for statement in rdf_model:
        print statement

def add(subject, predicate, object):
    statement = RDF.Statement(subject, predicate, object)
    rdf_model.append(statement)
    return statement


def annotate_workflow(workflow):
    workflow_url = "%sworkflow/%s" % (URL_BASE, workflow.id)
    wf = RDF.Uri(workflow_url) 
    scufl = Scufl(workflow.data)
    if scufl.author:
        add(wf, ns.author, scufl.author)
    if scufl.lsid:
        add(wf, ns.lsid, RDF.Uri(scufl.lsid))
    if scufl.title:    
        add(wf, ns.title, scufl.title)
    if scufl.description:
        add(wf, ns.description, scufl.description)
    # Find Processor info    
    xscufl = scufl.root    
    for processor in xscufl.findall(XSCUFL.processor):
        p_name = processor.attrib["name"] 
        p = RDF.Uri(workflow_url + "/proc/" + p_name)
        add(wf, ns.processor, p)
        for child in processor.getchildren():
            if child.tag == XSCUFL.description:
                add(p, ns.description, child.text.strip())
            elif child.tag == XSCUFL.iterationstrategy:
                add(p, ns.has, ns.iterationstrategy)
            else:
                # Transform tag to namespaced value
                type_name = child.tag.replace("}", "#")
                type_name = type_name.replace("{", "")
                add(p, ns.type, RDF.Uri(type_name))

def annotate_test(test):
    t = RDF.Uri("%stest/%s" % (URL_BASE, test.id))
    workflow = test.workflow
    wf = RDF.Uri("%sworkflow/%s" % (URL_BASE, workflow.id))
    add(t, ns.tests, wf)

def annotate_run(run):
    r = RDF.Uri("%stest_run/%s" % (URL_BASE, run.id))
    scufl = Scufl(run.workflow)

    test = run.workflow_test
    t = RDF.Uri("%stest/%s" % (URL_BASE, test.id))
    add(r, ns.runs, t)

    add(r, ns.uses, getattr(ns, "java/%s" % run.java))
    add(r, ns.uses, getattr(ns, "taverna/%s" % run.taverna))

    if run.return_code:
        add(r, ns.fails, ns.returncode)
    if run.stdout:
        add(r, ns.fails, ns.stdout)
    if run.stderr:
        add(r, ns.fails, ns.stderr)
    if not run.report:
        add(r, ns.fails, ns.report)

    outputs = None
    if not run.outputdoc:
        add(r, ns.fails, ns.outputdoc)
        add(r, ns.fails, ns.output)
    else:
        try:
            outputs = baclava.parse(run.outputdoc)
        except Exception, e:
            log.warn("Could not parse output document for test run %d: %s", run.id, e)
            add(r, ns.fails, ns.outputdoc)
            add(r, ns.fails, ns.output)
    
    expected_outputs = dict((out.port, out) for out in test.outputs)
    expected_outports = set(expected_outputs.keys())

    if outputs is not None:
        real_outports = set(outputs.keys())

        missing = expected_outports - real_outports
        for port in missing:
            p = RDF.Uri("%stest_run/%s/output/%s" % (URL_BASE, run.id, port))
            add(r, ns.missing_output, p)
            add(r, ns.fails, ns.output)

        extra = real_outports - expected_outports
        for port in extra:
            p = RDF.Uri("%stest_run/%s/output/%s" % (URL_BASE, run.id, port))
            add(r, ns.missing_output, p)
            add(r, ns.fails, ns.output)
        
        common = real_outports.intersection(expected_outports)
        for port in common:
            p = RDF.Uri("%stest_run/%s/output/%s" % (URL_BASE, run.id, port))
            add(r, ns.returned, p)
            expected = expected_outputs[port]
            real = outputs[port]
            real_s = str(real_s).strip()

            ex_p  = RDF.Uri("%stest/%s/output/%s" % (URL_BASE, test.id, port))
            if expected.type == "ignore":
                continue
            elif expected.type == "match":
                if expected.data.strip() == real_s:
                    add(p, ns.match, ex_p)
                else:
                    add(r, ns.fails, ns.output)
                    add(p, ns.not_match, ex_p)
            elif expected.type == "regex":
                if re.search(expected.data.strip(),
                    real_s,
                    re.DOTALL|re.MULTILINE):
                    add(p, ns.match, ex_p)
                else:
                    add(r, ns.fails, ns.output)
                    add(p, ns.not_match, ex_p)
            else:
                raise "Unknown expected type %s" % expected.type




def main():
    if "-c" in sys.argv:
        create_rdf_model()
        sys.exit(0)
    get_rdf_model()
    if "-p" in sys.argv:
        print_rdf_model()
        sys.exit(0)
    for workflow in model.Workflow.select():
        annotate_workflow(workflow)
    for test in model.WorkflowTest.select():
        annotate_test(test)
    for run in model.WorkflowTestRun.select():
        annotate_run(run)

if __name__ == "__main__":
    main()
