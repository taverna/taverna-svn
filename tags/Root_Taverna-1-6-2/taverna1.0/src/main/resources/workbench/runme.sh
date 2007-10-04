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

#uncomment and complete the next line and complete to change the directory taverna downloads jars to
#ARGS="$ARGS -Dtaverna.repository=<directory>"

#NB: Proxy configuration settings have now been reverted to being in conf/mygrid.properties

# Load customised properties if they exist
if [ -f "$TAVERNA_HOME/custom.sh" ] ; then
    source "$TAVERNA_HOME/custom.sh"
fi

java $ARGS -jar "$TAVERNA_HOME/taverna-bootstrap-1.6-SNAPSHOT.jar" $@
