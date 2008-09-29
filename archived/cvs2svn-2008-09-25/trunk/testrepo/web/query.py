#!/usr/bin/env python
import annotate

import sys

import RDF


def main():

    model = annotate.get_rdf_model()

    for file in sys.argv[1:]:
        query = open(file).read()
        lang = file.split(".")[-1]
        q = RDF.Query(query, query_language=lang) 
        for row in q.execute(model):
            for key,value in row.iteritems():
                print "%s=%s" % (key,value),
            print
    


if __name__ == "__main__":
    main()

