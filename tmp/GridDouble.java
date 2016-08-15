package density;

public abstract class GridDouble extends Grid {
    public GridDouble(GridDimension dim, String s) { super(dim, s); }
    public abstract double evalDouble(int r, int c);
    public float eval(int r, int c) { return (float) evalDouble(r,c); }
}
