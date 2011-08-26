#!/bin/sh
java -Xmx300m -Draven.profile=file:conf/current-profile.xml \
  -Djava.system.class.loader=net.sf.taverna.raven.prelauncher.BootstrapClassLoader \
  -Dapple.laf.useScreenMenuBar=true \
  -jar lib/prelauncher-1.7-SNAPSHOT.jar