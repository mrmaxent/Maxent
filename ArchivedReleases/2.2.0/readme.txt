Maximum Entropy Modeling of Species Geographic Distributions
============================================================

INSTRUCTIONS FOR DOWNLOADING AND GETTING STARTED

Miro Dudik          (mdudik@cs.princeton.edu)
Steven Phillips     (phillips@research.att.com)
Rob Schapire        (schapire@cs.princeton.edu)

Version 2.2, March 2006

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
any computer running Java version 1.4.2 or later.

If you are using Microsoft Windows, you should also download the file
maxent.bat, and save it in the same directory as maxent.jar.

Maxent.bat assumes that your computer has at least 512 megabytes of
memory.  If this is not the case, you should edit the file (for
example, using Notepad) to reduce the amount of memory it requests.  Just
replace the number "512" with a little less than the amount of memory
your computer has.  You also can increase the amount of memory
available to the program in this fashion.  If your computer has more
than 512 megabytes of memory, you should increase the amount described
in the file.  For example, if your computer has a gigabyte of memory,
replace the number "512" with "900" (leaving a little for other
programs).  However, if your computer is running Microsoft Windows,
there is unfortunately an upper limit of about 1.3 gigabytes that
Windows can give to Java.


Getting Started
---------------
If you are using Microsoft Windows, simply click on the file
maxent.bat.  Otherwise, enter "java -mx512m -jar maxent.jar" in a
command shell (where "512" can be replaced by the amount of memory you
want made available to the program).  The program will start up, and
further instructions can be obtained by pressing the "help" button.
Maxent.jar can be given command line arguments, which is
especially useful when running maxent in a batch (non-interactive)
mode. The list of command line options is
available by pressing "help" button after starting maxent.


Common Problems Getting Started
-------------------------------
1.  If clicking on maxent.bat causes a window to appear but immediately
disappear, it is probably because of a problem with your Java
installation.  To diagnose this problem, you should open an ms-dos
window (by clicking "start" then "run" and then typing "cmd").  Then
change to the directory where the maxent.bat and maxent.jar files are
and type "maxent.bat".  You might get a response like this:

   the java class could not be loaded. 
   java.lang.UnsupportedClassVersionError: density/maxent (unsupported 
   major.minor version 49.0)

which means that the version of java that is installed on your PC is
out of date.  To verify this, open a command window and type "java
-version".  You'll probably find that it is older than 1.4.2, which is
the oldest version recommended for use with Maxent.

To get a new version, go to java.sun.com; downloads are on the right
of the page.  Click on J2SE 5.0, and the latest version is JRE 5.0
Update 6 (at the time of writing).

Alternatively, you may probably get the response:

   'java' is not recognized as an internal or external command,
   operable program or batch file.

The problem is either that you don't have Java installed (you need to
install from java.sun.com as above), or Java is not mentioned in your
"path" variable.  To fix the latter problem:

   1.  From the desktop, right click My Computer and click properties.

   2. In the System Properties window, click on the Advanced tab.

   3. In the Advanced section, click the Environment Variables button. 

   4. Finally, in the Environment Variables window, highlight the path
   variable in the Systems Variable section and click edit.  Add the
   directory where the java executable is.  

For example, Java 1.4.2 typically is installed in C:\j2sdk1.4.2_06, in
which case you will add ";C:\j2sdk1.4.2_06\bin" to the end of the path
variable, like in the following example:

      C:\Program Files;C:\Winnt;C:\Winnt\System32;C:\j2sdk1.4.2_06\bin


2.  If the program gives "out-of-memory" or "heap space" errors, it
may be because you are clicking on the maxent.jar file.  You need to
click on the maxent.bat file instead.  If this is not the problem,
click on the "help" button for more advice.


What's new in version 2.2
---------------------------
Bug fixes
Improved hinge features, for slightly better modeling accuracy
Changes to handling of missing data in samples file
Binomial tests of significance computed exactly for up to 25 test
  samples, after which a normal approximation is used.


Information available from the "help" button
--------------------------------------------
The help button gives details on topics including:

  Input data formats
  Control parameters
  Outputs
  A quick overview of the maximum entropy method
  Command line options
  Troubleshooting