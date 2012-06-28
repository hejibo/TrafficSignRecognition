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
    private Mat Kernel;
	
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
            Kernel = new Mat();
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
        	capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_BGRA);
        	Imgproc.blur(mRgba, mRgba, new Size(15,15));
        	Imgproc.cvtColor(mRgba, mIntermediateMat, Imgproc.COLOR_BGR2HSV,3);
        	Core.inRange(mIntermediateMat, new Scalar(160, 100, 100, 0), new Scalar(180, 255, 255, 0), mIntermediateMat2);
        	Imgproc.cvtColor(mIntermediateMat2, mIntermediateMat, Imgproc.COLOR_GRAY2BGR, 0);
        	mIntermediateMat2.release();
        	Imgproc.cvtColor(mIntermediateMat, mRgba2, Imgproc.COLOR_BGR2RGBA, 0);
        	//Kernel = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(3,3), new Point(1,1));
        	//Imgproc.dilate(mRgba2, mRgba2, Kernel);
/*        	capture.retrieve(mIntermediateMat, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        	Imgproc.cvtColor(mIntermediateMat, mIntermediateMat2, Imgproc.COLOR_RGB2YCrCb, 4);
        // -> de aici incepem conversia. In OpenCV ordinea este BGR
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
        // -> aici terminam conversia 	*/
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
            mRgba = null;
            mRgba2 = null;
            mIntermediateMat = null;
            mIntermediateMat2 = null;
        }
    }

}
