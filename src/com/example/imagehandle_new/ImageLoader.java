package com.example.imagehandle_new;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.artcheckin.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader {
	private static final String LOG_TAG = "ImageLoader";
	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	private Context context ;
	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	// 线程池
	ExecutorService executorService;

	public ImageLoader(Context context) {
		fileCache = new FileCache(context);
		this.context = context;
		executorService = Executors.newFixedThreadPool(10);
	}

	// 当进入listview时默认的图片，可换成你自己的默认图片
	final int stub_id = R.drawable.default_picture;

	// 最主要的方法
	public void DisplayImage(String url, ImageView imageView) {
		Log.d(LOG_TAG, "------->>ImageLoader传入的url参数为="+url);
		if(url!=null){
			imageViews.put(imageView, url);
			// 先从内存缓存中查找
			if(url.equals("NULL")){
				imageView.setImageResource(stub_id);
				Log.d(LOG_TAG, "------->>ImageLoader 显示默认图片：因为传入的url参数为null");
			}
			else{
				Bitmap bitmap = memoryCache.get(url);
				if (bitmap != null){
					Log.d(LOG_TAG,"-----!!!!!!!!!!内存缓存中有此图片："+url);
					imageView.setImageBitmap(bitmap);
				}
				else {
					// 若没有的话则开启新线程加载图片
					//	Log.d(LOG_TAG,"-----!!!!!!!!!开启新线程加载图片"+groupID+"组"+studentID+"号:::");
					queuePhoto(url, imageView);
					imageView.setImageResource(stub_id);
					Log.d(LOG_TAG, "------->>ImageLoader 显示默认图片：因为内存缓存中没有此图片，因此开启新线程去加载图片，在加载到前，先显示默认图片");
				}
			}
		}
		else{
			Log.d(LOG_TAG,"-----!!!!!!!!!!imageViews.put(imageView, 0)");
			imageViews.put(imageView, "0");
		}

	}
	// 最主要的方法
	public void SaveImage(String url) {

		Log.d(LOG_TAG,"-----!!!!!!!!!SaveImage开启新线程加载图片");
		if(url!=null&&url.equals("NULL")){
			Bitmap bitmap = memoryCache.get(url);
			if(bitmap==null){
				queuePhotoSave(url);
			}
		}
	}
	private void queuePhoto(String url, ImageView imageView) {
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p));
	}
	private void queuePhotoSave(String url) {
		executorService.submit(new PhotosLoaderSave(url));
	}
	class PhotosLoaderSave implements Runnable {
		String picURL;

		PhotosLoaderSave(String picURL) {
			this.picURL = picURL;
		}
		@Override
		public void run() {
			Bitmap bmp = getBitmap(picURL);
			Log.d(LOG_TAG,"-----!!!!!!!!!!save从网上或文件缓存中得到的图片bmp="+bmp);
			if(bmp!=null){
				memoryCache.put(picURL, bmp);			
			}

		}
	}
	@SuppressWarnings("finally")
	private Bitmap getBitmap(String url) {
		File f = fileCache.getFile(url);
		Log.d(LOG_TAG,"-----!!!!!!!!原始文件:"+f.getName());
		// 先从文件缓存中查找是否有
		Bitmap b = decodeFile(f);
		if (b != null){
				Log.d(LOG_TAG,"-----!!!!!!!!!!文件缓存中有原始文件:"+f.getName());
			return b;

		}
//		Log.d(LOG_TAG,"-----!!!!!!!!!!!FileCache传过来的file："+fileCache.getFileDir(url).getName());
//		f.renameTo(fileCache.getFileDir(url));
//		Log.d(LOG_TAG,"-----!!!!!!!!!!!文件rename为_tmp后"+f.getName());
		//Log.d(LOG_TAG,"-----!!!!!!!!!文件缓存中没有"+groupID+"组"+studentID+"号:::");
		// 最后从指定的url中下载图片
		//	return getBitmapFromUrl(url);
		Bitmap bitmap = null;
		OutputStream os =null;
		//	try {

		URL imageUrl = null;
		try {
			imageUrl = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(LOG_TAG,"-----!!!!!!!!!!MalformedURLException new URL：");
			bitmap = null;
			f.delete();
			return bitmap;
		}
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) imageUrl
					.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(LOG_TAG,"-----!!!!!!!!!!IOException openConnection：");
			bitmap = null;
			f.delete();
			return bitmap;
		}
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(30000);
		conn.setInstanceFollowRedirects(true);
		try {
			if(conn.getResponseCode()==200){
				InputStream is = conn.getInputStream();
				os = new FileOutputStream(f);
				CopyStream(is, os);
				bitmap = decodeFile(f);
				//f.renameTo(fileCache.getFile(url));
				//Log.d(LOG_TAG,"-----!!!!!!!!!!!!.tmp文件rename"+f.getName());
			}
			return bitmap;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(LOG_TAG,"-----!!!!!!!!!!FileNotFoundException printStackTrace：");
			bitmap = null;
			f.delete();
			return bitmap;
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(LOG_TAG,"-----!!!!!!!!!!IOException printStackTrace：");
			bitmap = null;
			f.delete();
			return bitmap;
		}
		finally{
			Log.d(LOG_TAG,"-----!!!!!!!!!!!!执行finally:");
//			File f1 = fileCache.getFileDir(url);
//			if(f1.exists()){
//				Log.d(LOG_TAG,"-----!!!!!!!!!!!!!!!.tmp文件存在，删除:"+f1.getName());
//				f1.delete();
//			}
			try {
				if(os!=null){
					os.flush();
					os.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// decode这个图片并且按比例缩放以减少内存消耗，虚拟机对每张图片的缓存大小也是有限制的
	private Bitmap decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			//			final int REQUIRED_SIZE = 70;
			//			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			//			int scale = 1;
			//			while (true) {
			//				if (width_tmp / 2 < REQUIRED_SIZE
			//						|| height_tmp / 2 < REQUIRED_SIZE)
			//					break;
			//				width_tmp /= 2;
			//				height_tmp /= 2;
			//				scale *= 2;
			//			}
			final int REQUIRED_SIZE = 120;
			int height = o.outHeight * REQUIRED_SIZE / o.outWidth;
			int scale = o.outWidth/REQUIRED_SIZE;
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.outWidth = REQUIRED_SIZE;
			o2.outHeight = height;
			//	o2.inPreferredConfig = Bitmap.Config.ARGB_4444;
			//	o2.inPurgeable = true;
			//	o2.inInputShareable = true;
			// decode with inSampleSize
			o2.inJustDecodeBounds = false;
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp = getBitmap(photoToLoad.url);
				Log.d(LOG_TAG,"-----!!!!!!!!!!从网上或文件缓存中得到的图片bmp="+bmp);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			// 更新的操作放在UI线程中
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	/**
	 * 防止图片错位
	 * 
	 * @param photoToLoad
	 * @return
	 */
	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// 用于在UI线程中更新界面
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null){
				//	Log.d(LOG_TAG,"-----!!!!!!!!!BitmapDisplayer传入参数为："+bitmap+" "+groupID+"组"+studentID+"号:::");
				photoToLoad.imageView.setImageBitmap(bitmap);
			}

			else{
				//	Log.d(LOG_TAG,"-----!!!!!!!!!BitmapDisplayer传入参数为null,显示默认图片"+groupID+"组"+studentID+"号:::");
				photoToLoad.imageView.setImageResource(stub_id);
			//	Log.d(LOG_TAG, "------->>ImageLoader 显示默认图片：因为传入的url参数为null");
			}

		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}
}