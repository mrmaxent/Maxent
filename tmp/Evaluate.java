/*
Copyright (c) 2016 Steven Phillips, Miro Dudik and Rob Schapire

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions: 

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software. 

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
*/

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
