import ns
import unittest

class testNS(unittest.TestCase):
    def testMeta(self):
        class TestNS(ns.NS):
            NS = "http://test"
            tags = "document author"
        
        self.assertEqual(TestNS.document, "{http://test}document")
        self.assertEqual(TestNS.author, "{http://test}author")
    
    def testMissingTags(self):
        try:
            class TestNS(ns.NS):
                NS = "Something"
                # But forgot tags 
        except:
            return
        self.fail("Should throw exception for missing tags")        

    def testMissingNS(self):
        try:
            class TestNS(ns.NS):
                tags = "some tags"
                # But forgot NS
        except:
            return
        self.fail("Should throw exception for missing NS")        


if __name__ == "__main__":
    unittest.main()
