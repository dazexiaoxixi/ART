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
	// �̳߳�
	ExecutorService executorService;

	public ImageLoader(Context context) {
		fileCache = new FileCache(context);
		this.context = context;
		executorService = Executors.newFixedThreadPool(10);
	}

	// ������listviewʱĬ�ϵ�ͼƬ���ɻ������Լ���Ĭ��ͼƬ
	final int stub_id = R.drawable.default_picture;

	// ����Ҫ�ķ���
	public void DisplayImage(String url, ImageView imageView) {
		Log.d(LOG_TAG, "------->>ImageLoader�����url����Ϊ="+url);
		if(url!=null){
			imageViews.put(imageView, url);
			// �ȴ��ڴ滺���в���
			if(url.equals("NULL")){
				imageView.setImageResource(stub_id);
				Log.d(LOG_TAG, "------->>ImageLoader ��ʾĬ��ͼƬ����Ϊ�����url����Ϊnull");
			}
			else{
				Bitmap bitmap = memoryCache.get(url);
				if (bitmap != null){
					Log.d(LOG_TAG,"-----!!!!!!!!!!�ڴ滺�����д�ͼƬ��"+url);
					imageView.setImageBitmap(bitmap);
				}
				else {
					// ��û�еĻ��������̼߳���ͼƬ
					//	Log.d(LOG_TAG,"-----!!!!!!!!!�������̼߳���ͼƬ"+groupID+"��"+studentID+"��:::");
					queuePhoto(url, imageView);
					imageView.setImageResource(stub_id);
					Log.d(LOG_TAG, "------->>ImageLoader ��ʾĬ��ͼƬ����Ϊ�ڴ滺����û�д�ͼƬ����˿������߳�ȥ����ͼƬ���ڼ��ص�ǰ������ʾĬ��ͼƬ");
				}
			}
		}
		else{
			Log.d(LOG_TAG,"-----!!!!!!!!!!imageViews.put(imageView, 0)");
			imageViews.put(imageView, "0");
		}

	}
	// ����Ҫ�ķ���
	public void SaveImage(String url) {

		Log.d(LOG_TAG,"-----!!!!!!!!!SaveImage�������̼߳���ͼƬ");
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
			Log.d(LOG_TAG,"-----!!!!!!!!!!save�����ϻ��ļ������еõ���ͼƬbmp="+bmp);
			if(bmp!=null){
				memoryCache.put(picURL, bmp);			
			}

		}
	}
	@SuppressWarnings("finally")
	private Bitmap getBitmap(String url) {
		File f = fileCache.getFile(url);
		Log.d(LOG_TAG,"-----!!!!!!!!ԭʼ�ļ�:"+f.getName());
		// �ȴ��ļ������в����Ƿ���
		Bitmap b = decodeFile(f);
		if (b != null){
				Log.d(LOG_TAG,"-----!!!!!!!!!!�ļ���������ԭʼ�ļ�:"+f.getName());
			return b;

		}
//		Log.d(LOG_TAG,"-----!!!!!!!!!!!FileCache��������file��"+fileCache.getFileDir(url).getName());
//		f.renameTo(fileCache.getFileDir(url));
//		Log.d(LOG_TAG,"-----!!!!!!!!!!!�ļ�renameΪ_tmp��"+f.getName());
		//Log.d(LOG_TAG,"-----!!!!!!!!!�ļ�������û��"+groupID+"��"+studentID+"��:::");
		// ����ָ����url������ͼƬ
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
			Log.d(LOG_TAG,"-----!!!!!!!!!!MalformedURLException new URL��");
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
			Log.d(LOG_TAG,"-----!!!!!!!!!!IOException openConnection��");
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
				//Log.d(LOG_TAG,"-----!!!!!!!!!!!!.tmp�ļ�rename"+f.getName());
			}
			return bitmap;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(LOG_TAG,"-----!!!!!!!!!!FileNotFoundException printStackTrace��");
			bitmap = null;
			f.delete();
			return bitmap;
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(LOG_TAG,"-----!!!!!!!!!!IOException printStackTrace��");
			bitmap = null;
			f.delete();
			return bitmap;
		}
		finally{
			Log.d(LOG_TAG,"-----!!!!!!!!!!!!ִ��finally:");
//			File f1 = fileCache.getFileDir(url);
//			if(f1.exists()){
//				Log.d(LOG_TAG,"-----!!!!!!!!!!!!!!!.tmp�ļ����ڣ�ɾ��:"+f1.getName());
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

	// decode���ͼƬ���Ұ����������Լ����ڴ����ģ��������ÿ��ͼƬ�Ļ����СҲ�������Ƶ�
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
				Log.d(LOG_TAG,"-----!!!!!!!!!!�����ϻ��ļ������еõ���ͼƬbmp="+bmp);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			// ���µĲ�������UI�߳���
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	/**
	 * ��ֹͼƬ��λ
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

	// ������UI�߳��и��½���
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
				//	Log.d(LOG_TAG,"-----!!!!!!!!!BitmapDisplayer�������Ϊ��"+bitmap+" "+groupID+"��"+studentID+"��:::");
				photoToLoad.imageView.setImageBitmap(bitmap);
			}

			else{
				//	Log.d(LOG_TAG,"-----!!!!!!!!!BitmapDisplayer�������Ϊnull,��ʾĬ��ͼƬ"+groupID+"��"+studentID+"��:::");
				photoToLoad.imageView.setImageResource(stub_id);
			//	Log.d(LOG_TAG, "------->>ImageLoader ��ʾĬ��ͼƬ����Ϊ�����url����Ϊnull");
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