package density;

import java.text.NumberFormat;
import javax.swing.*;
import javax.swing.text.*;

public class DoubleNumberField extends JFormattedTextField {

    // why is this so hard?
    public DoubleNumberField(Double val) {
	super(new javax.swing.text.NumberFormatter());
	NumberFormat nf = NumberFormat.getNumberInstance();
	nf.setMaximumFractionDigits(9);
	((NumberFormatter) getFormatter()).setFormat(nf);
	setValue(val);
    }
}
