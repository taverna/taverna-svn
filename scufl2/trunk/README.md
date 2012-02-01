SCUFL2
======

See also the [SCUFL2 wiki][1]

(c) 2009-2011 [myGrid][2], University of Manchester

Licensed under the [GNU Lesser General Public License (LGPL) 2.1][6]. 
See LICENSE.txt for the full terms of LGPL 2.1.

This is the API, model and format of [SCUFL2][1], which replaces 
[Taverna][5]'s workflow format .t2flow. This API allows 
JVM applications to inspect, generate and modify Taverna workflow
definitions without depending on the Taverna runtime.

A new format, called [Scufl2 Workflow Bundle][7] is defined alongside this
API. This format can be inspected, generated and modified independently
of this API.

Note that the ability for Scufl2 API to read a workflow bundle (using
the `scufl2-rdfxml` module) does not guarantee it is valid or
structurally sound. The experimental modules `scufl2-validation-*` will
in the future be able to provide such verification.


Requisites
----------

* Java 1.5 or newer
* Maven 2.2 or newer (for building)


Building
--------

* `mvn clean install`

This will build each module and run their tests, producing JARs like
`scufl2-api/target/scufl2-api-0.9.jar`. 

First time you build Scufl2 this might download dependencies needed for
compliation. These have separate open source licenses, but should be
compatible with LGPL. None of the dependencies are neccessary for
using the compiled SCUFL2 API.

Some of the experimental modules are not built automatically, to build
them separately, run the same command from within their folder.



Usage
-----

Scufl2 is built as a Maven project, and the easiest way to use it is
from other Maven projects.

Typical users of the Scufl2 API will depend on the three modules
*scufl2-api*, *scufl2-t2flow* and *scufl2-rdfxml*. In your Maven
project's POM file, add this to your `<dependencies>` section:

		<dependency>
			<groupId>uk.org.taverna.scufl2</groupId>
			<artifactId>scufl2-api</artifactId>
			<version>0.9</version>
		</dependency>
		<dependency>
			<groupId>uk.org.taverna.scufl2</groupId>
			<artifactId>scufl2-rdfxml</artifactId>
			<version>0.9</version>
		</dependency>
		<dependency>
			<groupId>uk.org.taverna.scufl2</groupId>
			<artifactId>scufl2-t2flow</artifactId>
			<version>0.9</version>
		</dependency>

All Scufl2 modules are also valid OSGi bundles.

You can alternatively copy and add the JARs from these modules to your
classpath:

* scufl2-api/target/scufl2-api-0.9.jar
* scufl2-rdfxml/target/scufl2-rdfxml-0.9.jar
* scufl2-t2flow/target/scufl2-t2flow-0.9.jar


See the *scufl2-validation* folder for examples of
usage. The best classes to start exploring would be
`uk.org.taverna.scufl2.api.io.WorkflowBundleIO` and
`uk.org.taverna.scufl2.api.container.WorkflowBundle`.

Example of converting .t2flow to .wfbundle:

    import uk.org.taverna.scufl2.api.container.WorkflowBundle;
    import uk.org.taverna.scufl2.api.io.ReaderException;
    import uk.org.taverna.scufl2.api.io.WorkflowBundleIO;
    import uk.org.taverna.scufl2.api.io.WriterException;

    // ..
    
    WorkflowBundleIO io = new WorkflowBundleIO();
    File t2File = new File("workflow.t2flow");
    File scufl2File = new File("workflow.wfbundle");
    WorkflowBundle wfBundle = io.readBundle(t2File, "application/vnd.taverna.t2flow+xml");
    io.writeBundle(wfBundle, scufl2File, "application/vnd.taverna.scufl2.workflow-bundle");

Check out the GitHub project scufl2-examples[8] for examples of using Scufl2, 
including the above code.


Modules
-------

Official modules:

* *scufl2-api* Java Beans for working with SCUFL2 
* *scufl2-t2flow* .t2flow import from Taverna 2
* *scufl2-rdfxml* .wfbundle import/export (RDF/XML)

Experimental modules:

* *scufl2-usecases* Example code covering [SCUFL2 use cases][4] (out of date)
* *scufl2-rdf* Pure RDF export/import (out of date)
* *scufl2-scufl* SCUFL 1 .xml import from Taverna 1
* *scufl2-validation* API for validating a Scufl2 workflow bundle
* *scufl2-validation-correctness* 
  Validate correctness of Scufl2 workflow definition
* *scufl2-validation-structural*
  Validate that a Scufl2 workflow definition is structurally sound
* *scufl2-validation-integration*
  Integration tests for scufl2-validation modules



[1]: http://www.mygrid.org.uk/dev/wiki/display/developer/SCUFL2
[2]: http://www.mygrid.org.uk/
[3]: http://www.mygrid.org.uk/dev/wiki/display/story/Dataflow+serialization
[4]: http://www.mygrid.org.uk/dev/wiki/display/developer/SCUFL2+use+cases
[5]: http://www.taverna.org.uk/
[6]: http://www.gnu.org/licenses/lgpl-2.1.html
[7]: http://www.mygrid.org.uk/dev/wiki/display/developer/Taverna+Workflow+Bundle
[8]: https://github.com/mygrid/scufl2-examples
