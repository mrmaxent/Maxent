package density;

abstract class LayerFeature extends Feature {
    int layerType;
    public LayerFeature(int n, String s, int t) { super(n,s); layerType=t; }
    public int getLayerType() { return layerType; }
}
