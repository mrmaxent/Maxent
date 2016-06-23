# Tests for the PlotApplication class
#
# @Author: Christopher Hylands, Edward A. Lee
#
# @Version: $Id: PlotApplication.tcl,v 1.10 2005/02/28 19:48:13 cxh Exp $
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

########################################
#### plotApplicationTest
# Pass arguments to Pxgraph, run it, sleep, then return
#
proc plotApplicationTest { args } {
    set jargs [java::new {String[]} [llength $args] $args ]
    set plotapp [java::new ptolemy.plot.PlotApplication $jargs]
    set thread [java::call Thread currentThread ]
    # sleep 2 seconds
    $thread sleep 2000
    $plotapp dispose
}

######################################################################
####
#
test PlotApplication-1.1 {} {
    plotApplicationTest ""
} {}
