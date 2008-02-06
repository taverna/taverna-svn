"""
Baclava data document parsing and creation.

Author: Stian Soiland
Copyright: 2006-2008 University of Manchester, UK
URL: http://taverna.sourceforge.net/
Contact: taverna-hackers@lists.sourceforge.net
Licence: LGPL 3 (See LICENCE or http://www.gnu.org/licenses/lgpl.html)
"""

import base64
from elementtree import ElementTree as ET

try:
    from xml.dom.ext import PrettyPrint
    from xml.dom.ext.reader.Sax import FromXml
except ImportError:
    PrettyPrint, FromXML = None, None
import logging as log

from ns import Namespace
from scufl import XSCUFL

BACLAVA = Namespace("http://org.embl.ebi.escience/baclava/0.1alpha")


def make_input_doc(input_doc, inputs):
    """Generate a Baclava/Scufl input XML document.

    input_doc -- the file object or file name to write to

    inputs -- a dictionary of input values. Keys must be strings, while
    data can be strings or list of strings.
    """
    if hasattr(input_doc, "write"):
        output = input_doc
    else:    
        output = open(input_doc,  "w")
    
    root = make_input_elem(inputs)
        
    if PrettyPrint:
        PrettyPrint(FromXml(ET.tostring(root)), output)
    else:    
        tree = ET.ElementTree(root)
        tree.write(output)

def make_input_elem(inputs):
    root = ET.Element(BACLAVA.dataThingMap)
    for name, data in inputs.iteritems():

        thing = ET.SubElement(root, BACLAVA.dataThing)
        thing.attrib["key"] = name
        doc = ET.SubElement(thing, BACLAVA.myGridDataDocument)
        doc.attrib["lsid"] = ""
        metadata = ET.SubElement(doc, XSCUFL.metadata)
        mimetypes = ET.SubElement(metadata, XSCUFL.mimeTypes)
        mimeType = ET.SubElement(mimetypes, XSCUFL.mimeType)
        # FIXME: Don't hardcode text/plain
        mimeType.text = "text/plain"
        data_elem = data_element(data)
        doc.append(data_elem)
        syntactic_type = "'%s'" % mimeType.text
        depth = data_elem._depth
        syntactic_type = "l(" * depth + syntactic_type + ")" * depth    
        doc.attrib["syntactictype"] = syntactic_type
    return root


def data_element(data):
    if isinstance(data, basestring):
        return data_string_element(data)
    try:
        iterator = iter(data)
    except TypeError:
        return data_string_element(data)
    partialOrder = ET.Element(BACLAVA.partialOrder)    
    partialOrder.attrib["lsid"] = ""
    # FIXME: Support sets?
    partialOrder.attrib["type"] = "list"
    relationList = ET.SubElement(partialOrder, BACLAVA.relationList)
    itemList = ET.SubElement(partialOrder, BACLAVA.itemList)
    item_elem = None
    for (n, item) in enumerate(iterator):
        item_elem = data_element(item)
        item_elem.attrib["index"] = str(n)
        if n > 0:
            relation = ET.SubElement(relationList, BACLAVA.relation)
            relation.attrib["parent"] = str(n-1)
            relation.attrib["child"] = str(n)
        itemList.append(item_elem)    
    if not item_elem:
        # Empty list
        partialOrder._depth = 1
    else:
        partialOrder._depth = item_elem._depth + 1
    return partialOrder      
            
        
def data_string_element(data):
    if isinstance(data, unicode):
        dataString = data.encode("utf8")
    else:
        dataString = str(data)
    dataElement = ET.Element(BACLAVA.dataElement)
    dataElement.attrib["lsid"] = ""
    dataElementData = ET.SubElement(dataElement, BACLAVA.dataElementData)
    dataElementData.text = base64.encodestring(dataString).strip()
    dataElement._depth = 0
    return dataElement


def parse(xml=None, elem=None):
    """Parse Baclava data document. 
     
    Return a dictionary of BaclavaData instances as of the dataThingMap"""
    if xml is None and elem is None:
        raise TypeError("Need to specify either xml or elem")
    if xml is not None and elem is not None:
        raise TypeError("Can't specify both xml and elem")
        
    if xml is not None:
        elem = ET.XML(xml)
    things = {}
    for thing in elem.findall(BACLAVA.dataThing):
        thing_name = thing.attrib["key"]
        thing_obj = _parse_thing(thing)
        things[thing_name] = thing_obj 
    return things      

class BaclavaData(object):
    pass

class lsid(object): 
    types = {}
    @classmethod
    def wrap(cls, obj, lsid):
        try:
            obj.lsid = lsid
            return obj
        except AttributeError:
            pass
        t = type(obj)
        if t not in cls.types:
            class lsid_t(t):
                pass
            lsid_t.__name__ = "lsid_" + t.__name__
            cls.types[t] = lsid_t
        # FIXME: Assumes constructor copy works
        # (it does for str/unicode/list/dict/set we hope) 
        wrapped = cls.types[t](obj)
        wrapped.lsid = lsid
        return wrapped

def _parse_thing(thing):
    """Parse a BACLAVA.dataThing element"""
    result = BaclavaData()
    doc = thing.find(BACLAVA.myGridDataDocument)
    result.syntactictype = doc.attrib["syntactictype"]
    result.mime_types = [m.text.strip() for m in 
        doc.findall("/".join((XSCUFL.metadata,XSCUFL.mimeTypes,XSCUFL.mimeType)))]
    result.lsid = doc.attrib["lsid"]
    elem = doc.find(BACLAVA.dataElement) or doc.find(BACLAVA.partialOrder)
    # Decode as utf8 if it is text/something
    if "text/" in result.syntactictype:
        result.data = _parse_data(elem, "utf8")
    else:
        result.data = _parse_data(elem)
    return result

def _parse_data(element, try_encoding=None):
    """Parse a BACLAVA.dataElement or BACLAVA.partialOrder 
    
    Return a str or a list of _parse_data returns (ie. either
    str/unicode or deeper lists)

    If try_encoding is given, strings will be attempted decoded using
    given encoding. Successfully decoded strings will be unicode
    objects, while the rest will be the original str objects.
    This is only recommended for text/* mimetypes.
    """
    if element.tag == BACLAVA.dataElement:
        data = element.find(BACLAVA.dataElementData)
        if not data.text:
            result = ""
        else:    
            result = base64.decodestring(data.text)
        if try_encoding:
            try:
                result = result.decode(try_encoding)
            except UnicodeDecodeError:
                pass
    elif element.tag == BACLAVA.partialOrder:
        if element.attrib.get("type") == "list":
            result = []
            add = result.append
        else:
            result = set()
            add = result.add
        # ignore relationList    
        for child in element.findall("%s/*" % BACLAVA.itemList):
            child_parsed = _parse_data(child, try_encoding)
            if child_parsed is not None:
                add(child_parsed)
    else:
        # We don't know about this thing
        log.warn("Skipping unknown tag %s" % element.tag)
        return None
    return lsid.wrap(result, element.attrib["lsid"])     
            
