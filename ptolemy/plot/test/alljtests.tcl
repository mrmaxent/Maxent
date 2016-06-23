# CAUTION: automatically generated file by a rule in ptcommon.mk
# This file will source all the Tcl files that use Java. 
# This file will source the tcl files list in the
# makefile SIMPLE_JTESTS and GRAPHICAL_JTESTS variables
# This file is different from all.itcl in that all.itcl
# will source all the .itcl files in the current directory
#
# Set the following to avoid endless calls to exit
if {![info exists reallyExit]} {set reallyExit 0}
# Exiting when there are no more windows is wrong
#::tycho::TopLevel::exitWhenNoMoreWindows 0
# If there is no update command, define a dummy proc.  Jacl needs this
if {[info command update] == ""} then { 
    proc update {} {}
}
#Do an update so that we are sure tycho is done displaying
update
set savedir "[pwd]"
if {"dummy.tcl" != ""} {foreach i [list dummy.tcl] {puts $i; cd "$savedir"; if [ file exists $i ] { if [ catch {source $i} msg] {puts "\nWARNING: Sourcing $i resulted in an error,\nso we are incrementing the error count.\nThe error was: $msg\n"; incr FAILED}}}}
puts stderr HistogramMLApplication.tcl
cd "$savedir"
if [ file exists HistogramMLApplication.tcl ] { if [catch {source HistogramMLApplication.tcl} msg] {puts "\nWARNING: Sourcing $x resulted in an error,\nso we are incrementing the error count\nThe error was $msg\n"; incr FAILED}}
puts stderr PlotApplication.tcl
cd "$savedir"
if [ file exists PlotApplication.tcl ] { if [catch {source PlotApplication.tcl} msg] {puts "\nWARNING: Sourcing $x resulted in an error,\nso we are incrementing the error count\nThe error was $msg\n"; incr FAILED}}
puts stderr PlotBox.tcl
cd "$savedir"
if [ file exists PlotBox.tcl ] { if [catch {source PlotBox.tcl} msg] {puts "\nWARNING: Sourcing $x resulted in an error,\nso we are incrementing the error count\nThe error was $msg\n"; incr FAILED}}
puts stderr Pxgraph.tcl
cd "$savedir"
if [ file exists Pxgraph.tcl ] { if [catch {source Pxgraph.tcl} msg] {puts "\nWARNING: Sourcing $x resulted in an error,\nso we are incrementing the error count\nThe error was $msg\n"; incr FAILED}}
catch {doneTests}
exit
