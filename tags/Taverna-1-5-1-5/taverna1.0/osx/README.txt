Making an OS X release of Taverna
=================================

Assume we already have the generic taverna-1.5 release ready and
want to make a fancy Mac version. This is the advantages of the mac way:

You get one icon, one application, with a nice Taverna icon, called
"Icon". Users install it by just dragging it to /Applications. The icon
is really a folder, and inside is all our juicy java code and even
GraphWiz. The users just see an "Application", though, and treat it as a
single entity.

Copying Taverna.app
-------------------

To start, copy out this Taverna.app skeleton folder to a new folder you
can call "taverna-workbench-1.5", say in your home directory. You can
use cp -r or just a normal drag-and-drop to do the copy. We will
assume /Users/stain/taverna-workbench-1.5 in this document.

Then use a terminal window (Applications -> Utillities -> Terminal), or
possibly right click and select "Open package content", and walk down
into Taverna.app/Contents/Resources/Java. 

This is where we place our JAR files, thanks to Raven that only means
taverna-bootstrap-1.5.jar from the normal Taverna distribution.  This is
the jar file that will be run according to the XML property file, which is
Taverna.app/Contents/Info.plist. Edit this either using your favourite
text editor (like vim) or the GUI by double clicking it.

The skeleton should be without this JAR file, so that we are reminding
ourself to copy in this file from the normal Taverna distribution. Do
this using cp or a normal drag-and-drop.


Editing Info.plist
------------------

Most important in here is the version number and the Java options in the
bottom. Note how we run Java this way, and not through runme.sh. That
means that any fancy option in runme.sh needs to be here, like -Xms300m
Running java this way avoids any console windows, and makes the running
Taverna also use the nice icon in the doc.

Replace all "1.5.0" with the real version number. Update the path
to the taverna-bootstrap-1.5.jar.


Properties
++++++++++

In this file, we have specified some properties manually. 
You normally wouldn't have to update any of these unless the runme.sh of
the normal Taverna distribution has been updated. 

Example, enabling our locally bundled graphviz dot:

            <key>taverna.dotlocation</key>
              <string>$APP_PACKAGE/Contents/MacOS/dot</string>

or using our fancy bootloader:

            <key>java.system.class.loader</key>
             <string>net.sf.taverna.tools.BootstrapClassLoader</string>

In addition we turn on apple.laf.useScreenMenuBar that puts the menubar
on the very top of the screen, OS X style. 



Update shellscripts
-------------------

We also distribute some shellscripts, executeworkflow.sh and
dataviewer.sh. These are located in Contents/MacOS. You should make sure
both of these have been updated compared to the distribution, the
difference is that we replace this original line:

    TAVERNA_HOME=`dirname "$PRG"`
with:
    TAVERNA_HOME=`dirname "$PRG"`/../Resources/Java


You can then test them from outside your Taverna.app like this:

    : stain@mira ~/taverna-workbench-1.5; Taverna.app/Contents/MacOS/executeworkflow.sh -help
    usage: executeworkflow <workflow> [..]
       Execute workflow and save outputs. Inputs can be specified by multiple
       --input options, or loaded from an XML input document as saved from
       (..)

Users can symlink to these shell scripts and place the symlinks in their
PATH.


Testing Taverna.app
-------------------

To test the application itself, first move away your Taverna home
directory, on the Mac located in 
$HOME/Library/Application\ Support/Taverna/. This is so that you
test-start from blank. Double click on Taverna.app from the Finder or
run "open Taverna.app" in the terminal.

There should be a nice Taverna wheel dancing in the dock (not a white
Java icon), splashscreen, and after a while, Taverna should appear. The
menu on the very top of the screen should say "Taverna" in bold letters
and then "File" etc. Taverna->About should say the correct version
number.

Remember, if you run this on your laptop, be on the
allow-everything-ethernet and not on the firewalled-wireless, otherwise
everything will be slow.


Cleaning up CVS/crap
--------------------

Before we release this copy of your Taverna.app, let's remove all those
CVS folders. In a terminal, go inside Taverna.app and run mr. find:

: stain@mira ~/taverna-workbench-1.5; cd Taverna.app 
: stain@mira ~/taverna-workbench-1.5/Taverna.app; find . -name CVS|xargs rm -r


Building a disk image
---------------------

Now - for the actual release, what we do is to send out a read-only disk
image. But first we want to include the new and fancy examples. Go to
taverna-workbench-1.5 and copy the "Examples" folder from the unzipped
taverna-1.5-bin.zip.

Then, to make the disk image, start Disk Utillity, then select:
    New --> Disk image from folder
select the folder "taverna-workbench-1.5"

Choose the "read only" option. Save the disk image,
taverna-workbench-1.5.dmg is usually good.

Now, "internet enable" the image:

    hdiutil internet-enable -yes taverna-workbench-1.5.dmg
    
The internet enable makes it so that people who have downloaded the
image using Safari, copied over the application, and then eject it,
don't have to delete the .dmg file afterwards, because OSX knows that it
was just used for downloading software. However, this seems to only work
with Safari..

But! DO NOT open this dmg file! Make a *COPY* first. The first opening
turns of the internet-enable sign.

This dmg file is then ready to be released directly onto Sourceforge
just like the normal .zip release.


-- Stian Soiland, 2006-12-08

