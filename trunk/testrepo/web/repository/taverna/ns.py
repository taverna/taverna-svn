
class NS(object):
    NS = ""
    tags = ""
    class __metaclass__(type):
        def __new__(cls, name, bases, dict):
            if not ("tags" in dict and "NS" in dict):
                raise "Cannot create NS class without defining tags and NS"
            ns = dict["NS"]
            for tag in dict["tags"].split():
                dict[tag] = "{%s}%s" % (ns, tag)
            return type.__new__(cls, name, bases, dict)


class XSCUFL(NS):
    NS = "http://org.embl.ebi.escience/xscufl/0.1alpha"
    tags = """sink source workflowdescription metadata mimeTypes mimeType
              processor description soaplabwsdl iterationstrategy"""

class BACLAVA(NS):
    NS = "http://org.embl.ebi.escience/baclava/0.1alpha"
    tags = """dataThingMap dataThing myGridDataDocument dataElement
              dataElementData partialOrder relationList relation itemList"""
