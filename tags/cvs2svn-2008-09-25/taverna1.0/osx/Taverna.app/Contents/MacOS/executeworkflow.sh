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
  
TAVERNA_HOME=`dirname "$PRG"`/../Resources/Java
cd "$saveddir"

ARGS="-Xmx300m"
ARGS="$ARGS -Djava.system.class.loader=net.sf.taverna.tools.BootstrapClassLoader"
ARGS="$ARGS -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client"
ARGS="$ARGS -Djava.awt.headless=true"
ARGS="$ARGS -Draven.target.groupid=uk.org.mygrid.taverna.scufl "
ARGS="$ARGS -Draven.target.artifactid=scufl-tools "
ARGS="$ARGS -Draven.target.class=org.embl.ebi.escience.scufl.tools.WorkflowLauncher "
ARGS="$ARGS -Draven.target.method=main"

java $ARGS -jar $TAVERNA_HOME/taverna-bootstrap-1.5.0.jar $@
