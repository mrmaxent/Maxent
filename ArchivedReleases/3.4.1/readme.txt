Maximum Entropy Modeling of Species Geographic Distributions
============================================================

INSTRUCTIONS FOR DOWNLOADING AND GETTING STARTED

Miro Dudik          (mdudik@microsoft.com)
Steven Phillips     (mrmaxent@gmail.com)
Rob Schapire        (schapire@microsoft.com)

Version 3.4.0, December 2016

This file outlines the steps you should take to begin using maxent to
model species geographic distributions.  To download the program, or
for background on maxent and its application to species distribution
modeling, please see 
http://biodiversityinformatics.amnh.org/open_source/maxent.


Terms and Conditions
--------------------
This software is distributed under the MIT License.


Downloading the Software
------------------------
The software consists of a jar file, maxent.jar, which can be used on
any computer running Java version 1.5 or later.  Note that if you are
using a 64-bit computer, you will also need to be using a 64-bit
version of Java.

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

   Unable to access jarfile maxent.jar

This usually happens because you used Microsoft Internet Explorer to
download the maxent.jar file, and Explorer incorrectly renames the
file maxent.zip.  You need to rename the maxent.zip file back to
maxent.jar.  Alternatively, you might get a response like this:

   the java class could not be loaded. 
   java.lang.UnsupportedClassVersionError: density/maxent (unsupported 
   major.minor version 49.0)

which means that the version of java that is installed on your PC is
out of date.  To verify this, open a command window and type "java
-version".  You'll probably find that it is older than 1.5, which is
the oldest version recommended for use with Maxent.  To get a new
version, go to java.sun.com, and look for downloads for the Java
runtime environment (JRE).

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

For example, if you downloaded Java 1.6.0_13, it was probably
installed in C:\Program Files\Java\jdk1.6.0_13, in
which case you will add ";C:\Program Files\Java\jdk1.6.0_13\bin" to
the end of the path variable, like in the following example:

      C:\Program Files;C:\Winnt;C:\Winnt\System32;C:\Program Files\Java\jdk1.6.0_13\bin


2.  If the program gives "out-of-memory" or "heap space" errors, it
may be because you are clicking on the maxent.jar file.  You need to
click on the maxent.bat file instead.  If this is not the problem,
click on the "help" button for more advice.


Main Changes in Version 3.4.0:
   Added cloglog output transformation, and made it the default output format
   Threshold features are no longer used in default settings


Main changes in Version 3.3.3:
-----------------------------
   Added permutation importance to table of variable contributions
   Multivariate environmental suitability surfaces (MESS, formerly
     known as "Novel") integrated into html output
   Explain tool integrated into html output
   Updated tutorial
   Bug fixes


Main changes in Version 3.3.2:
-----------------------------

  "Novel" tool to identify areas of novel climate conditions when projecting
  "Explain" tool to aid understanding of Maxent predictions
  Faster reading of ascii files
  Bug fixes
  Updated tutorial, including Novel and Explain tools
  A more detailed API for calling Maxent from other Java code
  

Main changes in Version 3.3.0:
-----------------------------
  
  Replicated runs added, to allow cross-validation, bootstrapping and
    repeated subsampling.  After a replicated run, a web page is made
    that analyzes and combines the results of the individual runs.
  Clamping pictures have changed.  When projecting, the picture showing
    the effect of clamping now simply shows the difference between the
    predictions with and without clamping.
  Parameters are treated more uniformly, and many more are exposed
    in the Settings window and described in the Help window.
  Bug fixed in handling of bias files.
  The feature type buttons have a slightly different meaning, with
    "auto features" choosing a subset of the selected types.  This
    allows you to limit the feature types used by auto features.
  More efficient memory usage, to avoid memory problems with large grids.
  Pictures of predictions are now created by default.
  Duplicate presence records are now removed by default.
  Projection onto SWD-format files and writing of background
    predictions both preserve x and y coordinates of points.
  The html output now gives the command line with all flags needed to
    repeat a run.
  A limited API for calling Maxent from other Java code.


Main changes in Version 3.2.18:
------------------------------

  Fixed bug that sometimes caused '?' values due to arithmetic
    overflow when computing clamping grids
  Response curve thumbnails now all use the same 0-1 y-axis scale


What was new in Version 3.2.1:
-----------------------------

  Response curves are now in logistic (probability) space, rather than
    exponent (linear) space, so they're easier to interpret
  There's a second set of response curves and accompanying text, to show
    response of the species to variables individually rather than
    marginal responses
  The code that makes clamping pictures has been rewritten.  The clamp
    amount now shows what effect clamping can have on the logistic
    prediction, rather than the exponent.
  There is support for .bil format
  In pictures of predictions, the legend now goes in the corner with
    least overlap with non-NODATA 
  In .csv files, we detect ";" as separator (and in that case, also ","
    in place of "."), as made by French Excel 
  File browsers now show .CSV files in addition to .csv
  The dontcache and responseCurvesExponent command-line
    arguments are described in help file.
  There is no longer a complaint about extra columns in SWD input
    files if those columns are deselected layers
  There is a warning if occurrence data is not in SWD format when
    background is in SWD format
  Fixed bug in writing _samplePredictions.csv file:  training
    predictions were sorted by prediction value, but their lat/long
    weren't staying with them 
  Fixed bug in .html: was writing wrong feature categories when auto
    features was overridden 
  Fixed bug in project: clamping wasn't always keeping response
    constant outside the training range, e.g. for quadratic features


What was new in version 3.1
---------------------------

Fixed bugs in:
  DIVA output format
  calculation of estimate of maximum achievable AUC
  processing of occurrences with partial environmental data
  writing of logistic values to _samplePredictions.csv file
  reporting of output type in .html files
  option to change number of background points
Added logistic output to samplePredictions.csv and omission.csv files
Added new entropy-based threshold to thresholds table in .html files
Revised wording about maximum achievable AUC in .html files


What was new in version 3.0
---------------------------

  Variable contributions: The html output has a table that gives the
    percentage contribution of each variable to the Maxent model.
    This gives a new way (in addition to the jackknife) of
    interpreting the influence of each variable on the model.

  Logistic output format: There is a new default output grid format,
    called "logistic".  Logistic values range from 0 to 1, and model
    the probability of presence of the species, assuming that sampling
    effort is similar to the effort expended to obtain the training
    samples.  See the help button for more details.

  Clamping picture:  Whenever a model is projected onto a different
    set of environmental variables, a picture is produced showing where
    clamping has occurred, colored according to the amount of clamping.

  DIVA data types.  Compatibility problems have been resolved, so
    Maxent can now successfully read and write DIVA's .grd format. 

  Increased categorical regularization.  The default regularization of
    categorical features was causing over-fitting for some data sets,
    especially those with larger numbers of categories and training
    presences.  Categorical regularization has therefore been
    increased, but is still adjustable using the "beta_categorical"
    flag. 

  Better memory usage.  Larger grids of environmental data can now be
    used without running out of memory.

  Faster file handling.  By default, any .asc file used during
    training or projection will have a cached compressed version saved
    in a subdirectory called "maxent.cache".  The cached version is
    then used to speed up future runs.

  ROC plot axis labels have been fixed.

  Bug fixed in jackknifing: previously, test performance of
    leave-one-out runs was too low when running with lots of training
    presences.

  New options to control clamping during projection.  The flag
    "fadebyclamping" causes the prediction at a point to decrease when
    there is clamping there, with the amount of decrease determined by
    the amount of clamping.  The "dontextrapolate" option causes the
    prediction to be zero wherever there is clamping.

  More data and details added to maxentResults.csv file.


Information available from the "help" button
--------------------------------------------
The help button gives details on topics including:

  Input data formats
  Control parameters
  Outputs
  A quick overview of the maximum entropy method
  Command line options
  Troubleshooting