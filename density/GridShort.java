package density;

public abstract class GridShort extends Grid {
    public GridShort(GridDimension dim, String s) { super(dim, s); }
    public abstract short evalShort(int r, int c);
    public float eval(int r, int c) { return evalShort(r,c); }
}
