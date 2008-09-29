#!/usr/bin/env python
import pkg_resources
pkg_resources.require("TurboGears")

import turbogears
import cherrypy
cherrypy.lowercase_api = True

import os
import sys

base = os.path.dirname(__file__)
if os.path.exists(os.path.join(base, "setup.py")):
    turbogears.update_config(configfile=os.path.join(base, "dev.cfg"),
        modulename="repository.config")
else:
    turbogears.update_config(configfile=os.path.join(base, "prod.cfg"),
        modulename="repository.config")

from repository import model




def main():
    recurse = False
    args = sys.argv[1:]
    if "-r" in args:
        recurse = True
        args.remove("-r")
    add_workflows(args, recurse)    

def add_workflows(dirs, recurse):
    for dir in dirs:
        if not os.path.isdir(dir):
            continue
        if os.path.exists(_workflow_name(dir)):
            print "Adding workflow", dir
            add_workflow_test(dir)
        elif recurse:
            add_workflows((os.path.join(dir, subdir) 
                          for subdir in os.listdir(dir)), recurse)
    turbogears.database.commit_all()

def _workflow_name(workflow_dir):
    name = os.path.basename(workflow_dir)
    return os.path.join(workflow_dir, name+".xml")

def add_workflow_test(workflow_dir):
    name = os.path.basename(workflow_dir)
    description = "Imported from " + workflow_dir
    workflow_data = open(_workflow_name(workflow_dir)).read()
    workflow = model.Workflow(name=name, description=description,
                              created_by=None, data=workflow_data)
    workflow_test = model.WorkflowTest(name=name+" Test", workflow=workflow)
    input_dir = os.path.join(workflow_dir, "inputs")
    try:
        inputs = os.listdir(input_dir)
    except OSError:
        inputs = ()
    for input_file in inputs:
        input_name, input_ext = os.path.splitext(input_file)
        if input_ext != ".txt":
            continue
        input_data = open(os.path.join(input_dir, input_file)).read()
        input = model.WorkflowTestInput(workflow_test=workflow_test,
                                        port=input_name,
                                        data=input_data)

    output_dir = os.path.join(workflow_dir, "outputs")
    try:
        outputs = os.listdir(output_dir)
    except OSError:
        outputs = ()
    for output_file in outputs:
        output_name, output_ext = os.path.splitext(output_file)
        if output_ext not in (".txt", ".any", ".regex"):
            continue
        output_types = {
            ".txt": "exact",
            ".regex": "regex",
            ".any": "ignore",
        }
        output_type = output_types[output_ext]
        if output_type == "ignore":
            output_data = ""
        else:
            output_data = open(os.path.join(output_dir, output_file)).read()
        output = model.WorkflowTestOutput(workflow_test=workflow_test,
                                        port=output_name,
                                        type=output_type,
                                        data=output_data)


if __name__ == "__main__":
    main()


