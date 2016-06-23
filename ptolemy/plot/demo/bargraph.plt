# Test file for Tycho/Java Plotter
# @Version: $Id: bargraph.plt,v 1.11 2005/02/28 19:57:21 cxh Exp $
# Copyright (c) 1997-2005 The Regents of the University of California.
# All rights reserved.
# See the file ../copyright.txt for copyright notice,
# limitation of liability, and disclaimer of warranty provisions.

TitleText: Software Downloads
XRange: 0,10
YRange: 0,10000
# Manually specify X ticks
# Note that the 0 and 10 point result in clipping of the rectangles.
XTicks: 1993 0, 1994 1, 1995 2, 1996 3, 1997 4, 1998 5, 1999 6, 2000 7, 2001 8, 2002 9, 2003 10
XLabel: Year
YLabel: Downloads
Marks: none
Lines: off
# Width and offset of bars
Bars: 0.5, 0.2
NumSets: 3

DataSet: program a
0, 100
1, 300
2, 600
3, 1000
4, 4000
5, 6000
6, 3000
7, 1000
8, 400
9, 0
10, 0

DataSet: program b
0, 0
1, 0
2, 50
3, 100
4, 800
5, 400
6, 1000
7, 5000
8, 2000
9, 300
10, 0

DataSet: program c
0, 0
1, 0
2, 0
3, 10
4, 100
5, 400
6, 2000
7, 5000
8, 9000
9, 7000
10, 1000
