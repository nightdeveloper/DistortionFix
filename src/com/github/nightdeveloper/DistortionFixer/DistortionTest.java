package com.github.nightdeveloper.DistortionFixer;

import boofcv.alg.distort.AdjustmentType;
import boofcv.alg.distort.ImageDistort;
import boofcv.alg.distort.LensDistortionOps;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ScaleOptions;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.border.BorderType;
import boofcv.struct.calib.CameraPinhole;
import boofcv.struct.calib.CameraPinholeBrown;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Hashtable;

public class DistortionTest implements ChangeListener {

    private static Planar<GrayF32> distortedImg;
    private static ImagePanel imagePanel;

    private static CameraPinholeBrown param;

    private static void log(String str) {
        System.out.println(str);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();

        //if (!source.getValueIsAdjusting())
        {
            String name = source.getToolTipText();
            int value = source.getValue();

            log("state changed " + name + " = " + value);

            switch (name) {
                case "fx": {
                    param.setFx(value);
                    break;
                }
                case "fy": {
                    param.setFy(value);
                    break;
                }
                case "skew": {
                    param.setSkew(value);
                    break;
                }
                case "cx": {
                    param.setCx(value);
                    break;
                }
                case "cy": {
                    param.setCy(value);
                    break;
                }
                case "radial1": {
                    double[] current = param.getRadial();
                    current[0] = ((double)value)/1000;
                    param.setRadial(current);
                    break;
                }
                case "radial2": {
                    double[] current = param.getRadial();
                    current[1] = ((double)value)/1000;
                    param.setRadial(current);
                    break;
                }
                case "t1": {
                    param.setT1(value);
                    break;
                }
                case "t2": {
                    param.setT2(value);
                    break;
                }
            }

            javax.swing.SwingUtilities.invokeLater(DistortionTest::updateImage);
        }
    }

    private static void updateImage() {
        int numBands = distortedImg.getNumBands();

        CameraPinhole desired = new CameraPinhole(param);

        ImageDistort<Planar<GrayF32>, Planar<GrayF32>> allInside =
                LensDistortionOps.changeCameraModel(AdjustmentType.FULL_VIEW, BorderType.ZERO,
                    param, desired, null, ImageType.pl(numBands, GrayF32.class));

        Planar<GrayF32> undistortedImg = new Planar<>(GrayF32.class,
                distortedImg.getWidth(), distortedImg.getHeight(), distortedImg.getNumBands());

        allInside.apply(distortedImg, undistortedImg);
        BufferedImage out1 = ConvertBufferedImage.convertTo(undistortedImg, null, true);
        imagePanel.setImage(out1);
        imagePanel.repaintJustImage();
        log("image updated to " + param.toString());
    }

    private static JSlider getSlider(String name, int min, int max, int current) {
        JSlider slider = new JSlider();
        slider.setMinimum(min);
        slider.setMaximum(max);
        slider.setValue(current);
        slider.addChangeListener(new DistortionTest());
        slider.setToolTipText(name);

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel(name));
        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true);

        return slider;
    }

    private static BufferedImage rotateImageByDegrees(BufferedImage img, double angle) {

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

    public static void main(String[] args) {

        BufferedImage orig = UtilImageIO.loadImage("images", "test.jpg");

        orig = rotateImageByDegrees(orig, 90);

        distortedImg = ConvertBufferedImage.convertFromPlanar(orig, null, true, GrayF32.class);

        param = new CameraPinholeBrown(
                701.0, // fx
                698.0, // fy
                0.0, // skew
                308.0, // cx
                246.0, // cy
                orig.getWidth(), // width
                orig.getHeight() // height
        );
        param.setRadial(-0.25, 0.099);
        param.setT1(0.0);
        param.setT2(0.0);

        imagePanel = new ImagePanel();
        imagePanel.setScaling(ScaleOptions.DOWN);
        updateImage();

        JFrame frame = new JFrame("Distortion Fix");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        frame.add(imagePanel, "Center");

        frame.add(getSlider("fx", -1500, 2500, (int)param.getFx()));
        frame.add(getSlider("fy", -1500, 2500, (int)param.getFy()));
        frame.add(getSlider("skew", -1500, 2500, (int)param.getSkew()));
        frame.add(getSlider("cx", -1500, 2500, (int)param.getCx()));
        frame.add(getSlider("cy", -1500, 2500, (int)param.getCy()));
        frame.add(getSlider("radial1", -2000, 4000, (int)(param.getRadial()[0]*1000)));
        frame.add(getSlider("radial2", -2000, 4000, (int)(param.getRadial()[1]*1000)));
//        frame.add(getSlider("t1", -2000, 2000, (int)param.getT1()));
//        frame.add(getSlider("t2", -2000, 2000, (int)param.getT2()));

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