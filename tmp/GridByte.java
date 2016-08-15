package density;

public abstract class GridByte extends Grid {
    public GridByte(GridDimension dim, String s) { super(dim, s); }
    public abstract byte evalByte(int r, int c);
    public float eval(int r, int c) { return evalByte(r, c); }
}
