# Load test bed definitions
#
# @Author: Christopher Hylands
#
# @Version: $Id: testDefs.tcl,v 1.9 2005/02/28 21:00:22 cxh Exp $
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
#######################################################################

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

if [info exist env(PTOLEMY)] {
    set PTII $env(PTOLEMY)/tycho/java
}

if [info exist env(TYCHO)] {
    set PTII $env(TYCHO)/java
}

if [info exist env(PTII)] {
    set PTII $env(PTII)
}

if {![info exist PTII]} {
    # If we are here, then we are probably running jacl and we can't
    # read environment variables
    set PTII [file join [pwd] .. .. .. ]
}

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source [file join $PTII util testsuite testDefs.tcl]
}
