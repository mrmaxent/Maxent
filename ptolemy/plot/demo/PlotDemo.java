/* Demo for a signal plotter.

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

import ptolemy.plot.Plot;
import ptolemy.plot.PlotApplet;


//////////////////////////////////////////////////////////////////////////
//// PlotDemo

/**
   Plot a variety of test signals.

   @author Edward A. Lee
   @version $Id: PlotDemo.java,v 1.48 2005/04/25 22:48:51 cxh Exp $
   @since Ptolemy II 0.2
   @Pt.ProposedRating red (eal)
   @Pt.AcceptedRating red (cxh)
*/
public class PlotDemo extends PlotApplet {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "PlotDemo 2.0: Demo of Plot.\n" + "By: Edward A. Lee\n "
            + "($Id: PlotDemo.java,v 1.48 2005/04/25 22:48:51 cxh Exp $)";
    }

    /**
     * Initialize the applet.  Here we step through an example of what the
     * the applet can do.
     */
    public void init() {
        super.init();

        Plot plot = (Plot) plot();

        plot.setTitle("Line Plot Demo");
        plot.setYRange(-4, 4);
        plot.setXRange(0, 100);
        plot.setXLabel("time");
        plot.setYLabel("value");
        plot.addYTick("-PI", -Math.PI);
        plot.addYTick("-PI/2", -Math.PI / 2);
        plot.addYTick("0", 0);
        plot.addYTick("PI/2", Math.PI / 2);
        plot.addYTick("PI", Math.PI);
        plot.setMarksStyle("none");
        plot.setImpulses(true);

        boolean first = true;

        for (int i = 0; i <= 100; i++) {
            plot.addPoint(0, (double) i, 5 * Math.cos((Math.PI * i) / 20),
                    !first);
            plot.addPoint(1, (double) i, 4.5 * Math.cos((Math.PI * i) / 25),
                    !first);
            plot.addPoint(2, (double) i, 4 * Math.cos((Math.PI * i) / 30),
                    !first);
            plot.addPoint(3, (double) i, 3.5 * Math.cos((Math.PI * i) / 35),
                    !first);
            plot.addPoint(4, (double) i, 3 * Math.cos((Math.PI * i) / 40),
                    !first);
            plot.addPoint(5, (double) i, 2.5 * Math.cos((Math.PI * i) / 45),
                    !first);
            plot.addPoint(6, (double) i, 2 * Math.cos((Math.PI * i) / 50),
                    !first);
            plot.addPoint(7, (double) i, 1.5 * Math.cos((Math.PI * i) / 55),
                    !first);
            plot.addPoint(8, (double) i, 1 * Math.cos((Math.PI * i) / 60),
                    !first);
            plot.addPoint(9, (double) i, 0.5 * Math.cos((Math.PI * i) / 65),
                    !first);
            first = false;
        }
    }
}
