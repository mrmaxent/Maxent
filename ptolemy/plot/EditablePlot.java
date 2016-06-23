/* Extension of plot that allows interactive modification of plot data.

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
package ptolemy.plot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;


//////////////////////////////////////////////////////////////////////////
//// EditablePlot

/**
   This extension of Plot permits interactive modification of plotted
   data, one dataset at a time.  By default, you can modify dataset
   number zero (the first one given).  To change this default, call
   setEditable().  To edit a plot, use the right mouse button.
   Click and drag to the left to trace out new values for the data.
   To read back the modified data, use getData().  To undo a change to
   the data, type Control-Z.  To redo the change, type Control-Y.
   The undo history is infinite.
   <p>
   The style of editing is very particular.  This class assumes the data
   specify a function of <i>x</i>.  I.e., there there is exactly one
   <i>y</i> value for every <i>x</i> value.  Thus, with the right mouse
   button, you are allowed to trace out new <i>y</i> values
   starting with some leftmost <i>x</i> value.  You can only trace
   values to the right.  This feature makes it easy to trace values
   with discontinuities.  Just start at the left, and drag to the right
   to the point of the discontinuity, then drag to the left,
   then right again.  You will have to try it...
   Notice that this style of editing probably does not make sense with
   error bars, since there is no mechanism for editing the error bars.
   <p>
   To be able to modify the data in a dataset, of course, there must
   be data in the dataset.  Thus, you should create a dataset (for
   example by calling addPoint()) before editing it.  Only the visible
   part of the dataset can be edited (that is, the portion of the dataset
   along the visible part of the horizontal axis).  If you zoom in, then,
   you can edit particular points more precisely.
   <p>
   To be notified when the user sketches a new signal, create an
   object that implements the EditListener interface and add that
   listener using addEditListener().

   @author Edward A. Lee
   @version $Id: EditablePlot.java,v 1.34 2005/04/25 22:49:06 cxh Exp $
   @since Ptolemy II 0.4
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class EditablePlot extends Plot {
    /** Constructor.
     */
    public EditablePlot() {
        super();
        addMouseListener(new EditMouseListener());
        addMouseMotionListener(new ModifyListener());
        addKeyListener(new UndoListener());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a listener to be informed when the user modifies a data set.
     *  @param listener The listener.
     *  @see EditListener
     */
    public void addEditListener(EditListener listener) {
        if (_editListeners == null) {
            _editListeners = new Vector();
        } else {
            if (_editListeners.contains(listener)) {
                return;
            }
        }

        _editListeners.addElement(listener);
    }

    /** Get the data in the specified dataset. This is returned as
     *  a two-dimensional array, where the first index specifies
     *  X or Y data (index 0 or 1 respectively), and the second
     *  index specifies the point.
     *  @return The data in the specified dataset.
     */
    public double[][] getData(int dataset) {
        _checkDatasetIndex(dataset);

        Vector pts = (Vector) _points.elementAt(dataset);
        int size = pts.size();
        double[][] result = new double[2][size];

        for (int i = 0; i < size; i++) {
            PlotPoint pt = (PlotPoint) pts.elementAt(i);
            result[0][i] = pt.x;
            result[1][i] = pt.y;
        }

        return result;
    }

    /** Redo the latest signal editing operation that was undone by
     *  calling undo(), if there was one.  Otherwise, do nothing.
     */
    public void redo() {
        if (_redoStack.empty()) {
            return;
        }

        Object[] save = new Object[2];
        save[0] = new Integer(_dataset);
        save[1] = getData(_dataset);
        _undoStack.push(save);

        Object[] saved = (Object[]) _redoStack.pop();
        _setData(((Integer) saved[0]).intValue(), (double[][]) saved[1]);

        // Ensure replot of offscreen buffer.
        _plotImage = null;
        repaint();
        _notifyListeners(_dataset);
    }

    /** Unregister a edit listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which edit events are sent.
     */
    public void removeEditListener(EditListener listener) {
        if (_editListeners == null) {
            return;
        }

        _editListeners.removeElement(listener);
    }

    /** Specify which dataset is editable. By default, if this method is
     *  not called, dataset number zero is editable.  If you call this
     *  method with a negative number, then no dataset will be editable.
     *  @param dataset The editable dataset.
     */
    public void setEditable(int dataset) {
        if (dataset >= 0) {
            _checkDatasetIndex(dataset);
        }

        _dataset = dataset;
    }

    /** Undo the latest signal editing operation, if there was one.
     *  Otherwise, do nothing.
     */
    public void undo() {
        if (_undoStack.empty()) {
            return;
        }

        Object[] save = new Object[2];
        save[0] = new Integer(_dataset);
        save[1] = getData(_dataset);
        _redoStack.push(save);

        Object[] saved = (Object[]) _undoStack.pop();
        _setData(((Integer) saved[0]).intValue(), (double[][]) saved[1]);

        // Ensure replot of offscreen buffer.
        _plotImage = null;
        repaint();
        _notifyListeners(_dataset);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Clear the editing spec and modify the dataset.
    private synchronized void _edit(int x, int y) {
        if (_dataset < 0) {
            return;
        }

        // Save for undo.
        Object[] save = new Object[2];
        save[0] = new Integer(_dataset);
        save[1] = getData(_dataset);

        // FIXME: Need a way to notify menus to enable items...
        _undoStack.push(save);

        // NOTE: the clear() method was added in jdk 1.2, so we don't
        // use it here for maximal compatibility...
        // _redoStack.clear();
        while (!_redoStack.empty()) {
            _redoStack.pop();
        }

        // constrain to be in range
        if (y > _lry) {
            y = _lry;
        }

        if (y < _uly) {
            y = _uly;
        }

        if (x > _lrx) {
            x = _lrx;
        }

        if (x < _ulx) {
            x = _ulx;
        }

        _editPoint(x, y);

        // Edit the points in the signal.
        Vector pts = (Vector) _points.elementAt(_dataset);

        for (int i = 0; i < pts.size(); i++) {
            PlotPoint pt = (PlotPoint) pts.elementAt(i);

            // Only bother with points in visual range
            if ((pt.x >= _xMin) && (pt.x <= _xMax)) {
                int index = (int) ((pt.x - _xMin) * _xscale)
                    - (_lrx - _ulx - _editSpecX.length);

                if ((index >= 0) && (index < _editSpecX.length)) {
                    if (_editSpecSet[index]) {
                        pt.y = _yMax - ((_editSpecY[index] - _uly) / _yscale);

                        // For auto-ranging, keep track of min and max.
                        if (pt.y < _yBottom) {
                            _yBottom = pt.y;
                        }

                        if (pt.y > _yTop) {
                            _yTop = pt.y;
                        }
                    }
                }
            }
        }

        // Ensure replot of offscreen buffer.
        _plotImage = null;
        repaint();

        // Erase the guide
        // I don't think we need to do this, since we call repaint().
        //         graphics.setXORMode(_editColor);
        //         for (int i = 0; i < _editSpecX.length; i++) {
        //             if (_editSpecSet[i]) {
        //                 graphics.drawLine(_editSpecX[i], _editSpecY[i]-1,
        //                         _editSpecX[i], _editSpecY[i]+1);
        //             }
        //         }
        //         graphics.setPaintMode();
        _notifyListeners(_dataset);
    }

    // Make a record of a new edit point.
    private synchronized void _editPoint(int x, int y) {
        if (_dataset < 0) {
            return;
        }

        Graphics graphics = getGraphics();

        // constrain to be in range
        if (y > _lry) {
            y = _lry;
        }

        if (y < _uly) {
            y = _uly;
        }

        if (x > _lrx) {
            x = _lrx;
        }

        if (x < _ulx) {
            x = _ulx;
        }

        if ((x <= _currentEditX) || (x >= _lrx)) {
            // ignore
            return;
        }

        int step = _currentEditX;

        while (step <= x) {
            int index = step - (_lrx - _editSpecX.length);
            double proportion = (step - _currentEditX) / (double) (x
                    - _currentEditX);
            int newY = (int) (_currentEditY
                    + (proportion * (y - _currentEditY)));

            if (!_editSpecSet[index]) {
                _editSpecX[index] = step;
                _editSpecY[index] = newY;
                _editSpecSet[index] = true;

                // Draw point, linearly interpolated from previous point
                graphics.setXORMode(_editColor);
                graphics.drawLine(step, newY - 1, step, newY + 1);
                graphics.setPaintMode();
            }

            step++;
        }

        _currentEditX = x;
        _currentEditY = y;
    }

    // Make a record of the starting x and y position of an edit.
    private synchronized void _editStart(int x, int y) {
        if (_dataset < 0) {
            return;
        }

        // constrain to be in range
        if (y > _lry) {
            y = _lry;
        }

        if (y < _uly) {
            y = _uly;
        }

        if (x > _lrx) {
            x = _lrx;
        }

        if (x < _ulx) {
            x = _ulx;
        }

        // Allocate a vector to store the points.
        int size = _lrx - x + 1;
        _editSpecX = new int[size];
        _editSpecY = new int[size];
        _editSpecSet = new boolean[size];

        _editSpecX[0] = x;
        _editSpecY[0] = y;
        _editSpecSet[0] = true;

        _currentEditX = x;
        _currentEditY = y;

        Graphics graphics = getGraphics();

        // Draw point (as a 3 pixel vertical line, for thickness)
        graphics.setXORMode(_editColor);
        graphics.drawLine(x, y - 1, x, y + 1);
        graphics.setPaintMode();
    }

    // Notify all edit listeners that have registered.
    private void _notifyListeners(int dataset) {
        if (_editListeners == null) {
            return;
        } else {
            Enumeration listeners = _editListeners.elements();

            while (listeners.hasMoreElements()) {
                ((EditListener) listeners.nextElement()).editDataModified(this,
                        dataset);
            }
        }
    }

    // Set the data in the specified dataset. The argument is of the
    // form returned by getData.
    private void _setData(int dataset, double[][] data) {
        _checkDatasetIndex(dataset);

        Vector pts = (Vector) _points.elementAt(dataset);
        int size = pts.size();

        if (data[0].length < size) {
            size = data[0].length;
        }

        for (int i = 0; i < size; i++) {
            PlotPoint pt = (PlotPoint) pts.elementAt(i);
            pt.x = data[0][i];
            pt.y = data[1][i];
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int[] _editSpecX;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int[] _editSpecY;
    private boolean[] _editSpecSet;
    private int _currentEditX;
    private int _currentEditY;
    private int _dataset = 0;

    // Call setXORMode with a hardwired color because
    // _background does not work in an application,
    // and _foreground does not work in an applet
    private static final Color _editColor = Color.white;

    // Stack for undo.
    private Stack _undoStack = new Stack();
    private Stack _redoStack = new Stack();

    // Edit listeners.
    private Vector _editListeners = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    public class EditMouseListener implements MouseListener {
        public void mouseClicked(MouseEvent event) {
        }

        public void mouseEntered(MouseEvent event) {
        }

        public void mouseExited(MouseEvent event) {
        }

        public void mousePressed(MouseEvent event) {
            if ((event.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
                EditablePlot.this._editStart(event.getX(), event.getY());
            }
        }

        public void mouseReleased(MouseEvent event) {
            if ((event.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
                EditablePlot.this._edit(event.getX(), event.getY());
            }
        }
    }

    public class ModifyListener implements MouseMotionListener {
        public void mouseDragged(MouseEvent event) {
            if ((event.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
                EditablePlot.this._editPoint(event.getX(), event.getY());
            }
        }

        public void mouseMoved(MouseEvent event) {
        }
    }

    public class UndoListener implements KeyListener {
        public void keyPressed(KeyEvent e) {
            int keycode = e.getKeyCode();

            switch (keycode) {
            case KeyEvent.VK_CONTROL:
                _control = true;
                break;

            case KeyEvent.VK_Z:

                if (_control) {
                    undo();
                }

                break;

            case KeyEvent.VK_Y:

                if (_control) {
                    redo();
                }

                break;

            default:
                // None
            }
        }

        public void keyReleased(KeyEvent e) {
            int keycode = e.getKeyCode();

            switch (keycode) {
            case KeyEvent.VK_CONTROL:
                _control = false;
                break;

            default:
                // None
            }
        }

        // The keyTyped method is broken in jdk 1.1.4.
        // It always gets "unknown key code".
        public void keyTyped(KeyEvent e) {
        }

        private boolean _control = false;
    }
}
