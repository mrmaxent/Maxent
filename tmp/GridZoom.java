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
