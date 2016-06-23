package gui.layouts;

import java.awt.*;

public class PreferredSizeGridLayout
      extends GridLayout {

      private BoundableInterface boundableInterface =
          new PreferredBoundable();

      public PreferredSizeGridLayout() {
          this(1, 0, 0, 0);
      }

      public PreferredSizeGridLayout(int rows,
                                    int cols) {
          this(rows, cols, 0, 0);
      }

      public PreferredSizeGridLayout(int rows,
                                     int cols,
                                     int hgap,
                                     int vgap) {
          super(rows,cols,hgap,vgap);
      }

      /**
       * Lays out the specified container using this
       * layout.
       *
       * This method reshapes the components in the
       * specified target container in order to
       * satisfy the constraints of the
       *  PreferredSizeGridLayout object.
       *
       * The grid layout manager determines the size
       * of individual components by dividing the free
       * space in the container into equal-sized
       * portions according to the number of rows
       * and columns in the layout. The container's
       * free space equals the container's
       * size minus any insets and any specified
       * horizontal or vertical gap. All components
       * in a grid layout are given the Minimum of
       * the same size or the preferred size.
       *
       * @param target the container in which to do
       *        the layout.
       * @see java.awt.Container
       * @see java.awt.Container#doLayout
       */

      public void layoutContainer(Container parent) {
          synchronized (parent.getTreeLock()) {
              Insets insets = parent.getInsets();
              int ncomponents =
                  parent.getComponentCount();
              int nrows = getRows();
              int ncols = getColumns();

              if (ncomponents == 0) {
                  return;
              }
              if (nrows > 0) {
                  ncols = (ncomponents + nrows - 1) /
                          nrows;
              } else {
                  nrows = (ncomponents + ncols - 1) /
                          ncols;
              }
              int w = parent.getWidth() -
                     (insets.left + insets.right);
              int h = parent.getHeight() -
                     (insets.top + insets.bottom);
              w = (w - (ncols - 1) * getHgap()) /
                   ncols;
              h = (h - (nrows - 1) * getVgap()) /
                  nrows;

              for (int c = 0, x = insets.left;
                   c < ncols;
                   c++, x += w + getHgap()) {
                  for (int r = 0, y = insets.top;
                       r < nrows;
                       r++, y += h + getVgap()) {
                      int i = r * ncols + c;
                      if (i < ncomponents) {
                          boundableInterface.setBounds(
                              parent.getComponent(i),x, y,
                              w, h);
                      }
                  }
              }
          }
      }

      public BoundableInterface getBoundableInterface() {
          return boundableInterface;
      }

      public void setBoundableInterface(BoundableInterface
                     boundableInterface) {
          this.boundableInterface = boundableInterface;
      }

}
