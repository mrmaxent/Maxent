/* Plotter application that is capable of reading PlotML files.

@Author: Edward A. Lee

@Version: $Id: PlotMLApplication.java,v 1.33 2005/04/29 20:05:03 cxh Exp $

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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ptolemy.plot.Plot;
import ptolemy.plot.PlotApplication;
import ptolemy.plot.PlotBox;

import com.microstar.xml.XmlException;


//////////////////////////////////////////////////////////////////////////
//// PlotMLApplication

/**
   An application that can plot data in PlotML format from a URL or
   from files specified on the command line.
   To compile and run this application, do the following:
   <pre>
   javac -classpath ../.. PlotMLApplication.java
   java -classpath ../.. ptolemy.plot.plotml.PlotMLApplication
   </pre>

   @author Edward A. Lee
   @version $Id: PlotMLApplication.java,v 1.33 2005/04/29 20:05:03 cxh Exp $
   @since Ptolemy II 0.4
   @Pt.ProposedRating red (eal)
   @Pt.AcceptedRating red (cxh)
   @see PlotBox
   @see Plot
*/
public class PlotMLApplication extends PlotApplication {
    /** Construct a plot with no command-line arguments.
     *  It initially displays a sample plot.
     *  @exception Exception If command line arguments have problems.
     */
    public PlotMLApplication() throws Exception {
        this(null);
    }

    /** Construct a plot with the specified command-line arguments.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public PlotMLApplication(String[] args) throws Exception {
        this(new Plot(), args);
    }

    /** Construct a plot with the specified command-line arguments
     *  and instance of plot.
     *  @param plot The instance of Plot to use.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public PlotMLApplication(PlotBox plot, String[] args)
            throws Exception {
        super(plot, args);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new plot window and map it to the screen.
     *  The command to run would be:
     *  <pre>
     *  java -classpath $PTII ptolemy.plot.plotml.PlotMLApplication
     *  <pre>
     *  @param args Arguments suitable for the
     *  {@link ptolemy.plot.Plot} class.
     */
    public static void main(final String[] args) {
        try {
            Runnable doActions = new Runnable() {
                    public void run() {
                        try {
                            new PlotMLApplication(new Plot(), args);
                        } catch (Exception ex) {
                            System.err.println(ex.toString());
                            ex.printStackTrace();
                        }
                    }
                };

            // NOTE: Using invokeAndWait() here risks causing
            // deadlock.  However, the Sun Tutorial recommends calling
            // invokeAndWait so that the work finishes before returning.
            // if we call invokeLater() then demo/PlotFourierSeries.java
            // has problems.
            SwingUtilities.invokeAndWait(doActions);
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }

            System.exit(0);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    protected void _about() {
        JOptionPane.showMessageDialog(this,
                "PlotMLApplication class\n" + "By: Edward A. Lee "
                + "and Christopher Hylands\n" + "Version " + PlotBox.PTPLOT_RELEASE
                + ", Build: $Id: PlotMLApplication.java,v 1.33 2005/04/29 20:05:03 cxh Exp $\n\n"
                + "For more information, see\n"
                + "http://ptolemy.eecs.berkeley.edu/java/ptplot\n\n"
                + "Copyright (c) 1997-2005, "
                + "The Regents of the University of California.",
                "About Ptolemy Plot", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        JOptionPane.showMessageDialog(this,
                "PlotMLApplication is a standalone plot " + " application.\n"
                + "  File formats understood: PlotML and Ptplot ASCII.\n"
                + "  Left mouse button: Zooming.\n\n" + _usage(),
                "About Ptolemy Plot", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Read the specified stream.  This method checks to see whether
     *  the data is PlotML data, and if so, creates a parser to read it.
     *  If not, it defers to the parent class to read it.
     *  @param base The base for relative file references, or null if
     *   there are not relative file references.
     *  @param in The input stream.
     *  @exception IOException If the stream cannot be read.
     */
    protected void _read(URL base, InputStream in) throws IOException {
        // Create a buffered input stream so that mark and reset
        // are supported.
        BufferedInputStream bin = new BufferedInputStream(in);

        // Peek at the file...
        bin.mark(9);

        // Read 8 bytes in case 16-bit encoding is being used.
        byte[] peek = new byte[8];
        bin.read(peek);
        bin.reset();

        if ((new String(peek)).startsWith("<?xm")) {
            // file is an XML file.
            PlotBoxMLParser parser = _newParser();

            try {
                parser.parse(base, bin);
            } catch (Exception ex) {
                String msg;

                if (ex instanceof XmlException) {
                    XmlException xmlex = (XmlException) ex;
                    msg = "PlotMLApplication: failed to parse PlotML data:\n"
                        + "line: " + xmlex.getLine() + ", column: "
                        + xmlex.getColumn() + "\nIn entity: "
                        + xmlex.getSystemId() + "\n";
                } else {
                    msg = "PlotMLApplication: failed to parse PlotML data:\n";
                }

                System.err.println(msg + ex.toString());
                ex.printStackTrace();
            }
        } else {
            super._read(base, bin);
        }
    }

    /** Create a new parser object for the application.  Derived classes can
     *  redefine this method to return a different type of parser.
     *  @return A new parser.
     */
    protected PlotBoxMLParser _newParser() {
        if (plot instanceof Plot) {
            return new PlotMLParser((Plot) plot);
        } else {
            return new PlotBoxMLParser(plot);
        }
    }
}
