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
//// PlotFourierSeries

/**
   Plot a Fourier series approximation to a square wave.
   This is a demonstration of the use of the Plot class.

   @author Edward A. Lee
   @version $Id: PlotFourierSeries.java,v 1.42 2005/04/25 22:48:54 cxh Exp $
   @since Ptolemy II 0.2
   @Pt.ProposedRating red (eal)
   @Pt.AcceptedRating red (cxh)
*/
public class PlotFourierSeries extends PlotApplet {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "PlotFourierSeries 1.1: Demo of PlotApplet.\n"
            + "By: Edward A. Lee\n "
            + "($Id: PlotFourierSeries.java,v 1.42 2005/04/25 22:48:54 cxh Exp $)";
    }

    /**
     * Initialize the applet.
     */
    public void init() {
        super.init();

        Plot plot = (Plot) plot();

        plot.setTitle("Fourier Series Approximation to a Square Wave");
        plot.setXRange(0, 400);
        plot.setMarksStyle("none");
        plot.addLegend(0, "ideal");
        plot.addLegend(1, "1 sinusoid");

        for (int j = 2; j <= 10; j++) {
            plot.addLegend(j, j + " sinusoids");
        }

        boolean first = true;
        plot.addPoint(0, 0.0, 0.0, false);

        for (int i = 0; i <= 400; i++) {
            double approximation = 0.0;

            for (int j = 1; j <= 10; j++) {
                double sig = (4.0 * Math.sin((i * 2.0 * Math.PI * ((2 * j) - 1)) / 400.0)) / (Math.PI * ((2 * j)
                                                                                                      - 1));
                approximation += sig;
                plot.addPoint(j, (double) i, approximation, !first);
            }

            first = false;

            if (i <= 200) {
                plot.addPoint(0, (double) i, 1.0, true);
            }

            if (i >= 200) {
                plot.addPoint(0, (double) i, -1.0, true);
            }
        }

        plot.addPoint(0, 400.0, 0.0, true);
    }
}
