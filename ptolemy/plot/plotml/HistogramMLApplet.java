/* Applet containing the Histogram class.

@Copyright (c) 1997-2005 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

PT_COPYRIGHT_VERSION_2
COPYRIGHTENDKEY
*/
package ptolemy.plot.plotml;

import ptolemy.plot.Histogram;
import ptolemy.plot.PlotBox;


//////////////////////////////////////////////////////////////////////////
//// HistogramMLApplet

/**
   This applet reads a URL giving a PlotML file that contains data.
   It plots a histogram of that data.

   @see Histogram
   @author Edward A. Lee
   @version $Id: HistogramMLApplet.java,v 1.24 2005/04/25 22:52:16 cxh Exp $
   @since Ptolemy II 0.4
   @Pt.ProposedRating red (eal)
   @Pt.AcceptedRating red (cxh)
*/
public class HistogramMLApplet extends PlotMLApplet {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "HistogramMLApplet " + PlotBox.PTPLOT_RELEASE
            + ": A histogram plotter.\n" + "By: Edward A. Lee.\n"
            + "($Id: HistogramMLApplet.java,v 1.24 2005/04/25 22:52:16 cxh Exp $)";
    }

    /** Create a new Plot object for the applet.  Derived classes can
     *  redefine this method to return a different type of plot object.
     */
    public PlotBox newPlot() {
        return new Histogram();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a new parser object for the applet.
     */
    protected PlotMLParser _newParser() {
        return new HistogramMLParser((Histogram) plot());
    }
}
