To restore the folder icon, you need to reintroduce the form feed
character:

mv Icon $'Icon\015'

Then do open .. and click on the Example workflows folder. The icon
should appear.


This file and the icon has been hidden with 
/Developer/Tools/SetFile -a V Icon README.txt
and won't be shown in Finder

-- Stian Soiland 2007-01-26
