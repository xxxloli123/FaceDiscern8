package com.android.face_discern;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.android.face_discern.db.DaoMaster;
import com.android.face_discern.db.DaoSession;
import com.blankj.utilcode.util.FileUtils;

/**
 * Created by gqj3375 on 2017/4/28.
 */

public class Application extends android.app.Application {
	private final String TAG = this.getClass().toString();
	FaceDB mFaceDB;
	Uri mImage;
	private static DaoSession daoSession;

	@Override
	public void onCreate() {
		super.onCreate();
		//		mFaceDB = new FaceDB(this.getExternalCacheDir().getPath());
		Toast.makeText(this,"人脸数据目录" + FileUtils.createOrExistsDir(
				Environment.getExternalStorageDirectory().getPath()+"/233311/FaceData")
				,Toast.LENGTH_SHORT).show();
//		Toast.makeText(this,"获取目录下指定文件名的文件\n" + RxFileTool.searchFileInDir(
//				Environment.getExternalStorageDirectory().getPath() +"/233311/RegisterImg/",
//				"劳资.jpg").get(0).getPath()
//				,Toast.LENGTH_SHORT).show();
		mFaceDB = new FaceDB(Environment.getExternalStorageDirectory().getPath()+"/233311/FaceData");
		mImage = null;

		setupDatabase();
	}

	public void setCaptureImage(Uri uri) {
		mImage = uri;
	}

	public Uri getCaptureImage() {
		return mImage;
	}

	/**
	 * 配置数据库
	 */
	private void setupDatabase() {
		//创建数据库shop.db
		DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "shop.db", null);
		//获取可写数据库
		SQLiteDatabase db = helper.getWritableDatabase();
		//获取数据库对象
		DaoMaster daoMaster = new DaoMaster(db);
		//获取dao对象管理者
		daoSession = daoMaster.newSession();
	}

	public static DaoSession getDaoInstant() {
		return daoSession;
	}

	/**
	 * @param path
	 * @return
	 */
	public static Bitmap decodeImage(String path) {
		Bitmap res;
		try {
			ExifInterface exif = new ExifInterface(path);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			BitmapFactory.Options op = new BitmapFactory.Options();
			op.inSampleSize = 1;
			op.inJustDecodeBounds = false;
			//op.inMutable = true;
			res = BitmapFactory.decodeFile(path, op);
			//rotate and scale.
			Matrix matrix = new Matrix();

			if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
				matrix.postRotate(90);
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
				matrix.postRotate(180);
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
				matrix.postRotate(270);
			}

			Bitmap temp = Bitmap.createBitmap(res, 0, 0, res.getWidth(), res.getHeight(), matrix, true);
			Log.d("com.arcsoft", "check target Image:" + temp.getWidth() + "X" + temp.getHeight());

			if (!temp.equals(res)) {
				res.recycle();
			}
			return temp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
