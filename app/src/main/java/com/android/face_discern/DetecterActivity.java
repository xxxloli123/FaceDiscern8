package com.android.face_discern;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.face_discern.GPIO.FileUtils2;
import com.android.face_discern.GPIO.GPIOControl;
import com.android.face_discern.adapter.CaptureImageAdapter;
import com.android.face_discern.adapter.ContrastAdapter;
import com.android.face_discern.model.Contrast;
import com.android.face_discern.model.ContrastHelp;
import com.android.face_discern.util.BitmapFileSetting;
import com.android.face_discern.util.RxFileTool;
import com.android.face_discern.util.RxImageTool;
import com.android.face_discern.util.ToastUtil;
import com.arcsoft.ageestimation.ASAE_FSDKAge;
import com.arcsoft.ageestimation.ASAE_FSDKEngine;
import com.arcsoft.ageestimation.ASAE_FSDKError;
import com.arcsoft.ageestimation.ASAE_FSDKFace;
import com.arcsoft.ageestimation.ASAE_FSDKVersion;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKError;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.arcsoft.facetracking.AFT_FSDKVersion;
import com.arcsoft.genderestimation.ASGE_FSDKEngine;
import com.arcsoft.genderestimation.ASGE_FSDKError;
import com.arcsoft.genderestimation.ASGE_FSDKFace;
import com.arcsoft.genderestimation.ASGE_FSDKGender;
import com.arcsoft.genderestimation.ASGE_FSDKVersion;
import com.blankj.utilcode.util.FileUtils;
import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.java.ExtByteArrayOutputStream;
import com.guo.android_extend.tools.CameraHelper;
import com.guo.android_extend.widget.CameraFrameData;
import com.guo.android_extend.widget.CameraGLSurfaceView;
import com.guo.android_extend.widget.CameraSurfaceView;
import com.guo.android_extend.widget.CameraSurfaceView.OnCameraListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * Created by gqj3375 on 2017/4/28.
 */

public class DetecterActivity extends Activity implements OnCameraListener, View.OnTouchListener
		, Camera.AutoFocusCallback, View.OnClickListener {
	private final String TAG = this.getClass().getSimpleName();

	private int mWidth, mHeight, mFormat;
	private CameraSurfaceView mSurfaceView;
	private CameraGLSurfaceView mGLSurfaceView;
	private Camera mCamera;

	private RecyclerView rvListCapture;
	private CaptureImageAdapter captureImageAdapter;
	private List<File> files;

	private List<Contrast> contrasts;
	private RecyclerView rvListContrast;
	private ContrastAdapter contrastAdapter;

	AFT_FSDKVersion version = new AFT_FSDKVersion();
	AFT_FSDKEngine engine = new AFT_FSDKEngine();
	ASAE_FSDKVersion mAgeVersion = new ASAE_FSDKVersion();
	ASAE_FSDKEngine mAgeEngine = new ASAE_FSDKEngine();
	ASGE_FSDKVersion mGenderVersion = new ASGE_FSDKVersion();
	ASGE_FSDKEngine mGenderEngine = new ASGE_FSDKEngine();
	List<AFT_FSDKFace> result = new ArrayList<>();
	List<ASAE_FSDKAge> ages = new ArrayList<>();
	List<ASGE_FSDKGender> genders = new ArrayList<>();

	int mCameraID;
	int mCameraRotate;
	boolean mCameraMirror;
	byte[] mImageNV21 = null;
	FRAbsLoop mFRAbsLoop = null;
	AFT_FSDKFace mAFT_FSDKFace = null;
	Handler mHandler;
	boolean isPostted = false;

	Runnable hide = new Runnable() {
		@Override
		public void run() {
			mTextView.setAlpha(0.5f);
			mImageView.setImageAlpha(128);
			isPostted = false;
		}
	};

	String captureImagePath=Environment.getExternalStorageDirectory().getPath()+"/233311/CaptureImage/"+
			new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
//	String CaptureImagePath=SimpleDateFormat.getDateInstance().format(System.currentTimeMillis());
    private String keyout;
	private String path="";
    private  Timer timer;

	class FRAbsLoop extends AbsLoop {
		boolean isOneSecond=true;

		AFR_FSDKVersion version = new AFR_FSDKVersion();
		AFR_FSDKEngine engine = new AFR_FSDKEngine();
		AFR_FSDKFace result = new AFR_FSDKFace();
		List<FaceDB.FaceRegist> mResgist = ((Application)DetecterActivity.this.getApplicationContext()).mFaceDB.mRegister;
		List<ASAE_FSDKFace> face1 = new ArrayList<>();
		List<ASGE_FSDKFace> face2 = new ArrayList<>();
		
		@Override
		public void setup() {
			AFR_FSDKError error = engine.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
			Log.d(TAG, "AFR_FSDK_InitialEngine = " + error.getCode());
			error = engine.AFR_FSDK_GetVersion(version);
			Log.d(TAG, "FR=" + version.toString() + "," + error.getCode()); //(210, 178 - 478, 446), degree = 1　780, 2208 - 1942, 3370
		}

		@Override
		public void loop() {
			if (mImageNV21 != null) {
				long time = System.currentTimeMillis();
				AFR_FSDKError error = engine.AFR_FSDK_ExtractFRFeature(mImageNV21, mWidth, mHeight, AFR_FSDKEngine.CP_PAF_NV21, mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree(), result);
				Log.d(TAG, "AFR_FSDK_ExtractFRFeature cost :" + (System.currentTimeMillis() - time) + "ms");
				Log.d(TAG, "Face=" + result.getFeatureData()[0] + "," + result.getFeatureData()[1] + "," + result.getFeatureData()[2] + "," + error.getCode());
				AFR_FSDKMatching score = new AFR_FSDKMatching();
				float max = 0.0f;
				String name = null;
				//				注册
				for (FaceDB.FaceRegist fr : mResgist) {
					for (AFR_FSDKFace face : fr.mFaceList) {
						error = engine.AFR_FSDK_FacePairMatching(result, face, score);
						Log.d(TAG,  "Score:" + score.getScore() + ", AFR_FSDK_FacePairMatching=" + error.getCode());
						if (max < score.getScore()) {
							max = score.getScore();
							name = fr.mName;
						}
					}
				}

				//age & gender
				face1.clear();
				face2.clear();
				face1.add(new ASAE_FSDKFace(mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree()));
				face2.add(new ASGE_FSDKFace(mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree()));
				ASAE_FSDKError error1 = mAgeEngine.ASAE_FSDK_AgeEstimation_Image(mImageNV21, mWidth, mHeight, AFT_FSDKEngine.CP_PAF_NV21, face1, ages);
				ASGE_FSDKError error2 = mGenderEngine.ASGE_FSDK_GenderEstimation_Image(mImageNV21, mWidth, mHeight, AFT_FSDKEngine.CP_PAF_NV21, face2, genders);
				Log.d(TAG, "ASAE_FSDK_AgeEstimation_Image:" + error1.getCode() + ",ASGE_FSDK_GenderEstimation_Image:" + error2.getCode());
//				Log.d(TAG, "age:" + ages.get(0).getAge() + ",gender:" + genders.get(0).getGender());
//				final String age = ages.get(0).getAge() == 0 ? "年龄未知" : ages.get(0).getAge() + "岁";
//				final String gender = genders.get(0).getGender() == -1 ? "性别未知" : (genders.get(0).getGender() == 0 ? "男" : "女");
				//crop
				byte[] data = mImageNV21;
				YuvImage yuv = new YuvImage(data, ImageFormat.NV21, mWidth, mHeight, null);
				ExtByteArrayOutputStream ops = new ExtByteArrayOutputStream();
				yuv.compressToJpeg(mAFT_FSDKFace.getRect(), 100, ops);
				final Bitmap bmp = BitmapFactory.decodeByteArray(ops.getByteArray(), 0, ops.getByteArray().length);

				if (isOneSecond){
					isOneSecond=false;
					File f= BitmapFileSetting.saveBitmapFile(bmp,captureImagePath+"/" +new SimpleDateFormat
							("HH-mm-ss").format(time)+".jpg");
					if (files.size()>6)files.remove(files.size()-1);
					files.add(f);

					final Contrast contrast=new Contrast();
					contrast.setName(name);
					contrast.setContrastPercentage(((int) (max*100))+"%");
					contrast.setTime(System.currentTimeMillis());
					contrast.setCaptureImg(f.getPath());
					List<File> fs = RxFileTool.searchFileInDir(Environment.getExternalStorageDirectory
							().getPath() +"/233311/RegisterImg/", name + ".jpg");
					if (fs!=null){
						if (fs.size()>0) contrast.setRegisterImg(fs.get(0).getPath());
						else contrast.setRegisterImg(fs.toString());
					}
					Log.e("setRegisterImg== ",contrast.getRegisterImg());
					ContrastHelp.insertContrast(contrast);

					if (contrasts.size()>6)contrasts.remove(contrasts.size()-1);
					contrasts.add(0,contrast);

					DetecterActivity.this.runOnUiThread(new Runnable(){
						@Override
						public void run() {
							captureImageAdapter.setNewData(files);
							contrastAdapter.setNewData(contrasts);
						}
					});

					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							DetecterActivity.this.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									ToastUtil.showToast(DetecterActivity.this,"恢复capture");
									isOneSecond=true;
								}
							});
						}
					},1500);
				}

				try {
					ops.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (max > 0.6f) {
					if (FileUtils2.read(path).equals("0")) openDoor();
					//fr success.
					final float max_score = max;
					Log.d(TAG, "fit Score:" + max + ", NAME:" + name);
					final String mNameShow = name;
					mHandler.removeCallbacks(hide);
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mTextView.setAlpha(1.0f);
							mTextView.setText(mNameShow);
							mTextView.setTextColor(Color.RED);
							mTextView1.setVisibility(View.VISIBLE);
							mTextView1.setText("置信度：" + (float)((int)(max_score * 1000)) / 1000.0);
							mTextView1.setTextColor(Color.RED);
//							mImageView.setRotation(rotate);
							if (mCameraMirror) {
								mImageView.setScaleY(-1);
							}
							mImageView.setImageAlpha(255);
							mImageView.setImageBitmap(bmp);
						}
					});
				} else {
					final String mNameShow = "未识别";
					DetecterActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mTextView.setAlpha(1.0f);
							mTextView1.setVisibility(View.VISIBLE);
//							mTextView1.setText( gender + "," + age);
							mTextView1.setTextColor(Color.RED);
							mTextView.setText(mNameShow);
							mTextView.setTextColor(Color.RED);
							mImageView.setImageAlpha(255);
//							mImageView.setRotation(rotate);
							if (mCameraMirror) {
								mImageView.setScaleY(-1);
							}
							mImageView.setImageBitmap(bmp);
						}
					});
				}
				mImageNV21 = null;
			}

		}

		/**
		 *
		 * 大佬我用这个方法旋转图片

		 Matrixmatrix=newMatrix();
		 matrix.setRotate(degrees,px,py);
		 Bitmapret=Bitmap.createBitmap(src,0,0,src.getWidth(),src.getHeight(),matrix,true);
		 if(recycle&&!src.isRecycled()){
		 src.recycle();
		 }

		 Bitmap设置mImageView.setImageBitmap(bmp2);是旋转了
		 但是这样保存成图片 图片还是没有旋转
		 bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);

		 */
		@Override
		public void over() {
			AFR_FSDKError error = engine.AFR_FSDK_UninitialEngine();
			Log.d(TAG, "AFR_FSDK_UninitialEngine : " + error.getCode());
		}
	}

    private void openDoor() {
		Log.e("openDoor","开门");
		FileUtils2.method(path, "1");
		if (FileUtils2.read(path).equals("1")) timer.schedule(new TimerTask() {
			@Override
			public void run() {
				DetecterActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.e("closeDoor","关门");
						FileUtils2.method(path, "0");
						ToastUtil.showToast(DetecterActivity.this, "定时 关门咯");
					}
				});
			}
		}, 6000);
	}

    private TextView mTextView;
	private TextView mTextView1;
	private ImageView mImageView;
	private ImageButton mImageButton;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		Toast.makeText(this,"捕獲图片目录" + FileUtils.createOrExistsDir(captureImagePath)
				,Toast.LENGTH_SHORT).show();

//		mCameraID = getIntent().getIntExtra("Camera", 0) == 0 ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
		mCameraID =0;
		mCameraRotate = getIntent().getIntExtra("Camera", 0) == 0 ? 90 : 270;
		mCameraMirror = getIntent().getIntExtra("Camera", 0) == 0 ? false : true;
		mWidth = 1280;
		mHeight = 960;
		mFormat = ImageFormat.NV21;
		mHandler = new Handler();

		setContentView(R.layout.activity_camera);
		mGLSurfaceView = (CameraGLSurfaceView) findViewById(R.id.glsurfaceView);
		mGLSurfaceView.setOnTouchListener(this);
		mSurfaceView = (CameraSurfaceView) findViewById(R.id.surfaceView);
		mSurfaceView.setOnCameraListener(this);
		//boolean autofit 自动调整, boolean mirror 镜面, int render_egree 渲染度(翻译 实际用处是角度)
		mSurfaceView.setupGLSurafceView(mGLSurfaceView, true, mCameraMirror, 0);
		mSurfaceView.debug_print_fps(true, false);

		//snap
		mTextView = (TextView) findViewById(R.id.textView);
		mTextView.setText("");
		mTextView1 = (TextView) findViewById(R.id.textView1);
		mTextView1.setText("");

		mImageView = (ImageView) findViewById(R.id.imageView);
		mImageButton = (ImageButton) findViewById(R.id.imageButton);
		mImageButton.setOnClickListener(this);

		AFT_FSDKError err = engine.AFT_FSDK_InitialFaceEngine(FaceDB.appid, FaceDB.ft_key, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 16, 5);
		Log.d(TAG, "AFT_FSDK_InitialFaceEngine =" + err.getCode());
		err = engine.AFT_FSDK_GetVersion(version);
		Log.d(TAG, "AFT_FSDK_GetVersion:" + version.toString() + "," + err.getCode());

//		ASAE_FSDKError error = mAgeEngine.ASAE_FSDK_InitAgeEngine(FaceDB.appid, FaceDB.age_key);
//		Log.d(TAG, "ASAE_FSDK_InitAgeEngine =" + error.getCode());
//		error = mAgeEngine.ASAE_FSDK_GetVersion(mAgeVersion);
//		Log.d(TAG, "ASAE_FSDK_GetVersion:" + mAgeVersion.toString() + "," + error.getCode());
//
//		ASGE_FSDKError error1 = mGenderEngine.ASGE_FSDK_InitgGenderEngine(FaceDB.appid, FaceDB.gender_key);
//		Log.d(TAG, "ASGE_FSDK_InitgGenderEngine =" + error1.getCode());
//		error1 = mGenderEngine.ASGE_FSDK_GetVersion(mGenderVersion);
//		Log.d(TAG, "ASGE_FSDK_GetVersion:" + mGenderVersion.toString() + "," + error1.getCode());

		initView2();
		initGPIO();

		mFRAbsLoop = new FRAbsLoop();
		mFRAbsLoop.start();
	}

	private void initGPIO() {
		if (Build.MODEL.contains("rk312x")){
			keyout="/sys/devices/misc_power_en.19/";
		}else if (Build.MODEL.contains("rk3288")){
			keyout="/sys/devices/misc_power_en.23/";
		} else if (Build.MODEL.contains("rk3368")) {
			keyout="/sys/devices/misc_power_en.22/";
		}
		path=keyout+"key_out2";
		//请求root权限
		try {
			Process su = Runtime.getRuntime().exec("su");
			//获取读写权限
			String strcmd = "chmod 777"+ path;
			strcmd = strcmd + "\n exit\n";
			su.getOutputStream().write(strcmd.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
        timer = new Timer();
	}

	private void initView2() {
		files= RxFileTool.listFilesInDir(captureImagePath);
		if (files==null)files=new ArrayList<>();
//		Toast.makeText(this,files.size()+"",Toast.LENGTH_SHORT).show();
		Collections.reverse(files);
		if (files.size()>6) files=files.subList(0,6);
		captureImageAdapter=new CaptureImageAdapter(files);
		rvListCapture= (RecyclerView) findViewById(R.id.rv_list_capture_image);
		rvListCapture.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
		rvListCapture.setAdapter(captureImageAdapter);

//		Toast.makeText(this,"ContrastHelp.queryAll()"+ContrastHelp.queryAll(),Toast.LENGTH_SHORT).show();
		contrasts= ContrastHelp.queryAll();
		if (contrasts==null)contrasts=new ArrayList<>();
		Collections.reverse(contrasts);
		if (contrasts.size()>6) contrasts=contrasts.subList(0,6);
		contrastAdapter=new ContrastAdapter(contrasts);
		rvListContrast= (RecyclerView) findViewById(R.id.rv_list_contrast);
		rvListContrast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		rvListContrast.setAdapter(contrastAdapter);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mFRAbsLoop.shutdown();
		AFT_FSDKError err = engine.AFT_FSDK_UninitialFaceEngine();
		Log.d(TAG, "AFT_FSDK_UninitialFaceEngine =" + err.getCode());

		ASAE_FSDKError err1 = mAgeEngine.ASAE_FSDK_UninitAgeEngine();
		Log.d(TAG, "ASAE_FSDK_UninitAgeEngine =" + err1.getCode());

		ASGE_FSDKError err2 = mGenderEngine.ASGE_FSDK_UninitGenderEngine();
		Log.d(TAG, "ASGE_FSDK_UninitGenderEngine =" + err2.getCode());

		stopTimer();
	}

	// 停止定时器
	public void stopTimer(){
		if(timer != null){
			timer.cancel();
			// 一定设置为null，否则定时器不会被回收
			timer = null;
		}
	}
	@Override
	public Camera setupCamera() {
		// TODO Auto-generated method stub
		mCamera = Camera.open(mCameraID);
		try {
//			Camera.Parameters parameters = mCamera.getParameters();
//			parameters.setPreviewSize(mWidth, mHeight);
//			parameters.setPreviewFormat(mFormat);
//
//			for( Camera.Size size : parameters.getSupportedPreviewSizes()) {
//				Log.d(TAG, "SIZE:" + size.width + "x" + size.height);
//			}
//			for( Integer format : parameters.getSupportedPreviewFormats()) {
//				Log.d(TAG, "FORMAT:" + format);
//			}
//
//			List<int[]> fps = parameters.getSupportedPreviewFpsRange();
//			for(int[] count : fps) {
//				Log.d(TAG, "T:");
//				for (int data : count) {
//					Log.d(TAG, "V=" + data);
//				}
//			}

			mCamera.startPreview();
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewFormat(ImageFormat.NV21);//设置相机的图像格式
			mCamera.setParameters(parameters);

			//parameters.setPreviewFpsRange(15000, 30000);
			//parameters.setExposureCompensation(parameters.getMaxExposureCompensation());
			//parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
			//parameters.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
			//parmeters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			//parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
			//parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mCamera != null) {
			mWidth = mCamera.getParameters().getPreviewSize().width;
			mHeight = mCamera.getParameters().getPreviewSize().height;
		}
		return mCamera;
	}

	@Override
	public void setupChanged(int format, int width, int height) {
	}

	@Override//	   开始	预览	   立即
	public boolean startPreviewImmediately() {
		return true;
	}

	@Override
	public Object onPreview(byte[] data, int width, int height, int format, long timestamp) {
		AFT_FSDKError err = engine.AFT_FSDK_FaceFeatureDetect(data, width, height, AFT_FSDKEngine.CP_PAF_NV21, result);
		Log.d(TAG, "AFT_FSDK_FaceFeatureDetect =" + err.getCode());
		Log.d(TAG, "Face=" + result.size());
		for (AFT_FSDKFace face : result) {
			Log.d(TAG, "Face:" + face.toString());
		}
		if (mImageNV21 == null) {
			if (!result.isEmpty()) {
				mAFT_FSDKFace = result.get(0).clone();
				mImageNV21 = data.clone();
			} else {
				if (!isPostted) {
					mHandler.removeCallbacks(hide);
					mHandler.postDelayed(hide, 2000);
					isPostted = true;
				}
			}
		}
		//copy rects
		Rect[] rects = new Rect[result.size()];
		for (int i = 0; i < result.size(); i++) {
			rects[i] = new Rect(result.get(i).getRect());
		}

		mWidth = width;
		mHeight = height;

		//clear result.
		result.clear();
		//return the rects for render.
		return rects;
	}

	@Override
	public void onBeforeRender(CameraFrameData data) {
	}

	@Override//在渲染之后
	public void onAfterRender(CameraFrameData data) {
		mGLSurfaceView.getGLES2Render().draw_rect((Rect[])data.getParams(), Color.GREEN, 2);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		CameraHelper.touchFocus(mCamera, event, v, this);
		return false;
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		if (success) {
			Log.d(TAG, "Camera Focus SUCCESS!");
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.imageButton) {
			if (mCameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
				mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
				mCameraRotate = 270;
				mCameraMirror = true;
			} else {
				mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
				mCameraRotate = 90;
				mCameraMirror = false;
			}
			mSurfaceView.resetCamera();
			mGLSurfaceView.setRenderConfig(0, mCameraMirror);
			mGLSurfaceView.getGLES2Render().setViewAngle(mCameraMirror, 0);
		}
	}

}
