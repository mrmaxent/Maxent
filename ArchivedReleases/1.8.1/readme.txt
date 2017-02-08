Maximum Entropy Modeling of Species Geographic Distributions
============================================================

INSTRUCTIONS FOR DOWNLOADING AND GETTING STARTED

Miro Dudik          (mdudik@cs.princeton.edu)
Steven Phillips     (phillips@research.att.com)
Rob Schapire        (schapire@cs.princeton.edu)

February, 2005

This file outlines the steps you should take to begin using maxent to
model species geographic distributions.  To download the program, or
for background on maxent and its application to species distribution
modeling, please see http://www.cs.princeton.edu/~schapire/maxent.


Terms and Conditions
--------------------
This software may be freely downloaded and used for all educational
and research activities.  This software may not be used for any
commercial or for-profit purposes.  The software is provided "as-is",
and does not come with any warranty or guarantee of any kind.  The
software may not be further distributed.


Downloading the Software
------------------------
The software consists of a jar file, maxent.jar, which can be used on
any computer running Java version 1.3 or later.

If you are using Microsoft Windows, you should also download the file
maxent.bat, and save it in the same directory as maxent.jar.

Maxent.bat assumes that your computer has at least 512 megabytes of
memory.  If this is not the case, you should edit the file (for
example, using Notepad) to reduce amount of memory it requests.  Just
replace the number "512" with the amount of memory your computer has.
You also can increase the amount of memory available to the program in
this fashion.  If your computer has more than 512 megabytes of memory,
you should increase the amount described in the file.  For example, if
your computer has a gigabyte of memory, replace the number "512" with
"1024".  However, if your computer is running Microsoft Windows, there
is unfortunately an upper limit of about 1.3 gigabytes that Windows
can give to Java.


Getting Started
---------------
If you are using Microsoft Windows, simply click on the file
maxent.bat.  Otherwise, enter "java -mx512m -jar maxent.jar" in a
command shell (where "512" can be replaced by the amount of memory you
want made available to the program).  The program will start up, and
further instructions can be obtained by pressing the "help" button.
Maxent.jar can be followed by command line arguments, which is
especially useful when running maxent in a batch (non-interactive)
mode using the argument "-a". The list of command line options is
available by pressing "help" button after starting maxent.


What's new in version 1.8.1
---------------------------
New SWD (samples-with-data) input format
New Diva-GIS grid format
More efficient memory usage, especially with -K flag
Runs faster
Automatic selection of feature types
-L flag to write parameters and detailed information about a run to
  a logfile
Updated automatic setting of regularization value. As a result, you
  may obtain predictions that differ from the previous versions of
  maxent. The current version provides more accurate predictions.


Information available from the "help" button
--------------------------------------------
The help button gives details on topics including:

  Input data formats
  Control parameters
  Outputs
  A quick overview of the maximum entropy method
  What to do if you get "out of memory" problems with large datasets
  Command line options
