package density;

import java.util.HashMap;

public class Sample {
    int point, row, col;
    double lat, lon;
    String name;
    public HashMap featureMap;  // maps feature names to doubles

    public Sample(int p, int r, int c, double lat, double lon, String s) { this(p,r,c,lat,lon,s,null); }
    public Sample(int p, int r, int c, double lat, double lon, String s, HashMap map) {
	point = p; row=r; col=c;
	this.lat = lat;
	this.lon = lon;
	featureMap = map;
	name = s;
    }

    int getPoint() { return point; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    int getRow(GridDimension dim) { return dim.toRow(lat); }
    int getCol(GridDimension dim) { return dim.toCol(lon); }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public String getName() { return name; }
}
