#!/usr/bin/env python

import sys
import RDF

import template_script
from repository import model
from repository.taverna.scufl import Scufl
from repository.taverna.ns import XSCUFL

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
    print "Added", statement
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
    print "Annotated", workflow                

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

if __name__ == "__main__":
    main()
