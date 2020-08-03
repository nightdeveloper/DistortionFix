package com.github.nightdeveloper.DistortionFixer;

import boofcv.alg.distort.*;
import boofcv.alg.distort.pinhole.LensDistortionPinhole;
import boofcv.alg.distort.universal.LensDistortionUniversalOmni;
import boofcv.alg.interpolate.InterpolatePixel;
import boofcv.alg.interpolate.InterpolationType;
import boofcv.factory.distort.FactoryDistort;
import boofcv.factory.interpolate.FactoryInterpolation;
import boofcv.io.calibration.CalibrationIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.border.BorderType;
import boofcv.struct.calib.CameraPinhole;
import boofcv.struct.calib.CameraUniversalOmni;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.geometry.ConvertRotation3D_F32;
import georegression.struct.EulerType;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

public class FishEyeToPinHoleTest extends AbstractBaseTest implements ChangeListener {

    private static CameraUniversalOmni fisheyeModel;
    private static CameraPinhole pinholeModel;

    @Override
    protected void initParams() {

        fisheyeModel = new CameraUniversalOmni(2);
        fisheyeModel.setFx(1125.799138869);
        fisheyeModel.setFy(1127.163628308);
        fisheyeModel.setCx(479.4346090575);
        fisheyeModel.setCy(478.637265237);
        fisheyeModel.setWidth(originalImg.width);
        fisheyeModel.setHeight(originalImg.height);
        fisheyeModel.setSkew(0.0);

        fisheyeModel.setRadial(new double[]{0.226573352659, 6.72940754992});
        fisheyeModel.setT1(0.004624464338);
        fisheyeModel.setT2(0.000966390674543);
        fisheyeModel.setMirrorOffset(2.94487011878);

        pinholeModel = new CameraPinhole(400, 400, 0, 300, 300, 600, 600);
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
                case "p_fx": {
                    pinholeModel.setFx(value);
                    break;
                }
                case "p_fy": {
                    pinholeModel.setFy(value);
                    break;
                }
                case "p_skew": {
                    pinholeModel.setSkew(value);
                    break;
                }
                case "p_cx": {
                    pinholeModel.setCx(value);
                    break;
                }
                case "p_cy": {
                    pinholeModel.setCy(value);
                    break;
                }

                case "f_fx": {
                    fisheyeModel.setFx(value);
                    break;
                }
                case "f_fy": {
                    fisheyeModel.setFy(value);
                    break;
                }
                case "f_skew": {
                    fisheyeModel.setSkew(value);
                    break;
                }
                case "f_cx": {
                    fisheyeModel.setCx(value);
                    break;
                }
                case "f_cy": {
                    fisheyeModel.setCy(value);
                    break;
                }
                case "f_radial1": {
                    double[] current = fisheyeModel.getRadial();
                    current[0] = ((double) value) / 1000;
                    fisheyeModel.setRadial(current);
                    break;
                }
                case "f_radial2": {
                    double[] current = fisheyeModel.getRadial();
                    current[1] = ((double) value) / 1000;
                    fisheyeModel.setRadial(current);
                    break;
                }
                case "f_t1": {
                    fisheyeModel.setT1(((double) value) / 10000);
                    break;
                }
                case "f_t2": {
                    fisheyeModel.setT2(((double) value) / 10000);
                    break;
                }
                case "f_mirror": {
                    fisheyeModel.setMirrorOffset(((double) value) / 100);
                    break;
                }
            }

            javax.swing.SwingUtilities.invokeLater(this::updateImage);
        }
    }

    @Override
    protected void initSliders(JFrame frame) {

        frame.add(getSlider(this, "p_fx", -2500, 3500, (int) pinholeModel.getFx()));
        frame.add(getSlider(this, "p_fy", -2500, 3500, (int) pinholeModel.getFy()));
        frame.add(getSlider(this, "p_skew", -2500, 3500, (int) pinholeModel.getSkew()));
        frame.add(getSlider(this, "p_cx", -1500, 3500, (int) pinholeModel.getCx()));
        frame.add(getSlider(this, "p_cy", -1500, 3500, (int) pinholeModel.getCy()));

        frame.add(getSlider(this, "f_fx", -1500, 3500, (int) fisheyeModel.getFx()));
        frame.add(getSlider(this, "f_fy", -1500, 3500, (int) fisheyeModel.getFy()));
        frame.add(getSlider(this, "f_skew", -1500, 3500, (int) fisheyeModel.getSkew()));
        frame.add(getSlider(this, "f_cx", -1500, 3500, (int) fisheyeModel.getCx()));
        frame.add(getSlider(this, "f_cy", -1500, 3500, (int) fisheyeModel.getCy()));

        frame.add(getSlider(this, "f_radial1", -4000, 8000, (int) (fisheyeModel.getRadial()[0] * 1000)));
        frame.add(getSlider(this, "f_radial2", -4000, 8000, (int) (fisheyeModel.getRadial()[1] * 1000)));

        frame.add(getSlider(this, "f_t1", -4000, 4000, (int) (fisheyeModel.getT1() * 10000)));
        frame.add(getSlider(this, "f_t2", -4000, 4000, (int) (fisheyeModel.getT2() * 10000)));

        frame.add(getSlider(this, "f_mirror", -1000, 1000, (int) (fisheyeModel.getMirrorOffset() * 100)));
    }

    @Override
    protected void updateImage() {

        // Create the transform from pinhole to fisheye views
        LensDistortionNarrowFOV pinholeDistort = new LensDistortionPinhole(pinholeModel);
        LensDistortionWideFOV fisheyeDistort = new LensDistortionUniversalOmni(fisheyeModel);
        NarrowToWidePtoP_F32 transform = new NarrowToWidePtoP_F32(pinholeDistort,fisheyeDistort);

        // Create the image distorter which will render the image
        InterpolatePixel<Planar<GrayF32>> interp = FactoryInterpolation.
                createPixel(0, 255, InterpolationType.BILINEAR, BorderType.ZERO, originalImg.getImageType());
        ImageDistort<Planar<GrayF32>,Planar<GrayF32>> distorter =
                FactoryDistort.distort(false,interp,originalImg.getImageType());

        // Pass in the transform created above
        distorter.setModel(new PointToPixelTransform_F32(transform));

        // Render the image.  The camera will have a rotation of 0 and will thus be looking straight forward
        Planar<GrayF32> pinholeImage = originalImg.createNew(pinholeModel.width, pinholeModel.height);

        distorter.apply(originalImg,pinholeImage);
        BufferedImage bufferedPinhole0 = ConvertBufferedImage.convertTo(pinholeImage,null,true);

        // rotate the virtual pinhole camera to the right
        transform.setRotationWideToNarrow(ConvertRotation3D_F32.eulerToMatrix(EulerType.YXZ,0.8f,0,0,null));

        distorter.apply(originalImg,pinholeImage);
        BufferedImage bufferedPinhole1 = ConvertBufferedImage.convertTo(pinholeImage,null,true);

        imagePanel.setImage(bufferedPinhole0);
        imagePanel.repaintJustImage();

        imagePanel2.setImage(bufferedPinhole1);
        imagePanel2.repaintJustImage();

        log("image updated to pinhole = " + pinholeModel.toString() + ", fisheye = " + fisheyeModel.toString() +
                ", radix = " + Arrays.toString(fisheyeModel.getRadial()));
    }

    public static void main(String[] args) {
        FishEyeToPinHoleTest fishEyeToPinHoleTest = new FishEyeToPinHoleTest();
        fishEyeToPinHoleTest.run();
    }
}
