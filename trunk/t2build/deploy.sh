#!/bin/bash

# Script for deploying to a subversion checked out repository

# Check out the full maven repository (about 1.3 GB ..) using
# svn checkout svn+ssh://rosalind.cs.man.ac.uk/local/svn/www/mygrid/maven
# and modify REP below to note the path to where it is checked out
# on your machine

# Then for each deploy, first on Rosalind make sure there are no changes 
# to the repository not yet checked in: (this is done by the script)

# ssoiland@rosalind-) cd /local/www/mygrid/maven/
# ssoiland@rosalind-) pwd
# /local/www/mygrid/maven
# ssoiland@rosalind-) ls
# repository  snapshot-repository
# ssoiland@rosalind-) svn up && svn stat
# At revision 16.

# If there are any changes, use "svn add --force ."  and "svn commit"
# on rosalind to commit those changes, then this script will pick this
# up in it's initial svn update in your local copy $REP

# After running this script, repeat the procedure on rosalind to expose
# the newly committed artifacts in the official maven repositories
# at http://www.mygrid.org.uk/maven/  (also done by script)


# Variables

PROJECT=$HOME/Documents/workspace/t2build
REP=$HOME/Documents/workspace/www.mygrid.org.uk/maven/snapshot-repository

SERVER=rosalind.cs.man.ac.uk
SERVER_PUB_PATH=/local/www/mygrid/maven/snapshot-repository


# SCRIPT BEGINS

# exit on any error
set -e 


echo "Checking for uncommited changes in $SERVER_PUB_PATH on $SERVER"
stat=`ssh $SERVER "svn stat $SERVER_PUB_PATH"`
if [ -n "$stat" ] ; then
	echo $stat
	echo ""
	echo "Uncommited updates on server, ssh $SERVER and do:"
	echo "  cd $SERVER_PUB_PATH"
	echo "  svn add --force ."
	echo "  svn commit ."
	exit 1
fi


echo "Running update in $SERVER_PUB_PATH on $SERVER before deploy"
ssh $SERVER "svn update $SERVER_PUB_PATH"


echo "Checking for uncommited changes on local $REP"
stat=`svn stat $REP`
if [ -n "$stat" ] ; then
	echo $stat
	echo ""
	echo "Uncommited updates on client, do:"
	echo "  cd $REP"
	echo "  svn add --force ."
	echo "  svn commit ."
	exit 1
fi



echo "Update of local repository checkout"
cd $REP
svn update

echo "Building $PROJECT"
cd $PROJECT
mvn clean 
# If the tests fails, exit before we've done any deploying
mvn test

echo "Deploying locally from $PROJECT to $REP"
# Don't need to run tests again
mvn -Dmaven.test.skip=true deploy -DaltDeploymentRepository=svn-repository::default::file://$REP


echo "Committing locally deployed artifacts"
cd $REP
svn add --force .

date=`date +%Y-%m-%d`
svn commit -m "Deploy as of $date" .

echo "Committed, running update on $SERVER"
ssh $SERVER "svn update $SERVER_PUB_PATH"

echo "Deployed to $SERVER:$SERVER_PUB_PATH"

