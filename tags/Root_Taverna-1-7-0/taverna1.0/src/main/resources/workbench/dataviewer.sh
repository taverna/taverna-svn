#!/bin/sh

## resolve links - $0 may be a symlink
PRG="$0"
progname=`basename "$0"`
saveddir=`pwd`

# need this for relative symlinks
cd "$(dirname "$PRG")"
while [ -h "$PRG" ] ; do
    ls=$(ls -ld "$PRG")
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '.*/.*' > /dev/null; then
	PRG="$link"
    else
	PRG=`dirname "$PRG"`"/$link"
    fi
done
  
TAVERNA_HOME="`dirname "$PRG"`"
cd "$saveddir"

ARGS="-Xmx300m"
ARGS="$ARGS -Djava.system.class.loader=net.sf.taverna.tools.BootstrapClassLoader"
ARGS="$ARGS -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client"
ARGS="$ARGS -Draven.target.groupid=uk.org.mygrid.taverna.baclava "
ARGS="$ARGS -Draven.target.artifactid=baclava-tools "
ARGS="$ARGS -Draven.target.class=org.embl.ebi.escience.baclava.tools.DataThingViewer "
ARGS="$ARGS -Draven.target.method=main"

# Load customised properties if they exist
if [ -f "$TAVERNA_HOME/custom.sh" ] ; then
    source "$TAVERNA_HOME/custom.sh"
fi

java $ARGS -jar "$TAVERNA_HOME/taverna-bootstrap-1.7.0.0.jar" $@

