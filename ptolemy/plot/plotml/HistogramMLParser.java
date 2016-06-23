/* A parser for PlotML (Plot Markup Language) supporting Histogram commands.

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
package ptolemy.plot.plotml;

import ptolemy.plot.Histogram;

import com.microstar.xml.XmlException;


//////////////////////////////////////////////////////////////////////////
//// HistogramMLParser

/**
   This class constructs a histogram from specifications
   in PlotML (Plot Markup Language), which is an XML language.
   This class supports extends the base class to
   ensure that the associated plotter is an instance of Histogram.
   It ignores unrecognized elements in the DTD.
   The class contains an instance of the Microstar &AElig;lfred XML
   parser and implements callback methods to interpret the parsed XML.
   The way to use this class is to construct it with a reference to
   a Plot object and then call its parse() method.

   @author Edward A. Lee
   @version $Id: HistogramMLParser.java,v 1.18 2005/04/25 22:52:22 cxh Exp $
   @since Ptolemy II 0.4
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (cxh)
*/
public class HistogramMLParser extends PlotMLParser {
    /** Construct an parser to parse commands for the specified plot object.
     *  @param plot The plot object to which to apply the commands.
     */
    public HistogramMLParser(Histogram plot) {
        _plot = plot;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** End an element. Override the base class to ignore non-histogram
     *  operations.
     *  &AElig;lfred will call this method at the end of each element
     *  (including EMPTY elements).
     *  @param elementName The element type name.
     */
    public void endElement(String elementName) throws Exception {
        if (elementName.equals("bars") || elementName.equals("dataset")) {
            // Ignore
        } else {
            super.endElement(elementName);
        }
    }

    /** Start an element.
     *  This is called at the beginning of each XML
     *  element.  By the time it is called, all of the attributes
     *  for the element will already have been reported using the
     *  attribute() method.  Unrecognized elements are ignored.
     *  @param elementName The element type name.
     *  @exception XmlException If the element produces an error
     *   in constructing the model.
     */
    public void startElement(String elementName) throws XmlException {
        try {
            if (elementName.equals("barGraph")) {
                // Override the base class to ignore things irrelevant
                // to histograms...
                String widthSpec = (String) _attributes.get("width");
                String offsetSpec = (String) _attributes.get("offset");

                // NOTE: If only one of these is given, then the other
                // is ignored.
                if ((widthSpec != null) && (offsetSpec != null)) {
                    double width = (Double.valueOf(widthSpec)).doubleValue();
                    double offset = (Double.valueOf(offsetSpec)).doubleValue();
                    ((Histogram) _plot).setBars(width, offset);
                }
            } else if (elementName.equals("bin")) {
                // Ignore if plot is not an instance of Histogram.
                if (_plot instanceof Histogram) {
                    Histogram histogram = (Histogram) _plot;
                    String widthSpec = (String) _attributes.get("width");
                    String offsetSpec = (String) _attributes.get("offset");

                    // NOTE: If only one of these is given, then the other
                    // is ignored.
                    if ((widthSpec != null) && (offsetSpec != null)) {
                        double width = (Double.valueOf(widthSpec)).doubleValue();
                        histogram.setBinWidth(width);

                        double offset = (Double.valueOf(offsetSpec))
                            .doubleValue();
                        histogram.setBinOffset(offset);
                    }
                }
            } else if (elementName.equals("dataset")) {
                // Override the base class to ignore things irrelevant
                // to histograms...
                _currentDataset++;
                _currentPointCount = 0.0;

                String name = (String) _attributes.get("name");

                if (name != null) {
                    _plot.addLegend(_currentDataset, name);
                }
            } else if (elementName.equals("default")) {
                // Override the base class to ignore things irrelevant
                // to histograms...
            } else {
                super.startElement(elementName);
            }
        } catch (Exception ex) {
            if (ex instanceof XmlException) {
                throw (XmlException) ex;
            } else {
                String msg = "XML element \"" + elementName
                    + "\" triggers exception:\n  " + ex.toString();
                throw new XmlException(msg, _currentExternalEntity(),
                        _parser.getLineNumber(), _parser.getColumnNumber());
            }
        }

        // NOTE: if super is called, this gets done three times...
        // Any way to avoid it?
        _attributes.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a point based on the current attributes.
     *  The second argument is the element name, used for error reporting.
     *  @param connected Ignored.
     *  @param element The name of the element.
     */
    protected void _addPoint(boolean connected, String element)
            throws Exception {
        String ySpec = (String) _attributes.get("y");
        _checkForNull(ySpec, "No y value for element \"" + element + "\"");

        // NOTE: Do not use parseDouble() to maintain Java 1.1 compatibility.
        double y = (Double.valueOf(ySpec)).doubleValue();
        ((Histogram) _plot).addPoint(_currentDataset, y);
    }
}
