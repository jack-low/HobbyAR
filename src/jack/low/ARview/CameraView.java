package jack.low.ARview;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;



class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

	/**     2012/05/04 ver1.10   cameraARview             */


	//
	SurfaceHolder surfaceHolder;



	Camera camera;



	public CameraPreview(Context context) {
		super(context);
		// コールバック
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}




	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO 自動生成されたメソッド・スタブ

		Camera.Parameters parameters = camera.getParameters();
		//parameters.setPreviewSize(width, height); And2.1以上では使用不可
		camera.setParameters(parameters);

		//
		camera.startPreview();

	}


	public void surfaceCreated(SurfaceHolder holder) {
		// TODO 自動生成されたメソッド・スタブ

		camera = Camera.open();

		try{
		camera.setPreviewDisplay(surfaceHolder);
		}catch (IOException exception) {
			camera.release();
			camera = null;
		}

	}





	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO 自動生成されたメソッド・スタブ

		camera.stopPreview();
		camera.release();
		camera = null;

	}




}