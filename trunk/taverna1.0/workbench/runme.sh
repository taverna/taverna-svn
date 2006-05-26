#!/bin/sh

## resolve links - $0 may be a symlink
PRG="$0"
progname=`basename "$0"`
saveddir=`pwd`

# need this for relative symlinks
cd `dirname "$PRG"`
  
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '.*/.*' > /dev/null; then
	PRG="$link"
    else
	PRG=`dirname "$PRG"`"/$link"
    fi
done
  
TAVERNA_HOME=`dirname "$PRG"`

cd "$saveddir"

# make it fully qualified
TAVERNA_HOME=`cd "$TAVERNA_HOME" && pwd`


LAUNCHER=$(echo $TAVERNA_HOME/taverna-launcher*.jar)
CLASSP=$TAVERNA_HOME/resources:$TAVERNA_HOME/conf:$LAUNCHER

for i in $TAVERNA_HOME/libext/*.jar
do
  CLASSP=$CLASSP:$i
done

case "`uname`" in
  CYGWIN*) CLASSP=`cygpath --path --type windows $CLASSP`;;
esac

java -Xmx300m -classpath $CLASSP -Djava.protocol.handler.pkgs=uk.ac.rdg.resc.jstyx.client -Dtaverna.home=$TAVERNA_HOME -ea net.sf.taverna.tools.Launcher
