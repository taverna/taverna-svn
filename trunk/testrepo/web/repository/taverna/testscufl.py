#!/usr/bin/env python

from unittest import TestCase
import tempfile

from scufl import Scufl

from testworkflows import COMPLEX_WORKFLOW, OLD_WORKFLOW

class TestScuflConstructor(TestCase):

    def testDocument(self):
        scufl = Scufl(COMPLEX_WORKFLOW)
        self.assertEqual(scufl.root.tag,
            "{http://org.embl.ebi.escience/xscufl/0.1alpha}scufl")

    def testFilename(self):
        f = tempfile.NamedTemporaryFile()
        f.write(COMPLEX_WORKFLOW)
        f.flush()
        # as filename
        scufl = Scufl(file = f.name)
        self.assertEqual(scufl.root.tag,
            "{http://org.embl.ebi.escience/xscufl/0.1alpha}scufl")
        # and as a file handle
        f.seek(0)
        scufl = Scufl(file = f)
        self.assertEqual(scufl.root.tag,
            "{http://org.embl.ebi.escience/xscufl/0.1alpha}scufl")

class TestParsing(TestCase):
    def setUp(self):
        self.scufl = Scufl(COMPLEX_WORKFLOW)

    def testSinks(self):
        self.assertEquals(self.scufl.sinks,
                     ['merged_kegg_pathways', 'gene_and_pathway'])

    def testSources(self):
        self.assertEquals(self.scufl.sources, ['probeset_list'])

    def testAuthor(self):
        self.assertEquals(self.scufl.author, "Paul Fisher")

    def testTitle(self):
        self.assertEquals(self.scufl.title, "Probeset workflow")
        
    def testDescription(self):
        self.assertEquals(self.scufl.description, 
                          "A way to probe probesets in a workflow")

    def testLSID(self):
        self.assertEquals(self.scufl.lsid,
            "urn:lsid:www.mygrid.org.uk:operation:NXIYI8FZ5K0")

    
class TestParsingOld(TestCase):
    def setUp(self):
        self.scufl = Scufl(OLD_WORKFLOW)

    def testSinks(self):
        self.assertEquals(self.scufl.sinks,
                     ['PepStats_Out', 'HTH_NucBindingSites',
                     'ProteinLocation', 'ProteinLocation_2', 'SP_url',
                     'SP_outseq', 'sumo_out', 'COPYRIGHT_INFO',
                     'Blastp_nr_out', 'tblastn_nr_out', 'TargetP_out',
                     'pscan_out', 'pepcoil_out', 'ePEST_out',
                     'InterproRaw_out', 'epest_GraphicsPNG',
                     'pepwindow_Graphics'])

    def testSources(self):
        self.assertEquals(self.scufl.sources, ['PROTEINFASTA'])

    def testAuthor(self):
        self.assertEquals(self.scufl.author, None)

    def testTitle(self):
        self.assertEquals(self.scufl.title, None)
        
    def testDescription(self):
        self.assertEquals(self.scufl.description, None)

    def testLSID(self):
        self.assertEquals(self.scufl.lsid, None) 

if __name__ == "__main__":
    import unittest
    unittest.main()
          

