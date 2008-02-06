#!/usr/bin/env python

import ez_setup
ez_setup.use_setuptools()

from setuptools import setup, find_packages

setup(
    name = "tavernaclient",
    version = "0.4.0",
    packages = ["tavernaclient"],
    package_dir = {"": "src"},
    zip_safe = True,
    install_requires = ['elementtree>=1.2'],
    test_suite = "test",

    # metadata for upload to PyPI
    author = "Stian Soiland, David Withers",
    author_email = "taverna-hackers@lists.sourceforge.net",
    description = "Taverna Remote Execution service client",
    license = "LGPL", # Lesser GNU Public License
    keywords = "taverna rest client remote execution",
    url = "http://taverna.sourceforge.net/",   
)
