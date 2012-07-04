package test.app.pkg;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class TestApplicationActivity extends Activity {
	private static final String TAG = "Test::Activity";
	
	public static final String DATA_PATH = Environment
			.getExternalStorageDirectory().toString() + "/TestApplication/";
	public static final String lang = "eng";
	
	public static final String ValidSpeeds[] = {"10", "20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120", "130"};
	
	public static final int 	VIEW_MODE_RGBA = 0;
	public static final int 	VIEW_MODE_TEST = 1;
	public static final int 	VIEW_MODE_CHAR = 2;
	public static final int 	VIEW_MODE_SEGM = 3;
	
	private MenuItem 			mItemPreviewRGBA;
	private MenuItem			mItemPreviewTest;
	private MenuItem			mItemPreviewCharThr;
	private MenuItem			mItemPreviewSegmentation;
	
	public static int 			viewMode = VIEW_MODE_RGBA;
	
	private TestView 			mView;
	
	public TestApplicationActivity()
	{
		Log.i(TAG, "Instantiated new " + this.getClass());
	}
	
	@Override 
	protected void onPause()
	{
		Log.i(TAG, "onPause");
		super.onPause();
		mView.releaseCamera();
	}
	
	@Override
	protected void onResume()
	{
		Log.i(TAG, "onResume");
		super.onResume();
		if(!mView.openCamera())
		{
			AlertDialog ad = new AlertDialog.Builder(this).create();  
			ad.setCancelable(false); // This blocks the 'BACK' button  
			ad.setMessage("Fatal error: can't open camera!");  
			ad.setButton("OK", new DialogInterface.OnClickListener() {  
			    public void onClick(DialogInterface dialog, int which) {  
			        dialog.dismiss();                      
					finish();
			    }  
			});  
			ad.show();
		}
	}
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mView = new TestView(this);
        setContentView(mView);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");
        mItemPreviewRGBA = menu.add("Normal");
        mItemPreviewSegmentation = menu.add("Segmentation");
        mItemPreviewCharThr = menu.add("Characters");
        mItemPreviewTest = menu.add("Recognition");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Menu Item selected " + item);
        if (item == mItemPreviewRGBA)
            viewMode = VIEW_MODE_RGBA;
        else if (item == mItemPreviewTest)
            viewMode = VIEW_MODE_TEST;
        else if (item == mItemPreviewCharThr)
        	viewMode = VIEW_MODE_CHAR;
        else if (item == mItemPreviewSegmentation)
        	viewMode = VIEW_MODE_SEGM;
        return true;
    }
    
    @Override
    protected void onDestroy(){
    	System.runFinalizersOnExit(true);
    }
}