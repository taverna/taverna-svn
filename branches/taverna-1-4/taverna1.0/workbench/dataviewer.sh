#!/bin/sh

## resolve links - $0 may be a symlink
PRG="$0"
progname=`basename "$0"`
saveddir=`pwd`

# need this for relative symlinks
cd $(dirname "$PRG")
while [ -h "$PRG" ] ; do
    ls=$(ls -ld "$PRG")
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '.*/.*' > /dev/null; then
	PRG="$link"
    else
	PRG=`dirname "$PRG"`"/$link"
    fi
done
  
TAVERNA_HOME=`dirname "$PRG"`
cd "$saveddir"

MAIN=org.embl.ebi.escience.baclava.tools.DataThingViewer

sh $TAVERNA_HOME/runme.sh -Dtaverna.scrollDesktop -Dtaverna.main=$MAIN -jar $@
