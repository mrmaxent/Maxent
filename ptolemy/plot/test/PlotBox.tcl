# Tests for the PlotBox class
#
# @Author: Christopher Hylands, Edward A. Lee
#
# @Version: $Id: PlotBox.tcl,v 1.17 2005/02/28 19:48:21 cxh Exp $
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
#######################################################################

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test PlotBox-1.1 {} {
    set frame [java::new javax.swing.JFrame]
    set plot [java::new ptolemy.plot.PlotBox]
    $frame pack
    [$frame getContentPane] add $plot
    $frame setSize 500 300
    $frame show
    $frame repaint
    $plot setTitle "foo"
    $plot addXTick "X tick" -10
    $plot addYTick "Y tick" 10.1
    $plot setGrid false
    $plot repaint
    $plot clear true
    $plot repaint
    $plot fillPlot
} {}

test PlotBox-1.2 {addLegend, getLegend} {
    $plot addLegend 1 "A Legend"
    $plot addLegend 2 "Another Legend"
    $plot addLegend 3 "3rd Legend"
    $plot getLegend 2
} {Another Legend}

test PlotBox-2.1 {setDataurl, getDataurl, getDocumentBase} {
    $plot setDataurl ../demo/data.plt
    set url [$plot getDataurl]
    set docbase [$plot getDocumentBase]
    list $url [java::isnull $docbase]
} {../demo/data.plt 1}


test PlotBox-2.2 {setDataurl, getDataurl, getDocumentBase, setDocumentBase} {
    $plot setDataurl http://notasite/bar/foo.plt
    set url [$plot getDataurl]
    set docbase [$plot getDocumentBase]
    $plot setDocumentBase [java::new java.net.URL "http://notasite"]
    set newdocbase [$plot getDocumentBase]
    # Reset so we don't break tests below
    $plot setDocumentBase $docbase
    list $url [java::isnull $docbase] [$newdocbase toString]
} {http://notasite/bar/foo.plt 1 http://notasite/}

test PlotBox-3.1 {getMinimumSize getPreferredSize} {
    #$frame setSize 425 600
    #$frame repaint
    set minimumDimension [$plot getMinimumSize] 
    set preferredDimension [$plot getPreferredSize]
    # The results vary depending on the platform, so we just return {}
    #list [java::field $minimumDimension width] \
    #	    [java::field $minimumDimension height] \
    #	    [java::field $preferredDimension width] \
    #       [java::field $preferredDimension height]
    list {}

} {{}}

test PlotBox-3.5 {samplePlotn} {
    # Call samplePlot just to be sure it works.
    # Note that we call it before calling parseFile
    $plot samplePlot
    $plot repaint
} {}

test PlotBox-4.1 {parseFile} {
    $plot parseFile ../demo/data.plt
    $plot repaint
} {}

test PlotBox-4.5 {read} {
    set file [java::new {java.io.File java.lang.String java.lang.String} \
	    "../demo" "bargraph.plt"]
    set fileInputStream \
	    [java::new {java.io.FileInputStream java.lang.String} "../demo/bargraph.plt"]
    $plot read $fileInputStream
} {}

test PlotBox-5.1 {getColorByName} {
    set color [$plot getColorByName "red"]
    $color toString
} {java.awt.Color[r=255,g=0,b=0]}


test PlotBox-6.1 {setButtons} {
    $plot setButtons false
    $plot repaint
    $plot setButtons true
    $plot repaint
} {}

test PlotBox-7.1 {setSize} {
    $plot setSize 420 420
    $plot repaint
    $plot setSize 400 440
    $plot repaint
} {}

test PlotBox-8.1 {setBackground} {
    set color [$plot getColorByName "red"]
    $plot setBackground $color
    set color [$plot getColorByName "green"]
    $plot setForeground $color
    $plot repaint
} {}

test PlotBox-8.2 {setColor} {
    $plot setColor true
    $plot repaint
    set r1 [$plot getColor]
    $plot setColor false
    $plot repaint
    list $r1 [$plot getColor]
} {1 0}

test PlotBox-9.1 {setGrid} {
    $plot setGrid false
    $plot repaint
    set r1 [$plot getGrid]
    $plot setGrid true
    $plot repaint
    list $r1 [$plot getGrid]
} {0 1}

test PlotBox-10.1 {setTitleFont} {
    $plot setTitleFont Courier-BOLD-16
    $plot repaint
} {}

test PlotBox-10.2 {getTitle} {
    $plot getTitle
} {Software Downloads}


test PlotBox-12.1 {setXRange} {
    $plot setXRange 0.001 10
    $plot setYRange 1 1000
    $plot repaint
} {}

test PlotBox-13.1 {zoom} {
    $plot zoom 1 2 3 4
    $plot repaint
} {}

test PlotBox-14.1 {write} {
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
	    {java.io.PrintStream java.io.OutputStream} $stream]
    $plot write $printStream xxx
    $printStream flush
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
                [$stream toString] "\n" output

    list $output
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE plot SYSTEM "xxx">
<plot>
<!-- Ptolemy plot, version 3.1, PlotML format. -->
<title>Software Downloads</title>
<xLabel>Year</xLabel>
<yLabel>Downloads</yLabel>
<xRange min="0.0010" max="10.0"/>
<yRange min="1.0" max="1000.0"/>
<xTicks>
  <tick label="1993" position="0.0"/>
  <tick label="1994" position="1.0"/>
  <tick label="1995" position="2.0"/>
  <tick label="1996" position="3.0"/>
  <tick label="1997" position="4.0"/>
  <tick label="1998" position="5.0"/>
  <tick label="1999" position="6.0"/>
  <tick label="2000" position="7.0"/>
  <tick label="2001" position="8.0"/>
  <tick label="2002" position="9.0"/>
  <tick label="2003" position="10.0"/>
</xTicks>
<noColor/>
</plot>
}}

test PlotBox-14.2 {write with DTD included} {
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
	    {java.io.PrintStream java.io.OutputStream} $stream]
    # Call with only one arg, so we get the dtd
    $plot write $printStream
    $printStream flush
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
                [$stream toString] "\n" output

    list $output
} {{<?xml version="1.0" standalone="yes"?>
<!DOCTYPE plot [
<!-- PlotML DTD, created by Edward A. Lee, eal@eecs.berkeley.edu. --><!ELEMENT plot (barGraph | bin | dataset | default | noColor | 
	noGrid | title | wrap | xLabel | xLog | xRange | xTicks | yLabel | 
 yLog | yRange | yTicks)*>
  <!ELEMENT barGraph EMPTY>
    <!ATTLIST barGraph width CDATA #IMPLIED>
    <!ATTLIST barGraph offset CDATA #IMPLIED>
  <!ELEMENT bin EMPTY>
    <!ATTLIST bin width CDATA #IMPLIED>
    <!ATTLIST bin offset CDATA #IMPLIED>
  <!ELEMENT dataset (m | move | p | point)*>
    <!ATTLIST dataset connected (yes | no) #IMPLIED>
    <!ATTLIST dataset marks (none | dots | points | various) #IMPLIED>
    <!ATTLIST dataset name CDATA #IMPLIED>
    <!ATTLIST dataset stems (yes | no) #IMPLIED>
  <!ELEMENT default EMPTY>
    <!ATTLIST default connected (yes | no) "yes">
    <!ATTLIST default marks (none | dots | points | various) "none">
    <!ATTLIST default stems (yes | no) "no">
  <!ELEMENT noColor EMPTY>
  <!ELEMENT noGrid EMPTY>
  <!ELEMENT title (#PCDATA)>
  <!ELEMENT wrap EMPTY>
  <!ELEMENT xLabel (#PCDATA)>
  <!ELEMENT xLog EMPTY>
  <!ELEMENT xRange EMPTY>
    <!ATTLIST xRange min CDATA #REQUIRED>
    <!ATTLIST xRange max CDATA #REQUIRED>
  <!ELEMENT xTicks (tick)+>
  <!ELEMENT yLabel (#PCDATA)>
  <!ELEMENT yLog EMPTY>
  <!ELEMENT yRange EMPTY>
    <!ATTLIST yRange min CDATA #REQUIRED>
    <!ATTLIST yRange max CDATA #REQUIRED>
  <!ELEMENT yTicks (tick)+>
    <!ELEMENT tick EMPTY>
      <!ATTLIST tick label CDATA #REQUIRED>
      <!ATTLIST tick position CDATA #REQUIRED>
    <!ELEMENT m EMPTY>
      <!ATTLIST m x CDATA #IMPLIED>
      <!ATTLIST m x CDATA #REQUIRED>
      <!ATTLIST m lowErrorBar CDATA #IMPLIED>
      <!ATTLIST m highErrorBar CDATA #IMPLIED>
    <!ELEMENT move EMPTY>
      <!ATTLIST move x CDATA #IMPLIED>
      <!ATTLIST move x CDATA #REQUIRED>
      <!ATTLIST move lowErrorBar CDATA #IMPLIED>
      <!ATTLIST move highErrorBar CDATA #IMPLIED>
    <!ELEMENT p EMPTY>
      <!ATTLIST p x CDATA #IMPLIED>
      <!ATTLIST p x CDATA #REQUIRED>
      <!ATTLIST p lowErrorBar CDATA #IMPLIED>
      <!ATTLIST p highErrorBar CDATA #IMPLIED>
    <!ELEMENT point EMPTY>
      <!ATTLIST point x CDATA #IMPLIED>
      <!ATTLIST point x CDATA #REQUIRED>
      <!ATTLIST point lowErrorBar CDATA #IMPLIED>
      <!ATTLIST point highErrorBar CDATA #IMPLIED>
]>
<plot>
<!-- Ptolemy plot, version 3.1, PlotML format. -->
<title>Software Downloads</title>
<xLabel>Year</xLabel>
<yLabel>Downloads</yLabel>
<xRange min="0.0010" max="10.0"/>
<yRange min="1.0" max="1000.0"/>
<xTicks>
  <tick label="1993" position="0.0"/>
  <tick label="1994" position="1.0"/>
  <tick label="1995" position="2.0"/>
  <tick label="1996" position="3.0"/>
  <tick label="1997" position="4.0"/>
  <tick label="1998" position="5.0"/>
  <tick label="1999" position="6.0"/>
  <tick label="2000" position="7.0"/>
  <tick label="2001" position="8.0"/>
  <tick label="2002" position="9.0"/>
  <tick label="2003" position="10.0"/>
</xTicks>
<noColor/>
</plot>
}}

test PlotBox-14.3 {writeOldSyntax} {
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
	    {java.io.PrintStream java.io.OutputStream} $stream]
    $plot writeOldSyntax $printStream
    $printStream flush
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
                [$stream toString] "\n" output

    list $output
} {{# Ptolemy plot, version 2.0
TitleText: Software Downloads
XLabel: Year
YLabel: Downloads
XRange: 0.0010, 10.0
YRange: 1.0, 1000.0
XTicks: "1993" 0.0, "1994" 1.0, "1995" 2.0, "1996" 3.0, "1997" 4.0, "1998" 5.0, "1999" 6.0, "2000" 7.0, "2001" 8.0, "2002" 9.0, "2003" 10.0
Color: off
}}

test PlotBox-15.1 {export} {
    set stream [java::new java.io.ByteArrayOutputStream]
    $plot export $stream
    $stream flush
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
                [$stream toString] "\n" output
    # Since the output of export is platform dependent and huge,
    # We just test to see that the results is greater than 1000 chars in length
    list [expr {[string length "$output"] > 1000}]
} {1}

test PlotBox-16.1 {setXLabel, setYLabel, getXLabel, getYLabel} {
    $plot setXLabel "The X axis"
    $plot setYLabel "The Y axis"
    $plot repaint
    list [$plot getXLabel] [$plot getYLabel]
} {{The X axis} {The Y axis}}

# FIXME: we need to test setting log axes and then saving
# the results and also setting the ranges
test PlotBox-17.1 {setXLog, setYLog, getXLog, getYLog} {
    $plot setXLog true
    $plot setYLog true
    $plot repaint
    set r1 [$plot getXLog]
    set r2 [$plot getYLog]
    $plot setXLog false
    $plot setYLog false
    $plot repaint
    list $r1 $r2
} {1 1}

test PlotBox-18.1 {getXRange, getYRange} {
    set xrange [$plot getXRange]
    set yrange [$plot getYRange]
    list [$xrange getrange] [$yrange getrange]
} {{0.001 10.0} {1.0 1000.0}}

test PlotBox-18.2 {setXRange, setYRange} {
    $plot setXRange 0.002 11.0
    $plot setYRange 0.5 1020.0
    set xrange [$plot getXRange]
    set yrange [$plot getYRange]
    list [$xrange getrange] [$yrange getrange]
} {{0.002 11.0} {0.5 1020.0}}


test PlotBox-19.1 {getXTicks, getYTicks} {
    set xticks [$plot getXTicks]
    set xtick0 [$xticks get 0]
    set xtick1 [$xticks get 1]
    set yticks [$plot getYTicks]
    list [$xtick0 toString] [$xtick1 toString] [java::isnull $yticks]
} {{[0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0]} {[1993, 1994, 1995, 1996, 1997, 1998, 1999, 2000, 2001, 2002, 2003]} 1}

test PlotBox-20.1 {read} {
    $plot read "Title: Read Title Test"
    list [$plot getTitle]
} {}

# FIXME: Need a better test of setWrap 
test PlotBox-21.1 {setWrap} {
    $plot setWrap true
} {}


# Close the window
