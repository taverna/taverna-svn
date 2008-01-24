

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