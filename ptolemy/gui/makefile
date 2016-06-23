# Makefile for the Java classes used to implement the Ptolemy gui
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id: makefile,v 1.58 2005/02/28 19:04:43 cxh Exp $
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

ME =		ptolemy/gui

DIRS = 		demo

# Root of the Java directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)


# Used to build jar files
PTPACKAGE = 	gui
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	CloseListener.java \
	ComponentDialog.java \
	Query.java \
	QueryListener.java \

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include DIRS here, since demo is in DIRS, so 'make sources' will
# try to run 'make demo', which is wrong.
MISC_FILES =	test #$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	demo \
	doc \
	'ComponentDialog$$1.class' \
	'ComponentDialog$$2.class' \
	'Message$$1.class' \
	'Message$$2.class' \
	'Query$$1.class' \
	'Query$$CheckBoxListener.class' \
	'Query$$LineListener.class' \
	'Query$$QueryActionListener.class' \
	'Query$$QueryColorChooser.class' \
	'Query$$QueryFileChooser.class' \
	'Query$$QueryFocusListener.class' \
	'Query$$QueryItemListener.class' \
	'Query$$QueryScrollPane.class' \
	'Query$$SliderListener.class' \
	'Top$$1.class' \
	'Top$$2.class' \
	'Top$$3.class' \
	'Top$$4.class' \
	'Top$$5.class' \
	'Top$$6.class' \
	'Top$$7.class' \
	'Top$$8.class' \
	'Top$$9.class' \
	'Top$$FileMenuListener.class' \
	'Top$$HelpMenuListener.class'

JCLASS = $(JSRCS:%.java=%.class)


all: jclass
install: jclass $(PTCLASSJAR)


exec: jclass

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
