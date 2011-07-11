#!/bin/sh

set -e

## resolve links - $0 may be a symlink
prog="$0"

real_path() {
    readlink -m "$1" 2>/dev/null || python -c 'import os,sys;print os.path.realpath(sys.argv[1])' "$1"
}

realprog=`real_path "$prog"`
TAVERNA_DATAVIEWER_HOME=`dirname "$realprog"`

# 300 MB memory, 140 MB for classes
exec java -Xmx300m -XX:MaxPermSize=140m \
  "-Draven.profile=file://$TAVERNA_DATAVIEWER_HOME/conf/current-profile.xml" \
  "-Dtaverna.startup=$TAVERNA_DATAVIEWER_HOME" \
  -Djava.system.class.loader=net.sf.taverna.raven.prelauncher.BootstrapClassLoader \
  -Draven.launcher.app.main=net.sf.taverna.dataviewer.DataViewerTool \
  -Draven.launcher.show_splashscreen=false \
  -jar "$TAVERNA_DATAVIEWER_HOME/lib/"prelauncher-2.2.jar \
  ${1+"$@"}