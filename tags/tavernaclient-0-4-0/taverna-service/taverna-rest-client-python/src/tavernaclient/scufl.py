"""
SCUFL (Taverna workflow) basic information extraction.

Author: Stian Soiland
Copyright: 2006-2008 University of Manchester, UK
URL: http://taverna.sourceforge.net/
Contact: taverna-hackers@lists.sourceforge.net
Licence: LGPL 3 (See LICENCE or http://www.gnu.org/licenses/lgpl.html)
"""

from elementtree import ElementTree

import re

from ns import Namespace
XSCUFL = Namespace("http://org.embl.ebi.escience/xscufl/0.1alpha")


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

    def _port_name(self, port_element):
        try:
            return port_element.attrib["name"]
        except KeyError:
            return port_element.text.strip()
   
    def _parse(self):
        self.sinks = [self._port_name(e) for e in self.root.findall(XSCUFL.sink)]
        self.sources = [self._port_name(e) for e in self.root.findall(XSCUFL.source)]
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
        
