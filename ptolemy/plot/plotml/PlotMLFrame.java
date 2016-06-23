/* Plot frame that is capable of reading PlotML files.

@Author: Edward A. Lee

@Version: $Id: PlotMLFrame.java,v 1.28 2005/04/29 20:05:03 cxh Exp $

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

import ptolemy.plot.Plot;
import ptolemy.plot.PlotBox;
import ptolemy.plot.PlotFrame;

import com.microstar.xml.XmlException;


//////////////////////////////////////////////////////////////////////////
//// PlotMLFrame

/**
   PlotMLFrame is a versatile two-dimensional data plotter that runs as
   part of an application, but in its own window. It can read files
   in the PlotML format and, for backward compatibility, the old Ptolemy
   plot file format.
   An application can also interact directly with the contained Plot
   object, which is visible as a public member, by invoking its methods.
   <p>
   An application that uses this class should set up the handling of
   window-closing events.  Presumably, the application will exit when
   all windows have been closed. This is done with code something like:
   <pre>
   plotFrameInstance.addWindowListener(new WindowAdapter() {
   public void windowClosing(WindowEvent e) {
   // Handle the event
   }
   });
   </pre>
   <p>
   PlotMLFrame contains an instance of PlotBox. PlotBox is the base class for
   classes with drawing capability, e.g. Plot, LogicAnalyzer. If not
   specified in the constructor, the default is to contain a Plot object. This
   field is set once in the constructor and immutable afterwards.

   @see Plot
   @see PlotBox
   @author Christopher Hylands and Edward A. Lee
   @version $Id: PlotMLFrame.java,v 1.28 2005/04/29 20:05:03 cxh Exp $
   @since Ptolemy II 0.4
   @Pt.ProposedRating red (eal)
   @Pt.AcceptedRating red (cxh)
*/
public class PlotMLFrame extends PlotFrame {
    /** Construct a plot with no command-line arguments.
     *  It initially displays a sample plot.
     */
    public PlotMLFrame() {
        this("Ptolemy Plot Frame");
    }

    /** Construct a plot frame with the specified title and by default
     *  contains an instance of Plot.
     */
    public PlotMLFrame(String title) {
        this(title, null);
    }

    /** Construct a plot frame with the specified title and the specified
     *  instance of PlotBox.
     */
    public PlotMLFrame(String title, PlotBox plotArg) {
        super(title, plotArg);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Display basic information about the application.
     */
    protected void _about() {
        JOptionPane.showMessageDialog(this,
                "Ptolemy plot frame\n" + "By: Edward A. Lee\n"
                + "and Christopher Hylands\n" + "Version " + PlotBox.PTPLOT_RELEASE
                + ", Build: $Id: PlotMLFrame.java,v 1.28 2005/04/29 20:05:03 cxh Exp $"
                + "For more information, see\n"
                + "http://ptolemy.eecs.berkeley.edu/java/ptplot\n",
                "About Ptolemy Plot", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Read the specified stream.  This method checks to see whether
     *  the data is PlotML data, and if so, creates a parser to read it.
     *  If not, it defers to the parent class to read it.  The current
     *  working directory (or the directory of the last opened or saved
     *  file) is the base for relative references.
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
            PlotBoxMLParser parser;

            if (plot instanceof Plot) {
                parser = new PlotMLParser((Plot) plot);
            } else {
                parser = new PlotBoxMLParser(plot);
            }

            try {
                parser.parse(base, bin);
            } catch (Exception ex) {
                String msg;

                if (ex instanceof XmlException) {
                    XmlException xmlex = (XmlException) ex;
                    msg = "PlotMLFrame: failed to parse PlotML data:\n"
                        + "line: " + xmlex.getLine() + ", column: "
                        + xmlex.getColumn() + "\nIn entity: "
                        + xmlex.getSystemId() + "\n";
                } else {
                    msg = "PlotMLFrame: failed to parse PlotML data:\n";
                }

                System.err.println(msg + ex.toString());
                ex.printStackTrace();
            }
        } else {
            super._read(base, bin);
        }
    }
}
