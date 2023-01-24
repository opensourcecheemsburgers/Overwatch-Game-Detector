package com.owgamedetector;

import static org.opencv.core.CvType.CV_8U;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.camera.core.ImageProxy;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.visioncamerabase64.BitmapUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRotatedRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)

public class OpenCvTests {

    public static final String OPENCV_TEST_TAG = "OpenCV";
    public static final String MODEL_FILENAME = "east.pb";
    public static final String MODEL_FILENAME_WITHOUT_EXTENSION = "east";
    @Test
    public void templateMatchTest() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        int[] matchMethods = {
                Imgproc.TM_SQDIFF,
                Imgproc.TM_SQDIFF_NORMED,
                Imgproc.TM_CCORR,
                Imgproc.TM_CCORR_NORMED,
                Imgproc.TM_CCOEFF,
                Imgproc.TM_CCOEFF_NORMED
        };

        Mat[] sourceMats = {
                bitmapToMat(toGrayscale(BitmapFactory.decodeResource(context.getResources(), R.drawable.testsource_small))),
                bitmapToMat(toGrayscale(BitmapFactory.decodeResource(context.getResources(), R.drawable.testphonepic_small))),
                bitmapToMat(toGrayscale(BitmapFactory.decodeResource(context.getResources(), R.drawable.faketestsource_small)))
        };

        Mat templateMat = bitmapToMat(toGrayscale(BitmapFactory.decodeResource(context.getResources(), R.drawable.dorado_small)));

        for (int methodIndex = 0; methodIndex < matchMethods.length; methodIndex++) {
            int matchMethod = matchMethods[methodIndex];
            String matchMethodString = "";
            switch (matchMethod) {
                case Imgproc.TM_SQDIFF:
                    matchMethodString = "sqdiff";
                    break;
                case Imgproc.TM_SQDIFF_NORMED:
                    matchMethodString = "sqdiffnormed";
                    break;
                case Imgproc.TM_CCORR:
                    matchMethodString = "ccorr";
                    break;
                case Imgproc.TM_CCORR_NORMED:
                    matchMethodString = "ccorrnormed";
                    break;
                case Imgproc.TM_CCOEFF:
                    matchMethodString = "ccoeff";
                    break;
                case Imgproc.TM_CCOEFF_NORMED:
                    matchMethodString = "ccoeffnormed";
                    break;
                default:
                    throw new RuntimeException();
            }

            Log.d(OPENCV_TEST_TAG, "Using Method: " + matchMethodString);

            for (int sourceIndex = 0; sourceIndex < sourceMats.length; sourceIndex++) {
                Mat sourceMat = sourceMats[sourceIndex];
                Mat sourceMatCopy = new Mat();
                sourceMat.copyTo(sourceMatCopy);

                String sourceString = "";
                switch (sourceIndex) {
                    case 0:
                        sourceString = "source";
                        break;
                    case 1:
                        sourceString = "phonepic";
                        break;
                    case 2:
                        sourceString = "fake";
                }

                Mat resultMat = new Mat();

                int imageCols = sourceMat.cols();
                int imageRows = sourceMat.rows();

                int templateCols = templateMat.cols();
                int templateRows = templateMat.rows();

                int resultCols = sourceMat.cols() - templateMat.cols() + 1;
                int resultRows = sourceMat.rows() - templateMat.rows() + 1;

                resultMat.create(resultRows, resultCols, sourceMat.type());
//                Log.d(OPENCV_TEST_TAG, "Source Type? " + sourceMat.type());
//                Log.d(OPENCV_TEST_TAG, "Template Type? " + templateMat.type());
//                Log.d(OPENCV_TEST_TAG, "Result Type? " + resultMat.type());
//
//                Log.d(OPENCV_TEST_TAG, "Source Empty? " + sourceMat.empty());
//                Log.d(OPENCV_TEST_TAG, "Template Empty? " + templateMat.empty());
//                Log.d(OPENCV_TEST_TAG, "Result Empty? " + resultMat.empty());
//
//                Log.d(OPENCV_TEST_TAG, "Image Cols: " + imageCols);
//                Log.d(OPENCV_TEST_TAG, "Image Rows: " + imageRows);
//                Log.d(OPENCV_TEST_TAG, "Template Cols: " + templateCols);
//                Log.d(OPENCV_TEST_TAG, "Template Rows: " + templateRows);
//                Log.d(OPENCV_TEST_TAG, "Result Cols: " + resultCols);
//                Log.d(OPENCV_TEST_TAG, "Result Rows: " + resultRows);

                double result = performTemplateMatch
                        (
                                context,
                                sourceMat,
                                sourceMatCopy,
                                templateMat,
                                resultMat,
                                matchMethodString.concat("_").concat(sourceString),
                                matchMethods[methodIndex]
                        );
            }
        }
    }

    public void writeBitmapToFile(Context context, Bitmap bitmap, String fileName) {
        String genId = fileName.concat(".png");

        File f = new File(context.getFilesDir(), genId);
        System.out.println("Files dir = " + context.getFilesDir().getAbsolutePath());
        System.out.println("Bitmap gen id = " + genId);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bitmapDataArray = bos.toByteArray();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(bitmapDataArray);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Mat imageProxyToMat(ImageProxy imageProxy) {
        return bitmapToMat(base64ToBitmap(frameToBase64(imageProxy)));
    }

    public Bitmap base64ToBitmap(String imageAsBase64) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        byte[] decodedString = Base64.decode(imageAsBase64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    public Mat bitmapToMat(Bitmap imageAsBitmap) {
        Mat imageAsMat = new Mat();
        Utils.bitmapToMat(toGrayscale(imageAsBitmap), imageAsMat);
        return imageAsMat;
    }

    public Bitmap toGrayscale(Bitmap bmp) {
        int width, height;
        height = bmp.getHeight();
        width = bmp.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmp, 0, 0, paint);
        return bmpGrayscale;
    }

    public double performTemplateMatch(Context context, Mat sourceMat, Mat sourceMatCopy,
                                       Mat templateMat, Mat resultMat, String resultName,
                                       int match_method) {
        Imgproc.matchTemplate(sourceMat, templateMat, resultMat, match_method);
        Core.normalize(resultMat, resultMat, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Core.MinMaxLocResult mmr = Core.minMaxLoc(resultMat);

        Point matchLoc;

        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
        } else {
            matchLoc = mmr.maxLoc;
        }

        double result = match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED ? mmr.minVal : mmr.maxVal;


        Imgproc.rectangle(sourceMatCopy, matchLoc, new Point(matchLoc.x + templateMat.cols(), matchLoc.y + templateMat.rows()),
                new Scalar(255, 255, 255), 2, 8, 0);
        Imgproc.rectangle(resultMat, matchLoc, new Point(matchLoc.x + templateMat.cols(), matchLoc.y + templateMat.rows()),
                Scalar.all(255), 2, 8, 0);
        resultMat.convertTo(resultMat, CV_8U, 255.0);

        Bitmap resultBitmap = Bitmap.createBitmap(sourceMatCopy.width(), sourceMatCopy.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(sourceMatCopy, resultBitmap);
        writeBitmapToFile(context, resultBitmap, resultName);

//        Log.d(OPENCV_TEST_TAG, "Min " + mmr.minVal);
//        Log.d(OPENCV_TEST_TAG, "Max " + mmr.maxVal);

        Log.d("", "Result = " + result);
        return result;
    }

    public String frameToBase64(ImageProxy image) {
        Bitmap.CompressFormat imageFormat = Bitmap.CompressFormat.PNG;
        int quality = 100;

        @SuppressLint("UnsafeOptInUsageError")
        Bitmap bitmap = BitmapUtils.getBitmap(image);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(imageFormat, quality, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    @Test
    public void textDetectionTest() throws IOException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Read model from resources
        Resources resources = context.getResources();
        int modelFileId = resources.getIdentifier(MODEL_FILENAME_WITHOUT_EXTENSION, "raw", context.getPackageName());
        int ModelFileByteLength = (int) resources.openRawResourceFd(modelFileId).getLength();
        FileInputStream fileInputStream = resources.openRawResourceFd(modelFileId).createInputStream();
        byte[] modelFileData = new byte[ModelFileByteLength];
        assert ModelFileByteLength == fileInputStream.read(modelFileData);

        File modelFile = new File(context.getFilesDir(), MODEL_FILENAME);
        FileOutputStream fileOutputStream = new FileOutputStream(modelFile);
        fileOutputStream.write(modelFileData);

        String modelFilePath = context.getFilesDir().toString().concat("/").concat(MODEL_FILENAME);

        float scoreThresh = 0.5f;
        float nmsThresh = 0.4f;
        Net net = Dnn.readNetFromTensorflow(modelFilePath);

        net.setPreferableBackend(Dnn.DNN_BACKEND_OPENCV);
        net.setPreferableTarget(Dnn.DNN_TARGET_CPU);

        Mat sourceMat = Utils.loadResource(context, R.drawable.winston);
        Mat sourceMatCopy = new Mat();
        sourceMat.copyTo(sourceMatCopy);
        Mat sourceMatBlob = new Mat();

        Log.d(OPENCV_TEST_TAG, sourceMat.height() + " " + sourceMat.width());

        sourceMatBlob = Dnn.blobFromImage(
                sourceMat,
                1,
                sourceMat.size(),
                new Scalar(123.68, 116.78, 103.94),
                true,
                false
        );

        Log.d(OPENCV_TEST_TAG, sourceMatBlob.height() + " " + sourceMatBlob.width());

        List<Mat> outputs = new ArrayList<>(2);
        List<String> outputNames = new ArrayList<>();
        outputNames.add("winston");

        net.setInput(sourceMatBlob);
        net.forward(outputs, outputNames);

        Mat scores = outputs.get(0).reshape(1, sourceMat.height() / 4);
        Mat geometry = outputs.get(1).reshape(1, 5 * sourceMat.height() / 4);

        List<Float> confidencesList = new ArrayList<>();
        List<Rect> boxesList = decode(scores, geometry, confidencesList, scoreThresh);
    }

    private static List<Rect> decode(Mat scores, Mat geometry, List<Float> confidences, float scoreThresh) {
        // size of 1 geometry plane
        int W = geometry.cols();
        int H = geometry.rows() / 5;
        //System.out.println(geometry);
        //System.out.println(scores);

        List<Rect> detections = new ArrayList<>();
        for (int y = 0; y < H; ++y) {
            Mat scoresData = scores.row(y);
            Mat x0Data = geometry.submat(0, H, 0, W).row(y);
            Mat x1Data = geometry.submat(H, 2 * H, 0, W).row(y);
            Mat x2Data = geometry.submat(2 * H, 3 * H, 0, W).row(y);
            Mat x3Data = geometry.submat(3 * H, 4 * H, 0, W).row(y);
            Mat anglesData = geometry.submat(4 * H, 5 * H, 0, W).row(y);

            for (int x = 0; x < W; ++x) {
                double score = scoresData.get(0, x)[0];
                if (score >= scoreThresh) {
                    double offsetX = x * 4.0;
                    double offsetY = y * 4.0;
                    double angle = anglesData.get(0, x)[0];
                    double cosA = Math.cos(angle);
                    double sinA = Math.sin(angle);
                    double x0 = x0Data.get(0, x)[0];
                    double x1 = x1Data.get(0, x)[0];
                    double x2 = x2Data.get(0, x)[0];
                    double x3 = x3Data.get(0, x)[0];
                    double h = x0 + x2;
                    double w = x1 + x3;
                    Point offset = new Point(offsetX + cosA * x1 + sinA * x2, offsetY - sinA * x1 + cosA * x2);
                    Point p1 = new Point(-1 * sinA * h + offset.x, -1 * cosA * h + offset.y);
                    Point p3 = new Point(-1 * cosA * w + offset.x,      sinA * w + offset.y); // original trouble here !
                    Rect r = new Rect(new Point(0.5 * (p1.x + p3.x), 0.5 * (p1.y + p3.y)), new Size(w, h));
                    detections.add(r);
                    confidences.add((float) score);
                }
            }
        }
        return detections;
    }
}
