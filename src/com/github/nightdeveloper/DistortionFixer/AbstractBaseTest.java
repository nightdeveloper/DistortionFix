package com.github.nightdeveloper.DistortionFixer;

import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ScaleOptions;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Hashtable;

public abstract class AbstractBaseTest {

    protected static BufferedImage orig;
    protected static Planar<GrayF32> originalImg;

    protected static ImagePanel imagePanel;
    protected static ImagePanel imagePanel2;

    protected static void log(String str) {
        System.out.println(str);
    }

    protected JSlider getSlider(ChangeListener slidersListener, String name, int min, int max, int current) {
        JSlider slider = new JSlider();
        slider.setMinimum(min);
        slider.setMaximum(max);
        slider.setValue(current);
        slider.addChangeListener(slidersListener);
        slider.setToolTipText(name);

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel(name));
        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true);

        return slider;
    }

    protected BufferedImage rotateImageByDegrees(BufferedImage img, double angle) {

        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = img.getWidth();
        int h = img.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((double)(newWidth - w) / 2, (double)(newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, (img1, infoFlags, x1, y1, width, height) -> false);
        g2d.setColor(Color.RED);
        g2d.drawRect(0, 0, newWidth - 1, newHeight - 1);
        g2d.dispose();

        return rotated;
    }

    protected abstract void initParams();

    protected abstract void initSliders(JFrame frame);

    protected abstract void updateImage();

    public void run() {
        orig = UtilImageIO.loadImage("images", "test2.jpg");

        //orig = rotateImageByDegrees(orig, 90);

        originalImg = ConvertBufferedImage.convertFromPlanar(orig, null, true, GrayF32.class);

        initParams();

        imagePanel = new ImagePanel();
        imagePanel.setScaling(ScaleOptions.DOWN);

        imagePanel2 = new ImagePanel();
        imagePanel2.setScaling(ScaleOptions.DOWN);
        updateImage();

        JFrame frame = new JFrame("Distortion Fix");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        frame.add(imagePanel, "Center");
        frame.add(imagePanel2, "Right");

        initSliders(frame);

        frame.pack();
        frame.setMinimumSize(new Dimension(1038, 1098));
        Dimension windowSize = frame.getSize();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point centerPoint = ge.getCenterPoint();
        int dx = centerPoint.x - windowSize.width / 2;
        int dy = centerPoint.y - windowSize.height / 2;
        frame.setLocation(dx, dy);
        frame.setVisible(true);

        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                Component c = (Component)evt.getSource();
                Dimension size = c.getSize();

                log("resize to " + size.getWidth() + " x " + size.getHeight());
            }
        });
    }
}
