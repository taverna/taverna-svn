from tavernaclient.ns import Namespace
import unittest

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
