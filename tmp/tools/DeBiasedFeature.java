package density;

class DeBiasedFeature extends Feature {
    double sampleMean;
    Feature bias, f;

    public DeBiasedFeature(Feature f, Feature bias, Sample[] ss) {
	this.f = f;
	this.bias = bias;
	n=f.n; 
	name=f.name;
	sampleMean = 0.0;
	for (int i=0; i<ss.length; i++)
	    sampleMean += f.eval(ss[i]);
	sampleMean /= ss.length;
    }

    public double eval(int p) { 
	//	return bias.eval(p) * f.eval(p) + (1-bias.eval(p)) * sampleMean;
	return sampleMean + bias.eval(p) * (f.eval(p) - sampleMean);
    }
    public double eval(Sample s) { 
	return f.eval(s);
	//	return sampleMean + bias.eval(s) * (f.eval(s) - sampleMean);
    }
}
