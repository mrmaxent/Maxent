/*
Copyright (c) 2016 Steven Phillips, Miro Dudik and Rob Schapire

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions: 

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software. 

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
*/

package density;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;

public class Zoom extends JPanel implements MouseMotionListener, MouseListener {
    BufferedImage img;
    int[] pixels;
    Dimension imageDim;
    Point center, showingDot;
    double zoomLevel=1;

    public int showColor(int r, int c) {
	return new Color(256*r/imageDim.height, 256*c/imageDim.width, 0).getRGB();
    }

    public void adjustZoom(double r) {
	zoomLevel *= r;
	makeImage();
    }

    public void right(double r) {
	center = new Point(center.x, (int) (center.y - r * imageDim.width / zoomLevel));
	makeImage();
    }

    public void down(double r) {
	center = new Point((int) (center.x - r * imageDim.height / zoomLevel), center.y);
	makeImage();
    }

    public void sweep(int r1, int c1, int r2, int c2) {
	if (r1==r2 || c1==c2) return;
	center = new Point((int) ((r1+r2)/2.0), (int) ((c1+c2)/2.0));
	double rdiff = Math.abs(r1-r2), cdiff = Math.abs(c1-c2);
	double rscale = imageDim.height/rdiff, cscale = imageDim.width/cdiff;
	zoomLevel = rscale < cscale ? rscale : cscale;
	makeImage();
    }

    public void setImageDim(Dimension d) { 
	if (!d.equals(imageDim)) {
	    imageDim = d; 
	    recenter(false);
	}
    }

    public void recenter(boolean remake) { 
	center = new Point(imageDim.height/2, imageDim.width/2);
	zoomLevel = 1;
	if (remake) makeImage();
    }

    public Zoom() { 
	addMouseListener(this);
	addMouseMotionListener(this);
    }

    int startrow, startcol, width, height;
    double scale;
    public void makeImage() {
	width = getWidth();
	height = getHeight();
	if (img==null || img.getWidth() != width || img.getHeight() != height) {
	    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
	}
	Arrays.fill(pixels, 0);
	double hratio = imageDim.height / zoomLevel / (height-10);
	double wratio = imageDim.width / zoomLevel / (width-10);
	scale = (hratio > wratio) ? hratio : wratio;
	startrow = center.x  - (int) ((height * scale)/2);
	startcol = center.y  - (int) ((width * scale)/2);
	for (int r=0; r<height; r++) {
	    int ir = gridrow(r);
	    if (ir < 0 || ir >= imageDim.height) continue;
	    for (int c=0; c<width; c++) {
		int ic = gridcol(c);
		if (ic < 0 || ic >= imageDim.width) continue;
		pixels[r*width+c] = showColor(ir, ic);
	    }
	}
	if (showingDot!=null) {
	    for (int r=-2; r<3; r++) {
		int rr = r+row2y(showingDot.y);
		if (rr<0 || rr >= height) continue;
		for (int c=-2; c<3; c++) {
		    int cc = c + col2x(showingDot.x);
		    if (cc>=0 && cc<width)
			pixels[rr*width+cc] = showDotColor;
		}
	    }
	}
	repaint();
    }

    int gridrow(int r) { return startrow + (int) (r * scale); }
    int gridcol(int c) { return startcol + (int) (c * scale); }
    int row2y(int r) { return (int) ((r+0.5-startrow) / scale); }
    int col2x(int c) { return (int) ((c+0.5-startcol) / scale); }

    public void paint(Graphics g) {
	if (img==null || img.getWidth() != getWidth() || img.getHeight() != getHeight())
	    makeImage();
	else
	    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
    }

    public void update(Graphics g) {paint(g);}    // don't blank canvas first

    
    int showDotColor = (255<<24) | (160<<16) | (32<<8) | 240;
    public void showDot() {
	showingDot = clicked;
	makeImage();
    }
    Point pressed, clicked;
    Rectangle rect;
    public void mouseClicked(MouseEvent e) {
	clicked = new Point(gridcol(e.getX()), gridrow(e.getY()));
	Zoom.this.mouseClicked(gridrow(e.getY()), gridcol(e.getX()));
    }
    public void mouseClicked(int r, int c) {}

    public void mousePressed(MouseEvent e) {
	pressed = new Point(e.getX(), e.getY());
	rect=null;
    }

    public void mouseReleased(MouseEvent e) {
	undrawRect();
	if (pressed!=null)
	    sweep(gridrow(pressed.y), gridcol(pressed.x), gridrow(e.getY()), gridcol(e.getX()));
    }

    public void mouseMoved(MouseEvent e) {
	mousePointAt(gridrow(e.getY()), gridcol(e.getX()));
    }

    void drawRect() {
	Graphics g = getGraphics();
	g.setXORMode(Color.white);
	g.drawRect(rect.x, rect.y, rect.width, rect.height);
    }

    void undrawRect() {
	if (rect!=null)
	    drawRect();
	rect=null;
    }

    public void mouseDragged(MouseEvent e) { 
	undrawRect();
	int x = e.getX(), y=e.getY();
	rect = new Rectangle(x<pressed.x?x:pressed.x, y<pressed.y?y:pressed.y, Math.abs(x-pressed.x), Math.abs(y-pressed.y));
	drawRect();
    }

    public void mousePointAt(int r, int c) {
	System.out.println(r + " " + c);
    }

    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

}
