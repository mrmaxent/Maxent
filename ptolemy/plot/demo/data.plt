# Test file for Ptolemy II Java Plotter
# @Version: $Id: data.plt,v 1.11 2005/02/28 19:57:34 cxh Exp $
# Copyright (c) 1997-2005 The Regents of the University of California.
# All rights reserved.
# See the file ../copyright for copyright notice,
# limitation of liability, and disclaimer of warranty provisions.

TitleText: My Plot
XRange: 0,4
YRange: -4,4
# Manually specify X ticks
XTicks: zero 0, "one" 1, two 2, three 3, four 4, five 5
Grid: off
XLabel: X Axis
YLabel: Y Axis
Marks: various
NumSets: 11
Color: off

DataSet: dot
0,-4
1,-3
2,-2
3,-1
4, 0

DataSet: cross
0,-3.5
1,-2.5
2,-1.5
3,-0.5
4, 0.5

Lines: off

DataSet: square
0,-3
1,-2
2,-1
3,0
4,1

DataSet: triangle
0,-2.5
1,-1.5
2,-0.5
3, 0.5
4, 1.5

DataSet: diamond
0,-2
1,-1
2, 0
3,1
4,2

DataSet: circle
0,-1.5
1,-0.5
2, 0.5
3, 1.5
4, 2.5

DataSet: plus
0,-1
1, 0
2,1
3,2
4,3

DataSet: square
0,-0.5
1, 0.5
2, 1.5
3, 2.5
4, 3.5

DataSet: triangle
0, 0
1, 1
2, 2
3,3
4,4

DataSet: diamond
0,0.5
1, 1.5
2, 2.5
3, 3.5
4, 4.5

DataSet: dot
0,1
1,2
2,3
3,4
