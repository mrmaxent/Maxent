package gui.layouts;

import java.awt.*;

public class AspectBoundable
      implements BoundableInterface {
      public void setBounds(Component c,
                            int x, int y,
                            int w, int h) {
          Dimension wantedSize =
              new Dimension(w, h);
          Dimension d = c.getPreferredSize();
          d = scale(d, wantedSize);
          c.setBounds(x, y, d.width, d.height);
      }

      /**
       * scale returns a new dimension that has
       * the same aspect ratio as the first
       * dimension but has no part larger than the
       * second dimension
       */
      public Dimension scale(Dimension imageDimension,
                             Dimension availableSize) {
          double ar =
             imageDimension.width /
             (imageDimension.height*1.0);
          double availableAr = availableSize.width /
             (availableSize.height*1.0);
          int newHeight =
              (int)(availableSize.width / ar);
          int newWidth =
              (int)(availableSize.height * ar);
          if (availableAr < ar )
              return new Dimension (availableSize.width,
                                    newHeight);
          return new Dimension(newWidth,
                               availableSize.height);
      }

      public Dimension scaleWidth(Dimension d1,
                                  Dimension d2) {
          double scaleFactor =
              d2.width / (d1.width * 1.0);
          return scale(d1, scaleFactor);
      }

      private Dimension scale(Dimension d1,
                              double scaleFactor) {
          return new Dimension(
                     (int) (d1.width * scaleFactor),
                     (int) (d1.height * scaleFactor));
      }

      public Dimension scaleHeight(Dimension d1,
                                   Dimension d2) {
          double scaleFactor = d2.height /
                               (d1.height * 1.0);
          return scale(d1, scaleFactor);
      }

}

