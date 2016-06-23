/* Plotter applet that is capable of reading pxgraph files.

@Author: Edward A. Lee

@Version: $Id: PxgraphApplet.java,v 1.30 2005/04/29 20:04:35 cxh Exp $

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
package ptolemy.plot.compat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ptolemy.plot.CmdLineArgException;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotApplet;
import ptolemy.plot.PlotBox;


//////////////////////////////////////////////////////////////////////////
//// PxgraphApplet

/**
   An Applet that can plot data in pxgraph format from a URL.
   The URL should be specified using the dataurl applet parameter
   or as part of the <i>pxgraphargs</i> applet parameter.
   That parameter contains command-line style arguments compatible with
   the older pxgraph program.
   See the documentation for the PxgraphParser class for the format of
   these arguments.

   @author Edward A. Lee
   @version $Id: PxgraphApplet.java,v 1.30 2005/04/29 20:04:35 cxh Exp $
   @since Ptolemy II 0.4
   @Pt.ProposedRating red (eal)
   @Pt.AcceptedRating red (cxh)
   @see PxgraphParser
   @see ptolemy.plot.PlotBox
   @see ptolemy.plot.Plot
*/
public class PxgraphApplet extends PlotApplet {
    /** Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "PxgraphApplet " + PlotBox.PTPLOT_RELEASE
            + ": A data plotter.\n" + "By: Edward A. Lee and\n "
            + "Christopher Hylands\n"
            + "($Id: PxgraphApplet.java,v 1.30 2005/04/29 20:04:35 cxh Exp $)";
    }

    /** Return information about parameters.
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
                "pxgraphargs",
                "args",
                "pxgraph style command line arguments"
            }
        };
        return pinfo;
    }

    /** Initialize the applet.  Read the applet parameters.
     */
    public void init() {
        if (plot() == null) {
            // This is a bit of a hack to make sure that we actually
            // have a plot to operate on.
            setPlot(newPlot());
        }

        _parser = new PxgraphParser((Plot) plot());

        super.init();

        // Process the pxgraphargs parameter.
        String pxgraphargs = null;
        pxgraphargs = getParameter("pxgraphargs");

        if (pxgraphargs != null) {
            try {
                // Since there may be filenames specified here, we
                // set the document base.  Note that we prefer that files and
                // URLs be specified using the dataurl parameter.
                showStatus("Reading arguments");
                _parser.parsePxgraphargs(pxgraphargs, getDocumentBase());
                showStatus("Done reading arguments");
            } catch (CmdLineArgException e) {
                System.err.println("PxgraphApplet: failed to parse `"
                        + pxgraphargs + "': " + e);
            } catch (FileNotFoundException e) {
                System.err.println(e.toString());
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        }

        // Because things may occur in an odd order above...
        plot().repaint();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Read the specified stream, assuming it is pxgraph formatted data.
     *  @param in The input stream.
     *  @exception IOException If the stream cannot be read.
     */
    protected void _read(InputStream in) throws IOException {
        _parser.read(in);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // Parser.
    private PxgraphParser _parser;
}
