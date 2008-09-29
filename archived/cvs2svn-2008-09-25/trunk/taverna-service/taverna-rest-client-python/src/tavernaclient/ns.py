"""
Namespaces for ElementTree.

Author: Stian Soiland
Copyright: 2006-2008 University of Manchester, UK
URL: http://taverna.sourceforge.net/
Contact: taverna-hackers@lists.sourceforge.net
Licence: LGPL 3 (See LICENCE or http://www.gnu.org/licenses/lgpl.html)
"""

class Namespace(object):
    def __init__(self, namespace):
        self.namespace = namespace
    
    def get(self, tag):
        return "{%s}%s" % (self.namespace, tag)
    
    def __getattribute__(self, tag):
        try:
            return super(Namespace, self).__getattribute__(tag)
        except AttributeError:
            return self.get(tag)