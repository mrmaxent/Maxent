package density;

public abstract class GridInt extends Grid {
    public GridInt(GridDimension dim, String s) { super(dim, s); }
    public abstract int evalInt(int r, int c);
    public float eval(int r, int c) { return evalInt(r,c); }
}
