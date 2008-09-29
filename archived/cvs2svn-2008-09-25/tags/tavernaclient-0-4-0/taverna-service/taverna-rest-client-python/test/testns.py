#!/usr/bin/env python
"""
Tests for ns.py

Author: Stian Soiland
Copyright: 2006-2008 University of Manchester, UK
URL: http://taverna.sourceforge.net/
Contact: taverna-hackers@lists.sourceforge.net
Licence: LGPL 3 (See LICENCE or http://www.gnu.org/licenses/lgpl.html)
"""

import unittest

from tavernaclient.ns import Namespace

class testNS(unittest.TestCase):
    
    def setUp(self):
        self.TestNS = Namespace("http://test")
    
    def testGetAttrib(self):
        self.assertEqual("{http://test}document", self.TestNS.document)
        self.assertEqual("{http://test}author", self.TestNS.author)
        
    def testGet(self):
        self.assertEqual("{http://test}fish", self.TestNS.get("fish"))


if __name__ == "__main__":
    unittest.main()
