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


CLASSP=$TAVERNA_HOME/resources:$TAVERNA_HOME/conf

for i in $TAVERNA_HOME/lib/*.jar
do
  CLASSP=$CLASSP:$i
done
for i in $TAVERNA_HOME/plugins/*.jar
do
  CLASSP=$CLASSP:$i
done

java -classpath $CLASSP -Dtaverna.scrollDesktop -ea org.embl.ebi.escience.scuflui.workbench.Workbench