/* Demo for a live signal plotter.

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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ptolemy.plot.PlotLive;


//////////////////////////////////////////////////////////////////////////
//// PlotLiveDemo

/**
   Dynamically plot a test signal, illustrating how to use the
   PlotLive class.

   @author Edward A. Lee
   @version $Id: PlotLiveDemo.java,v 1.66 2005/04/29 20:03:27 cxh Exp $
   @since Ptolemy II 0.2
   @Pt.ProposedRating red (eal)
   @Pt.AcceptedRating red (cxh)
*/
public class PlotLiveDemo extends PlotLive {
    /** Construct a plot for live, animated signal display.
     *  Configure the title, axes, points style, and persistence.
     */
    public PlotLiveDemo() {
        setYRange(-1, 1);
        setXRange(-1, 1);
        setPointsPersistence(60);
        setMarksStyle("dots");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add points to the plot.  This is called by the base class
     *  run() method when the plot is live.
     */
    public synchronized void addPoints() {
        // You could plot multiple points at a time here
        // for faster response, but in our case, we really need
        // to slow down the response for visual aesthetics.
        addPoint(0, Math.sin((Math.PI * _count) / 25),
                Math.cos((Math.PI * _count) / 100), false);
        addPoint(0, Math.sin((Math.PI * _count) / 45),
                Math.cos((Math.PI * _count) / 70), true);
        addPoint(1, Math.sin((Math.PI * _count) / 45),
                Math.cos((Math.PI * _count) / 70), false);
        addPoint(2, Math.sin((Math.PI * _count) / 20),
                Math.cos((Math.PI * _count) / 100), false);
        addPoint(3, Math.sin((Math.PI * _count) / 50),
                Math.cos((Math.PI * _count) / 70), false);
        _count += 1.0;

        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
        }
    }

    /** Run the demo as an application.
     *        This is very useful for debugging.  The command to run would be
     * java -classpath $PTII ptolemy.plot.demo.PlotLiveDemo
     */
    public static void main(String[] args) {
        // Run this in the Swing Event Thread.
        Runnable doActions = new Runnable() {
                public void run() {
                    try {
                        final PlotLiveDemo plotLiveDemo = new PlotLiveDemo();

                        JFrame frame = new JFrame("PlotLiveDemo");
                        frame.addWindowListener(new WindowAdapter() {
                                public void windowClosing(WindowEvent event) {
                                    plotLiveDemo.stop();
                                    System.exit(0);
                                }
                            });
                        frame.getContentPane().add("Center", plotLiveDemo);
                        frame.show();
                        plotLiveDemo.setButtons(true);
                        plotLiveDemo.start();
                        frame.pack();
                    } catch (Exception ex) {
                        System.err.println(ex.toString());
                        ex.printStackTrace();
                    }
                }
            };

        try {
            SwingUtilities.invokeAndWait(doActions);
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial Value being plotted */
    private double _count = 0.0;
}
