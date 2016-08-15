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

public class GridZoom extends Zoom {
    Grid grid;
    double min, max;

    public GridZoom(Grid g) { 
	setGrid(g);
    }

    public void setGrid(Grid g) {
	grid = g;
	float[] minmax = g.minmax();
	min = minmax[0];
	max = minmax[1];
	setImageDim(new java.awt.Dimension(g.getDimension().getncols(), g.getDimension().getnrows()));
    }

    public int showColor(int r, int c) {
	if (!grid.hasData(r,c)) return 0;
	double v = grid.eval(r,c);
	int i = (int) ((max - v) * 1020.0 / (max-min));
	int red = (i<256)?255:(i>510) ? 0 : 510-i;
	int green = (i<256)?i:(i<765)?255:1020-i;
	int blue = (i<510)?0:(i<765)?i-510:255;
	return ((255<<24) | (red<<16) | (green<<8) | blue);
    }

}
