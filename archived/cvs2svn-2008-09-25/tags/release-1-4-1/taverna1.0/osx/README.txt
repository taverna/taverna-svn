Making an OS X release of Taverna
=================================

Assume we already have the generic taverna-1.4-bin.zip release ready and
want to make a fancy Mac version. This is the advantages of the mac way:

You get one icon, one application, with a nice Taverna icon, called
"Icon". Users install it by just dragging it to /Applications. The icon
is really a folder, and inside is all our juicy java code and even
GraphWiz. The users just see an "Application", though, and treat it as a
single entity.

To start, copy out this Taverna.app skeleton to a new folder you can
call "taverna-workbench-1.4". 

Then use a terminal window, or possibly right click and select "Open
package content", and walk down into
Taverna.app/Contents/Resources/Java. 

This will be our TAVERNA_HOME for the Mac version. So in each of the
directories conf, lib and plugins, just copy everything from the
respective folder from taverna-1.4-bin.zip. Note: Do NOT unzip the
whole folder inside here. 

Then we'll also need taverna-launcher-1.4.jar to be directly in
Taverna.app/Contents/Resources/Java. This is the jar file that will be
run by the XML property file, which is Taverna.app/Contents/Info.plist.

Most important in here is the version number and the Java options in the
bottom. Note how we run Java this way, and not through runme.sh. That
means that any fancy option in runme.sh needs to be here, like -Xms300m
Running java this way avoids any console windows, and makes the running
Taverna also use the nice icon.

Then we'll unfortunately have to edit a configuration file to tell
Taverna we're on a mac. (Yes yes, this could have been done
automatically in the Java code!):

edit Taverna.app/Contents/Resources/Java/conf/mygrid.properties with vim
and make sure you uncomment "useinternalframes" and add the osxpresent
line:

    #   taverna.workbench.useinternalframes = true
    #--------------------------------------------------------------------
    taverna.osxpresent = true


Due to some clever design, it is not possible to do
.useinternalframes = false - because ANY value triggers
the internal frames option. The same is true for .osxpresent.
These switch takes away the "workbench" window and makes windows
independant in the Mac-way. The Mac menubar is enabled by the Info.plist
startup.


And.. That should be it!


Make *a copy*[1] of Taverna.app to somewhere, and try to double click the
*COPY* in the Finder. There should be a nice icon, splashscreen, and after a
while, Taverna should appear. Remember, if you run this on your laptop,
be on the  allow-everything-ethernet and not on the firewalled-wireless,
or everything will be slow.

.. [1] The reason why you need to make a copy is because Taverna might
create some new files in TAVERNA?HOME when run, and we don't want to
distribute those.


Now - for the actual release, what we do is to send out a read-only disk
image. But first we want to include the new and fancy examples. Go to
taverna-workbench-1.4 and copy the "Exampels" folder from the unzipped
taverna-1.4-bin.zip.

Then, to make the disk image, start Disk Utillity, then select:
    New --> Disk image from folder
select the folder "taverna-workbench-1.4"

Choose the "read only" option. Save the disk image,
taverna-workbench-1.4.dmg is usually good.

Now, "internet enable" the image:

    hdiutil internet-enable -yes taverna-workbench-1.4.dmg
    
The internet enable makes it so that people who have downloaded the
image, copied over the application, and then eject it, don't have to
delete the .dmg file afterwards, because OSX knows that it was just used
for downloading software. However, this seems to only work with Safari..

But! DO NOT open this dmg file! Make a *COPY* first. The first opening
turns of the internet-enable sign.


-- Stian Soiland, 2006-06-06
