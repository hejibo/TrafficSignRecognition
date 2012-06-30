package test.app.pkg;

import org.opencv.android.Utils;
import org.opencv.core.Core;
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

class TestView extends CvViewBase {
	private Mat mRgba;
	private Mat mRgba2;
    private Mat mIntermediateMat;
    private Mat mIntermediateMat2;
    private Mat mKernel;
    private Mat mCircles;

	
    public TestView(Context context) {
        super(context);
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        synchronized (this) {
            // initialize Mats before usage
            mRgba = new Mat();
            mRgba2 = new Mat();
            mIntermediateMat = new Mat();
            mIntermediateMat2 = new Mat();
            mKernel = new Mat();

        }
        super.surfaceCreated(holder);
    }
    
    @Override
    protected Bitmap processFrame(VideoCapture capture) {
    	switch (TestApplicationActivity.viewMode) {
        case TestApplicationActivity.VIEW_MODE_RGBA:
            capture.retrieve(mRgba2, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
            break;
        case TestApplicationActivity.VIEW_MODE_TEST:
        	
        	//0. Red Thresholding
        	capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA); 
        	Imgproc.GaussianBlur(mRgba, mRgba2, new Size(11,11), 2, 2);
        	Imgproc.cvtColor(mRgba2, mRgba, Imgproc.COLOR_RGB2HSV,3);
        	Core.inRange(mRgba, new Scalar(140, 75, 75, 0), new Scalar(180, 255, 255, 0), mRgba2);  
        	Core.bitwise_not(mRgba2, mRgba);
        	
        	//1. Closing smoothing
        	mKernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(2,2), new Point(1,1));
        	Imgproc.dilate(mRgba, mRgba2, mKernel);
        	Imgproc.erode(mRgba2, mRgba, mKernel);
        	//2. Opening smoothing
        	mKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(2,2), new Point(1,1));
        	Imgproc.erode(mRgba, mRgba2, mKernel);
        	Imgproc.dilate(mRgba2, mRgba, mKernel);
        	
        	//3. Circle detection
        	mCircles = new Mat();
        	Imgproc.cvtColor(mRgba, mRgba2, Imgproc.COLOR_GRAY2RGBA, 0);
        	// from now on mRgba will be gray, while mRgba2 will be in RGBA mode
        	Imgproc.HoughCircles(mRgba, mCircles, Imgproc.CV_HOUGH_GRADIENT, 3.0, mRgba2.rows()/4, 200, 100, 0, 0);
        	if (mCircles.cols() > 0)
        	{
        		for (int i = 0; i < mCircles.cols(); i++)
        		{
        			double vCircle[] = mCircles.get(0, i);
        			if (vCircle == null)
        				break;
        			
        			Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
        			int radius = (int)Math.round(vCircle[2]);
        			
        			
        			
        			// draw circle characteristics
        			Core.circle(mRgba2, pt, radius, new Scalar(255, 0 , 0, 255), 2, 8, 0);
        			Core.circle(mRgba2, pt, 2, new Scalar(0, 0 , 255, 255), 2, 8, 0);
        			Core.rectangle(mRgba2, new Point(pt.x-radius, pt.y-radius), new Point(pt.x+radius, pt.y+radius), new Scalar(0, 255, 0, 255), 2);
        		}
        	}
        	
        	//3a. Circle detection alternative
        	
        	
        	
/*        	capture.retrieve(mIntermediateMat, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        	Imgproc.cvtColor(mIntermediateMat, mIntermediateMat2, Imgproc.COLOR_RGB2YCrCb, 4);
        // -> In OpenCV ordinea este BGR
        	for (int i = 0; i < mIntermediateMat2.height(); i++)
        		for (int j = 0; j < mIntermediateMat2.width(); j++)
        		{	
        			if ((mIntermediateMat2.get(i,j)[2] >= 0.37*255) && (mIntermediateMat2.get(i,j)[2] <= 0.52*255))
        			{
        				if ((mIntermediateMat2.get(i,j)[1] >= 0.54*255) && (mIntermediateMat2.get(i,j)[1] <= 0.75*255))
        				{
        					mRgba.put(i, j, new double[]{0,0,0,255});
        				}
        				else
        				{
        					mRgba.put(i, j, new double[]{255,255,255,255});
        				}
        			
        			}
        		}
        // 	*/
			break;
        }
    	
    	
        Bitmap bmp = Bitmap.createBitmap(mRgba2.cols(), mRgba2.rows(), Bitmap.Config.ARGB_8888);

        try {
        	Utils.matToBitmap(mRgba2, bmp);
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
            if (mIntermediateMat != null)
                mIntermediateMat.release();
            if (mIntermediateMat2 != null)
                mIntermediateMat2.release();
            if (mKernel != null)
                mKernel.release();
            if (mCircles != null)
                mCircles.release();
            mRgba = null;
            mRgba2 = null;
            mIntermediateMat = null;
            mIntermediateMat2 = null;
            mKernel = null;
            mCircles = null;
        }
    }

}
