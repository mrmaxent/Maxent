/* Appletable Plotter

@Author: Edward A. Lee and Christopher Hylands

@Version: $Id: PlotApplet.java,v 1.73 2005/04/25 22:49:23 cxh Exp $

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
package ptolemy.plot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;


//////////////////////////////////////////////////////////////////////////
//// PlotApplet

/**
   An Applet that can plot data from a URL.
   The URL should be specified using the dataurl applet parameter.
   The formatting commands are included in the file with the
   the data.
   If no URL is given, then a sample plot is generated.

   @author Edward A. Lee, Christopher Hylands, Contributor: Roger Robins
   @version $Id: PlotApplet.java,v 1.73 2005/04/25 22:49:23 cxh Exp $
   @since Ptolemy II 0.2
   @Pt.ProposedRating Yellow (cxh)
   @Pt.AcceptedRating Yellow (cxh)
   @see PlotBox
   @see Plot
*/
public class PlotApplet extends JApplet {
    //     /** Construct a Plot applet */
    //     public PlotApplet() {
    // FIXME: having the mutex causes applets to hang.
    // The mutex was introduced to Work around problem in Java
    // 1.3.1_08 where if we create 6 instances of a Plot applet then
    // navigate forward then back - IE and Navigator hang.
    // However, since we are now operating inside the Swing Event
    // thread, I think this code is unnecessary.
    //         if (_mutex == null) {
    //             _mutex = new Object();
    //         }
    //     }

    /** Return a string describing this applet.
     *  @return A string describing the applet.
     */
    public String getAppletInfo() {
        return "PlotApplet " + PlotBox.PTPLOT_RELEASE + ": A data plotter.\n"
            + "By: Edward A. Lee and\n " + "Christopher Hylands\n"
            + "($Id: PlotApplet.java,v 1.73 2005/04/25 22:49:23 cxh Exp $)";
    }

    /** Return information about parameters.
     *  @return A array of arrays giving parameter names, the type,
     *   and the default value or description.
     */
    public String[][] getParameterInfo() {
        String[][] pinfo = {
            {
                "background",
                "hexcolor value",
                "background color"
            },
            {
                "foreground",
                "hexcolor value",
                "foreground color"
            },
            {
                "dataurl",
                "url",
                "the URL of the data to plot"
            },
            {
                "height",
                "integer",
                "100"
            },
            {
                "width",
                "integer",
                "100"
            },
        };
        return pinfo;
    }

    /** Initialize the applet.  Read the applet parameters.

    *  Subclasses that extend this method and call Swing UI methods
    *  should do so in the Swing Event thread by calling
    *  SwingUtilities.invokeAndWait().
    *  Note that some Plot methods will automatically run in the
    *  Swing Event thread, some will not.
    *  For details about SwingUtilities.invokeAndWait(), see
    *  <a href="http://java.sun.com/docs/books/tutorial/uiswing/components/applet.html#thread">The Sun Applet Tutorial</a>
    */
    public void init() {
        super.init();

        // FIXME: having the mutex causes applets to hang.
        //synchronized (_mutex) {
        Runnable doActions = new Runnable() {
                public void run() {
                    if (_plot == null) {
                        _plot = newPlot();
                    }

                    getContentPane().add(plot(), BorderLayout.NORTH);

                    // Process the width and height applet parameters
                    int width;

                    // Process the width and height applet parameters
                    int height;
                    String widthspec = getParameter("width");

                    if (widthspec != null) {
                        width = Integer.parseInt(widthspec);
                    } else {
                        width = 400;
                    }

                    String heightspec = getParameter("height");

                    if (heightspec != null) {
                        height = Integer.parseInt(heightspec);
                    } else {
                        height = 400;
                    }

                    _setPlotSize(width, height);
                    plot().setButtons(true);

                    // Process the background parameter.
                    Color background = Color.white;
                    String colorspec = getParameter("background");

                    if (colorspec != null) {
                        background = PlotBox.getColorByName(colorspec);
                    }

                    setBackground(background);
                    plot().setBackground(background);
                    getContentPane().setBackground(background);

                    // Process the foreground parameter.
                    Color foreground = Color.black;
                    colorspec = getParameter("foreground");

                    if (colorspec != null) {
                        foreground = PlotBox.getColorByName(colorspec);
                    }

                    setForeground(foreground);
                    plot().setForeground(foreground);
                    plot().setVisible(true);

                    // Process the dataurl parameter.
                    String dataurlspec = getParameter("dataurl");

                    if (dataurlspec != null) {
                        try {
                            showStatus("Reading data");

                            URL dataurl = new URL(getDocumentBase(), dataurlspec);
                            InputStream in = dataurl.openStream();
                            _read(in);
                            showStatus("Done");
                        } catch (MalformedURLException e) {
                            System.err.println(e.toString());
                        } catch (FileNotFoundException e) {
                            System.err.println("PlotApplet: file not found: "
                                    + e);
                        } catch (IOException e) {
                            System.err.println(
                                    "PlotApplet: error reading input file: " + e);
                        }
                    }
                }
            };

        try {
            // NOTE: Using invokeAndWait() here risks causing
            // deadlock.  However, the Sun Tutorial recommends calling
            // invokeAndWait so that the work finishes before returning.
            // if we call invokeLater() then demo/PlotFourierSeries.java
            // has problems.
            SwingUtilities.invokeAndWait(doActions);
        } catch (Exception ex) {
            // Ignore InterruptedException.
            // Other exceptions should not occur.
        }

        //}
    }

    /** Create a new Plot object for the applet.  Derived classes can
     *  redefine this method to return a different type of plot object.
     *  @return A new instance of PlotBox.
     */
    public PlotBox newPlot() {
        return new Plot();
    }

    /** Return the plot object to operate on.
     *  @return The plot object associated with this applet.
     */
    public PlotBox plot() {
        return _plot;
    }

    /** Set the plot object to operate on.
     *  @param plot The plot object to associate with this applet.
     */
    public void setPlot(PlotBox plot) {
        // FIXME: this method is necessary for PxgraphApplet to work
        // properly.  We could modify newPlot() to set _plot, but
        // that would change the newPlot() contract, so we add another method.
        _plot = plot;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Read the specified stream.  Derived classes may override this
     *  to support other file formats.
     *  @param in The input stream.
     *  @exception IOException If the stream cannot be read.
     */
    protected void _read(InputStream in) throws IOException {
        plot().read(in);
    }

    /** Given the size of the applet, set the size of the plot.
     *  Derived classes may override this to allow room for other
     *  widgets below the plot.
     *  @param appletWidth The width of the applet.
     *  @param appletHeight The height of the applet.
     */
    protected void _setPlotSize(int appletWidth, int appletHeight) {
        plot().setSize(appletWidth, appletHeight);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Work around problem in Java 1.3.1_08 where if we create
    // 6 instances of a Plot applet then navigate forward then
    // back - IE and Navigator hang. (Roger Robins)
    // FIXME: having the mutex causes applets to hang.
    //private static Object _mutex = null;
    // The Plot component we are running.
    private transient PlotBox _plot;
}
