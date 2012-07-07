package jack.low.ARview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

public class CustomDrawView extends View implements SurfaceHolder.Callback{

	/**     2012/05/04 ver1.10   cameraARview             */

	//private Bitmap image, image2, image3;
	//private int getX = 100, getY = 100;
	//private int i = 0;
	//private String test = "test";
	private Matrix Matrix = new Matrix();
	//private  int getx, gety = 0;
	//private boolean touchDown;
	//private Object thread = null;
	//private int init = 0;
	private boolean sw = false;
	private Bitmap[]      bmp=new Bitmap[4];
	private int[][] box= new int[2][2];

	// 移動、回転、ズーム用の変換行列
    private Matrix matrix      = new Matrix();

    //ズーム関連
    private float oldDist      = 0f;
    private PointF mid         = new PointF();
    private float curRatio     = 1f;

    private PointF previous =new PointF();
    private PointF speed = new PointF();
    //回転に関するもの
    private Line previousLine;

    //モード判別（NONE: 未操作状態, ONE_POINT: ドラッグ中, TWO_POINT: 拡大縮小、回転中）
    enum Mode{
        NONE,
        ONE_POINT,
        TWO_POINT
    }
    private Mode mode = Mode.NONE;
	//private String wm = null;
	private int getX, getViewX = 0;
	private int getY, getViewY = 0;

	//
	int action;




	public CustomDrawView(Context context) {
		super(context);
		setFocusable(true);
		// TODO 自動生成されたコンストラクター・スタブ


		//Resources r = context.getResources();
		//ビットマップの読み込み
        for (int i=1;i<4;i++) bmp[i]=readBitmap(context,"test"+i);




	}






	public CustomDrawView(Context context, AttributeSet attrs, int defStyle) {
		super(context);
		// TODO 自動生成されたコンストラクター・スタブ
	}





	//タッチ時に呼ばれる
    public boolean onTouchEvent(MotionEvent event) {





        switch(event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            //現在の慣性をリセット
            speed.set(0, 0);
            //移動（ズームは無し）開始
            previous.set(event.getX(), event.getY());
            mode = Mode.ONE_POINT;
            break;
        case MotionEvent.ACTION_POINTER_DOWN:
            //移動・回転・ズーム開始
            previous.set(event.getX(), event.getY());
            oldDist = spacing(event);

            // Android のポジション誤検知を無視
            if (oldDist > 10f) {
                midPoint(previous, event);
                mode = Mode.TWO_POINT;

                previousLine = new Line(
                 new PointF(event.getX(0), event.getY(0)),
                 new PointF(event.getX(1), event.getY(1))
               );
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            mode = Mode.NONE;
            break;
        case MotionEvent.ACTION_MOVE:
            PointF current = null;

            if (mode == Mode.ONE_POINT) {
                current = new PointF(event.getX(), event.getY());
            }else if (mode == Mode.TWO_POINT) {
                current = new PointF();
                midPoint(current, event);
            }else{
                return false;
            }

            //移動処理
            float distanceX = current.x - previous.x;
            float distanceY = current.y - previous.y;
            matrix.postTranslate(distanceX, distanceY);

            if (mode == Mode.TWO_POINT) {
                //ズーム処理
                float newDist = spacing(event);
                midPoint(mid, event);
                float scale = newDist / oldDist;
                float tempRatio = curRatio * scale;
                oldDist = newDist;

                //倍率が上限値下限値の範囲外なら補正する
                curRatio = Math.min(Math.max(0.1f, curRatio), 20f);

                if (0.1f < tempRatio && tempRatio < 20f) {
                    curRatio = tempRatio;
                    matrix.postScale(scale, scale, mid.x, mid.y);
                }

                //回転処理
                Line line = new Line(
                        new PointF(event.getX(0), event.getY(0)),
                        new PointF(event.getX(1), event.getY(1))
                );

                float angle = (float) (previousLine.getAngle(line) * 180 / Math.PI);
                matrix.postRotate(angle, current.x, current.y);

                previousLine = line;
            }


            //次回の準備
            speed = new PointF(current.x - previous.x, current.y - previous.y);
            previous.set(current.x, current.y);
        }


            getX = (int) event.getX();
            getY = (int) event.getY();

            action = event.getActionIndex();






        Matrix = matrix;
     // 再描画の指示
        invalidate();

        return true; // イベントがハンドリングされたことを示す
    }






	/**
     * 2点間の距離を計算
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /**
     * 2点間の中間点を計算
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    static class Line{



        enum LineType{
            /** 第1引数の点と第2引数の点を通る直線（終端はない） */
            STRAIGHT,
            /** 第1引数の点から第2引数のほうに伸びる半直線 */
            HALF,
            /** 第1引数の点と第2引数の点の間の線分 */
            SEGMENT
        }



        public PointF p1;
        public PointF p2;
        public LineType type;

        /**
         * @param p1
         * @param p2
         */
        public Line(PointF p1, PointF p2){
            this(p1, p2, null);
        }

        /**
         * @param p1
         * @param p2
         * @param type
         */
        public Line(PointF p1, PointF p2, LineType type){
            this.p1 = p1;
            this.p2 = p2;
            this.type = (type == null) ? LineType.STRAIGHT : type;
        }

        /**
         * 2つのLineインスタンスの交点を表わすPointインスタンスを取得する
         * 交点がない場合はnullを返す
         * @param line
         * @return
         *
         */
        public PointF getIntersectionPoint(Line line){
            PointF vector1 = this.getVector();
            PointF vector2 = line.getVector();

            if(cross(vector1, vector2) == 0.0){
                //2直線が並行の場合はnullを返す
                return null;
            }

            // 交点を this.p1 + s * vector1 としたとき
            float s = cross(vector2, subtract(line.p1, this.p1)) / cross(vector2, vector1);
            // 交点を line.p1 + t * vector2 としたとき
            float t = cross(vector1, subtract(this.p1, line.p1)) / cross(vector1, vector2);

            if(this.validateIntersect(s) && line.validateIntersect(t)){
                vector1.x *= s;
                vector1.y *= s;
                this.p1.set(p1.x + vector1.x, p1.y + vector1.y);
                return p1;
            }else{
                return null;
            }
        }

        /**
         * 2つのLineインスタンスが作る角度のラジアン値を返す
         * @param line
         * @return
         */
        public float getAngle(Line line){
            PointF vector1 = this.getVector();
            PointF vector2 = line.getVector();

            return (float)Math.atan2(vector1.x * vector2.y - vector1.y * vector2.x, vector1.x * vector2.x + vector1.y * vector2.y);
        }

        public PointF getVector(){
            return new PointF(p2.x - p1.x, p2.y - p1.y);
        }

        public PointF subtract(PointF p1, PointF p2){
            return new PointF(p1.x - p2.x, p1.y - p2.y);
        }

        /**
         * 交点までのベクトルを p1 + n * (p2 - p1) であらわしたとき、
         * nが適切な値の範囲内かどうかを判定する。
         *
         * 直線の場合：nはどの値でもよい
         * 半直線の場合：nは0以上である必要がある
         * 線分の場合：nは0以上1以下である必要がある
         * @param n
         * @return
         *
         */
        private boolean validateIntersect(float n){
            if(LineType.HALF.equals(this.type)){
                return (0 <= n);
            }else if(LineType.SEGMENT.equals(this.type)){
                return ((0 <= n) && (n <= 1));
            }else{
                return true;
            }
        }

        /**
         * 2つの2次元ベクトルの外積を返す
         * @param vector1 2次ベクトルを表わすPointインスタンス
         * @param vector2 2次ベクトルを表わすPointインスタンス
         * @return
         *
         */
        private float cross(PointF vector1, PointF vector2){
            return (vector1.x * vector2.y - vector1.y * vector2.x);
        }

        public String toString(){
            String str = "";
            if(LineType.STRAIGHT.equals(type)){
                str += "---> ";
            }
            str += "(" + p1.x + ", " + p1.y + ") ---> (" + p2.x + ", " + p2.y + ")";
            if(LineType.STRAIGHT.equals(type) || LineType.HALF.equals(type)){
                str += " --->";
            }


            return str;
        }
    }











    	 /*String action = "";



         try{ switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
              action = "ACTION_DOWN";
              break;
          case MotionEvent.ACTION_UP:
              action = "ACTION_UP";
              break;
          case MotionEvent.ACTION_MOVE:
              action = "ACTION_MOVE";
              break;
          case MotionEvent.ACTION_CANCEL:
              action = "ACTION_CANCEL";
              break;
          }}catch (Exception e1) {

          	ercd1 = String.valueOf(e1);

          	Log.v("TouchMotion",ercd1+"エラーが発生しました。(0x0101)");
  		}



          Log.v("MotionEvent",
                  "action = " + action + ", " +
                  "x = " + String.valueOf(event.getX()) + ", " +
                  "y = " + String.valueOf(event.getY()));



          try{
        //触る
          if(event.getAction() == MotionEvent.ACTION_DOWN){
              getX = (int) event.getX();
              getY = (int) event.getY();
          }

          //触ったままスライド
          else if(event.getAction() == MotionEvent.ACTION_MOVE){
              getX = (int) event.getX();
              getY = (int) event.getY();
          }
          //離す
          else if(event.getAction() == MotionEvent.ACTION_UP){
              getX = (int) event.getX();
              getY = (int) event.getY();
          }

          // 再描画の指示
          invalidate();
          }catch (Exception e) {
  			// TODO: handle exception
          	  ercd1 = String.valueOf(e);
          	  Log.v("TouchMotion",ercd1+"エラーが発生しました。(0x0021)");

  		}

          return true;

    }//*/


	public void onDraw(Canvas canvas){

		super.onDraw(canvas);
		box[0][0] = 0;			box[1][0] = 150;
		box[0][1] = 150;		box[1][1] = 0;


		//int cx = canvas.getWidth()/2;
		//int cy = canvas.getHeight()/2;






		canvas.drawColor(Color.TRANSPARENT);


		Paint paint = new Paint();
      	paint.setAntiAlias(true);

		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.BLACK);
		boolean T = false;






		//image



			if(T == false ){
			if(sw == false){

				canvas.drawBitmap(bmp[1],  Matrix, null);
				canvas.drawBitmap(bmp[2], 0, 0, null);
				getViewX = bmp[2].getWidth();
				getViewY = bmp[2].getHeight();

				T = true;
				sw = false;



			}
			else if(action != getViewX){
				 //

				canvas.drawBitmap(bmp[3],  0, 0, null);

				T = false;
				sw = false;

			}


			}else {
				T = false;
				sw = true;

			}








		return;



	}
	private static Bitmap readBitmap(Context context,String name) {
        int resID=context.getResources().getIdentifier(
            name,"drawable",context.getPackageName());
        return BitmapFactory.decodeResource(
            context.getResources(),resID);
    }






	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO 自動生成されたメソッド・スタブ

	}






	public void surfaceCreated(SurfaceHolder holder) {
		// TODO 自動生成されたメソッド・スタブ

	}






	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO 自動生成されたメソッド・スタブ

	}




}