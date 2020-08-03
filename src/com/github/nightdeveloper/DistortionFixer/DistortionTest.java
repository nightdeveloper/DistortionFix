package com.github.nightdeveloper.DistortionFixer;

import boofcv.alg.distort.AdjustmentType;
import boofcv.alg.distort.ImageDistort;
import boofcv.alg.distort.LensDistortionOps;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.border.BorderType;
import boofcv.struct.calib.CameraPinhole;
import boofcv.struct.calib.CameraPinholeBrown;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.image.BufferedImage;

public class DistortionTest extends AbstractBaseTest implements ChangeListener {

    private static CameraPinholeBrown param;

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

            javax.swing.SwingUtilities.invokeLater(this::updateImage);
        }
    }

    @Override
    protected void updateImage() {
        int numBands = originalImg.getNumBands();

        CameraPinhole desired = new CameraPinhole(param);

        ImageDistort<Planar<GrayF32>, Planar<GrayF32>> allInside =
                LensDistortionOps.changeCameraModel(AdjustmentType.FULL_VIEW, BorderType.ZERO,
                    param, desired, null, ImageType.pl(numBands, GrayF32.class));

        Planar<GrayF32> undistortedImg = new Planar<>(GrayF32.class,
                originalImg.getWidth(), originalImg.getHeight(), originalImg.getNumBands());

        allInside.apply(originalImg, undistortedImg);
        BufferedImage out1 = ConvertBufferedImage.convertTo(undistortedImg, null, true);
        imagePanel.setImage(out1);
        imagePanel.repaintJustImage();
        log("image updated to " + param.toString());
    }

    @Override
    protected void initParams() {
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
    }

    @Override
    protected void initSliders(JFrame frame) {
        frame.add(getSlider(this, "fx", -1500, 2500, (int)param.getFx()));
        frame.add(getSlider(this, "fy", -1500, 2500, (int)param.getFy()));
        frame.add(getSlider(this, "skew", -1500, 2500, (int)param.getSkew()));
        frame.add(getSlider(this, "cx", -1500, 2500, (int)param.getCx()));
        frame.add(getSlider(this, "cy", -1500, 2500, (int)param.getCy()));
        frame.add(getSlider(this, "radial1", -2000, 4000, (int)(param.getRadial()[0]*1000)));
        frame.add(getSlider(this, "radial2", -2000, 4000, (int)(param.getRadial()[1]*1000)));
//        frame.add(getSlider("t1", -2000, 2000, (int)param.getT1()));
//        frame.add(getSlider("t2", -2000, 2000, (int)param.getT2()));
    }

    public static void main(String[] args) {
        DistortionTest distortionTest = new DistortionTest();
        distortionTest.run();
    }
}