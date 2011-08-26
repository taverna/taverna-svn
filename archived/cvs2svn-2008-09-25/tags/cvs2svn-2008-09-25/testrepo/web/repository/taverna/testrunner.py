#!/usr/bin/env python

from unittest import TestCase
import tempfile
import time
import os
import runner
from testworkflows import SIMPLE_WORKFLOW, ITERATION_WORKFLOW

class TestTimeoutRun(TestCase):
    def testNormally(self):
        cmd = ["echo", "I am a fish"]
        now = time.time()
        code, stdout, stderr = runner.timeout_run(cmd, 2)
        finished = time.time()
        # Should not wait unneccessary
        self.assert_(finished-now < 1)
        self.assertEquals(code, 0)
        self.assertEquals(stdout.read(), "I am a fish\n")
        self.assertEquals(stderr.read(), "")

    def testStdErr(self):
        cmd = ["ls", "/non/existing"]
        code, stdout, stderr = runner.timeout_run(cmd, 2)
        self.assert_(code)
        self.assert_(stderr.read())

    def testKill(self):
        # A script that sleeps 2 seconds before creating a file named
        # itsownname.finished
        script = """#!/bin/sh
sleep 2
touch $0.finished
"""     
        scriptFile = tempfile.NamedTemporaryFile()
        scriptFile.write(script)
        scriptFile.flush()
        finished = scriptFile.name + ".finished"
        # Let's first test our script 
        self.assertFalse(os.path.exists(finished))
        # Should finish in 4 seconds
        code, stdout, stderr = runner.timeout_run(["sh", scriptFile.name], 4)
        self.assert_(os.path.exists(finished))
        os.unlink(finished)
        
        # OK, let's set the timer too low so that we'll have to kill it
        code, stdout, stderr = runner.timeout_run(["sh", scriptFile.name], 1)
        self.assertEqual(code, -15) 
        time.sleep(2)
        # If it was still running, finished would have been created
        self.assertFalse(os.path.exists(finished))
        # (So that was the white-box testing of the process actually
        # being killed!)


class TestExecuteWorkflow(TestCase):
    def testExecuteSimpleExample(self):
        execute = runner.WorkflowExecuter(SIMPLE_WORKFLOW)
        self.assertEqual(execute.return_code, 0)
        self.assertEqual(execute.stdout, "")
        self.assertEqual(execute.stderr, "")
        self.assertEqual(execute.outputs, {"out": "foobar"})
        self.assertEqual(execute.report[:5], "<?xml")

    def testExecuteInputExample(self):
        execute = runner.WorkflowExecuter(ITERATION_WORKFLOW,
                                    inputs={"food": ["fish", "soup"]})
        self.assertEqual(execute.stderr, "")
        self.assertEqual(execute.stdout, "")
        self.assertEqual(execute.return_code, 0)
        self.assertEqual(execute.outputs, 
                         {"result": ["fishbowl", "soupbowl"]})
        self.assertEqual(execute.report[:5], "<?xml")



if __name__ == "__main__":
    import unittest
    unittest.main()
          

