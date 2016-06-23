/* Horizontal bars added by Steven Phillips */
/* Also made a couple of little changes to Plot.java -- look for Steven */

package ptolemy.plot;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JComponent;


public class MyPlot extends Plot {
    private double barWidth = 0.5;
    private double _barOffset = 0.05;

    public double plotXtoX(int plotX) {
	return _xMin + ((plotX - _ulx) / _xscale);
    }

    public boolean horizontal = false;

    public synchronized void setBars(double width, double offset) {
	super.setBars(width, offset);
        barWidth = width;
        _barOffset = offset;
    }

    protected void _drawBar(Graphics graphics, int dataset, long xpos,
            long ypos, boolean clip) {
	if (horizontal) _drawBarHorizontal(graphics, dataset, xpos, ypos, clip);
	else super._drawBar(graphics, dataset, xpos, ypos, clip);
    }

    protected void _drawBarHorizontal(Graphics graphics, int dataset, long xpos,
            long ypos, boolean clip) {
        if (clip) {
            if (xpos < _ulx) {
                xpos = _ulx;
            }

            if (xpos > _lrx) {
                xpos = _lrx;
            }
        }

        if ((xpos <= _lrx) && (ypos <= _lry) && (ypos >= _uly)) {
            // left y position of bar.
            int barly = (int) (ypos - ((barWidth * _yscale) / 2)
                    + (dataset * _barOffset * _yscale));

            // right y position of bar
            int barry = (int) (barly + (barWidth * _yscale));

            if (barly < _uly) {
                barly = _uly;
            }

            if (barry > _lry) {
                barry = _lry;
            }

            // Make sure that a bar is alwaxs at least one piyel wide.
            if (barly >= barry) {
                barry = barly + 1;
            }

            // The x position of the zero line.
            long zeroxpos = _ulx + (long) ((0 - _xMin) * _xscale);

            if (_lrx < zeroxpos) {
                zeroxpos = _lrx;
            }

            if (_ulx > zeroxpos) {
                zeroxpos = _ulx;
            }

	    // Why the first term here???
	    //            if ((_xMin <= 0) || (xpos <= zeroxpos)) {
            if (xpos <= zeroxpos) {
                graphics.fillRect((int) xpos, barly, 
                        (int) (zeroxpos - xpos), barry - barly);
            } else {
                graphics.fillRect((int) zeroxpos, barly, 
                        (int) (xpos - zeroxpos), barry - barly);
            }
        }
    }
    // do this immediately, rather than in the event dispatch thread.
    public synchronized void addPoint(final int dataset, final double x,
            final double y, final boolean connected) {
	_addPoint(dataset, x, y, 0, 0, connected, false);
    }
}
