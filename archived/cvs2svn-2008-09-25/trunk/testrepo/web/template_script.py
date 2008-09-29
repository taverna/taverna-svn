#!/usr/bin/env python
import pkg_resources
pkg_resources.require("TurboGears")

import turbogears
import cherrypy
cherrypy.lowercase_api = True

import os
import sys

# first look on the command line for a desired config file,
# if it's not on the command line, then
# look for setup.py in this directory. If it's not there, this script is
# probably installed
#if len(sys.argv) > 1:
#    turbogears.update_config(configfile=sys.argv[1], 
#        modulename="repository.config")
base = os.path.dirname(__file__)
if os.path.exists(os.path.join(base, "setup.py")):
    turbogears.update_config(configfile=os.path.join(base, "dev.cfg"),
        modulename="repository.config")
else:
    turbogears.update_config(configfile=os.path.join(base, "prod.cfg"),
        modulename="repository.config")

from repository import model


def main():
    pass 


if __name__ == "__main__":
    main()


