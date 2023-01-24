package com.owgamedetector.opencvtemplatematch;

import static org.opencv.core.CvType.*;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.ImageReader;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.camera.core.ImageProxy;

import com.facebook.react.bridge.ReactApplicationContext;
import com.mrousavy.camera.frameprocessor.FrameProcessorPlugin;
import com.owgamedetector.R;
import com.visioncamerabase64.BitmapUtils;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.engine.OpenCVEngineInterface;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.osgi.OpenCVInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OpenCvTemplateMatchPlugin extends FrameProcessorPlugin {

    public static final String DORADO_B64 = "";

    Mat imageMat, templateMat, resultMat = new Mat();
    int match_method = Imgproc.TM_CCORR_NORMED;

    ReactApplicationContext reactContext;

    public OpenCvTemplateMatchPlugin(ReactApplicationContext reactContext) {
        super("openCvTemplateMatch");
        this.reactContext = reactContext;
    }

    @Override
    public Object callback(ImageProxy imageProxy, Object[] params) {
        imageMat = imageProxyToMat(imageProxy);

        BitmapFactory.Options bitmapOpts = new BitmapFactory.Options();
        bitmapOpts.outHeight = 500;
        bitmapOpts.outWidth = 313;

        templateMat = bitmapToMat(BitmapFactory.decodeResource(reactContext.getResources(), R.drawable.dorado_small, bitmapOpts));

        try {
            templateMat = Utils.loadResource(reactContext, R.drawable.dorado_small);
        } catch (IOException e) {
            e.printStackTrace();
        }


        int imageCols = imageMat.cols();
        int imageRows = imageMat.rows();

        int templateCols = templateMat.cols();
        int templateRows = templateMat.rows();

        int resultCols = imageMat.cols() - templateMat.cols() + 1;
        int resultRows = imageMat.rows() - templateMat.rows() + 1;

        Log.d("OpenCV", "Type? " + imageMat.type());
        Log.d("OpenCV", "Type? " + templateMat.type());
        Log.d("OpenCV", "Type? " + resultMat.type());

        Log.d("OpenCV", "Empty? " + imageMat.empty());
        Log.d("OpenCV", "Empty? " + templateMat.empty());
        Log.d("OpenCV", "Empty? " + resultMat.empty());

        Log.d("OpenCV", "Image Cols: " + imageCols);
        Log.d("OpenCV", "Image Rows: " + imageRows);
        Log.d("OpenCV", "Template Cols: " + templateCols);
        Log.d("OpenCV", "Template Rows: " + templateRows);
        Log.d("OpenCV", "Result Cols: " + resultCols);
        Log.d("OpenCV", "Result Rows: " + resultRows);

        resultMat.create(resultRows, resultCols, CV_8U);

        Log.d("OpenCV", "Empty? " + resultMat.empty());

        return performTemplateMatch(imageMat, templateMat, resultMat, match_method);
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
        Utils.bitmapToMat(imageAsBitmap, imageAsMat);
        return imageAsMat;
    }

    public double performTemplateMatch(Mat image, Mat templateImage, Mat resultImage, int match_method) {
        Imgproc.matchTemplate(image, templateImage, resultImage, match_method);
        Core.normalize(resultImage, resultImage, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        double result;
        Core.MinMaxLocResult mmr = Core.minMaxLoc(resultImage);
        result = match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED ? mmr.minVal : mmr.maxVal;
        Log.d("", String.valueOf(result));
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
}