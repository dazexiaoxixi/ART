package com.example.imagehandle;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

public class AsyncImageLoader {
	
	// Ϊ�˼ӿ��ٶȣ����ڴ��п������棨��ҪӦ�����ظ�ͼƬ�϶�ʱ������ͬһ��ͼƬҪ��α����ʣ�������ListViewʱ���ع�����
		public Map<String, SoftReference<Drawable>> imageCache = new HashMap<String, SoftReference<Drawable>>();
		
		private ExecutorService executorService = Executors.newFixedThreadPool(10); // �̶�10���߳���ִ������
		private final Handler handler = new Handler();

		/**
		 * 
		 * @param imageUrl
		 *            ͼ��url��ַ
		 * @param callback
		 *            �ص��ӿ�
		 * @return �����ڴ��л����ͼ�񣬵�һ�μ��ط���null
		 */
		public Drawable loadDrawable(final String imageUrl,
				final ImageCallback callback) {
			// ���������ʹӻ�����ȡ������
			if (imageCache.containsKey(imageUrl)) {
				SoftReference<Drawable> softReference = imageCache.get(imageUrl);
				if (softReference.get() != null) {
					return softReference.get();
				}
			}
			// ������û��ͼ�����������ȡ�����ݣ�����ȡ�������ݻ��浽�ڴ���
			executorService.submit(new Runnable() {
				public void run() {
					try {
						final Drawable drawable = loadImageFromUrl(imageUrl); 
							
						imageCache.put(imageUrl, new SoftReference<Drawable>(
								drawable));

						handler.post(new Runnable() {
							public void run() {
								callback.imageLoaded(drawable);
							}
						});
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
			return null;
		}

		// ��������ȡ���ݷ���
		protected Drawable loadImageFromUrl(String imageUrl) {
			try {
				// ����ʱ��ģ��������ʱ��ʵ��ʱ���д��벻����
				//SystemClock.sleep(2000);

				return Drawable.createFromStream(new URL(imageUrl).openStream(),
						"image.png");

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		// ����翪�ŵĻص��ӿ�
		public interface ImageCallback {
			// ע�� �˷�������������Ŀ������ͼ����Դ
			public void imageLoaded(Drawable imageDrawable);
		}
}
