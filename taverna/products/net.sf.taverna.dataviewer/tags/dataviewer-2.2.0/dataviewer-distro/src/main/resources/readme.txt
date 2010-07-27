=============================
Taverna DataViewer Tool 2.2.0
=============================
http://www.taverna.org.uk/ http://www.mygrid.org.uk/

Released by myGrid, 2010-07-05
(c) Copyright 2005-2010 University of Manchester, UK


Licence
=======
Taverna DataViewer Tool is licenced under the GNU Lesser General Public Licence. (LGPL) 2.1.
See the file LICENCE.txt for details.

If the source code was not included in this download, you can download it from
http://www.taverna.org.uk/developers/source-code/

Taverna uses various third-party libraries that are included under compatible
open source licences such as Apache Licence.


Documentation
=============
See http://www.taverna.org.uk/documentation/taverna-2-x/dataviewer-tool/ for
documentation on the DataViewer Tool.

See the file release-notes.txt for the release notes of the Tool.


Usage
=====
On Windows use "dataviewer.bat", while on OSX/Linux/UNIX
use "sh dataviewer.sh" to run the Tool from the command prompt.

On UNIX you may set the executable bit using 
"chmod 755 dataviewer.sh", allowing you to execute ./dataviewer.sh
directly. You can make symlinks to this shell script from /usr/local/bin or
equivalent. 

usage: dataviewer [<file_path>]
 <file_path>                Absolute path to the file you want loaded at startup.
              				If you provide a path to the file you want to load 
							in the DataViewer, the DataViewer Tool will start 
							and load the file. If you omit the file parameter, 
							it will simply start the DataViewer Tool. 
 

For example:

$ dataviewer.sh /Users/foo/results.xml
Starts the Tool and loads the file /Users/foo/results.xml.


Support
=======
See http://www.taverna.org.uk/about/contact-us/ for contact details.

You may email support@mygrid.org.uk for any questions on using Taverna
and the associated tools. myGrid's support team should respond to your 
query within a week.


Mailing lists
=============

We also encourage you to sign up to the public *taverna-users* mailing list,
where you may post about any problem or give us feedback on using Taverna.
myGrid developers are actively monitoring the list.

 * http://lists.sourceforge.net/lists/listinfo/taverna-users
 * http://taverna-users.markmail.org/search/?q=

If you are a developer, writing plugins for Taverna, dealing with the code
behind Taverna or integrating Taverna with other software, you might find it
interesting to also sign up for the public *taverna-hackers* mailing list,
where you can also track the latest developments of Taverna.

  * http://lists.sourceforge.net/lists/listinfo/taverna-hackers
  * http://taverna-hackers.markmail.org/search/?q=


Requirements
============
Taverna tools require the Java Runtime Environment (JRE) version 5 or 6 from Sun.
No other versions of Java are officially tested with Taverna tools. 

*Note that future versions of Taverna will require Java 6.*

Mac OS X 10.5 (Leopard) and later should come with Java 5 or newer.  

Windows users might need to download Java from http://java.com/

Linux users have different options to install Java depending on their Linux
distribution. Some distributions, such as Ubuntu, might come with alternative
open source implementations of Java, like Gnu GCJ and OpenJDK. We've identified
some issues with these implementations, and recommend using the official Java
implementation from Sun. 

To download Sun Java 6 for Ubuntu, start a Terminal, and type the following:
  sudo aptitude install sun-java6-jre

and follow the instructions. You might also need to change the default Java
implementation by running:
  sudo update-alternatives --config java

Read http://www.taverna.org.uk/download/workbench/system-requirements/
for more requirement details.


Memory usage 
============
The Taverna DataViewer Tool will by default uses a maximum of 300 MB. 

If you need to increase the available memory, edit "dataviewer.sh" or
"dataviewer.bat" script and replace "-Xmx300m" with say "-Xmx600m" to use
600 MB.
