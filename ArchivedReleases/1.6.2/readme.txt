Maximum Entropy Modeling of Species Geographic Distributions
============================================================

INSTRUCTIONS FOR DOWNLOADING AND GETTING STARTED

Miro Dudik          (mdudik@cs.princeton.edu)
Steven Phillips     (phillips@research.att.com)
Rob Schapire        (schapire@cs.princeton.edu)

Version 1.6.2 (beta)
August, 2004

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
this fashion.


Getting Started
---------------
If you are using Microsoft Windows, simply click on the file
maxent.bat.  Otherwise, enter "java -mx512m -jar maxent.jar" in a
command shell (where "512" can be replaced by the amount of memory you
want made available to the program).  The program will start up, and
further instructions can be obtained by pressing the "help" button.
Maxent.jar can be followed by command line arguments, which is
especially useful when running maxent in a batch (non-interactive)
mode using the argument "-a".  See a list of command line options
below.


Input Formats
-------------
Input files can be specified through the user interface, or on a
command line.  The format is as follows:
              
* Samples: Given by a file in comma-separated value format.  The
  first line is a header line (ignored by the program), while later
  lines have the format: species, longitude, latitude.

  For example:

  Species, Long, Lat
  Blue-headed Vireo, -89.9, 48.6
  Loggerhead Shrike, -87.15, 34.95
  ...

* Environmental layers: Given by a directory containing the layers.
  The layers must be in ESRI ASCII grid format, and their filenames
  must end in ".asc".

  The ASCII grid file consists of a set of keywords, followed by cell
  values in row-major order.  The file format is

  ncols xxx
  nrows xxx
  xllcorner xxx
  yllcorner xxx
  cellsize xxx
  NODATA_value xxx
  row 1
  row 2
  ...
  row n

  For example:

  ncols         386
  nrows         286
  xllcorner     -128.66338
  yllcorner     13.7502065
  cellsize      0.2
  NODATA_value  -9999
  -9999 -9999 -123 -123 -123 -9999 -9999 -9999 -9999 -9999 ...
  -9999 -9999 -123 -123 -123 -9999 -9999 -9999 -9999 -9999 ...
  -9999 -9999 -117 -117 -117 -119 -119 -119 -119 -119 -9999 ...
  ...

* Projection layers (optional): Given by a directory containing a
  second set of environmental layers.  The layers must have the same
  names as those in the "Environmental layers" directory, though they
  might describe a different geographic area.

Output files
------------
* maxentResults.csv For each species, the number of training samples
  used for learning, values of training loss and test loss, and an
  average contribution of individual features is given.  Training loss
  includes regularization, test loss is given only when a test sample
  file is provided.

In addition, maxent produces several files for every species. For a
species called "mySpecies", it produces files

* mySpecies.asc
  containing the predicted probabilities in ESRI ASCII grid format

* mySpecies.lambdas
  describing the computed model

* mySpecies.thr
  describing feature profiles (if threshold features are used)

* mySpecies_projDir.asc
  containing the projected weights (if the projection directory
  projDir is specified)

The output format for probabilities and projected weights is either
raw, with the actual probabilities (weights) of each cell given, or
cumulative, with the output value at a grid cell being the sum of the
probabilities (weights) of all grid cells with no higher probability
(weight) than the grid cell, times 100 (rescaled for projected weights
so that the maximum is 100).  For example, the grid cell that is
predicted as having the best conditions for the species, according to
the model, will have cumulative value 100, while cumulative values
close to 0 indicate predictions of unsuitable conditions.


Command Line Arguments
----------------------
  -p   use product, quadratic and linear features
  -q   use quadratic and linear features
  -l   use linear features
  -h   use threshold features
  -V   print the version number
  -a   automatically start running, and terminate when done
  -r   redo species if ".asc" output file already exists
  -k   skip species if ".asc" output file already exists
  -s<sample_file>
       set sample file to <sample_file>
  -e<layers_directory>
       set environmental layers directory to <layers_directory>
  -j<projection_directory>
       set projection directory to <projection_directory>
  -o<output_directory>
       set output directory to <output_directory>
  -m<max_iters>
       set the maximum number of iterations to <max_iters>
  -b<beta>
       set the regularization constant to <beta>
  -c<threshold>
       set the convergence threshold to <threshold>
  -t<variable>
       make <variable> a categorical variable
  -T<test_sample_file>
       use samples in test_sample_file to evaluate test loss
  -C   don't do clamping while projecting
  -Q   don't use "cumulative" output format
  -K   minimize memory usage, but run approximately half as fast
  -W   write outputs in a compressed (.mxe) format, to save disk space
