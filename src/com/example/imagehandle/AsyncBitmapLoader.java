package com.example.imagehandle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import com.example.artcheckin.FrameFormat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

public class AsyncBitmapLoader    
{    
    /**   
     * �ڴ�ͼƬ�����û���   
     */    
	private static final String LOG_TAG = "AsyncBitmapLoader";
    private HashMap<String, SoftReference<Bitmap>> imageCache = null;    
    private int mGroupID = 0;

    public AsyncBitmapLoader()    
    {    
        imageCache = new HashMap<String, SoftReference<Bitmap>>();    
    }    
        
    public Bitmap loadBitmap(int groupID,final ImageView imageView, final String imageURL, final ImageCallBack imageCallBack)    
    {    
    	 Log.i(LOG_TAG, "-----<<<<<<<<�ս���loadBitmap����");
    	 mGroupID = groupID;
        //���ڴ滺���У��򷵻�Bitmap����    
        if(imageCache.containsKey(imageURL))    
        {    
        	Log.i(LOG_TAG, "-----<<<<<<<<���imageCach��������imageURL��"+imageURL);
            SoftReference<Bitmap> reference = imageCache.get(imageURL);    
            Bitmap bitmap = reference.get();    
            Log.i(LOG_TAG, "-----<<<<<<<<����ڴ滺���и���imageURL תΪbitma");
            if(bitmap != null)    
            {    
            	Log.i(LOG_TAG, "-----<<<<<<<<����ڴ滺���и���imageURL תΪbitma��=null������");
                return bitmap;    
            }    
        }    
        else    
        {    
            /**   
             * ����һ���Ա��ػ���Ĳ���   
             */    
        	Log.i(LOG_TAG, "-----<<<<<<<<���imageCach���ڴ滺���в���������imageURL��"+imageURL);
        	
            String bitmapName = imageURL.substring(imageURL.lastIndexOf("/") + 1); 
            Log.i(LOG_TAG, "-----<<<<<<<<����ͼƬ��������֣�"+bitmapName);
            File cacheDir = new File(FrameFormat.ROOTFILENAME+groupID+"/");   
            Log.i(LOG_TAG, "-----<<<<<<<<�����ļ���");
            File[] cacheFiles = cacheDir.listFiles();    
            int i = 0;    
            if(null!=cacheFiles){  
            	Log.i(LOG_TAG, "-----<<<<<<<<���cacheFilesҲ�����ļ����е��ļ���Ϊnull");
            for(; i<cacheFiles.length; i++)    
            {    
            	Log.i(LOG_TAG, "-----<<<<<<<<�����������ļ����е��ļ�");
                if(bitmapName.equals(cacheFiles[i].getName()))    
                {    
                	Log.i(LOG_TAG, "-----<<<<<<<<������õı���ͼƬ�����ֵ��ڴ��ļ������ļ������֣���break");
                    break;    
                }    
            }    
                
            if(i < cacheFiles.length)    
            {    
            	Log.i(LOG_TAG, "-----<<<<<<<<���ļ������ҵ��˺����õ�ͼƬ������ͬ���ļ������ش��ļ���ת��ʽΪbitmap");
                return BitmapFactory.decodeFile(FrameFormat.ROOTFILENAME+groupID+"/" + bitmapName);    
            }  
            }  
        }    
            
        final Handler handler = new Handler()    
        {    
            /* (non-Javadoc)   
             * @see android.os.Handler#handleMessage(android.os.Message)   
             */    
            @Override    
            public void handleMessage(Message msg)    
            {    
            	Log.i(LOG_TAG, "-----<<<<<<<<����handleMessage����������imageLoad");
                // TODO Auto-generated method stub    
                imageCallBack.imageLoad(imageView, (Bitmap)msg.obj);    
            }    
        };    
            
        //��������ڴ滺���У�Ҳ���ڱ��أ���jvm���յ����������߳�����ͼƬ    
        new Thread()    
        {    
            /* (non-Javadoc)   
             * @see java.lang.Thread#run()   
             */    
            @Override    
            public void run()    
            {    
                // TODO Auto-generated method stub 
            	Log.i(LOG_TAG, "-----<<<<<<<<����thread run");
                InputStream bitmapIs = HttpUtils.getStreamFromURL(imageURL);    
                    
                Bitmap bitmap = BitmapFactory.decodeStream(bitmapIs);    
                imageCache.put(imageURL, new SoftReference<Bitmap>(bitmap));    
                Message msg = handler.obtainMessage(0, bitmap);    
                handler.sendMessage(msg);    
                Log.i(LOG_TAG, "-----<<<<<<<<thread run-handler sendMessage:"+msg);
                File dir = new File(FrameFormat.ROOTFILENAME+mGroupID+"/");   
                Log.i(LOG_TAG, "-----<<<<<<<<thread run -�������ļ���dir��"+dir);
                if(!dir.exists())    
                {    
                	Log.i(LOG_TAG, "-----<<<<<<<<thread run -���ļ���dir��"+dir+"�����ڣ��� ����");
                    dir.mkdirs();    
                }    
                    
                File bitmapFile = new File(FrameFormat.ROOTFILENAME+mGroupID+"/" +     
                        imageURL.substring(imageURL.lastIndexOf("/") + 1));    
                Log.i(LOG_TAG, "-----<<<<<<<<thread run -�������ļ�bitmapFile��"+bitmapFile);
                if(!bitmapFile.exists())    
                {    
                	Log.i(LOG_TAG, "-----<<<<<<<<thread run -���ļ���bitmapFile��"+bitmapFile+"�����ڣ��� ����");
                    try    
                    {    
                        bitmapFile.createNewFile();    
                        Log.i(LOG_TAG, "-----<<<<<<<<thread run -���ļ���bitmapFile��"+bitmapFile+"�����ɹ�");
                    }    
                    catch (IOException e)    
                    {    
                        // TODO Auto-generated catch block 
                    	Log.i(LOG_TAG, "-----<<<<<<<<thread run -���ļ���bitmapFile��"+bitmapFile+"�������ɹ�");
                        e.printStackTrace();    
                    }    
                }    
                FileOutputStream fos;    
                try    
                {    
                	 Log.i(LOG_TAG, "-----<<<<<<<<thread run fos����");
                    fos = new FileOutputStream(bitmapFile); 
                    Log.i(LOG_TAG, "-----<<<<<<<<thread run fos�����ɹ�");
                    if(bitmap!=null){
                    	bitmap.compress(Bitmap.CompressFormat.PNG,     
                                100, fos);   
                    }
                    fos.close();    
                }    
                catch (FileNotFoundException e)    
                {    
                    // TODO Auto-generated catch block    
                	Log.i(LOG_TAG, "-----<<<<<<<<thread run fos����������FileNotFoundException");
                    e.printStackTrace();    
                }    
                catch (IOException e)    
                {    
                    // TODO Auto-generated catch block  
                	Log.i(LOG_TAG, "-----<<<<<<<<thread run fos����������IOException");
                    e.printStackTrace();    
                }    
            }    
        }.start();    
            
        return null;    
    }    
  
    public interface ImageCallBack    
    {    
        public void imageLoad(ImageView imageView, Bitmap bitmap);    
    }   
   
}  