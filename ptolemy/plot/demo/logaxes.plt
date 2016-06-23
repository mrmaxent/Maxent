# Test file for Tycho/Java Plotter Log axes
# $Id: logaxes.plt,v 1.14 2005/02/28 19:57:24 cxh Exp $
# Copyright (c) 1998-2005 The Regents of the University of California.
# All rights reserved.
# See the file $TYCHO/copyright for copyright notice,
# limitation of liability, and disclaimer of warranty provisions.

# We set use the pxgraphargs applet parameter to control
# whether we plot with log axes or not so we can compare plotting
# with log axes with plotting without log axes.
#XLog: on
#YLog: on
Marks: dots
# We use scientific notation in a few places so we can test the code
# that reads in numbers.
1E-01 1.0E-03
2.0E-01 0.002
.3 3.0E-03
.4 0.004
.5 0.005
.6 0.006
.7 0.007
.8 0.008
.9 0.009
1 0.001
2 0.002
3 0.003
4 0.004
5 0.005
6 0.006
7 0.007
8 0.008
9 0.009
10 0.01
20 0.02
100 0.1 1E-01 0.9
200 0.2
300 0.3
400 0.4
500 0.5 0.4 0.6
600 0.6
7E02 0.7
800 0.8
9E02 9E-01 1E-01 9E-01
1000 1
4000 1.0
50000 10
62000 100
700000 150
2000000 100
3000000 200
4000000 300
5000000 400
6000000 500
7000000 600
8000000 700
9000000 800
10000000 900
11000000 1000
