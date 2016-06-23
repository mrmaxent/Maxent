/* A parser for PlotML (Plot Markup Language) supporting Plot commands.

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


// Ptolemy imports.
import ptolemy.plot.Plot;

import com.microstar.xml.XmlException;


//////////////////////////////////////////////////////////////////////////
//// PlotMLParser

/**
   This class constructs a plot from specifications
   in PlotML (Plot Markup Language), which is an XML language.
   This class supports extends the base class to
   support the subset that applies to the Plot class.
   It ignores unrecognized elements in the DTD.
   The class contains an instance of the Microstar &AElig;lfred XML
   parser and implements callback methods to interpret the parsed XML.
   The way to use this class is to construct it with a reference to
   a Plot object and then call its parse() method.

   @author Edward A. Lee
   @version $Id: PlotMLParser.java,v 1.25 2005/04/25 22:52:37 cxh Exp $
   @since Ptolemy II 0.4
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (cxh)
*/
public class PlotMLParser extends PlotBoxMLParser {
    /** Construct an parser to parse commands for the specified plot object.
     *  @param plot The plot object to which to apply the commands.
     */
    public PlotMLParser(Plot plot) {
        super(plot);
    }

    /** Protected constructor allows derived classes to set _plot
     *  differently.
     */
    protected PlotMLParser() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** End an element. This method
     *  calls the appropriate Plot methods.
     *  &AElig;lfred will call this method at the end of each element
     *  (including EMPTY elements).
     *  @param elementName The element type name.
     */
    public void endElement(String elementName) throws Exception {
        super.endElement(elementName);

        if (elementName.equals("dataset")) {
            // Reset the default, in case it was changed for this dataset.
            ((Plot) _plot).setConnected(_connected);
        }
    }

    /** Start a document.  This method is called just before the parser
     *  attempts to read the first entity (the root of the document).
     *  It is guaranteed that this will be the first method called.
     */
    public void startDocument() {
        super.startDocument();
        _currentDataset = -1;
        _currentPointCount = 0.0;
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
            // NOTE: The elements are alphabetical below...
            if (elementName.equals("barGraph")) {
                String widthSpec = (String) _attributes.get("width");
                String offsetSpec = (String) _attributes.get("offset");

                // NOTE: If only one of these is given, then the other
                // is ignored.
                if ((widthSpec == null) || (offsetSpec == null)) {
                    ((Plot) _plot).setBars(true);
                } else {
                    double width = (Double.valueOf(widthSpec)).doubleValue();
                    double offset = (Double.valueOf(offsetSpec)).doubleValue();
                    ((Plot) _plot).setBars(width, offset);
                }
            } else if (elementName.equals("dataset")) {
                String name = (String) _attributes.get("name");

                if (!((Plot) _plot).getReuseDatasets() || (name == null)
                        || (_currentDataset < 0)) {
                    // reuseDatasets was not present or if it was,
                    // the current dataset does not have a name
                    // or we have not yet seen a dataset.
                    _currentDataset++;
                    _currentPointCount = 0.0;
                } else {
                    // reuseDatasets was set to true and name is not null.
                    int possibleDataset = ((Plot) _plot).getLegendDataset(name);

                    if (possibleDataset != -1) {
                        _currentDataset = possibleDataset;
                    } else {
                        // Did not yet have a dataset with that name.
                        _currentDataset++;
                        _currentPointCount = 0.0;
                    }
                }

                if (name != null) {
                    ((Plot) _plot).addLegend(_currentDataset, name);
                }

                String connected = (String) _attributes.get("connected");

                if (connected != null) {
                    if (connected.equals("no")) {
                        ((Plot) _plot).setConnected(false, _currentDataset);
                    } else {
                        ((Plot) _plot).setConnected(true, _currentDataset);
                    }
                }

                String marks = (String) _attributes.get("marks");

                if (marks != null) {
                    ((Plot) _plot).setMarksStyle(marks, _currentDataset);
                }

                String stems = (String) _attributes.get("stems");

                if (stems != null) {
                    if (stems.equals("yes")) {
                        ((Plot) _plot).setImpulses(true, _currentDataset);
                    } else {
                        ((Plot) _plot).setImpulses(false, _currentDataset);
                    }
                }
            } else if (elementName.equals("default")) {
                String connected = (String) _attributes.get("connected");

                if (connected.equals("yes")) {
                    ((Plot) _plot).setConnected(true);
                    _connected = true;
                } else {
                    ((Plot) _plot).setConnected(false);
                    _connected = false;
                }

                String marks = (String) _attributes.get("marks");

                if (marks != null) {
                    ((Plot) _plot).setMarksStyle(marks);
                }

                String stems = (String) _attributes.get("stems");

                if (stems.equals("no")) {
                    ((Plot) _plot).setImpulses(false);
                } else {
                    ((Plot) _plot).setImpulses(true);
                }
            } else if (elementName.equals("m")) {
                _addPoint(false, elementName);
            } else if (elementName.equals("move")) {
                _addPoint(false, elementName);
            } else if (elementName.equals("p")) {
                _addPoint(true, elementName);
            } else if (elementName.equals("point")) {
                _addPoint(true, elementName);
            } else if (elementName.equals("reuseDatasets")) {
                ((Plot) _plot).setReuseDatasets(true);
            } else {
                super.startElement(elementName);
            }
        } catch (Exception ex) {
            if (ex instanceof XmlException) {
                throw (XmlException) ex;
            } else {
                // FIXME: Temporary for debugging.
                System.err.println(ex.toString());
                ex.printStackTrace();

                String msg = "XML element \"" + elementName
                    + "\" triggers exception:\n  " + ex.toString();
                throw new XmlException(msg, _currentExternalEntity(),
                        _parser.getLineNumber(), _parser.getColumnNumber());
            }
        }

        // NOTE: if super is called, this gets done twice.
        // Any way to avoid it?
        _attributes.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The default connected state. */
    protected boolean _connected = true;

    /** The current dataset number in a "dataset" element. */
    protected int _currentDataset = -1;

    /** A count within the current dataset, in case no x value is given. */
    protected double _currentPointCount = 0.0;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a point based on the current attributes.
     *  If the first argument is true, connect it to the previous point.
     *  The second argument is the element name, used for error reporting.
     *  @param connected If true, connect to the previous point.
     *  @param element The name of the element.
     */
    protected void _addPoint(boolean connected, String element)
            throws Exception {
        String xSpec = (String) _attributes.get("x");
        double x;

        if (xSpec == null) {
            // No x value given.  Use _currentPointCount.
            x = _currentPointCount;
            _currentPointCount += 1.0;
        } else {
            // NOTE: Do not use parseDouble() to maintain
            // Java 1.1 compatibility.
            x = (Double.valueOf(xSpec)).doubleValue();
        }

        String ySpec = (String) _attributes.get("y");
        _checkForNull(ySpec, "No y value for element \"" + element + "\"");

        // NOTE: Do not use parseDouble() to maintain Java 1.1 compatibility.
        double y = (Double.valueOf(ySpec)).doubleValue();

        String lowSpec = (String) _attributes.get("lowErrorBar");
        String highSpec = (String) _attributes.get("highErrorBar");

        if ((lowSpec == null) && (highSpec == null)) {
            ((Plot) _plot).addPoint(_currentDataset, x, y, connected);
        } else {
            double low;
            double high;

            if (lowSpec != null) {
                low = (Double.valueOf(lowSpec)).doubleValue();
            } else {
                low = x;
            }

            if (highSpec != null) {
                high = (Double.valueOf(highSpec)).doubleValue();
            } else {
                high = x;
            }

            ((Plot) _plot).addPointWithErrorBars(_currentDataset, x, y, low,
                    high, connected);
        }
    }
}
