/* A simple plot application with two plots

Copyright (c) 1998-2005 The Regents of the University of California.
All rights reserved.
Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ptolemy.plot.Plot;


//////////////////////////////////////////////////////////////////////////
//// TwoPlotExample

/**
   TwoPlotExample is a simple example that uses displays two plots side by side
   To compile and run this application, do the following:
   <pre>
   javac -classpath ../../.. TwoPlotExample.java
   java -classpath ../../.. ptolemy.plot.demo.TwoPlotExample
   </pre>

   @author Christopher Hylands
   @version $Id: TwoPlotExample.java,v 1.34 2005/04/29 20:03:27 cxh Exp $
   @since Ptolemy II 0.2
   @Pt.ProposedRating red (eal)
   @Pt.AcceptedRating red (cxh)
*/
public class TwoPlotExample extends JFrame {
    /** We use a constructor here so that we can call methods
     *  directly on the Frame.  The main method is static
     *  so getting at the Frame is a little trickier.
     */
    TwoPlotExample() {
        // Instantiate the two plots.
        Plot leftPlot = new Plot();
        Plot rightPlot = new Plot();

        // Set the size of the toplevel window.
        setSize(800, 300);

        // Create the left plot by calling methods.
        // Note that most of these methods should be called in
        // the event thread, see the Plot.java class comment.
        // In this case, main() is invoking this constructor in
        // the event thread.
        leftPlot.setSize(350, 300);
        leftPlot.setButtons(true);
        leftPlot.setTitle("Left Plot");
        leftPlot.setYRange(-4, 4);
        leftPlot.setXRange(0, 100);
        leftPlot.setXLabel("time");
        leftPlot.setYLabel("value");
        leftPlot.addYTick("-PI", -Math.PI);
        leftPlot.addYTick("-PI/2", -Math.PI / 2);
        leftPlot.addYTick("0", 0);
        leftPlot.addYTick("PI/2", Math.PI / 2);
        leftPlot.addYTick("PI", Math.PI);
        leftPlot.setMarksStyle("none");
        leftPlot.setImpulses(true);

        // Call setConnected before reading in data.
        leftPlot.setConnected(false, 1);

        boolean first = true;

        for (int i = 0; i <= 100; i++) {
            leftPlot.addPoint(0, (double) i, 5 * Math.cos((Math.PI * i) / 20),
                    !first);
            leftPlot.addPoint(1, (double) i,
                    4.5 * Math.cos((Math.PI * i) / 25), !first);
            leftPlot.addPoint(2, (double) i, 4 * Math.cos((Math.PI * i) / 30),
                    !first);
            leftPlot.addPoint(3, (double) i,
                    3.5 * Math.cos((Math.PI * i) / 35), !first);
            leftPlot.addPoint(4, (double) i, 3 * Math.cos((Math.PI * i) / 40),
                    !first);
            leftPlot.addPoint(5, (double) i,
                    2.5 * Math.cos((Math.PI * i) / 45), !first);
            leftPlot.addPoint(6, (double) i, 2 * Math.cos((Math.PI * i) / 50),
                    !first);
            leftPlot.addPoint(7, (double) i,
                    1.5 * Math.cos((Math.PI * i) / 55), !first);
            leftPlot.addPoint(8, (double) i, 1 * Math.cos((Math.PI * i) / 60),
                    !first);
            leftPlot.addPoint(9, (double) i,
                    0.5 * Math.cos((Math.PI * i) / 65), !first);
            first = false;
        }

        // Create the right plot by reading in a file.
        rightPlot.setButtons(true);
        leftPlot.setSize(350, 300);

        File file = new File(".", "data.plt");

        try {
            rightPlot.clear(true);
            rightPlot.read(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + file + " : " + ex);
        } catch (IOException ex) {
            System.err.println("Error reading input: " + file + " : " + ex);
        }

        // Override the title in the file.
        rightPlot.setTitle("Right Plot");

        // Layout the two plots
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        getContentPane().setLayout(gridbag);

        // Handle the leftPlot
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        gridbag.setConstraints(leftPlot, c);
        getContentPane().add(leftPlot);

        // Handle the rightPlot
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        gridbag.setConstraints(rightPlot, c);
        getContentPane().add(rightPlot);

        show();
    }

    /** main method called in a standalone java application.
     *  We simple instantiate this class, most of the work
     *  happens in the constructor.
     */
    public static void main(String[] args) {
        // We execute everything in the Swing Event Thread, see
        // the comment
        Runnable doAction = new Runnable() {
                public void run() {
                    new TwoPlotExample();
                }
            };

        SwingUtilities.invokeLater(doAction);
    }
}
