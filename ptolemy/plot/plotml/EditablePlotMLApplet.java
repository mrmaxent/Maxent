/* Applet containing the EditablePlot class.

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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ptolemy.plot.EditablePlot;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotBox;


//////////////////////////////////////////////////////////////////////////
//// EditablePlotMLApplet

/**
   This applet reads a URL giving a PlotML file for a plot and places
   the data into an editable plot.

   @see EditablePlot
   @author Edward A. Lee
   @version $Id: EditablePlotMLApplet.java,v 1.30 2005/04/29 20:05:04 cxh Exp $
   @since Ptolemy II 0.4
   @Pt.ProposedRating red (eal)
   @Pt.AcceptedRating red (cxh)
*/
public class EditablePlotMLApplet extends PlotMLApplet {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.  Place an instance of EditablePlot
     *  and a widget for selecting the data set to edit.
     */
    public void init() {
        super.init();

        JPanel controlPanel = new JPanel();

        // Make the panel transparent so that the background shows through.
        controlPanel.setOpaque(false);
        getContentPane().add(controlPanel, BorderLayout.SOUTH);

        controlPanel.add(new JLabel("Data set to edit:"));

        _choice = new JComboBox();
        controlPanel.add(_choice);

        for (int i = 0; i < ((Plot) plot()).getNumDataSets(); i++) {
            _choice.addItem(plot().getLegend(i));
        }

        _choice.addActionListener(new ChoiceListener());
    }

    /**
     * Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "EditablePlotDemo " + PlotBox.PTPLOT_RELEASE
            + ": Demo of EditablePlot.\n" + "By: Edward A. Lee\n "
            + "($Id: EditablePlotMLApplet.java,v 1.30 2005/04/29 20:05:04 cxh Exp $)";
    }

    /** Create a new Plot object for the applet.  Derived classes can
     *  redefine this method to return a different type of plot object.
     */
    public PlotBox newPlot() {
        return new EditablePlot();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Given the size of the applet, set the size of the plot.
     *  Derived classes may override this to allow room for other
     *  widgets below the plot.
     *  @param appletWidth The width of the applet.
     *  @param appletHeight The height of the applet.
     */
    protected void _setPlotSize(int appletWidth, int appletHeight) {
        if (appletHeight > 50) {
            plot().setSize(appletWidth, appletHeight - 50);
        } else {
            plot().setSize(appletWidth, appletHeight);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Choice widget for selecting the editable data set.
    private JComboBox _choice;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    private class ChoiceListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            ((EditablePlot) plot()).setEditable(_choice.getSelectedIndex());
        }
    }
}
