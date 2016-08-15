package density;

import java.util.HashMap;
import java.io.IOException;

/**
 * An Evaluate object is used to make a prediction from a Maxent model for 
 * a given set of enviromental variable values.
 */
public class Evaluate {
    Grid projectGrid;
    HashMap varmap;
    Params params = new Params();

    /**
     * Construct an evaluation object for a Maxent model file
     * @param lambdafile the filename for the Maxent model file
     * @throws IOException in case of problems with the file
     */
    public Evaluate(String lambdafile) throws IOException {
	Project proj = new Project(params);
	proj.mapping = true;
	proj.varmap = varmap = new HashMap();
	projectGrid = proj.projectGrid(lambdafile, null)[0];
    }

    /**
     * Set a value for an environmental variable
     * @param variable the environmental variable
     * @param value the value to be assigned to the variable
     */
    public void setValue(String variable, double value) {
	varmap.put(variable, value);
    }

    /**
     * Evaluate the Maxent model on the given environmental variable values.
     * Produces output in Maxent's logistic format.
     * This method should only be called after all variables have been assigned
     * values.
     * @return The model output value
     */
    public double evaluate() {
	return projectGrid.eval(0,0);
    }

    /**
     * Returns the Params object, which can be used to set parameters
     * that affect evaluation of the model, such as "extrapolate".
     * @return A Params object
     */
    public Params getParams() { return params; }

}
