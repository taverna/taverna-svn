#!/bin/sh
REP=/Users/stain/Documents/workspace/www.mygrid.org.uk/maven/snapshot-repository

mvn clean install deploy -DaltDeploymentRepository=svn-repository::default::file://$REP
