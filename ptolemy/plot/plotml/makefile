# Makefile for the Java classes used to implement the plotml package.
#
# @Authors: Edward A. Lee
#
# @Version: $Id: makefile,v 1.20 2005/04/20 15:16:27 cxh Exp $
#
# @Copyright (c) 1998-2005 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY

ME =		ptolemy/plot/plotml

DIRS =

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	plotml
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = EditablePlotMLApplet.java \
	EditablePlotMLApplication.java \
	HistogramMLApplet.java \
	HistogramMLApplication.java \
	HistogramMLParser.java \
	PlotBoxMLParser.java \
	PlotMLApplet.java \
	PlotMLApplication.java \
	PlotMLFrame.java \
	PlotMLParser.java

EXTRA_SRCS =	$(JSRCS) \
	PlotML_1.dtd \
	plotml.dtd

# Files or directories that are present, but that 'make checkjunk'
# should not complain about
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	$(PTCLASSJAR) \
	'EditablePlotMLApplet$$1.class' \
	'EditablePlotMLApplet$$ChoiceListener.class' \
	'EditablePlotMLApplication$$SelectListener.class' \
	'EditablePlotMLApplication$$1.class' \
	'HistogramMLApplication$$1.class' \
	'PlotMLApplication$$1.class'

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass $(PTCLASSJAR)


demo: all

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
