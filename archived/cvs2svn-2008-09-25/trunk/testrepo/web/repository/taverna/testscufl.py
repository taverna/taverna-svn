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
    
    def testProcessorNames(self):
        procs = set(self.scufl.processors.keys())
        exp_procs = set(['merge_gene_pathways', 'lister',
            'merge_probesets', 'mergePathways_2', 'split_by_regex',
            'Parse_swiss_ids', 'Remove_swiss_nulls', 'split_gene_ids',
            'get_pathways_by_genes', 'Add_ncbi_to_string',
            'probeset_to_gene', 'Ensembl_gene_info', 'merge_pathways',
            'regex1', 'comma', 'Swissprot_to_Gi', 'split_by_regex_2',
            'Concatenate_two_strings', 'species', 'getcurrentdatabase',
            'Kegg_gene_ids_all_species'])
        self.assertEquals(exp_procs, procs)
    
    def testProcessorTypes(self):     
        self.assertEquals("stringconstant",
            self.scufl.processors["species"]["type"])

        self.assertEquals("workflow", 
            self.scufl.processors["Swissprot_to_Gi"]["type"])

        self.assertEquals("local", 
            self.scufl.processors["merge_pathways"]["type"])

        self.assertEquals("arbitrarywsdl", 
            self.scufl.processors["Kegg_gene_ids_all_species"]["type"])

        self.assertEquals("beanshell", 
            self.scufl.processors["split_gene_ids"]["type"])

    
class TestParsingOld(TestCase):
    """Try to parse older workflow"""
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
          

