package gui.layouts;

import java.awt.*;

public class PreferredBoundable
      implements BoundableInterface {
      public void setBounds(Component c,
                            int x, int y,
                            int w, int h) {
          Dimension wantedSize = new Dimension(w,h);
          Dimension d = c.getPreferredSize();
          // We select the minimum of the wantedSize
          // and the PreferredSize. Thus, the
          // component cannot grow beyond the
          // Preferred size, but it can shrink to
          // the wantedSize.
          d = min(d,wantedSize);
          c.setBounds(x,y,d.width,d.height);
      }

      public Dimension min(Dimension d1,
                           Dimension d2) {
          if (d1.width < d2.width) return d1;
          if (d1.height < d2.height) return d1;
          return d2;
      }
}

