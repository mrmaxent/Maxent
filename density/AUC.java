package density;
import java.io.*;
import java.util.Random;

public class AUC {

    public static void main(String[] args) {
	if (args.length < 2) {
	    System.out.println("Usage: AUC testpoints prediction");
	    System.exit(1);
	}
	try { 
	    new AUC().go(args);
	}
	catch (IOException e) { 
	    System.out.println("Error in AUC: " + e.toString());
	    System.exit(1);
	}
    }

    void go(String[] args) throws IOException {
	boolean zeroseed = true;
	Utils.generator = new Random(zeroseed ? 0 : System.currentTimeMillis());
	String testpoints = args[0], predfile = args[1];
	String[] species = (args.length>2) ? new String[] {args[2]} : null;
	Extractor ext = new Extractor();
	ext.extractSamples(new String[] {predfile}, 10000, testpoints, null, species);
	double[] back = new double[ext.numBackground];
	for (int i=0; i<back.length; i++)
	    back[i] = ext.randextract[i].vals[0];
	double[] presence = new double[ext.sampleextract.length];
	for (int i=0; i<presence.length; i++)
	    presence[i] = ext.sampleextract[i].vals[0];
	System.out.println(density.tools.Stats.auc(presence, back));
    }
}
