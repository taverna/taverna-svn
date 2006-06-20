
from elementtree import ElementTree

import re

from ns import XSCUFL


class Scufl(object):
    def __init__(self, document=None, file=None):
        if file:
            self.root = ElementTree.parse(file).getroot()
        elif document:
            self.root = ElementTree.XML(document)
        else:
            raise TypeError, "Missing argument document or file"
        self.sinks = None
        self.sources = None
        self.author = None
        self.title = None
        self.lsid = None
        self.description = None
        self._parse()
   
    def _parse(self):
        def port_name(port_element):
            try:
                return port_element.attrib["name"]
            except KeyError:
                return port_element.text.strip()
        self.sinks = [port_name(e) for e in self.root.findall(XSCUFL.sink)]
        self.sources = [port_name(e) for e in self.root.findall(XSCUFL.source)]
        desc = self.root.find(XSCUFL.workflowdescription)
        if desc is not None:
            self.author = desc.attrib["author"]
            self.title = desc.attrib["title"]
            self.lsid = desc.attrib["lsid"]
            self.description = (desc.text or "").strip()
        self.processors = {}
        for processor in self.root.findall(XSCUFL.processor): 
            p_name = processor.attrib["name"]
            p = dict()
            self.processors[p_name] = p
            for child in processor.getchildren():
                if child.tag == XSCUFL.description:
                    p["description"] = child.text.strip()
                elif child.tag == XSCUFL.iterationstrategy:
                    p["iterationstrategy"] = True
                else:
                    # Type is just the tag without namespace
                    p["type"] = re.sub("{.*}", "", child.tag)
        
