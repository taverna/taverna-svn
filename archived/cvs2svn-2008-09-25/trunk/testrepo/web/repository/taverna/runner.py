#!/usr/bin/env python

import os
import signal
import time
import tempfile
import shutil
import subprocess
import codecs

import baclava

# How often to check if the process is finished
POLL_PERIOD=0.1
# How long to allow the process to die after a normal kill before
# a kill -9 will be tried
KILL_9_TIMEOUT=0.5



LAUNCHER_CLASS="org.embl.ebi.escience.scufl.tools.WorkflowLauncher"

CMD = ["java", "-Xms64m", "-Xmx64m", "-Djava.awt.headless=true",
       "-Dtaverna.home=/local/stain/download/taverna-1.3.cvs.2005.05.19",
       "-Dtaverna.path=/local/stain/repository/executer-1.3.1.jar",
       "-Dtaverna.main=net.sf.taverna.tools.WorkflowExecuter",
       "-jar", "/local/stain/repository/launcher-1.3.1.jar"]

def utf8open(file, mode="r"):
    return codecs.open(file, mode, encoding="utf8", errors="ignore")

class WorkflowExecuter(object):
    def __init__(self, workflow_data, inputs=None, timeout=60, cmd=None):
        """Execute the Taverna workflow.

        parameters:

            workflow_data -- XSCUFL string of the workflow

            inputs -- a dictionary of inputs, the keys are the input port names,
            and the values should be strings.

            timeout -- terminate workflow execution if using more than
            the specified seconds, default is 60

            cmd -- (optional) full list of command line options for
            executing the WorkflowExecuter. By default CMD will be run.
        """
        if inputs is None:
            inputs = {}
        old_dir = os.curdir
        try:
            if cmd is None:
                cmd = list(CMD)
            else:   
                cmd = list(cmd) # Copy, since we'll modify the list
            self.run_dir = tempfile.mkdtemp(prefix="taverna")
            os.chdir(self.run_dir)
            workflow_file = "workflow.xml"
            f = utf8open(workflow_file, "w")
            f.write(workflow_data)
            f.close()
            cmd.append(workflow_file)

            outputdoc = "outputs.xml"
            cmd.extend(["-outputdoc", outputdoc])

            output_dir = "output"
            os.mkdir(output_dir)
            cmd.extend(["-output", output_dir])

            progressReport = "progressReport.xml"
            cmd.extend(["-report", progressReport])

            input_doc = "inputs.xml"
            baclava.make_input_doc(input_doc, inputs)
            cmd.extend(["-inputdoc", input_doc])
            
            return_code, stdout, stderr = timeout_run(cmd, timeout)
                
            self.return_code = return_code
            self.stdout = stdout.read()
            self.stderr = stderr.read()
            self.report = None
            self.outputdoc = None
            self.outputs = None
            if os.path.isfile(progressReport):
                self.report = utf8open(progressReport).read()
            if os.path.isfile(outputdoc):
                self.outputdoc = utf8open(outputdoc).read()
            if os.path.isdir(output_dir):
                self.outputs = self.read_outputs(output_dir)
        finally:
            os.chdir(old_dir)
            #shutil.rmtree(self.run_dir, ignore_errors=True)
    
        
    def read_outputs(self, output_dir):
        outputs = {}
        for output_file in os.listdir(output_dir):
            output_path = os.path.join(output_dir, output_file)
            if os.path.isdir(output_path):
                # Recurse 
                data = self.read_outputs(output_path).items()
                # Increasing by filenames (which we throw away)
                data.sort()
                output_data = [output for name,output in data]
            else:    
                output_data = open(output_path).read()
            outputs[output_file] = output_data
        return outputs     

def timeout_run(cmd, timeout):
        #print "Running", " ".join(cmd)
        stdout = tempfile.TemporaryFile()
        stderr = tempfile.TemporaryFile()
        started = time.time()  
        process = subprocess.Popen(cmd, stdout=stdout,
                                   stderr=stderr, close_fds=True)
        while time.time() < (started+timeout):
            return_code = process.poll()
            if return_code is not None:
                break
            time.sleep(POLL_PERIOD)

        if process.poll() is None:
            os.kill(process.pid, signal.SIGTERM)
            timeout = KILL_9_TIMEOUT
            while timeout > 0:
                time.sleep(POLL_PERIOD)
                timeout - POLL_PERIOD
                return_code = process.poll()    
                if return_code is not None:
                    break
            if return_code is None:
                os.kill(process.pid, signal.SIGKILL)
                return_code = process.poll()    

        stdout.seek(0)
        stderr.seek(0)
        return return_code, stdout, stderr

