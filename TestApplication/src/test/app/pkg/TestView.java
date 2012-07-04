package test.app.pkg;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.SurfaceHolder;

import com.googlecode.tesseract.android.TessBaseAPI;

class TestView extends CvViewBase {
	private Mat mRgba;
	private Mat mRgba2;
    private Mat mOriginal;
    private Mat mSmoothKD;
    private Mat mSmoothKE;
    private Mat mCircles;
    private int j = 1;
    private String oldSpeed;
    private boolean isAquired;
    
    private TessBaseAPI baseApi;
	
    public TestView(Context context) {
        super(context);
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        synchronized (this) {
            // initialize Mats before usage
            mRgba = new Mat();
            mRgba2 = new Mat();
            mOriginal = new Mat();
            mSmoothKD = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(2,2), new Point(1,1));
            mSmoothKE = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(2,2), new Point(1,1));
            mCircles = new Mat();
            oldSpeed = "searching...";
            isAquired = false;
        }
        super.surfaceCreated(holder);
    }
    
    @Override
    protected Bitmap processFrame(VideoCapture capture) {
    	switch (TestApplicationActivity.viewMode) {
    	
        case TestApplicationActivity.VIEW_MODE_RGBA:
            capture.retrieve(mOriginal, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
            isAquired = false;
            oldSpeed = "searching...";
            break;
            
        case TestApplicationActivity.VIEW_MODE_TEST:
        	
        	//0. Red Thresholding
        	capture.retrieve(mOriginal, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA); 
        	Imgproc.GaussianBlur(mOriginal, mRgba2, new Size(5,5), 2, 2);
        	Imgproc.cvtColor(mRgba2, mRgba, Imgproc.COLOR_RGB2HSV,3);
        	Core.inRange(mRgba, new Scalar(140, 50, 50, 0), new Scalar(180, 255, 255, 0), mRgba2);   
        	
        	//0b. Inverting matrix values
        	Core.bitwise_not(mRgba2, mRgba);
        	
        	//1. Closing smoothing
        	Imgproc.dilate(mRgba, mRgba2, mSmoothKD);
        	Imgproc.erode(mRgba2, mRgba, mSmoothKD);
        	//2. Opening smoothing
        	Imgproc.erode(mRgba, mRgba2, mSmoothKE);
        	Imgproc.dilate(mRgba2, mRgba, mSmoothKE);
        	        	
        	//3. Circle detection
        	Imgproc.cvtColor(mRgba, mRgba2, Imgproc.COLOR_GRAY2RGBA, 0);
        	Imgproc.HoughCircles(mRgba, mCircles, Imgproc.CV_HOUGH_GRADIENT, 3.0, mOriginal.rows()/6, 200, 120, 0, 0);
        	if (mCircles.cols() > 0)
        	{
        		Mat mTest = new Mat(mOriginal.rows(), mOriginal.cols(), CvType.CV_8UC1);
        		Imgproc.GaussianBlur(mOriginal, mRgba2, new Size(11,11), 0);
        		Imgproc.cvtColor(mRgba2, mTest, Imgproc.COLOR_RGB2GRAY, CvType.CV_8UC1);
        		Imgproc.threshold(mTest, mTest, 100, 255, Imgproc.THRESH_BINARY_INV);
        		Core.bitwise_not(mTest, mTest);
        		Imgproc.dilate(mTest, mTest, mSmoothKD);
            	Imgproc.erode(mTest, mTest, mSmoothKD);
            	Imgproc.erode(mTest, mTest, mSmoothKE);
            	Imgproc.dilate(mTest, mTest, mSmoothKE);
            	Imgproc.cvtColor(mTest, mRgba2, Imgproc.COLOR_GRAY2RGBA,4);
            	mTest.release();
        		
        		Bitmap ocrOriginal = Bitmap.createBitmap(mOriginal.cols(), mOriginal.rows(), Bitmap.Config.ARGB_8888);
    			Utils.matToBitmap(mRgba2, ocrOriginal);
    			
        		for (int i = 0; i < mCircles.cols(); i++){
        			double vCircle[] = mCircles.get(0, i);
        			if (vCircle == null)
        				break;
        			
        			Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
        			int radius = (int)Math.round(vCircle[2]);
        			
        			int dim = 2 * radius; 
        			String recognizedText = oldSpeed;
        			if (((pt.x - radius) > 0) && ((pt.y - radius) > 0) && ((pt.x + radius) < mOriginal.height()) &&
        					((pt.y + radius) < mOriginal.width())){
        				if (j != 0){
        					j--;
        				}
        				else if (isAquired == false){
        					//3a. OCR
        					baseApi = new TessBaseAPI();
        					baseApi.init(TestApplicationActivity.DATA_PATH, TestApplicationActivity.lang);
        					Bitmap ocrPart = Bitmap.createBitmap(ocrOriginal, (int)pt.x-radius, (int)pt.y-radius, dim, dim);
        					baseApi.setImage(ocrPart);
        					ocrPart.recycle();
    	      				recognizedText = baseApi.getUTF8Text();
    	      				baseApi.end();
    	      				j=1;
        				}
        			
        				recognizedText = recognizedText.replaceAll("[^0-9]", "");
        			
        				if (recognizedText == ""){
        					recognizedText = oldSpeed;
        				}
        			
        				for (String valid : TestApplicationActivity.ValidSpeeds)
        					if (valid.equalsIgnoreCase(recognizedText) == true){
        						isAquired = true;
        						oldSpeed = recognizedText;
        						break;
        					}
        			
        			
        				// draw circle characteristics + speed
        				Core.circle(mOriginal, pt, radius, new Scalar(255, 0 , 0, 255), 2, 8, 0);
        				Core.circle(mOriginal, pt, 2, new Scalar(0, 0 , 255, 255), 2, 8, 0);
        				Core.rectangle(mOriginal, new Point(pt.x-radius, pt.y-radius), new Point(pt.x+radius, pt.y+radius), 
        						new Scalar(0, 255, 0, 255), 2);
        				Core.putText(mOriginal, recognizedText, pt, 3, 1, new Scalar(255, 20, 147, 255), 2);
        			}
        		}
    			ocrOriginal.recycle();
        	}

			break;
			
        case TestApplicationActivity.VIEW_MODE_CHAR:
        	capture.retrieve(mOriginal, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        	Mat mTest = new Mat(mOriginal.rows(), mOriginal.cols(), CvType.CV_8UC1);
        	Imgproc.GaussianBlur(mOriginal, mRgba, new Size(11,11), 0);
        	Imgproc.cvtColor(mRgba, mTest, Imgproc.COLOR_RGB2GRAY, CvType.CV_8UC1);
    //    	Imgproc.adaptiveThreshold(mTest, mTest, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, 5);
        	Imgproc.threshold(mTest, mTest, 100, 255, Imgproc.THRESH_BINARY_INV);
    		Core.bitwise_not(mTest, mTest);
    		Imgproc.dilate(mTest, mTest, mSmoothKD);
        	Imgproc.erode(mTest, mTest, mSmoothKD);
        	Imgproc.erode(mTest, mTest, mSmoothKE);
        	Imgproc.dilate(mTest, mTest, mSmoothKE);
        	Imgproc.cvtColor(mTest, mOriginal, Imgproc.COLOR_GRAY2RGBA,4);
        	mTest.release();
        	break;
        	
        case TestApplicationActivity.VIEW_MODE_SEGM:
            capture.retrieve(mOriginal, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
            Imgproc.GaussianBlur(mOriginal, mRgba2, new Size(5,5), 2, 2);
        	Imgproc.cvtColor(mRgba2, mRgba, Imgproc.COLOR_RGB2HSV,3);
        	Core.inRange(mRgba, new Scalar(140, 50, 50, 0), new Scalar(180, 255, 255, 0), mRgba2);   
        	Core.bitwise_not(mRgba2, mRgba);
        	Imgproc.dilate(mRgba, mRgba2, mSmoothKD);
        	Imgproc.erode(mRgba2, mRgba, mSmoothKD);
        	Imgproc.erode(mRgba, mRgba2, mSmoothKE);
        	Imgproc.dilate(mRgba2, mOriginal, mSmoothKE);
            break;
        }
    	
        Bitmap bmp = Bitmap.createBitmap(mOriginal.cols(), mOriginal.rows(), Bitmap.Config.ARGB_8888);

        try {
        	Utils.matToBitmap(mOriginal, bmp);
            return bmp;
        } catch(Exception e) {
        	Log.e("test.app.pkg", "Utils.matToBitmap() throws an exception: " + e.getMessage());
            bmp.recycle();
            return null;
        }
    }
    
    @Override
    public void run() {
        super.run();

        synchronized (this) {
            // Explicitly deallocate Mats
            if (mRgba != null)
                mRgba.release();
            if (mRgba2 != null)
                mRgba2.release();
            if (mOriginal != null)
                mOriginal.release();
            if (mCircles != null)
                mCircles.release();
            if (mSmoothKD != null)
                mSmoothKD.release();
            if (mSmoothKE != null)
                mSmoothKE.release();
            mSmoothKD = null;
            mSmoothKE = null;
            mRgba = null;
            mRgba2 = null;
            mOriginal = null;
            mCircles= null;
        }
    }

}
