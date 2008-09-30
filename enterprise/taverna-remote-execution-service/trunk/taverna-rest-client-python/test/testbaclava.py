#!/usr/bin/env python

"""
Tests for baclava.py

Author: Stian Soiland
Copyright: 2006-2008 University of Manchester, UK
URL: http://taverna.sourceforge.net/
Contact: taverna-hackers@lists.sourceforge.net
Licence: LGPL 3 (See LICENCE or http://www.gnu.org/licenses/lgpl.html)
"""

from unittest import TestCase
import tempfile
try:
    from cStringIO import StringIO
except ImportError: 
    from StringIO import StringIO
import time
import os
import base64
from elementtree import ElementTree

from tavernaclient.baclava import BACLAVA
from tavernaclient.scufl import XSCUFL
from tavernaclient import baclava



class TestExecuteWorkflow(TestCase):

    def testMakeInputEmpty(self):
        input_doc = StringIO()
        baclava.make_input_doc(input_doc, {})
        self.assertEqual('<ns0:dataThingMap '
                         'xmlns:ns0="http://org.embl.ebi.escience/baclava/0.1alpha" />', input_doc.getvalue())
    
    def testMakeInputSingleString(self):
        root = baclava.make_input_elem({"in": "my value"})
        self.assertEqual(root.tag, BACLAVA.dataThingMap)
        self.assertEqual(len(root), 1)
        thing = root[0]
        self.assertEqual(thing.tag, BACLAVA.dataThing)
        self.assertEqual(thing.attrib["key"], "in")
        self.assertEqual(len(thing), 1)
        doc = thing[0]
        self.assertEqual(doc.tag, BACLAVA.myGridDataDocument)
        self.assertEqual(len(doc), 2)
        self.assertEqual(doc.attrib["syntactictype"], "'text/plain'")
        metadata = doc[0]
        self.assertEqual(metadata.tag, XSCUFL.metadata)
        self.assertEqual(metadata[0].tag, XSCUFL.mimeTypes)
        self.assertEqual(metadata[0][0].tag, XSCUFL.mimeType)
        self.assertEqual(metadata[0][0].text, "text/plain")
        dataElement = doc[1]
        self.assertEqual(dataElement.tag, BACLAVA.dataElement)
        dataElement.attrib["lsid"]
        self.assertEqual(dataElement[0].tag, BACLAVA.dataElementData)
        self.assertEqual(dataElement[0].text.strip(),
                         base64.encodestring("my value").strip())
        

    
    def testMakeInputTwoStrings(self):
        root = baclava.make_input_elem(
                    {"in": "my value", "other": "other value"})
        self.assertEqual(root.tag, BACLAVA.dataThingMap)
        self.assertEqual(len(root), 2)
        keys = set(thing.attrib["key"] for thing in root)
        self.assertEqual(keys, set(("in", "other")))
        values = set(thing[0][1][0].text.strip() for thing in root)
        self.assertEqual(values, 
                         set(base64.encodestring(s).strip() 
                             for s in ("my value", "other value")))


    def testMakeInputList(self):
        root = baclava.make_input_elem(
                    {"in": ["first value", "second value"]})
        self.assertEqual(root.tag, BACLAVA.dataThingMap)
        self.assertEqual(len(root), 1)
        thing = root[0]
        self.assertEqual(thing[0].attrib["syntactictype"], "l('text/plain')")
        partial = thing[0][1]
        self.assertEqual(partial.tag, BACLAVA.partialOrder)
        self.assertEqual(partial.attrib["type"], "list")
        partial.attrib["lsid"]
        self.assertEqual(partial[0].tag, BACLAVA.relationList)
        relation = partial[0][0]
        # Actually the relations are ignored by Taverna 
        self.assertEqual(relation.tag, BACLAVA.relation)
        self.assertEqual(relation.attrib["parent"], "0")
        self.assertEqual(relation.attrib["child"], "1")
        itemList = partial[1]
        self.assertEqual(itemList.tag, BACLAVA.itemList)
        self.assertEqual(len(itemList), 2)
        # But the order of the items in the item list is important
        data1 = itemList[0]
        self.assertEqual(data1.tag, BACLAVA.dataElement)
        self.assertEqual(data1.attrib["index"], "0")
        self.assertEqual(data1[0].tag, BACLAVA.dataElementData)
        self.assertEqual(data1[0].text.strip(), 
                            base64.encodestring("first value").strip())
        data2 = itemList[1]
        self.assertEqual(data2.tag, BACLAVA.dataElement)
        self.assertEqual(data2.attrib["index"], "1")
        self.assertEqual(data2[0].tag, BACLAVA.dataElementData)
        self.assertEqual(data2[0].text.strip(), 
                            base64.encodestring("second value").strip())

    def testDeepInputList(self):
        root = baclava.make_input_elem(
                    {"in": 
                      [["first value", "second value"], 
                       ["third value"]]})
        thing = root[0]
        self.assertEqual(thing[0].attrib["syntactictype"],
                        "l(l('text/plain'))")
        partial = thing[0][1]
        self.assertEqual(partial.tag, BACLAVA.partialOrder)
        self.assertEqual(partial.attrib["type"], "list")
        partial.attrib["lsid"]
        relation = partial[0][0]
        itemList = partial[1]
        self.assertEqual(itemList.tag, BACLAVA.itemList)
        data1 = itemList[0]
        self.assertEqual(data1.tag, BACLAVA.partialOrder)
        self.assertEqual(data1.attrib["index"], "0")
        self.assertEqual(data1[1][0].tag, BACLAVA.dataElement)
        self.assertEqual(data1[1][0][0].text.strip(), 
                            base64.encodestring("first value").strip())
        self.assertEqual(data1[1][1][0].text.strip(), 
                            base64.encodestring("second value").strip())
        self.assertEqual(itemList[1][1][0][0].text.strip(), 
                            base64.encodestring("third value").strip())

class TestParser(TestCase):
    def testSimple(self):
        outputs = baclava.parse(SINGLE_ELEMENT)
        self.assertEqual(outputs.keys(), ["dbinfo"])
        dbinfo = outputs["dbinfo"] 
        self.assertEqual(dbinfo.mime_types, ["text/plain"])
        self.assertEqual(dbinfo.syntactictype, "'text/plain'")
        self.assertEqual(dbinfo.lsid, "")
        self.assertEqual(dbinfo.data, """linkdb           Database of Link Information
ld               Release 06-05-14, May 06
                 Kyoto University Bioinformatics Center
                 193,321,205 entries
                 Last update:  06/05/14
                 <dblink>
""")
        self.assertEqual(dbinfo.data.lsid, "")

    def testTwoList(self):
        outputs = baclava.parse(TWO_LIST)
        self.assertEqual(set(outputs.keys()), set(("out", "meta")))
        out = outputs["out"]
        self.assertEqual(out.lsid, "")
        self.assertEqual(out.syntactictype, "l('text/plain')")
        self.assertEqual(out.mime_types, ["text/plain"])
        self.assertEqual(out.data, ["Unknown input\n"])
        self.assertEqual(out.data.lsid, "")
        self.assertEqual(out.data[0].lsid, "")
        meta = outputs["meta"]
        self.assertEqual(meta.lsid, "")
        self.assertEqual(meta.syntactictype, "'text/plain,text/html'")
        self.assertEqual(meta.mime_types, ["text/plain", "text/html"])
        self.assertEqual(meta.data,
"""<html><head><title>title</title></head><body><H1>HTML</H1><a href="replacelsid:output">output</a>
<a href="replacelsid:output[0]">output.[0]</a>=Unknown input
 (14 chars)
</body></html>
""")
  
    def testDeepLists(self):
        outputs = baclava.parse(DEEP_LISTS) 
        out = outputs["Output"]
        # From the famous IterationStrategyExample
        # (don't ask me why some have spaces and some don't) 
        self.assertEqual(out.syntactictype, "l(l('text/plain'))")
        self.assertEqual(out.data[0][0], "square red cat")
        self.assertEqual(out.data[0][1], "square greenrabbit")
        self.assertEqual(out.data[1][0], "circular red cat")
        self.assertEqual(out.data[1][1], "circular greenrabbit")
        self.assertEqual(out.data[2][0], "triangularred cat")
        self.assertEqual(out.data[2][1], "triangulargreenrabbit")
        self.assertEqual(out.data.lsid, "")
        self.assertEqual(out.data[0].lsid, "")
        self.assertEqual(out.data[0][0].lsid, "")

    def testEmpty(self):
        outputs = baclava.parse(EMPTY) 
        out = outputs["gene_info"]
        self.assertEqual(out.data.lsid, "")
        self.assertEqual(out.data, [""])
        self.assertEqual(out.data[0].lsid, "")


SINGLE_ELEMENT="""<?xml version="1.0" encoding="UTF-8"?>
<b:dataThingMap xmlns:b="http://org.embl.ebi.escience/baclava/0.1alpha">
  <b:dataThing key="dbinfo">
    <b:myGridDataDocument lsid="" syntactictype="'text/plain'">
      <s:metadata xmlns:s="http://org.embl.ebi.escience/xscufl/0.1alpha">
        <s:mimeTypes>
          <s:mimeType>text/plain</s:mimeType>
        </s:mimeTypes>
      </s:metadata>
      <b:dataElement lsid="">
        <b:dataElementData>bGlua2RiICAgICAgICAgICBEYXRhYmFzZSBvZiBMaW5rIEluZm9ybWF0aW9uCmxkICAgICAgICAg
ICAgICAgUmVsZWFzZSAwNi0wNS0xNCwgTWF5IDA2CiAgICAgICAgICAgICAgICAgS3lvdG8gVW5p
dmVyc2l0eSBCaW9pbmZvcm1hdGljcyBDZW50ZXIKICAgICAgICAgICAgICAgICAxOTMsMzIxLDIw
NSBlbnRyaWVzCiAgICAgICAgICAgICAgICAgTGFzdCB1cGRhdGU6ICAwNi8wNS8xNAogICAgICAg
ICAgICAgICAgIDxkYmxpbms+Cg==</b:dataElementData>
      </b:dataElement>
    </b:myGridDataDocument>
  </b:dataThing>
</b:dataThingMap>
"""

TWO_LIST="""<?xml version="1.0" encoding="UTF-8"?>
<b:dataThingMap xmlns:b="http://org.embl.ebi.escience/baclava/0.1alpha">
  <b:dataThing key="out">
    <b:myGridDataDocument lsid="" syntactictype="l('text/plain')">
      <s:metadata xmlns:s="http://org.embl.ebi.escience/xscufl/0.1alpha">
        <s:mimeTypes>
          <s:mimeType>text/plain</s:mimeType>
        </s:mimeTypes>
      </s:metadata>
      <b:partialOrder lsid="" type="list">
        <b:relationList />
        <b:itemList>
          <b:dataElement lsid="" index="0">
            <b:dataElementData>VW5rbm93biBpbnB1dAo=</b:dataElementData>
          </b:dataElement>
        </b:itemList>
      </b:partialOrder>
    </b:myGridDataDocument>
  </b:dataThing>
  <b:dataThing key="meta">
    <b:myGridDataDocument lsid="" syntactictype="'text/plain,text/html'">
      <s:metadata xmlns:s="http://org.embl.ebi.escience/xscufl/0.1alpha">
        <s:mimeTypes>
          <s:mimeType>text/plain</s:mimeType>
          <s:mimeType>text/html</s:mimeType>
        </s:mimeTypes>
      </s:metadata>
      <b:dataElement lsid="">
        <b:dataElementData>PGh0bWw+PGhlYWQ+PHRpdGxlPnRpdGxlPC90aXRsZT48L2hlYWQ+PGJvZHk+PEgxPkhUTUw8L0gx
PjxhIGhyZWY9InJlcGxhY2Vsc2lkOm91dHB1dCI+b3V0cHV0PC9hPgo8YSBocmVmPSJyZXBsYWNl
bHNpZDpvdXRwdXRbMF0iPm91dHB1dC5bMF08L2E+PVVua25vd24gaW5wdXQKICgxNCBjaGFycykK
PC9ib2R5PjwvaHRtbD4K</b:dataElementData>
      </b:dataElement>
    </b:myGridDataDocument>
  </b:dataThing>
</b:dataThingMap>
"""

DEEP_LISTS="""<?xml version="1.0" encoding="UTF-8"?>
<b:dataThingMap xmlns:b="http://org.embl.ebi.escience/baclava/0.1alpha">
  <b:dataThing key="Output">
    <b:myGridDataDocument lsid="" syntactictype="l(l('text/plain'))">
      <s:metadata xmlns:s="http://org.embl.ebi.escience/xscufl/0.1alpha">
        <s:mimeTypes>
          <s:mimeType>text/plain</s:mimeType>
        </s:mimeTypes>
      </s:metadata>
      <b:partialOrder lsid="" type="list">
        <b:relationList>
          <b:relation parent="0" child="1" />
          <b:relation parent="1" child="2" />
        </b:relationList>
        <b:itemList>
          <b:partialOrder lsid="" type="list" index="0">
            <b:relationList>
              <b:relation parent="0" child="1" />
            </b:relationList>
            <b:itemList>
              <b:dataElement lsid="" index="0">
                <b:dataElementData>c3F1YXJlIHJlZCBjYXQ=</b:dataElementData>
              </b:dataElement>
              <b:dataElement lsid="" index="1">
                <b:dataElementData>c3F1YXJlIGdyZWVucmFiYml0</b:dataElementData>
              </b:dataElement>
            </b:itemList>
          </b:partialOrder>
          <b:partialOrder lsid="" type="list" index="1">
            <b:relationList>
              <b:relation parent="0" child="1" />
            </b:relationList>
            <b:itemList>
              <b:dataElement lsid="" index="0">
                <b:dataElementData>Y2lyY3VsYXIgcmVkIGNhdA==</b:dataElementData>
              </b:dataElement>
              <b:dataElement lsid="" index="1">
                <b:dataElementData>Y2lyY3VsYXIgZ3JlZW5yYWJiaXQ=</b:dataElementData>
              </b:dataElement>
            </b:itemList>
          </b:partialOrder>
          <b:partialOrder lsid="" type="list" index="2">
            <b:relationList>
              <b:relation parent="0" child="1" />
            </b:relationList>
            <b:itemList>
              <b:dataElement lsid="" index="0">
                <b:dataElementData>dHJpYW5ndWxhcnJlZCBjYXQ=</b:dataElementData>
              </b:dataElement>
              <b:dataElement lsid="" index="1">
                <b:dataElementData>dHJpYW5ndWxhcmdyZWVucmFiYml0</b:dataElementData>
              </b:dataElement>
            </b:itemList>
          </b:partialOrder>
        </b:itemList>
      </b:partialOrder>
    </b:myGridDataDocument>
  </b:dataThing>
</b:dataThingMap>
"""

EMPTY="""<?xml version="1.0" encoding="UTF-8"?>
<b:dataThingMap xmlns:b="http://org.embl.ebi.escience/baclava/0.1alpha">
  <b:dataThing key="gene_info">
    <b:myGridDataDocument lsid="" syntactictype="l('text/plain')">
      <s:metadata xmlns:s="http://org.embl.ebi.escience/xscufl/0.1alpha">
        <s:mimeTypes>
          <s:mimeType>text/plain</s:mimeType>
        </s:mimeTypes>
      </s:metadata>
      <b:partialOrder lsid="" type="list">
        <b:relationList />
        <b:itemList>
          <b:dataElement lsid="" index="0">
            <b:dataElementData />
          </b:dataElement>
        </b:itemList>
      </b:partialOrder>
    </b:myGridDataDocument>
  </b:dataThing>
</b:dataThingMap>
"""

BINARY="""
<?xml version="1.0" encoding="UTF-8"?>
<b:dataThingMap xmlns:b="http://org.embl.ebi.escience/baclava/0.1alpha">
  <b:dataThing key="seq">
    <b:myGridDataDocument lsid="" syntactictype="'text/plain,chemical/x-embl-dl-nucleotide'">
      <s:metadata xmlns:s="http://org.embl.ebi.escience/xscufl/0.1alpha">
        <s:mimeTypes>
          <s:mimeType>text/plain</s:mimeType>
          <s:mimeType>chemical/x-embl-dl-nucleotide</s:mimeType>
        </s:mimeTypes>
      </s:metadata>
      <b:dataElement lsid="">
        <b:dataElementData>SUQgICBQRkVCQTE3NSAgIHN0YW5kYXJkOyBETkE7IFVOQzsgNDUwNyBCUC4KQUMgICBYNTI1MjQ7
ClNWICAgWDUyNTI0LjEKREUgICBQbGFzbW9kaXVtIGZhbGNpcGFydW0gZ2VuZSBmb3IgZXJ5dGhy
b2N5dGUtYmluZGluZyBhbnRpZ2VuIEVCQS0xNzUKS1cgICBlcnl0aHJvY3l0ZSBhbnRpZ2VuLgpP
UyAgIFBsYXNtb2RpdW0gZmFsY2lwYXJ1bSAobWFsYXJpYSBwYXJhc2l0ZSBQCk9DICAgZmFsY2lw
YXJ1bSk7IEV1a2FyeW90YTsgQWx2ZW9sYXRhOyBBcGljb21wbGV4YTsgSGFlbW9zcG9yaWRhOyBQ
bGFzbW9kaXVtLgpGSCAgIEtleSAgICAgICAgICAgICBMb2NhdGlvbi9RdWFsaWZpZXJzCkZICkZU
ICAgc291cmNlICAgICAgICAgIDEuLjQ1MDcKRlQgICAgICAgICAgICAgICAgICAgL29yZ2FuaXNt
PSJQbGFzbW9kaXVtIGZhbGNpcGFydW0iCkZUICAgICAgICAgICAgICAgICAgIC9zdHJhaW49IkNh
bXAgTWFsYXlzaWEiCkZUICAgICAgICAgICAgICAgICAgIC9tb2xfdHlwZT0iZ2Vub21pYyBETkEi
CkZUICAgICAgICAgICAgICAgICAgIC9kZXZfc3RhZ2U9ImxhdGUgc2NoaXpvbnQiCkZUICAgICAg
ICAgICAgICAgICAgIC9kYl94cmVmPSJ0YXhvbjo1ODMzIgpGVCAgIENEUyAgICAgICAgICAgICA1
Ni4uNDM2MwpGVCAgICAgICAgICAgICAgICAgICAvcHJvZHVjdD0iRUJBLTE3NSBwcm90ZWluIgpG
VCAgICAgICAgICAgICAgICAgICAvZGJfeHJlZj0iR09BOlAxOTIxNCIKRlQgICAgICAgICAgICAg
ICAgICAgL2RiX3hyZWY9IkludGVyUHJvOklQUjAwODYwMiIKRlQgICAgICAgICAgICAgICAgICAg
L2RiX3hyZWY9IlVuaVByb3RLQi9Td2lzcy1Qcm90OlAxOTIxNCIKRlQgICAgICAgICAgICAgICAg
ICAgL3Byb3RlaW5faWQ9IkNBQTM2NzU2LjEiCkZUICAgICAgICAgICAgICAgICAgIC90cmFuc2xh
dGlvbj0iTUtDTklTSVlGRkFTRkZWTFlGQUtBUk5FWURJS0VORUtGTERWWUtFS0ZORUxECkZUICAg
ICAgICAgICAgICAgICAgIEtLS1lHTlZRS1RES0tJRlRGSUVOS0xESUxOTlNLRk5LUldLU1lHVFBE
TklES05NU0xJTktITk5FRU1GCkZUICAgICAgICAgICAgICAgICAgIE5OTllRU0ZMU1RTU0xJS1FO
S1lWUElOQVZSVlNSSUxTRkxEU1JJTk5HUk5UU1NOTkVWTFNOQ1JFS1JLCkZUICAgICAgICAgICAg
ICAgICAgIEdNS1dEQ0tLS05EUlNOWVZDSVBEUlJJUUxDSVZOTFNJSUtUWVRLRVRNS0RIRklFQVNL
S0VTUUxMTEtLCkZUICAgICAgICAgICAgICAgICAgIE5ETktZTlNLRkNORExLTlNGTERZR0hMQU1H
TkRNREZHR1lTVEtBRU5LSVFFVkZLR0FIR0VJU0VIS0lLCkZUICAgICAgICAgICAgICAgICAgIE5G
UktFV1dORUZSRUtMV0VBTUxTRUhLTk5JTk5DS05JUFFFRUxRSVRRV0lLRVdIR0VGTExFUkROUlNL
CkZUICAgICAgICAgICAgICAgICAgIExQS1NLQ0tOTlRMWUVBQ0VLRUNJRFBDTUtZUkRXSUlSU0tG
RVdIVExTS0VZRVRRS1ZQS0VOQUVOWUxJCkZUICAgICAgICAgICAgICAgICAgIEtJU0VOS05EQUtW
U0xMTE5OQ0RBRVlTS1lDRENLSFRUVExWS1NWTE5HTkROVElLRUtSRUhJRExEREZTCkZUICAgICAg
ICAgICAgICAgICAgIEtGR0NES05TVkRUTlRLVldFQ0tOUFlJTFNUS0RWQ1ZQUFJSUUVMQ0xHTklE
UklZREtOTExNSUtFSElMCkZUICAgICAgICAgICAgICAgICAgIEFJQUlZRVNSSUxLUktZS05LRERL
RVZDS0lJTktURkFESVJESUlHR1REWVdORExTTlJLTFZHS0lOVE5TCkZUICAgICAgICAgICAgICAg
ICAgIEtZVkhSTktLTkRLTEZSREVXV0tWSUtLRFZXTlZJU1dWRktES1RWQ0tFRERJRU5JUFFGRlJX
RlNFV0dECkZUICAgICAgICAgICAgICAgICAgIERZQ1FES1RLTUlFVExLVkVDS0VLUENFREROQ0tT
S0NOU1lLRVdJU0tLS0VFWU5LUUFLUVlRRVlRS0dOCkZUICAgICAgICAgICAgICAgICAgIE5ZS01Z
U0VGS1NJS1BFVllMS0tZU0VLQ1NOTE5GRURFRktFRUxIU0RZS05LQ1RNQ1BFVktEVlBJU0lJCkZU
ICAgICAgICAgICAgICAgICAgIFJOTkVRVFNRRUFWUEVFTlRFSUFIUlRFVFBTSVNFR1BLR05FUUtF
UkRERFNMU0tJU1ZTUEVOU1JQRVRECkZUICAgICAgICAgICAgICAgICAgIEFLRFRTTkxMS0xLR0RW
RElTTVBLQVZJR1NTUE5ETklOVlRFUUdETklTR1ZOU0tQTFNERFZSUERLS0VMCkZUICAgICAgICAg
ICAgICAgICAgIEVEUU5TREVTRUVUVlZOSElTS1NQU0lOTkdERFNHU0dTQVRWU0VTU1NTTlRHTFNJ
REREUk5HRFRGVlJUCkZUICAgICAgICAgICAgICAgICAgIFFEVEFOVEVEVklSS0VOQURLREVERUtH
QURFRVJIU1RTRVNMU1NQRUVLTUxURE5FR0dOU0xOSEVFVktFCkZUICAgICAgICAgICAgICAgICAg
IEhUU05TRE5WUVFTR0dJVk5NTlZFS0VMS0RUTEVOUFNTU0xERUdLQUhFRUxTRVBOTFNTRFFETVNO
VFBHCkZUICAgICAgICAgICAgICAgICAgIFBMRE5UU0VFVFRFUklTTk5FWUtWTkVSRURFUlRMVEtF
WUVESVZMS1NITU5SRVNEREdFTFlERU5TRExTCkZUICAgICAgICAgICAgICAgICAgIFRWTkRFU0VE
QUVBS01LR05EVFNFTVNITlNTUUhJRVNEUVFLTkRNS1RWR0RMR1RUSFZRTkVJU1ZQVlRHCkZUICAg
ICAgICAgICAgICAgICAgIEVJREVLTFJFU0tFU0tJSEtBRUVFUkxTSFRESUhLSU5QRURSTlNOVExI
TEtESVJORUVORVJITFROUU5JCkZUICAgICAgICAgICAgICAgICAgIE5JU1FFUkRMUUtIR0ZIVE1O
TkxIR0RHVlNFUlNRSU5IU0hIR05SUURSR0dOU0dOVkxOTVJTTk5OTkZOCkZUICAgICAgICAgICAg
ICAgICAgIE5JUFNSWU5MWURLS0xETERMWUVOUk5EU1RUS0VMSUtLTEFFSU5LQ0VORUlTVktZQ0RI
TUlIRUVJUExLCkZUICAgICAgICAgICAgICAgICAgIFRDVEtFS1RSTkxDQ0FWU0RZQ01TWUZUWURT
RUVZWU5DVEtSRUZERFBTWVRDRlJLRUFGU1NNSUZLRkxJCkZUICAgICAgICAgICAgICAgICAgIFRO
S0lZWVlGWVRZS1RBS1ZUSUtLSU5GU0xJRkZGRkZTRiIKU1EgICBTZXF1ZW5jZSA0NTA3IEJQOyAx
OTU1IEE7IDQ1OCBDOyA3OTYgRzsgMTI5OCBUOyAwIG90aGVyOwogICAgIHRhdGF0YXRhdGEgdGF0
YXRhdGF0YSBnYXRhYXRhYWNhIHRhdGFhYXRhdGEgdHRjYWF0Z3RnYyBhdGFjYWF0Z2FhICAgICAg
ICA2MAogICAgIGF0Z3RhYXRhdHQgYWd0YXRhdGF0dCB0dHR0dGdjdHRjIGN0dGN0dHRndGcgdHRh
dGF0dHR0ZyBjYWFhYWdjdGFnICAgICAgIDEyMAogICAgIGdhYXRnYWF0YXQgZ2F0YXRhYWFhZyBh
Z2FhdGdhYWFhIGF0dHR0dGFnYWMgZ3RndGF0YWFhZyBhYWFhYXR0dGFhICAgICAgIDE4MAogICAg
IHRnYWF0dGFnYXQgYWFhYWFnYWFhdCBhdGdnYWFhdGd0IHRjYWFhYWFhY3QgZ2F0YWFnYWFhYSB0
YXR0dGFjdHR0ICAgICAgIDI0MAogICAgIHRhdGFnYWFhYXQgYWFhdHRhZ2F0YSB0dHR0YWFhdGFh
IHR0Y2FhYWF0dHQgYWF0YWFhYWdhdCBnZ2FhZ2FndHRhICAgICAgIDMwMAogICAgIHRnZ2FhY3Rj
Y2EgZ2F0YWF0YXRhZyBhdGFhYWFhdGF0IGd0Y3R0dGFhdGEgYWF0YWFhY2F0YSBhdGFhdGdhYWdh
ICAgICAgIDM2MAogICAgIGFhdGd0dHRhYWMgYWFjYWF0dGF0YyBhYXRjYXR0dHR0IGF0Y2dhY2Fh
Z3QgdGNhdHRhYXRhYSBhZ2NhYWFhdGFhICAgICAgIDQyMAogICAgIGF0YXRndHRjY3QgYXR0YWFj
Z2N0ZyB0YWNndGd0Z3RjIHRhZ2dhdGF0dGEgYWd0dHRjY3RnZyBhdHRjdGFnYWF0ICAgICAgIDQ4
MAogICAgIHRhYXRhYXRnZ2EgYWdhYWF0YWN0dCBjYXRjdGFhdGFhIGNnYWFndHR0dGEgYWd0YWF0
dGd0YSBnZ2dhYWFhYWFnICAgICAgIDU0MAogICAgIGdhYWFnZ2FhdGcgYWFhdGdnZ2F0dCBndGFh
YWFhZ2FhIGFhYXRnYXRhZ2EgYWdjYWFjdGF0ZyB0YXRndGF0dGNjICAgICAgIDYwMAogICAgIHRn
YXRjZ3RhZ2EgYXRjY2FhdHRhdCBnY2F0dGd0dGFhIHRjdHRhZ2NhdHQgYXR0YWFhYWNhdCBhdGFj
YWFhYWdhICAgICAgIDY2MAogICAgIGdhY2NhdGdhYWcgZ2F0Y2F0dHRjYSB0dGdhYWdjY3RjIHRh
YWFhYWFnYWEgdGN0Y2FhY3R0dCB0Z2N0dGFhYWFhICAgICAgIDcyMAogICAgIGFhYXRnYXRhYWMg
YWFhdGF0YWF0dCBjdGFhYXR0dHRnIHRhYXRnYXR0dGcgYWFnYWF0YWd0dCB0dHR0YWdhdHRhICAg
ICAgIDc4MAogICAgIHRnZ2FjYXRjdHQgZ2N0YXRnZ2dhYSBhdGdhdGF0Z2dhIHR0dHRnZ2FnZ3Qg
dGF0dGNhYWN0YSBhZ2djYWdhYWFhICAgICAgIDg0MAogICAgIGNhYWFhdHRjYWEgZ2FhZ3R0dHR0
YSBhYWdnZ2djdGNhIHRnZ2dnYWFhdGEgYWd0Z2FhY2F0YSBhYWF0dGFhYWFhICAgICAgIDkwMAog
ICAgIHR0dHRhZ2FhYWEgZ2FhdGdndGdnYSBhdGdhYXR0dGFnIGFnYWdhYWFjdHQgdGdnZ2FhZ2N0
YSB0Z3R0YXRjdGdhICAgICAgIDk2MAogICAgIGdjYXRhYWFhYXQgYWF0YXRhYWF0YSBhdHRndGFh
YWFhIHRhdHRjY2NjYWEgZ2FhZ2FhdHRhYyBhYWF0dGFjdGNhICAgICAgMTAyMAogICAgIGF0Z2dh
dGFhYWEgZ2FhdGdnY2F0ZyBnYWdhYXR0dHR0IGdjdHRnYWFhZ2EgZ2F0YWF0YWdhdCBjYWFhYXR0
Z2NjICAgICAgMTA4MAogICAgIGFhYWFhZ3RhYWEgdGd0YWFhYWF0YSBhdGFjYXR0YXRhIHRnYWFn
Y2F0Z3QgZ2FnYWFnZ2FhdCBndGF0dGdhdGNjICAgICAgMTE0MAogICAgIGF0Z3RhdGdhYWEgdGF0
YWdhZ2F0dCBnZ2F0dGF0dGFnIGFhZ3RhYWF0dHQgZ2FhdGdnY2F0YSBjZ3R0YXRjZ2FhICAgICAg
MTIwMAogICAgIGFnYWF0YXRnYWEgYWN0Y2FhYWFhZyB0dGNjYWFhZ2dhIGFhYXRnY2dnYWEgYWF0
dGF0dHRhYSB0Y2FhYWF0dHRjICAgICAgMTI2MAogICAgIGFnYWFhYWNhYWcgYWF0Z2F0Z2N0YSBh
YWd0YWFndHR0IGF0dGF0dGdhYXQgYWF0dGd0Z2F0ZyBjdGdhYXRhdHRjICAgICAgMTMyMAogICAg
IGFhYWF0YXR0Z3QgZ2F0dGd0YWFhYyBhdGFjdGFjdGFjIHRjdGNndHRhYWEgYWdjZ3R0dHRhYSBh
dGdndGFhY2dhICAgICAgMTM4MAogICAgIGNhYXRhY2FhdHQgYWFnZ2FhYWFnYyBndGdhYWNhdGF0
IHRnYXR0dGFnYXQgZ2F0dHR0dGN0YSBhYXR0dGdnYXRnICAgICAgMTQ0MAogICAgIHRnYXRhYWFh
YXQgdGNjZ3R0Z2F0YSBjYWFhY2FjYWFhIGdndGd0Z2dnYWEgdGd0YWFhYWFjYyBjdHRhdGF0YXR0
ICAgICAgMTUwMAogICAgIGF0Y2NhY3RhYWEgZ2F0Z3RhdGd0ZyB0YWNjdGNjZ2FnIGdhZ2djYWFn
YWEgdHRhdGd0Y3R0ZyBnYWFhY2F0dGdhICAgICAgMTU2MAogICAgIHRhZ2FhdGF0YWMgZ2F0YWFh
YWFjYyB0YXR0YWF0Z2F0IGFhYWFnYWdjYXQgYXR0Y3R0Z2N0YSB0dGdjYWF0YXRhICAgICAgMTYy
MAogICAgIHRnYWF0Y2FhZ2EgYXRhdHRnYWFhYyBnYWFhYXRhdGFhIGdhYXRhYWFnYXQgZ2F0YWFh
Z2FhZyB0dHRndGFhYWF0ICAgICAgMTY4MAogICAgIGNhdGFhYXRhYWEgYWN0dHRjZ2N0ZyBhdGF0
YWFnYWdhIHRhdHRhdGFnZ2EgZ2d0YWN0Z2F0dCBhdHRnZ2FhdGdhICAgICAgMTc0MAogICAgIHR0
dGdhZ2NhYXQgYWdhYWFhdHRhZyB0YWdnYWFhYWF0IHRhYWNhY2FhYXQgdGNhYWFhdGF0ZyB0dGNh
Y2FnZ2FhICAgICAgMTgwMAogICAgIHRhYWFhYWFhYXQgZ2F0YWFnY3R0dCB0dGNndGdhdGdhIGd0
Z2d0Z2dhYWEgZ3R0YXR0YWFhYSBhYWdhdGd0YXRnICAgICAgMTg2MAogICAgIGdhYXRndGdhdGEg
dGNhdGdnZ3RhdCB0Y2FhZ2dhdGFhIGFhY3RndHR0Z3QgYWFhZ2FhZ2F0ZyBhdGF0dGdhYWFhICAg
ICAgMTkyMAogICAgIHRhdGFjY2FjYWEgdHRjdHRjYWdhdCBnZ3R0dGFndGdhIGF0Z2dnZ3RnYXQg
Z2F0dGF0dGdjYyBhZ2dhdGFhYWFjICAgICAgMTk4MAogICAgIGFhYWFhdGdhdGEgZ2FnYWN0Y3Rn
YSBhZ2d0dGdhYXRnIGNhYWFnYWFhYWEgY2N0dGd0Z2FhZyBhdGdhY2FhdHRnICAgICAgMjA0MAog
ICAgIHRhYWFhZ3RhYWEgdGd0YWF0dGNhdCBhdGFhYWdhYXRnIGdhdGF0Y2FhYWEgYWFhYWFhZ2Fh
ZyBhZ3RhdGFhdGFhICAgICAgMjEwMAogICAgIGFjYWFnY2NhYWEgY2FhdGFjY2FhZyBhYXRhdGNh
YWFhIGFnZ2FhYXRhYXQgdGFjYWFhYXRndCBhdHRjdGdhYXR0ICAgICAgMjE2MAogICAgIHRhYWF0
Y3RhdGEgYWFhY2NhZ2FhZyB0dHRhdHR0YWFhIGdhYWF0YWN0Y2cgZ2FhYWFhdGd0dCBjdGFhY2N0
YWFhICAgICAgMjIyMAogICAgIHR0dGNnYWFnYXQgZ2FhdHR0YWFnZyBhYWdhYXR0YWNhIHR0Y2Fn
YXR0YXQgYWFhYWF0YWFhdCBndGFjZ2F0Z3RnICAgICAgMjI4MAogICAgIHRjY2FnYWFndGEgYWFn
Z2F0Z3RhYyBjYWF0dHRjdGF0IGFhdGFhZ2FhYXQgYWF0Z2FhY2FhYSBjdHRjZ2NhYWdhICAgICAg
MjM0MAogICAgIGFnY2FndHRjY3QgZ2FnZ2FhYWFjYSBjdGdhYWF0YWdjIGFjYWNhZ2FhY2cgZ2Fh
YWN0Y2NhdCBjdGF0Y3RjdGdhICAgICAgMjQwMAogICAgIGFnZ2FjY2FhYWEgZ2dhYWF0Z2FhYyBh
YWFhYWdhYWNnIHRnYXRnYWNnYXQgYWd0dHRnYWd0YSBhYWF0YWFndGd0ICAgICAgMjQ2MAogICAg
IGF0Y2FjY2FnYWEgYWF0dGNhYWdhYyBjdGdhYWFjdGdhIHRnY3RhYWFnYXQgYWN0dGN0YWFjdCB0
Z3R0YWFhYXR0ICAgICAgMjUyMAogICAgIGFhYWFnZ2FnYXQgZ3R0Z2F0YXR0YSBndGF0Z2NjdGFh
IGFnY2FndHRhdHQgZ2dnYWdjYWd0YyBjdGFhdGdhdGFhICAgICAgMjU4MAogICAgIHRhdGFhYXRn
dHQgYWN0Z2FhY2FhZyBnZ2dhdGFhdGF0IHR0Y2NnZ2dndGcgYWF0dGN0YWFhYyBjdHR0YXRjdGdh
ICAgICAgMjY0MAogICAgIHRnYXRndGFjZ3QgY2NhZ2F0YWFhYSBhZ2dhYXR0YWdhIGFnYXRjYWFh
YXQgYWd0Z2F0Z2FhdCBjZ2dhYWdhYWFjICAgICAgMjcwMAogICAgIHRndGFndGFhYXQgY2F0YXRh
dGNhYSBhYWFndGNjYXRjIHRhdGFhYXRhYXQgZ2dhZ2F0Z2F0dCBjYWdnY2FndGdnICAgICAgMjc2
MAogICAgIGFhZ3RnY2FhY2EgZ3RnYWd0Z2FhdCBjdGFndGFndHRjIGFhYXRhY3RnZ2EgdHRndGN0
YXR0ZyBhdGdhdGdhdGFnICAgICAgMjgyMAogICAgIGFhYXRnZ3RnYXQgYWNhdHR0Z3R0YyBnYWFj
YWNhYWdhIHRhY2FnY2FhYXQgYWN0Z2FhZ2F0ZyB0dGF0dGFnYWFhICAgICAgMjg4MAogICAgIGFn
YWFhYXRnY3QgZ2FjYWFnZ2F0ZyBhYWdhdGdhYWFhIGFnZ2NnY2FnYXQgZ2FhZ2FhYWdhYyBhdGFn
dGFjdHRjICAgICAgMjk0MAogICAgIHRnYWFhZ2N0dGEgYWd0dGNhY2N0ZyBhYWdhYWFhYWF0IGd0
dGFhY3RnYXQgYWF0Z2FhZ2dhZyBnYWFhdGFndHR0ICAgICAgMzAwMAogICAgIGFhYXRjYXRnYWEg
Z2FnZ3RnYWFhZyBhYWNhdGFjdGFnIHRhYXR0Y3RnYXQgYWF0Z3R0Y2FhYyBhZ3RjdGdnYWdnICAg
ICAgMzA2MAogICAgIGFhdHRndHRhYXQgYXRnYWF0Z3R0ZyBhZ2FhYWdhYWN0IGFhYWFnYXRhY3Qg
dHRhZ2FhYWF0YyBjdHRjdGFndGFnICAgICAgMzEyMAogICAgIGN0dGdnYXRnYWEgZ2dhYWFhZ2Nh
YyBhdGdhYWdhYXR0IGF0Y2FnYWFjY2EgYWF0Y3RhYWdjYSBndGdhY2NhYWdhICAgICAgMzE4MAog
ICAgIHRhdGd0Y3RhYXQgYWNhY2N0Z2dhYyBjdHR0Z2dhdGFhIGNhY2NhZ3RnYWEgZ2FhYWN0YWNh
ZyBhYWFnYWF0dGFnICAgICAgMzI0MAogICAgIHRhYXRhYXRnYWEgdGF0YWFhZ3R0YSBhY2dhZ2Fn
Z2dhIGFnYXRnYWdhZ2EgYWNnY3R0YWN0YSBhZ2dhYXRhdGdhICAgICAgMzMwMAogICAgIGFnYXRh
dHRndHQgdHRnYWFhYWd0YyBhdGF0Z2FhdGFnIGFnYWF0Y2FnYWMgZ2F0Z2d0Z2FhdCB0YXRhdGdh
Y2dhICAgICAgMzM2MAogICAgIGFhYXR0Y2FnYWMgdHRhdGN0YWN0ZyB0YWFhdGdhdGdhIGF0Y2Fn
YWFnYWMgZ2N0Z2FhZ2NhYSBhYWF0Z2FhYWdnICAgICAgMzQyMAogICAgIGFhYXRnYXRhY2EgdGN0
Z2FhYXRndCBjZ2NhdGFhdGFnIHRhZ3RjYWFjYXQgYXR0Z2FnYWd0ZyBhdGNhYWNhZ2FhICAgICAg
MzQ4MAogICAgIGFhYWNnYXRhdGcgYWFhYWN0Z3R0ZyBndGdhdHR0Z2dnIGFhY2NhY2FjYXQgZ3Rh
Y2FhYWFjZyBhYWF0dGFndGd0ICAgICAgMzU0MAogICAgIHRjY3RndHRhY2EgZ2dhZ2FhYXR0ZyBh
dGdhYWFhYXR0IGFhZ2dnYWFhZ3QgYWFhZ2FhdGNhYSBhYWF0dGNhdGFhICAgICAgMzYwMAogICAg
IGdnY3RnYWFnYWcgZ2FhYWdhdHRhYSBndGNhdGFjYWdhIHRhdGFjYXRhYWEgYXR0YWF0Y2N0ZyBh
YWdhdGFnYWFhICAgICAgMzY2MAogICAgIHRhZ3RhYXRhY2EgdHRhY2F0dHRhYSBhYWdhdGF0YWFn
IGFhYXRnYWdnYWEgYWFjZ2FhYWdhYyBhY3R0YWFjdGFhICAgICAgMzcyMAogICAgIHRjYWFhYWNh
dHQgYWF0YXR0YWd0YyBhYWdhYWFnZ2dhIHR0dGdjYWFhYWEgY2F0Z2dhdHRjYyBhdGFjY2F0Z2Fh
ICAgICAgMzc4MAogICAgIHRhYXRjdGFjYXQgZ2dhZ2F0Z2dhZyB0dHRjY2dhYWFnIGFhZ3RjYWFh
dHQgYWF0Y2F0YWd0YyBhdGNhdGdnYWFhICAgICAgMzg0MAogICAgIGNhZ2FjYWFnYXQgY2dnZ2dn
Z2dhYSBhdHRjdGdnZ2FhIHRndHR0dGFhYXQgYXRnYWdhdGN0YSBhdGFhdGFhdGFhICAgICAgMzkw
MAogICAgIHR0dHRhYXRhYXQgYXR0Y2NhYWd0YSBnYXRhdGFhdHR0IGF0YXRnYXRhYWEgYWFhdHRh
Z2F0dCB0YWdhdGN0dHRhICAgICAgMzk2MAogICAgIHRnYWFhYWNhZ2EgYWF0Z2F0YWd0YSBjYWFj
YWFhYWdhIGF0dGFhdGFhYWcgYWFhdHRhZ2NhZyBhYWF0YWFhdGFhICAgICAgNDAyMAogICAgIGF0
Z3RnYWdhYWMgZ2FhYXR0dGN0ZyB0YWFhYXRhdHRnIHRnYWNjYXRhdGcgYXR0Y2F0Z2FhZyBhYWF0
Y2NjYXR0ICAgICAgNDA4MAogICAgIGFhYWFhY2F0Z2MgYWN0YWFhZ2FhYSBhYWFjYWFnYWFhIHRj
dGd0Z3R0Z3QgZ2NhZ3RhdGNhZyBhdHRhY3RndGF0ICAgICAgNDE0MAogICAgIGdhZ2N0YXR0dHQg
YWNhdGF0Z2F0dCBjYWdhZ2dhYXRhIHR0YXRhYXR0Z3QgYWNnYWFhYWdnZyBhYXR0dGdhdGdhICAg
ICAgNDIwMAogICAgIHRjY2F0Y3R0YXQgYWNhdGd0dHRjYSBnYWFhZ2dhZ2djIHR0dHR0Y2FhZ3Qg
YXRnYXRhdHRjYSBhYXR0dHR0YWF0ICAgICAgNDI2MAogICAgIGFhY2FhYXRhYWEgYXRhdGF0dGF0
dCBhdHR0dHRhdGFjIHR0YWNhYWFhY3QgZ2NhYWFhZ3RhYSBjYWF0YWFhYWFhICAgICAgNDMyMAog
ICAgIGFhdHRhYXR0dGMgdGNhdHRhYXR0dCB0dHR0dHR0Y3R0IHR0dHR0Y3R0dHQgdGFnZ3RhdGdj
YyBhdGF0dGF0Z2NhICAgICAgNDM4MAogICAgIGdnYWdjYWdndGcgdGd0dGF0dHRhdCB0YXRhdHRn
Z3R0IGF0dHR0YWdndGcgY3R0Y2FjYWFnYyBjYWFhdGF0Y2FhICAgICAgNDQ0MAogICAgIGFnZ3R0
YWdhYWEgYWFhdGFhYXRhYSBhYWF0YWFhYXR0IGdhZ2FhZ2FhdGcgdGFhYXR0YWFhdCBhdGFnYWF0
dGNnICAgICAgNDUwMAogICAgIGFnY3RjZ2cgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAg
ICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgNDUwNwovLwo=</b:dataElementData>
      </b:dataElement>
    </b:myGridDataDocument>
  </b:dataThing>
</b:dataThingMap>
"""


if __name__ == "__main__":
    import unittest
    unittest.main()
          

