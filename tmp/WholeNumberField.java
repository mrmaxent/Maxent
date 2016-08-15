package density;

import javax.swing.*;

public class WholeNumberField extends JFormattedTextField {

    public WholeNumberField(int val, int min, int max) {
	super(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#######")));
	javax.swing.text.NumberFormatter nf = (javax.swing.text.NumberFormatter) getFormatter();
	nf.setMinimum(new Integer(min));
	nf.setMaximum(new Integer(max));
	nf.setValueClass(Integer.class);
	setValue(new Integer(val));
    }
}
