package jack.low.ARview;


import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;

public class ARviewActivity extends Activity {

	/**     2012/05/04 ver1.10   cameraARview             */
	CameraPreview camerapreview;
	SurfaceView bitmap;
	CustomDrawView customDrawView;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);

        requestWindowFeature(Window.FEATURE_NO_TITLE); 	// タイトルバー消去


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);	//スリープにしない




      //title消去
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

      //スリープにしない
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        camerapreview = new CameraPreview(this);
        setContentView(camerapreview);					//カメラプレビューインスタンス





        addContentView(new CustomDrawView(this), new LayoutParams(LayoutParams.FILL_PARENT,		//描画
        																		LayoutParams.FILL_PARENT));




    }


}





