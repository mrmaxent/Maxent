# Makefile for Java ptplot program
#
# @Authors: Christopher Hylands
#
# @Version: $Id: makefile,v 1.220.2.4 2005/07/28 19:37:03 cxh Exp $
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
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

ME =		ptolemy/plot

DIRS = 		demo doc test plotml compat

# Root of the Ptolemy II directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Variables and rules used to create source file distributions
PTPACKAGE = 	plot
# If you change PTVERSION, modify the version numbers in:
#    PlotBox.java,
#    doc/main.htm, doc/changes.htm, doc/install.htm, doc/download/index.htm
PTVERSION =	5.5
PTDIST =	ptplot$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		plotml/plotml.jar \
		compat/compat.jar

PTCLASSALLJAR = 	plot.jar

# Jar file consisting of classes used by an applet.
PTAUXJAR = 		plotapplet.jar

# Jar file consisting of classes used by pxgraph applets.
# The pxgraph classes appear in this jar file and in the plot.jar
PXGRAPHAPPLETJAR =	pxgraphapplet.jar

# Jar file consisting of classes used by an application, including the
# com/microstar/xml classes.  Note that this jar file does not include
# the pxgraph classes.
PLOTMLAPPLETJAR =	plotmlapplet.jar

# Jar file consisting of classes used by an application, including the
# ptolemy/gui and com/microstar/xml classes.  Note that this
# jar file does not include the pxgraph classes.
PLOTAPPLICATIONJAR =	plotapplication.jar

# Java Sources, keep these alphabetical,
JSRCS = \
	CmdLineArgException.java \
	EditablePlot.java \
	EditListener.java \
	EPSGraphics.java \
	Histogram.java \
	HistogramApplet.java \
	Plot.java \
	PlotApplet.java \
	PlotApplication.java \
	PlotBox.java \
	PlotDataException.java \
	PlotFormatter.java \
	PlotFrame.java \
	PlotLive.java \
	PlotLiveApplet.java \
	PlotPoint.java

# Used to build distributions with InstallShield's isjava tool
ISJAVA_SRC = ptplot.isj

# Files that are not compiled, such as .tcl and .itcl files.
EXTRA_SRCS = $(JSRCS) \
	$(ISJAVA_SRC) \
	copyright.txt \
	index.htm \
	ptplot.jnlp.in \
	ptplot.iap_xml \
	ptplot_ismp.xml

# Non-source files such as README files and subdirectories
MISC_FILES = test img servlet #bean

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(DIRS) \
	adm \
	histogram \
	manifest.tmp \
	$(PTCLASSALLJAR) \
	'EditablePlot$$EditMouseListener.class' \
	'EditablePlot$$ModifyListener.class' \
	'EditablePlot$$UndoListener.class' \
	'Histogram$$1.class' \
	'Histogram$$2.class' \
	'Histogram$$3.class' \
	'PlotApplet$$1.class' \
	'PlotApplication$$1.class' \
	'PlotApplication$$2.class' \
	'PlotApplication$$3.class' \
	'Plot$$1.class' \
	'Plot$$2.class' \
	'Plot$$3.class' \
	'Plot$$4.class' \
	'Plot$$5.class' \
	'Plot$$6.class' \
	'Plot$$7.class' \
	ptolemy.plot$(JVERSION).tar.gz \
	ptolemy.plot$(JVERSION).zip \
	$(PTAUXJAR) \
	$(PXGRAPHAPPLETJAR) \
	$(PLOTMLAPPLETJAR) \
	$(PLOTAPPLICATIONJAR) \
	'PlotBox$$1.class' \
	'PlotBox$$ButtonListener.class' \
	'PlotBox$$CommandListener.class' \
	'PlotBox$$DragListener.class' \
	'PlotBox$$ZoomListener.class' \
	'PlotFormatter$$1.class' \
	'PlotFormatter$$2.class' \
	'PlotLive$$StartButtonListener.class' \
	'PlotLive$$StopButtonListener.class' \
	'Plot$$Format.class' \
	'PlotApplication$$1.class' \
	'PlotApplication$$2.class' \
	'PlotFrame$$EPSFileFilter.class' \
	'PlotFrame$$FileMenuListener.class' \
	'PlotFrame$$FormatListener.class' \
	'PlotFrame$$PLTOrXMLFileFilter.class' \
	'PlotFrame$$SpecialMenuListener.class'

# Files to be removed by 'make clean'
KRUFT = \
	vergil.jnlp \
	$(PLOTAPPLICATIONJAR) \
	$(PLOTMLAPPLETJAR) \
	$(PXGRAPHAPPLETJAR)

JCLASS = $(JSRCS:%.java=%.class)

# make jars will build $(OTHER_JARS)
OTHER_JARS = $(PXGRAPHAPPLETJAR) $(PLOTMLAPPLETJAR) $(PLOTAPPLICATIONJAR)

all: sources jclass

install: all jars
	(cd ../../bin; $(MAKE) install)


# PlotBox uses PlotFormatter which uses ptolemy.gui.Query and other classes
# ptolemy.gui has lots of classes, we need only a few of them.
PTOLEMYGUIFILES = \
	ptolemy/gui/Query.class \
	'ptolemy/gui/Query$$1.class' \
	ptolemy/gui/QueryListener.class \
	'ptolemy/gui/Query$$QueryScrollPane.class' \
	'ptolemy/gui/Query$$QueryFileChooser.class' \
	'ptolemy/gui/Query$$QueryColorChooser.class' \
	'ptolemy/gui/Query$$QueryActionListener.class' \
	'ptolemy/gui/Query$$QueryFocusListener.class' \
	'ptolemy/gui/Query$$QueryItemListener.class' \
	'ptolemy/gui/ComponentDialog.class' \
	'ptolemy/gui/ComponentDialog$$1.class' \
	'ptolemy/gui/ComponentDialog$$2.class' \
	ptolemy/gui/CloseListener.class

# PlotFrame uses StringUtilities.getProperty() 
# ptolemy.util has lots of classes, we need only a few of them.
PTOLEMYUTILFILES = ptolemy/util/StringUtilities.class

# Class files used in all applets and applications
BASECLASSFILES = \
		$(PTOLEMYGUIFILES) \
		$(PTOLEMYUTILFILES) \
		$(ME)/CmdLineArgException.class \
		$(ME)/EPSGraphics.class \
		$(ME)/Histogram.class \
		'$(ME)/Histogram$$1.class' \
		'$(ME)/Histogram$$2.class' \
		'$(ME)/Histogram$$3.class' \
		$(ME)/HistogramApplet.class \
		$(ME)/Plot.class \
		'$(ME)/Plot$$1.class' \
		'$(ME)/Plot$$2.class' \
		'$(ME)/Plot$$3.class' \
		'$(ME)/Plot$$4.class' \
		'$(ME)/Plot$$5.class' \
		'$(ME)/Plot$$6.class' \
		'$(ME)/Plot$$7.class' \
		$(ME)/PlotBox.class \
		'$(ME)/PlotBox$$1.class' \
		'$(ME)/PlotBox$$ButtonListener.class' \
		$(ME)/PlotDataException.class \
		$(ME)/PlotPoint.class \
		'$(ME)/Plot$$Format.class' \
		'$(ME)/PlotFormatter$$1.class' \
		'$(ME)/PlotFormatter$$2.class' \
		'$(ME)/PlotFormatter.class' \
		'$(ME)/PlotBox$$CommandListener.class' \
		'$(ME)/PlotBox$$DragListener.class' \
		'$(ME)/PlotBox$$ZoomListener.class' \
		$(ME)/img/fill.gif \
		$(ME)/img/format.gif \
		$(ME)/img/print.gif \
		$(ME)/img/reset.gif

# Class files used in simple applets
APPLETCLASSFILES = \
		$(ME)/PlotApplet.class \
		'$(ME)/PlotApplet$$1.class' \
		'$(ME)/PlotLive$$StartButtonListener.class' \
		'$(ME)/PlotLive$$StopButtonListener.class' \
		$(ME)/PlotLive.class \
		$(ME)/PlotLiveApplet.class

# Class files that permit editing and histograms, used by
# complex applets and applications
COMPLEXCLASSFILES = \
		'$(ME)/EditablePlot$$EditMouseListener.class' \
		'$(ME)/EditablePlot$$ModifyListener.class' \
		'$(ME)/EditablePlot$$UndoListener.class' \
		$(ME)/EditablePlot.class \
		$(ME)/EditListener.class \
		$(ME)/Histogram.class \
		$(ME)/HistogramApplet.class \
		'$(ME)/PlotFrame$$EPSFileFilter.class' \
		'$(ME)/PlotFrame$$FileMenuListener.class' \
		'$(ME)/PlotFrame$$FormatListener.class' \
		'$(ME)/PlotFrame$$PLTOrXMLFileFilter.class' \
		'$(ME)/PlotFrame$$SpecialMenuListener.class' \
		$(ME)/PlotFrame.class

# Class files used in applications
APPLICATIONCLASSFILES = \
		'$(ME)/PlotApplication$$1.class' \
		'$(ME)/PlotApplication$$2.class' \
		'$(ME)/PlotApplication$$3.class' \
		$(ME)/PlotApplication.class

# Jar file for simple applets
$(PTAUXJAR): $(JSRCS) $(JCLASS)
	rm -f $@
	(cd $(ROOT); "$(JAR)" cf $(ME)/$@ \
		$(BASECLASSFILES) \
		$(APPLETCLASSFILES) \
            )


# Class files used in pxgraph applets
$(PXGRAPHAPPLETJAR): $(JSRCS) $(JCLASS)
	rm -f $@
	(cd $(ROOT); "$(JAR)" cf $(ME)/$@ \
		$(BASECLASSFILES) \
		$(APPLETCLASSFILES) \
		$(ME)/compat/PxgraphApplet.class \
		$(ME)/compat/PxgraphApplication.class \
		$(ME)/compat/PxgraphParser.class \
            )

# Jar file for PlotML applets, including xml classes
$(PLOTMLAPPLETJAR): $(JSRCS) $(JCLASS)
	rm -f $@
	(cd $(ROOT); "$(JAR)" cf $(ME)/$@ \
		$(BASECLASSFILES) \
		$(APPLETCLASSFILES) \
		$(APPLICATIONCLASSFILES) \
		$(COMPLEXCLASSFILES) \
		$(ME)/plotml/*.class \
		com/microstar/xml/README.txt \
		com/microstar/xml/*.class \
            )

# Jar file for applications, including ptolemy/gui and xml classes
$(PLOTAPPLICATIONJAR): $(JSRCS) $(JCLASS)
	rm -f $@
	(cd $(ROOT); "$(JAR)" cf $(ME)/$@ \
		$(BASECLASSFILES) \
		$(APPLICATIONCLASSFILES) \
		$(COMPLEXCLASSFILES) \
		$(ME)/plotml/*.class \
		com/microstar/xml/README.txt \
		com/microstar/xml/*.class \
            )

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

# Include rules necessary to build a distribution
include $(ROOT)/mk/ptdist.mk

# Java Network Launch Protocol aka Web Start
ptplot.jnlp: ptplot.jnlp.in
	sed 's%@PTII_LOCALURL@%$(PTII_LOCALURL)%' $< > $@

FIND=/usr/bin/find
PTTMPME= $(PTTMPDIR)/$(PTDIST)/$(ME)
# Fix the temporary directory used when building a distribution
fixtmpdist:
	@echo "Fixing $(PTTMPDIR)/$(PTDIST)"
	echo "all makefiles sources install docs depend checkjunk sccsinfo sccsclean test extraclean clean: mk/ptII.mk" > $(PTTMPDIR)/$(PTDIST)/makefile
	echo '	cd ptolemy/gui; make $$@' >>$(PTTMPDIR)/$(PTDIST)/makefile
	echo '	cd ptolemy/util; make $$@' >>$(PTTMPDIR)/$(PTDIST)/makefile
	echo '	cd ptolemy/plot; make $$@' >>$(PTTMPDIR)/$(PTDIST)/makefile
	echo '	cd bin; make $$@' >>$(PTTMPDIR)/$(PTDIST)/makefile
	echo '# Generate ptII.mk by running configure' >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo 'mk/ptII.mk: configure mk/ptII.mk.in' >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo '	./configure' >> $(PTTMPDIR)/$(PTDIST)/makefile
	echo "realclean itcldocs tests idocs weblint dists:" >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo "	echo \"Nothing need be done for $$@ in ptolemy/plot)\"" >> \
		$(PTTMPDIR)/$(PTDIST)/makefile
	echo "$(PTDIST) - A Java plotting package" > \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Ptplot is a set of two dimensional signal plotters" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "implemented in Java." >>  $(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Ptplot is embeddable in applets and applications." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "For more information, see ptolemy/plot/doc/index.htm" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> $(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Installer notes:" >> $(PTTMPDIR)/$(PTDIST)/README.txt
	echo "If you are using the auto installer, then note that" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "under Windows, the Ptplot programs will be added to" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "your start menu under Ptolemy -> Ptplot.  Note further" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "that directory names with spaces do not work very well" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo " because of limitations Java 1.4.0" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "If you are building from a tar or zip file, then note that" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "the file ptplot${PTVERSION}.tar.gz or" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "ptplot${PTVERSION}.zip, will unpack into a directory" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "called ptplot${PTVERSION}, and the configure-make-make install" \
		>> $(PTTMPDIR)/$(PTDIST)/README.txt
	echo "process will not install any files outside of this " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "ptplot${PTVERSION} directory." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "So, if you unpack the TAR file into /usr/local, after " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "installing you will have /usr/local/ptplot${PTVERSION}," >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "and you may want to tell users to add " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "/usr/local/ptplot${PTVERSION}/bin to their PATH." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "The scripts in the bin directory will run the plotter" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "as a standalone application, or they can add" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "/usr/local/ptplot${PTVERSION} to their Java CLASSPATH." >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> $(PTTMPDIR)/$(PTDIST)/README.txt
	echo "To build under Solaris:" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Set the PTII variable to this directory: " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "PTII=`pwd` " >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "Then configure and make:" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "./configure" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "make install" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "bin/ptplot" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	echo "" >> \
		$(PTTMPDIR)/$(PTDIST)/README.txt
	#@echo "JShield needs the ico file to be in the top level directory"
	#cp $(PTTMPME)/doc/img/ptplot.ico \
	#	$(PTTMPDIR)/$(PTDIST)
	@echo "Copy over xml stuff"
	mkdir -p $(PTTMPDIR)/$(PTDIST)/com
	cp -r "$(PTII)/com/microstar" "$(PTII)/com/makefile" \
		$(PTTMPDIR)/$(PTDIST)/com
	@echo "Copy over bin"
	mkdir -p $(PTTMPDIR)/$(PTDIST)/bin
	cp -r   "$(PTII)/bin/ptinvoke.in" \
		"$(PTII)/bin/histogram.bat" \
		"$(PTII)/bin/ptplot.bat" \
		"$(PTII)/bin/pxgraph.bat" \
		$(PTTMPDIR)/$(PTDIST)/bin
	@echo "Create bin/makefile"
	echo "ME =		bin" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "ROOT =		.." >> 	$(PTTMPDIR)/$(PTDIST)/bin/makefile
	# Use awk here so that make variables don't get substituted
	echo "foo" | \
		awk '{	  printf("CONFIG =\t%c%s/mk/ptII.mk\n",36,"(ROOT)"); \
			 printf("include %c%s\n", 36,"(CONFIG)"); \
		}'  >> 		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "SHELL_FILES = \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	histogram \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ptplot \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	pxgraph" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "# DOS batch files" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "BAT_FILES = \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	histogram.bat \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ptplot.bat \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	pxgraph.bat " >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "EXTRA_SRCS = \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ptinvoke.in \\" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "foo" | \
		awk '{	 printf("\t%c%s\n", 36, "(BAT_FILES)")}' \
		>> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo '	$(BAT_FILES)' >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "all: ptinvoke" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	-chmod a+x ptinvoke" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	rm -f histogram" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ln -s ptinvoke histogram" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	rm -f ptplot" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ln -s ptinvoke ptplot" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	rm -f pxgraph" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	ln -s ptinvoke pxgraph" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "install: all" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "foo" | \
		awk '{	 printf("\t-chmod a+x %c%s %c%s\n", 36, "(SHELL_FILES)", 36, "(BAT_FILES)")}' >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "ptinvoke: ptinvoke.in ../config.status" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	(cd ..; ./config.status)" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "# zip might not get the permissions of configure right, so we run /bin/sh" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "../config.status: ../configure" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "	(cd ..; /bin/sh ./configure)" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "" >> $(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "# Get the rest of the rules" >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	echo "foo" | \
		awk '{	 printf("include %c%s/mk/ptcommon.mk\n", 36, "(ROOT)")}' >> \
		$(PTTMPDIR)/$(PTDIST)/bin/makefile
	@echo "Copy over gui stuff"
	mkdir $(PTTMPDIR)/$(PTDIST)/ptolemy/gui
	cp "$(PTII)/ptolemy/gui/Query.java" \
		$(PTTMPDIR)/$(PTDIST)/ptolemy/gui
	cp "$(PTII)/ptolemy/gui/QueryListener.java" \
		$(PTTMPDIR)/$(PTDIST)/ptolemy/gui
	cp "$(PTII)/ptolemy/gui/CloseListener.java" \
		$(PTTMPDIR)/$(PTDIST)/ptolemy/gui
	cp "$(PTII)/ptolemy/gui/ComponentDialog.java" \
		$(PTTMPDIR)/$(PTDIST)/ptolemy/gui
	cp "$(PTII)/ptolemy/gui/package.html" \
		$(PTTMPDIR)/$(PTDIST)/ptolemy/gui
	# Remove most of the .java files
	egrep -v "BasicJApplet|CancelException|GraphicalMessageHandler|JTextAreaExec|MessageHandler|ShellInterpreter|ShellTextArea|StatusBar|StreamExec|SwingWorker|Top.java" \
		"$(PTII)/ptolemy/gui/makefile" > \
		$(PTTMPDIR)/$(PTDIST)/ptolemy/gui/makefile
	@echo "Copy over util stuff"
	mkdir $(PTTMPDIR)/$(PTDIST)/ptolemy/util
	# Remove most of the .java files
	cp "$(PTII)/ptolemy/util/StringUtilities.java" \
		$(PTTMPDIR)/$(PTDIST)/ptolemy/util
	cp "$(PTII)/ptolemy/util/ClassUtilities.java" \
		$(PTTMPDIR)/$(PTDIST)/ptolemy/util
	cp "$(PTII)/ptolemy/util/FileUtilities.java" \
		$(PTTMPDIR)/$(PTDIST)/ptolemy/util
	cp "$(PTII)/ptolemy/util/package.html" \
		$(PTTMPDIR)/$(PTDIST)/ptolemy/util
	egrep -v "CancelException|MessageHandler|XSLTUtilities" \
		"$(PTII)/ptolemy/util/makefile" > \
		$(PTTMPDIR)/$(PTDIST)/ptolemy/util/makefile

	# Get rid of CVS directories in com and gui
	-(cd $(PTTMPDIR)/$(PTDIST); \
		$(FIND) . \( -name CVS -o -name "META-INF" -o -name ".#*" -o -name "#*" -o -name "*.zip" -o -name "*.tar.gz" -o -name "*~" -o -name "adm" -o -name "autom4te.cache" -o -name "*Build_output" -o -name ".buildinstaller*" -o -name "ptplot*locales" \) -print -exec rm -rf {} \; )
	#
	# Setting up top level doc directory
	#
	mkdir -p $(PTTMPDIR)/$(PTDIST)/doc/img
	cp -r "$(PTII)/doc/banner.htm" \
		"$(PTII)/doc/contentsheader.htm" \
		"$(PTII)/doc/default.css" \
		"$(PTII)/doc/footer.htm" \
		"$(PTII)/doc/mainheader.htm" \
		"$(PTII)/doc/toc.css" \
		 $(PTTMPDIR)/$(PTDIST)/doc
	cp -r "$(PTII)/doc/img/ptIIplanetIcon.gif" \
		"$(PTII)/doc/img/ptIIbanner3.gif" \
		"$(PTII)/doc/img/prev.gif" \
		$(PTTMPDIR)/$(PTDIST)/doc/img
	# Create a file in the top level doc directory that points to
	# the real docs
	echo "<html>" > $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "<head>" >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "<title>Ptplot</title>" > $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "</head>" >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "<h1>Ptplot</h1>" >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo 'The Ptplot documentation is in <a href="../ptolemy/plot/doc/index.htm"><code>../ptolemy/plot/doc</code>' >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "</body>" >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	echo "</htmp>" >> $(PTTMPDIR)/$(PTDIST)/doc/index.htm
	#
	# Setting up doc/codeDoc directory
	#
	mkdir -p $(PTTMPDIR)/$(PTDIST)/doc/codeDoc
	cd $(PTTMPDIR)/$(PTDIST); \
	CLASSPATH=. "$(JAVADOC)" $(JDOCFLAGS) -d doc/codeDoc \
		com.microstar.xml \
		ptolemy.gui \
		ptolemy.plot \
		ptolemy.plot.compat \
		ptolemy.plot.plotml \
		ptolemy.util
	@echo "Fixing up doc/download directory"
	rm -rf $(PTTMPME)/doc/download
	mkdir $(PTTMPME)/doc/download
	echo "<html>" > $(PTTMPME)/doc/download/index.htm
	echo "<head>" >> $(PTTMPME)/doc/download/index.htm
	echo "<title>Download Ptplot</title>" >> $(PTTMPME)/doc/download/index.htm
	echo '<body bgcolor="#faf0e6">'  >> $(PTTMPME)/doc/download/index.htm
	echo "<H1>Download Ptplot</H1>" >> $(PTTMPME)/doc/download/index.htm
	echo "The Ptplot distribution itself does not include all the available download formats." >> $(PTTMPME)/doc/download/index.htm
	echo 'See <a href="http://ptolemy.eecs.berkeley.edu/java/ptplot" target="_top"><code>http://ptolemy.eecs.berkeley.edu/java/ptplot</code></a>' >> $(PTTMPME)/doc/download/index.htm
	echo "</body>" >> $(PTTMPME)/doc/download/index.htm
	echo "</html>" >> $(PTTMPME)/doc/download/index.htm
	chmod -R a+r $(PTTMPDIR)/$(PTDIST)


isj:
	echo "Setting up for installshield"
	rm -rf $(PTTMPDIR)/installshield
	mkdir -p \
		$(PTTMPDIR)/installshield/doc/$(PTDIST)/ptolemy/plot \
		$(PTTMPDIR)/installshield/src/ \
		$(PTTMPDIR)/installshield/runtime/$(PTDIST)/ptolemy/plot/plotml
	cp -r $(PTTMPDIR)/$(PTDIST) $(PTTMPDIR)/installshield/src
	@echo "Setup doc dir"
	mv $(PTTMPDIR)/installshield/src/$(PTDIST)/doc \
		$(PTTMPDIR)/installshield/doc/$(PTDIST)
	mv 	$(PTTMPDIR)/installshield/src/$(PTDIST)/ptolemy/plot/doc \
		$(PTTMPDIR)/installshield/src/$(PTDIST)/ptolemy/plot/demo \
		$(PTTMPDIR)/installshield/doc/$(PTDIST)/ptolemy/plot
	mv $(PTTMPDIR)/installshield/src/$(PTDIST)/copyright.txt \
		$(PTTMPDIR)/installshield/src/$(PTDIST)/ptplot.ico \
		$(PTTMPDIR)/installshield/runtime/$(PTDIST)
	@echo "Copy .jar files to runtime dir"
	(cd $(PTTMPDIR)/installshield/src/$(PTDIST); tar -cf - `$(FIND) . -name "*.jar" -print`) | ( cd $(PTTMPDIR)/installshield/runtime/$(PTDIST); tar -xf -)
	mv $(PTTMPDIR)/installshield/src/$(PTDIST)/ptolemy/plot/plotml/*.dtd \
		$(PTTMPDIR)/installshield/runtime/$(PTDIST)/ptolemy/plot/plotml
	@echo "Remove the .jar and .class files from the src dir"
	(cd $(PTTMPDIR)/installshield/src/$(PTDIST); \
		$(FIND) . \( -name "*.class" -o -name "*.jar" \) -exec rm {} \;)
	@echo "Cleanup META-INF dirs etc"
	(cd $(PTTMPDIR)/installshield/; \
		$(FIND) . \( -name "META-INF" -o -name "autom4te.cache" -o -name "#*" -o -name CVS \) -print -exec rm -rf {} \;


# Update the ftp site
# Update a location with the files necessary to download
DIST_BASE = /vol/ptolemy/pt0/ptweb/java
DIST_DIR = $(DIST_BASE)/$(PTDIST)


update_ftp:
	rm -rf $(DIST_DIR)
	cat $(PTDIST).tar.gz | (cd $(DIST_BASE); tar -zxf -)
	cp -r $(DIST_BASE)/$(PTDIST)_tmp/* $(DIST_DIR)/ptolemy/plot/doc/download
	cp doc/download/index.htm $(DIST_DIR)/ptolemy/plot/doc/download
	cp $(PTDIST).tar.gz $(DIST_DIR)/ptolemy/plot/
	cp $(PTDIST).zip $(DIST_DIR)/ptolemy/plot/
