/* Parser for pxgraph command line arguments and binary files.

@Author: Edward A. Lee and Christopher Hylands

@Version: $Id: PxgraphParser.java,v 1.38 2005/04/29 20:04:35 cxh Exp $

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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import ptolemy.plot.CmdLineArgException;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotBox;


//////////////////////////////////////////////////////////////////////////
//// PxgraphParser

/**
   This class provides backwards compatibility with an older plotting
   program, pxgraph.  It provides two methods, one for parsing command-line
   arguments, and one for reading binary data from a file. In pxgraph,
   the binary files have no format information; all format information
   is provided by command line arguments.
   <p>
   Below we describe the <code>pxgraph</code> arguments.  The
   text is based on the <code>xgraph</code> Unix man page written
   by David Harrison (University of California).
   To see the command line options, you can type
   <code>pxgraph -help</code>.
   <p>
   The <code>pxgraph</code> program draws a graph on a display given data
   read from either data files or from standard input if no
   files are specified. It can display up to 64 independent
   data sets using different colors and/or line styles for each
   set. It annotates the graph with a title, axis labels,
   grid lines or tick marks, grid labels, and a legend. There
   are options to control the appearance of most components of
   the graph.
   <p>
   The input format is similar to <code>graph(<i>1G</i>)</code> but differs
   slightly. The data consists of a number of <I>data</I> <I>sets</I>. Data
   sets are separated by a blank line. A new data set is also
   assumed at the start of each input file. A data set consists
   of an ordered list of points of the form <code><i>directive</i>
   X Y</code>.

   The directive is either <code>draw</code> or <code>move</code> and can
   be omitted (Note that with binary data files, you must have a directive,
   the above statement only applies to ascii format data files). If the
   directive is <code>draw</code>, a line will be drawn
   between the previous point and the current point (if a line
   graph is chosen). Specifying a <code>move</code> directive tells
   xgraph not to draw a line between the points. If the directive
   is omitted, <code>draw</code> is assumed for all points in a data
   set except the first point where <code>move</code> is assumed. The
   <code>move</code> directive is used most often to allow discontinuous
   data in a data set.

   After <code>pxgraph</code> has read the data, it will create a new window
   to graphically display the data.

   Once the window has been opened, all of the data sets will
   be displayed graphically (subject to the options explained
   below) with a legend in the upper right corner of the
   screen. To zoom in on a portion of the graph, depress a
   mouse button in the window and sweep out a region. <code>pxgraph</code>
   will then the window will be redrawn with just that portion of
   the graph. <code>pxgraph</code> also presents four control buttons in
   the lower left corner of each window: <code>Exit</code>,
   <code>Print</code>, <code>HTML</code> and <code>About</code>.
   <p>The <code>Exit</code> button will exit the process.  You can also
   type <code>Control-D</code>, <code>Control-C</code> or <code>q</code>
   to exit.
   <p>The <code>Print</code> button brings up a print dialog window.
   <p>The <code>About</code> button brings up a message about
   <code>pxgraph</code>.
   <p>The <code>HTML</code> button prints an HTML file to stdout that
   can be used to display the file with applet <code>Plot</code> classes
   (Experimental).
   <p>
   <code>pxgraph</code> accepts a large number of command line options.
   A list of these options is given below.
   <p>

   <dl>
   <dt><code>=<i>W</i>x<i>H</i>+<i>X</i>+<i>Y</i></code>
   <dd>Specifies the initial size and location of the pxgraph
   window.

   <dt> <code>-<i>&lt;digit&gt; &lt;name&gt;</i></code>
   <dd> These options specify the data
   set name for the corresponding data set. The digit
   should be in the range 0 to 63. This name will be
   used in the legend.

   <dt><code>-bar</code>
   <dd>Specifies that vertical bars should be drawn from the
   data points to a base point which can be specified with
   <code>-brb</code>.
   Usually, the <code>-nl</code> flag is used with this option.
   The point itself is located at the center of the bar.

   <dt><code>-bb</code>
   <dd>Draw a bounding box around the data region. This is
   very useful if you prefer to see tick marks rather than
   grid lines (see <code>-tk</code>).
   <b>Ignored in the Java version because the plotting area is a different
   color than the border where the axes are labeled.</b>

   <dt><code>-bd</code> <code><i>&lt;color&gt;</i></code>
   <dd>This specifies the border color of the <code>pxgraph</code> window.
   <b>Unsupported in the Java version.</b>

   <dt><code>-bg</code> <code><i>&lt;color&gt;</i></code>
   <dd>Background color of the area where the labels and legend are rendered.
   <b>In the Java version, this argument takes hexadecimal color values
   (<code>fffff</code>), not color names.</b>  Note that the background
   of the data plotting region is always white because the dataset colors
   were designed for a white background.

   <dt><a name="-bigendian flag"><code>-bigendian</code></a>
   <dd>Data files are in big-endian, or network binary format.
   See the <code>-binary</code> command line argument documentation
   below for details about the format.
   If you are on a little-endian machine, such as a machine
   with an Intel x86 chip, and you would like to read a binary
   format file, created on a big-endian machine, such as a Sun SPARC,
   use the <code>-bigendian</code> flag.

   <dt><a name="-binary flag"><code>-binary</code></a>
   <dd>Data files are in a binary format.
   The endian-ism of the data depends on which of the two
   subformats below are chosen.
   The <code>-binary</code>
   argument is the primary difference between <code>xgraph</code>
   and <code>pxgraph</code>.  The
   <A HREF="http://ptolemy.eecs.berkeley.edu">Ptolemy Project</A> software
   makes extensive use of <code>-binary</code>.
   <br>There are two binary formats, both of which use 4 byte floats.
   <ol>
   <li>If the first byte of the data file is not a <code>d</code>, then
   we assume that the file contains 4 byte floats in big-endian ordering
   with no plot commands.
   <li>If the first byte of the data file is a <code>d</code>, then
   we assume that the plot commands are encoded as single characters,
   and the numeric data is a 4 byte float encoded in the
   native endian format of the machine that the java interpreter is
   running on.
   <br>The commands are encoded as follows:
   <dl>
   <dt> <code>d <I>&lt;4byte float&gt; &lt;4byte float&gt;</I></code>
   <dd> Draw a X, Y point
   <dt> <code>e</code>
   <dd> End of dataset
   <dt> <code>n <I>&lt;dataset name&gt;</I>&#92n</code>
   <dd> New dataset name, ends in <code>&#92n</code>
   <dt> <code>m <I>&lt;4byte float&gt; &lt;4byte float&gt;</I></code>
   <dd> Move to a X, Y point.
   </dl>
   </ol>
   <br>To view a binary plot file under unix, we can use the
   <code>od</code> command.  Note that the first character is a <code>d</code>
   followed by eight bytes of data consisting of two floats of four bytes.
   <pre>
   cxh@carson 324% od -c data/integrator1.plt
   0000000   d  \0  \0  \0  \0  \0  \0  \0  \0   d   ? 200  \0  \0   ? 200
   0000020  \0  \0   d   @  \0  \0  \0   @   , 314 315   d   @   @  \0  \0
   </pre>
   For further information about endian-ism, see the
   <code>-bigendian</code> and <code>-littleendian</code> command
   line argument documentation.

   <dt><code>-brb</code> <code><i>&lt;base&gt;</i></code>
   <dd>This specifies the base for a bar graph. By default,
   the base is zero.
   <b>Unsupported in the Java version.</b>

   <dt><code>-brw</code> <code><i>&lt;width&gt;</i></code>
   <dd>This specifies the width of bars in a bar graph. The
   amount is specified in the user units. By default,
   a bar one pixel wide is drawn.

   <dt><code>-bw</code> <code><i>&lt;size&gt;</i></code>
   <dd>Border width (in pixels) of the <code>pxgraph</code> window.
   <b>Unsupported in the Java version.</b>

   <dt><code>-db</code>
   <dd>Causes xgraph to run in synchronous mode and prints out
   the values of all known defaults.

   <dt><code>-fg</code> <code><i>&lt;color&gt;</i></code>
   <dd>Foreground color. This color is used to draw all text
   and the normal grid lines in the window.
   <b>In the Java version, this argument takes hexadecimal color values
   (<code>fffff</code>), not color names.</b>

   <dt><code>-gw</code> <dd>
   Width, in pixels, of normal grid lines.
   <b>Unsupported in the Java version.</b>

   <dt><code>-gs</code> <dd>
   Line style pattern of normal grid lines.

   <dt><code>-impulses</code> <dd>
   Draw a line from any plotted point down to the x axis.
   (This argument is not present in the X11 <code>pxgraph</code>,
   but it is similar to <code>-nl -bar</code>).

   <dt><code>-lf</code> <code><i>&lt;fontname&gt;</i></code>
   <dd>Label font. All axis labels and grid labels are drawn
   using this font.
   <b>Note that the Java version does not use X11 style font specification.</b>
   In the Java version, fonts may be specified as
   <menu>
   <li><code><i>fontname</i></code>, where
   <code><i>fontname</i></code> is one of <code>helvetica</code>,
   <code>TimesRoman</code>, <code>Courier</code>,  <code>Dialog</code>,
   <code>DialogInput</code>, <code>ZapfDingbats</code>.

   <li><code><i>fontname</i>-<i>style</i></code>, where
   <code><i>style</i></code> is one of
   <code>PLAIN</code>, <code>ITALIC</code>, <code>BOLD</code>,
   i.e. <code>helvetica-ITALIC</code>
   <li><code><i>fontname</i>-<i>size</i></code>, or
   <li><code><i>fontname</i>-<i>style</i>-<i>size</i></code>, where
   <code><i>size</i></code> is an integer font size in points.
   </menu>
   The default is <code>helvetica-PLAIN-12</code>.

   <dt><a name="-littleendian"><code>-littleendian</code></a>
   <dd>Data files are in little-endian, or x86 binary format.
   See the <code>-binary</code> command line argument documentation
   above for details about the format.
   If you are on a big-endian machine, such as a Sun Sparc,
   and you would like to read a binary
   format file created on a little-endian machine, such as Intel x86
   machine, then use the <code>-littleendian</code> flag.

   <dt><code>-lnx</code> <dd>
   Specifies a logarithmic X axis. Grid labels represent
   powers of ten.  If <code>-lnx</code> is present, then
   x values must be greater than zero.

   <dt><code>-lny</code> <dd>
   Specifies a logarithmic Y axis. Grid labels represent
   powers of ten.   If <code>-lny</code> is present, then
   y values must be greater than zero.

   <dt><code>-lw</code> <code><i>width</i></code> <dd>
   Specifies the width of the data lines in pixels. The
   default is zero.
   <b>Unsupported in the Java version.</b>

   <dt><code>-lx</code> <code><i>&lt;xl,xh&gt;</i></code> <dd>
   This option limits the range of the X axis to the
   specified interval. This (along with <code>-ly</code>) can be used
   to zoom in on a particularly interesting portion of a
   larger graph.

   <dt><code>-ly</code> <code><i>&lt;yl,yh&gt;</i></code> <dd>
   This option limits the range of the Y axis to the
   specified interval.

   <dt><code>-m</code> <dd>
   Mark each data point with a distinctive marker. There
   are eight distinctive markers used by xgraph. These
   markers are assigned uniquely to each different line
   style on black and white machines and varies with each
   color on color machines.

   <dt><code>-M</code>
   <dd>Similar to <code>-m</code> but markers are assigned uniquely to each
   eight consecutive data sets (this corresponds to each
   different line style on color machines).

   <dt><code>-nl</code>
   <dd>Turn off drawing lines. When used with <code>-m</code>,
   <code>-M</code>, <code>-p</code>, or <code>-P</code> this can be used
   to produce scatter plots. When used with -bar, it can be used to
   produce standard bar graphs.

   <dt><code>-o</code> <code><i>output filename</i></code>
   <dd>The name of the file to place the print output in.  Currently
   defaults to <code>/tmp/t.ps</code>.  See also the
   <code>-print</code> option.

   <dt><code>-p</code>
   <dd>Marks each data point with a small marker (pixel
   sized). This is usually used with the -nl option for
   scatter plots.

   <dt><code>-P</code>
   <dd>Similar to <code>-p</code> but marks each pixel with a large dot.

   <dt><code>-print</code>
   <dd>Bring up the print dialog immediately upon startup.  Unfortunately,
   there is no way to automatically print in JDK1.1, the user must hit
   the <code>Ok</code> button.  See also the <code>-o</code> option.

   <dt><code>-rv</code>
   <dd>Reverse video. On black and white displays, this will
   invert the foreground and background colors. The
   behaviour on color displays is undefined.

   <dt><code>-t</code> <code><i>&lt;string&gt;</i></code>
   <dd>Title of the plot. This string is centered at the top
   of the graph.

   <dt><code>-tf</code> <code><i>&lt;fontname&gt;</i></code>
   <dd>Title font. This is the name of the font to use for
   the graph title.  See the <code>-lf</code> description above
   for how to specify fonts.
   The default is <code>helvetica-BOLD-14</code>

   <dt><code>-tk</code>
   <dd>This option causes <code>pxgraph</code> to draw tick marks rather
   than full grid lines. The <code>-bb</code> option is also useful
   when viewing graphs with tick marks only.

   <dt><code>-x</code>  <code><i>&lt;unitname&gt;</i></code>
   <dd>This is the unit name for the X axis. Its default is "X".

   <dt><code>-y</code> <code><i>&lt;unitname&gt;</i></code>
   <dd>This is the unit name for the Y axis. Its default is "Y".

   <dt><code>-zg</code> <code><i>&lt;color&gt;</i></code>
   <dd>This is the color used to draw the zero grid line.
   <b>Unsupported in the Java version.</b>

   <dt><code>-zw</code> <code><i>&lt;width&gt;</i></code>
   <dd>This is the width of the zero grid line in pixels.
   <b>Unsupported in the Java version.</b>
   </dl>

   <h2><a name="pxgraph script compatibility issues">Compatibility Issues</a></h2>
   Various compatibility issues are documented above in <b>bold</b>.
   Below are some other issues:
   <ol>
   <li>The original <code>xgraph</code> program allowed many formatting
   directives inside the file.  This version only supports
   <code>draw</code> and <code>move</code>.
   <li>This original <code>xgraph</code> program allowed blank lines
   to separate datasets.  This version does not.  Instead, use the
   <code>move <i>X</i> <i>Y</i></code> directive.
   <li>This version does not support X resources.
   <li>The Java version of <code>pxgraph</code> takes longer to start up
   than the X11 version.  This is an inherent problem with standalone
   Java applications.  One guess is that most of the startup time comes
   from paging in the shared libraries.

   </ol>

   <p>
   For further information about this tool, see the
   <a href="http://ptolemy.eecs.berkeley.edu/java/ptplot">Java Plot Website</a>.

   @author Edward A. Lee and Christopher Hylands
   @version $Id: PxgraphParser.java,v 1.38 2005/04/29 20:04:35 cxh Exp $
   @since Ptolemy II 0.4
   @Pt.ProposedRating red (eal)
   @Pt.AcceptedRating red (cxh)
   @see PxgraphApplication
   @see PxgraphApplet
*/
public class PxgraphParser {
    /** Construct a parser to configure the specified plot.
     *  @param plot The Plot object that is configured.
     */
    public PxgraphParser(Plot plot) {
        _plot = plot;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Parse pxgraph style command-line arguments.
     *  @param args A set of command-line arguments.
     *  @return The number of arguments read.
     *  @exception CmdLineArgException If there is a problem parsing
     *   the command line arguments.
     *  @exception FileNotFoundException If a file is specified that is not
     *   found.
     *  @exception IOException If an error occurs reading an input file.
     */
    public int parseArgs(String[] args)
            throws CmdLineArgException, FileNotFoundException, IOException {
        return parseArgs(args, null);
    }

    /** Parse pxgraph style command-line arguments, using the specified
     *  base URL for any relative URL references.
     *  @param args A set of command-line arguments.
     *  @param base A base URL for relative URL references, or null if
     *   there is none.
     *  @return The number of arguments read.
     *  @exception CmdLineArgException If there is a problem parsing
     *   the command line arguments.
     *  @exception FileNotFoundException If a file is specified that is not
     *   found.
     *  @exception IOException If an error occurs reading an input file.
     */
    public int parseArgs(String[] args, URL base)
            throws CmdLineArgException, FileNotFoundException, IOException {
        int i = 0;
        int j;
        int argumentsRead = 0;

        // If we see both -nl and -bar, assume we do a stem plot.
        boolean sawbararg = false; // Saw -bar arg.
        boolean sawnlarg = false; // Saw -nl arg.
        String savedmarks = "none"; // Save _marks in case we have -P -bar -nl.
        _binary = false; // Read a binary xgraph file.

        int width = 400;
        int height = 400;

        String arg;
        String[] unsupportedOptions = {
            "-bd",
            "-brb",
            "-bw",
            "-gw",
            "-lw",
            "-zg",
            "-zw"
        };

        while ((args != null) && (i < args.length)
                && (args[i].startsWith("-") || args[i].startsWith("="))) {
            arg = args[i++];

            if (arg.startsWith("-")) {
                // Search for unsupported options that take arguments
                boolean badarg = false;

                for (j = 0; j < unsupportedOptions.length; j++) {
                    if (arg.equals(unsupportedOptions[j])) {
                        System.err.println("Warning: pxgraph: " + arg
                                + " is not supported");
                        i++;
                        badarg = true;
                    }
                }

                if (badarg) {
                    continue;
                }

                if (arg.equals("-bb")) {
                    // We ignore -bb because the Java version of pxgraph plot
                    // region is a different color from the surrounding region.
                    continue;
                } else if (arg.equals("-bg")) {
                    _plot.setBackground(Plot.getColorByName(args[i++]));
                    continue;
                } else if (arg.equals("-brw")) {
                    // -brw <width> BarWidth Bars:
                    // We default the baroffset to 0 here if the value does
                    // not include a comma.
                    double[] spec = _parseDoubles(args[i++]);

                    if (spec.length == 1) {
                        _plot.setBars(spec[0], 0);
                    } else {
                        _plot.setBars(spec[0], spec[1]);
                    }

                    continue;
                } else if (arg.equals("-lf")) {
                    // -lf <labelfont>
                    _plot.setLabelFont(args[i++]);
                    continue;
                } else if (arg.equals("-lx")) {
                    double[] spec = _parseDoubles(args[i++]);

                    if (spec.length == 1) {
                        throw new CmdLineArgException("Failed to parse `" + arg
                                + "'");
                    } else {
                        _plot.setXRange(spec[0], spec[1]);
                    }

                    continue;
                } else if (arg.equals("-ly")) {
                    double[] spec = _parseDoubles(args[i++]);

                    if (spec.length == 1) {
                        throw new CmdLineArgException("Failed to parse `" + arg
                                + "'");
                    } else {
                        _plot.setYRange(spec[0], spec[1]);
                    }

                    continue;
                } else if (arg.equals("-t")) {
                    // -t <title> TitleText "An X Graph"
                    String title = args[i++];
                    _plot.setTitle(title);
                    continue;
                } else if (arg.equals("-tf")) {
                    // -tf <titlefont>
                    _plot.setTitleFont(args[i++]);
                    continue;
                } else if (arg.equals("-x")) {
                    // -x <unitName> XUnitText XLabel:
                    _plot.setXLabel(args[i++]);
                    continue;
                } else if (arg.equals("-y")) {
                    // -y <unitName> YUnitText YLabel:
                    _plot.setYLabel(args[i++]);
                    continue;
                } else if (arg.equals("-bar")) {
                    //-bar BarGraph Bars: on Marks: none Lines: off
                    // If we saw the -nl arg, then assume impulses
                    sawbararg = true;

                    if (sawnlarg) {
                        _plot.setImpulses(true);
                    } else {
                        _plot.setBars(true);
                        _plot.setMarksStyle("none");
                    }

                    _plot.setConnected(false);
                    continue;
                } else if (arg.equals("-binary")) {
                    _binary = true;
                    _endian = _NATIVE_ENDIAN;
                    continue;
                } else if (arg.equals("-bigendian")) {
                    _binary = true;
                    _endian = _BIG_ENDIAN;
                    continue;
                } else if (arg.equals("-littleendian")) {
                    _binary = true;
                    _endian = _LITTLE_ENDIAN;
                    continue;
                } else if (arg.equals("-db")) {
                    _debug = 10;
                    continue;
                } else if (arg.equals("-debug")) {
                    // -debug is not in the original X11 pxgraph.
                    _debug = (int) Integer.valueOf(args[i++]).intValue();
                    continue;
                } else if (arg.equals("-fg")) {
                    _plot.setForeground(PlotBox.getColorByName(args[i++]));
                    continue;
                } else if (arg.equals("-help")) {
                    // -help is not in the original X11 pxgraph.
                    //_help();
                    continue;
                } else if (arg.equals("-impulses")) {
                    // -impulses is not in the original X11 pxgraph.
                    _plot.setImpulses(true);
                    _plot.setConnected(false);
                    continue;
                } else if (arg.equals("-lnx")) {
                    _plot.setXLog(true);
                    continue;
                } else if (arg.equals("-lny")) {
                    _plot.setYLog(true);
                    continue;
                } else if (arg.equals("-m")) {
                    // -m Markers Marks: various
                    _plot.setMarksStyle("various");
                    savedmarks = "various";
                    continue;
                } else if (arg.equals("-M")) {
                    // -M StyleMarkers Marks: various
                    _plot.setMarksStyle("various");
                    savedmarks = "various";
                    continue;
                } else if (arg.equals("-nl")) {
                    // -nl NoLines Lines: off
                    // If we saw the -bar arg, then assume impulses
                    sawnlarg = true;

                    if (sawbararg) {
                        // Restore the _marks in case we did -P -bar -nl
                        _plot.setMarksStyle(savedmarks);
                        _plot.setBars(false);
                        _plot.setImpulses(true);
                    }

                    _plot.setConnected(false);
                    continue;
                } else if (arg.equals("-o")) {
                    // -o <output filename>
                    // _outputFile =  args[i++];
                    i++;
                    continue;
                } else if (arg.equals("-p")) {
                    // -p PixelMarkers Marks: points
                    _plot.setMarksStyle("points");
                    savedmarks = "points";
                    continue;
                } else if (arg.equals("-P")) {
                    // -P LargePixel Marks: dots\n
                    _plot.setMarksStyle("dots");
                    savedmarks = "dots";
                    continue;
                } else if (arg.equals("-print")) {
                    // -print is not in the original X11 pxgraph.
                    continue;
                } else if (arg.equals("-rv")) {
                    _plot.setBackground(PlotBox.getColorByName("black"));
                    _plot.setForeground(PlotBox.getColorByName("white"));
                    continue;
                } else if (arg.equals("-test")) {
                    // -test is not in the original X11 pxgraph.
                    //_test = true;
                    continue;
                } else if (arg.equals("-tk")) {
                    _plot.setGrid(false);
                    continue;
                } else if (arg.equals("-v") || arg.equals("-version")) {
                    // -version is not in the original X11 pxgraph.
                    //_version();
                    continue;
                } else if ((arg.length() > 1) && (arg.charAt(0) == '-')) {
                    // Process '-<digit> <datasetname>'
                    try {
                        Integer datasetnumberint = new Integer(arg.substring(1));
                        int datasetnumber = datasetnumberint.intValue();

                        if (datasetnumber >= 0) {
                            _plot.addLegend(datasetnumber, args[i++]);
                            continue;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            } else {
                if (arg.startsWith("=")) {
                    // Process =WxH+X+Y
                    width = (int) Integer.valueOf(arg.substring(1,
                                                          arg.indexOf('x'))).intValue();

                    int plusIndex = arg.indexOf('+');
                    int minusIndex = arg.indexOf('-');

                    if ((plusIndex != -1) || (minusIndex != -1)) {
                        // =WxH+X+Y, =WxH-X+Y, =WxH-X-Y, =WxH+X-Y
                        if ((plusIndex != -1) && (minusIndex != -1)) {
                            // =WxH-X+Y or =WxH+X-Y
                            int index = minusIndex;

                            if (plusIndex < minusIndex) {
                                index = plusIndex;
                            }

                            height = Integer.valueOf(arg.substring(arg.indexOf(
                                                                           'x') + 1, index)).intValue();
                        } else {
                            if (plusIndex != -1) {
                                // =WxH+X+Y
                                height = Integer.valueOf(arg.substring(arg
                                                                 .indexOf('x') + 1,
                                                                 plusIndex)).intValue();
                            } else {
                                // =WxH-X-Y
                                height = Integer.valueOf(arg.substring(arg
                                                                 .indexOf('x') + 1,
                                                                 minusIndex)).intValue();
                            }
                        }
                    } else {
                        if (arg.length() > arg.indexOf('x')) {
                            // =WxH
                            height = Integer.valueOf(arg.substring(arg.indexOf(
                                                                           'x') + 1, arg.length())).intValue();
                        }
                    }

                    // FIXME: it is unclear what X and Y in =WxH+X+Y mean
                    // in a non-toplevel window, so we don't process
                    // those here.  See Pxgraph.java for how to process
                    // X and Y for a toplevel window.
                    continue;
                }
            }

            // If we got to here, then we failed to parse the arg
            throw new CmdLineArgException("Failed to parse `" + arg + "'");
        }

        argumentsRead = i++;

        _plot.setSize(width, height);

        for (i = argumentsRead; i < args.length; i++) {
            // Have a filename.  First attempt to open it as a URL.
            InputStream instream;

            try {
                URL inurl = new URL(base, args[i]);
                instream = inurl.openStream();
            } catch (MalformedURLException ex) {
                instream = new FileInputStream(args[i]);
            }

            read(instream);
        }

        return argumentsRead;
    }

    /** Split a string containing pxgraph-compatible command-line arguments
     *  into an array and call parseArgs() on the array.  This is used
     *  in the rare circumstance that you want to control the format
     *  of a plot from an applet HTML file rather than in the plot data
     *  file.
     *  @param pxgraphargs The command line arguments.
     *  @param base A base URL for relative URL references, or null if
     *   there is none.
     *  @return The number of arguments read.
     *  @exception CmdLineArgException If there is a problem parsing
     *   the command line arguments.
     *  @exception FileNotFoundException If a file is specified that is not
     *   found.
     *  @exception IOException If an error occurs reading an input file.
     */
    public int parsePxgraphargs(String pxgraphargs, URL base)
            throws CmdLineArgException, FileNotFoundException, IOException {
        // We convert the String to a Stream and then use a StreamTokenizer
        // to parse the arguments into a Vector and then copy
        // the vector into an array of Strings.  We use a Vector
        // so that we can handle an arbitrary number of arguments
        Vector argvector = new Vector();
        boolean prependdash = false; // true if we need to add a -

        StringReader pin = new StringReader(pxgraphargs);

        try {
            StreamTokenizer stoken = new StreamTokenizer(pin);

            // We don't want to parse numbers specially, so we reset
            // the syntax and then add back what we want.
            stoken.resetSyntax();
            stoken.whitespaceChars(0, ' ');
            stoken.wordChars('(', '~');
            stoken.quoteChar('"');
            stoken.quoteChar('\'');

            int c;
            String partialarg = null;
            out:
            while (true) {
                c = stoken.nextToken();

                //System.out.print(c + " "+stoken.ttype+" "+stoken.sval+" ");
                switch (stoken.ttype) { // same as value of 'c'
                case StreamTokenizer.TT_EOF:
                    break out;

                case StreamTokenizer.TT_WORD:

                    //System.out.println("Word: " + stoken.sval);
                    if (prependdash) {
                        prependdash = false;

                        if (partialarg == null) {
                            argvector.addElement("-" + stoken.sval);
                        } else {
                            argvector.addElement("-" + partialarg + stoken.sval);
                        }
                    } else {
                        if (partialarg == null) {
                            argvector.addElement(stoken.sval);
                        } else {
                            argvector.addElement(partialarg + stoken.sval);
                        }
                    }

                    partialarg = null;
                    break;

                case '-':
                    prependdash = true;
                    break;

                case '#':
                case '$':
                case '%':
                case '&':

                    // The above chars can be part of a URL.  For example
                    // perl scripts use &.  However, we cannot include
                    // them in the wordChars() range of chars, since
                    // the single quote is between them and the rest of the
                    // chars. So we have to process them by hand.
                    partialarg = ((String) argvector.lastElement()) + (char) c;
                    argvector.removeElementAt(argvector.size() - 1);
                    break;

                case '"':
                case '\'':

                    //System.out.println("String: " + stoken.sval);
                    argvector.addElement(stoken.sval);
                    break;

                default:
                    throw new IOException("Failed to parse: '" + (char) c
                            + "' in `" + pxgraphargs + "'");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create a array
        String[] args = new String[argvector.size()];

        for (int i = 0; i < argvector.size(); i++) {
            args[i] = (String) argvector.elementAt(i);
        }

        return parseArgs(args, base);
    }

    /** Read a pxgraph-compatible binary or ASCII encoded file.
     *  @param inputStream The input stream.
     *  @exception java.io.IOException If an I/O error occurs.
     */
    public void read(InputStream inputStream) throws IOException {
        DataInputStream in = new DataInputStream(new BufferedInputStream(
                                                         inputStream));

        if (_binary) {
            int c;
            float x = 0;
            float y = 0;
            float pointCount = 0;
            boolean byteSwapped = false;
            boolean connected = false;
            byte[] input = new byte[4];

            if (_connected) {
                connected = true;
            }

            switch (_endian) {
            case _NATIVE_ENDIAN:

                try {
                    if (System.getProperty("os.arch").equals("x86")) {
                        byteSwapped = true;
                    }
                } catch (SecurityException e) {
                }

                break;

            case _BIG_ENDIAN:
                break;

            case _LITTLE_ENDIAN:
                byteSwapped = true;
                break;

            default:
                throw new IOException("Internal Error: Don't know about '"
                        + _endian + "' style of endian");
            }

            try {
                // Flag that we are starting a new data set.
                _firstInSet = true;

                // Flag that we have not seen a DataSet line in this file.
                _sawFirstDataset = false;

                c = in.readByte();

                if (c != 'd') {
                    // Assume that the data is one data set, consisting
                    // of 4 byte floats.  None of the Ptolemy pxgraph
                    // binary format extensions apply.
                    // Note that the binary format is bigendian, or network
                    // order.  Little-endian machines, like x86 will not
                    // be able to write binary data directly
                    // (However, they could use Java's mechanisms for
                    // writing binary files).
                    // Read 3 more bytes, create the x float.
                    int bits = c;
                    bits = bits << 8;
                    bits += in.readByte();
                    bits = bits << 8;
                    bits += in.readByte();
                    bits = bits << 8;
                    bits += in.readByte();

                    x = Float.intBitsToFloat(bits);
                    y = in.readFloat();

                    // _addLegendIfNecessary might increment _currentdataset
                    connected = _addLegendIfNecessary(connected);
                    _plot.addPoint(_currentdataset, x, y, connected);

                    if (_connected) {
                        connected = true;
                    }

                    while (true) {
                        x = in.readFloat();
                        y = in.readFloat();
                        connected = _addLegendIfNecessary(connected);
                        _plot.addPoint(_currentdataset, x, y, connected);

                        if (_connected) {
                            connected = true;
                        }
                    }
                } else {
                    // Assume that the data is in the pxgraph binary format.
                    while (true) {
                        // For speed reasons, the Ptolemy group extended
                        // pxgraph to read binary format data.
                        // The format consists of a command character,
                        // followed by optional arguments
                        // d <4byte float> <4byte float> - Draw a X, Y point
                        // e                             - End of a data set
                        // n <chars> \n           - New set name, ends in \n
                        // m                             - Move to a point
                        switch (c) {
                        case 'd':

                            // Data point.
                            if (byteSwapped) {
                                in.readFully(input);
                                x = Float.intBitsToFloat(((input[3] & 0xFF) << 24)
                                        | ((input[2] & 0xFF) << 16)
                                        | ((input[1] & 0xFF) << 8)
                                        | (input[0] & 0xFF));
                                in.readFully(input);
                                y = Float.intBitsToFloat(((input[3] & 0xFF) << 24)
                                        | ((input[2] & 0xFF) << 16)
                                        | ((input[1] & 0xFF) << 8)
                                        | (input[0] & 0xFF));
                            } else {
                                x = in.readFloat();
                                y = in.readFloat();
                            }

                            pointCount++;
                            connected = _addLegendIfNecessary(connected);
                            _plot.addPoint(_currentdataset, x, y, connected);

                            if (_connected) {
                                connected = true;
                            }

                            break;

                        case 'e':

                            // End of set name.
                            connected = false;
                            break;

                        case 'n':
                            _firstInSet = true;
                            _sawFirstDataset = true;

                            StringBuffer datasetname = new StringBuffer();
                            _currentdataset++;

                            // New set name, ends in \n.
                            while (c != '\n') {
                                datasetname.append(in.readChar());
                            }

                            _plot.addLegend(_currentdataset,
                                    datasetname.toString());
                            _plot.setConnected(true);
                            break;

                        case 'm':

                            // a disconnected point
                            connected = false;
                            break;

                        default:
                            throw new IOException("Don't understand `"
                                    + (char) c + "' character "
                                    + "(decimal value = " + c
                                    + ") in binary file.  Last point was (" + x
                                    + "," + y + ").\nProcessed " + pointCount
                                    + " points successfully");
                        }

                        c = in.readByte();
                    }
                }
            } catch (EOFException e) {
            }
        } else {
            // Read ASCII files.
            // NOTE: These are not in xgraph format, but rather in the
            // old ptplot format!!
            _plot.read(inputStream);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The current dataset, used for handling multiple files. */
    protected int _currentdataset = -1;

    /** The plot object to which to apply commands. */
    protected Plot _plot;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Add a legend if necessary, return the value of the connected flag.
    private boolean _addLegendIfNecessary(boolean connected) {
        if (!_sawFirstDataset || (_currentdataset < 0)) {
            // We did not set a DataSet line, but
            // we did get called with -<digit> args
            _sawFirstDataset = true;
            _currentdataset++;
        }

        if (_plot.getLegend(_currentdataset) == null) {
            // We did not see a "DataSet" string yet,
            // nor did we call addLegend().
            _firstInSet = true;
            _sawFirstDataset = true;
            _plot.addLegend(_currentdataset, "Set " + _currentdataset);
        }

        if (_firstInSet) {
            connected = false;
            _firstInSet = false;
        }

        return connected;
    }

    // Parse a string with a comma into two doubles.
    // If there is no comma, return a single double.
    private double[] _parseDoubles(String spec) {
        int comma = spec.indexOf(",");

        if (comma < 0) {
            double[] result = new double[1];
            result[0] = (Double.valueOf(spec)).doubleValue();
            return result;
        } else {
            double[] result = new double[2];
            String spec1 = spec.substring(0, comma);
            result[0] = (Double.valueOf(spec1)).doubleValue();

            String spec2 = spec.substring(comma + 1);
            result[1] = (Double.valueOf(spec2)).doubleValue();
            return result;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // Check the osarch and use the appropriate endian.
    private static final int _NATIVE_ENDIAN = 0;

    // Data is in big-endian
    private static final int _BIG_ENDIAN = 1;

    // Data is in little-endian
    private static final int _LITTLE_ENDIAN = 2;

    // Flag indicating whether the command specified that the format
    private boolean _binary = false;
    private boolean _connected = true;

    // For debugging, call with -db or -debug.
    private static int _debug = 0;

    /** @serial Format to read data in. */
    private int _endian = _NATIVE_ENDIAN;

    // Is this the first datapoint in a set?
    private boolean _firstInSet = true;

    // Have we seen a DataSet line in the current data file?
    private boolean _sawFirstDataset = false;
}
