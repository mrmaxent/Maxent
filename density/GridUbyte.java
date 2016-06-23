package density;

public abstract class GridUbyte extends Grid {
    public GridUbyte(GridDimension dim, String s) { super(dim, s); }
    public abstract short evalUbyte(int r, int c);
    public float eval(int r, int c) { return evalUbyte(r,c); }
}
