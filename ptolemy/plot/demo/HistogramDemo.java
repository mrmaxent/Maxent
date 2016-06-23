/* HistogramDemo

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
package ptolemy.plot.demo;

import ptolemy.plot.HistogramApplet;


//////////////////////////////////////////////////////////////////////////
//// HistogramDemo

/**
   A Histogram demo.  Data can be given in ASCII format at a URL.
   If none is given, then a sample histogram is generated.

   @author Edward A. Lee
   @version $Id: HistogramDemo.java,v 1.16 2005/04/25 17:19:44 cxh Exp $
   @since Ptolemy II 0.4
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (cxh)
*/
public class HistogramDemo extends HistogramApplet {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.  Read the applet parameters.
     */
    public void init() {
        super.init();

        // samplePlot runs in the event thread, so no need to do it here.
        plot().samplePlot();
    }
}
