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
     * 内存图片软引用缓冲   
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
    	 Log.i(LOG_TAG, "-----<<<<<<<<刚进入loadBitmap函数");
    	 mGroupID = groupID;
        //在内存缓存中，则返回Bitmap对象    
        if(imageCache.containsKey(imageURL))    
        {    
        	Log.i(LOG_TAG, "-----<<<<<<<<如果imageCach包含键名imageURL："+imageURL);
            SoftReference<Bitmap> reference = imageCache.get(imageURL);    
            Bitmap bitmap = reference.get();    
            Log.i(LOG_TAG, "-----<<<<<<<<则从内存缓存中根据imageURL 转为bitma");
            if(bitmap != null)    
            {    
            	Log.i(LOG_TAG, "-----<<<<<<<<则从内存缓存中根据imageURL 转为bitma！=null，返回");
                return bitmap;    
            }    
        }    
        else    
        {    
            /**   
             * 加上一个对本地缓存的查找   
             */    
        	Log.i(LOG_TAG, "-----<<<<<<<<如果imageCach即内存缓存中不包含键名imageURL："+imageURL);
        	
            String bitmapName = imageURL.substring(imageURL.lastIndexOf("/") + 1); 
            Log.i(LOG_TAG, "-----<<<<<<<<设置图片保存的名字："+bitmapName);
            File cacheDir = new File(FrameFormat.ROOTFILENAME+groupID+"/");   
            Log.i(LOG_TAG, "-----<<<<<<<<创建文件夹");
            File[] cacheFiles = cacheDir.listFiles();    
            int i = 0;    
            if(null!=cacheFiles){  
            	Log.i(LOG_TAG, "-----<<<<<<<<如果cacheFiles也就是文件夹中的文件不为null");
            for(; i<cacheFiles.length; i++)    
            {    
            	Log.i(LOG_TAG, "-----<<<<<<<<遍历创建的文件夹中的文件");
                if(bitmapName.equals(cacheFiles[i].getName()))    
                {    
                	Log.i(LOG_TAG, "-----<<<<<<<<如果设置的保存图片的名字等于此文件夹中文件的名字，则break");
                    break;    
                }    
            }    
                
            if(i < cacheFiles.length)    
            {    
            	Log.i(LOG_TAG, "-----<<<<<<<<在文件夹中找到了和设置的图片名字相同的文件，返回此文件，转格式为bitmap");
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
            	Log.i(LOG_TAG, "-----<<<<<<<<进入handleMessage函数，调用imageLoad");
                // TODO Auto-generated method stub    
                imageCallBack.imageLoad(imageView, (Bitmap)msg.obj);    
            }    
        };    
            
        //如果不在内存缓存中，也不在本地（被jvm回收掉），则开启线程下载图片    
        new Thread()    
        {    
            /* (non-Javadoc)   
             * @see java.lang.Thread#run()   
             */    
            @Override    
            public void run()    
            {    
                // TODO Auto-generated method stub 
            	Log.i(LOG_TAG, "-----<<<<<<<<进入thread run");
                InputStream bitmapIs = HttpUtils.getStreamFromURL(imageURL);    
                    
                Bitmap bitmap = BitmapFactory.decodeStream(bitmapIs);    
                imageCache.put(imageURL, new SoftReference<Bitmap>(bitmap));    
                Message msg = handler.obtainMessage(0, bitmap);    
                handler.sendMessage(msg);    
                Log.i(LOG_TAG, "-----<<<<<<<<thread run-handler sendMessage:"+msg);
                File dir = new File(FrameFormat.ROOTFILENAME+mGroupID+"/");   
                Log.i(LOG_TAG, "-----<<<<<<<<thread run -创建新文件夹dir："+dir);
                if(!dir.exists())    
                {    
                	Log.i(LOG_TAG, "-----<<<<<<<<thread run -新文件夹dir："+dir+"不存在，则 创建");
                    dir.mkdirs();    
                }    
                    
                File bitmapFile = new File(FrameFormat.ROOTFILENAME+mGroupID+"/" +     
                        imageURL.substring(imageURL.lastIndexOf("/") + 1));    
                Log.i(LOG_TAG, "-----<<<<<<<<thread run -创建新文件bitmapFile："+bitmapFile);
                if(!bitmapFile.exists())    
                {    
                	Log.i(LOG_TAG, "-----<<<<<<<<thread run -新文件夹bitmapFile："+bitmapFile+"不存在，则 创建");
                    try    
                    {    
                        bitmapFile.createNewFile();    
                        Log.i(LOG_TAG, "-----<<<<<<<<thread run -新文件夹bitmapFile："+bitmapFile+"创建成功");
                    }    
                    catch (IOException e)    
                    {    
                        // TODO Auto-generated catch block 
                    	Log.i(LOG_TAG, "-----<<<<<<<<thread run -新文件夹bitmapFile："+bitmapFile+"创建不成功");
                        e.printStackTrace();    
                    }    
                }    
                FileOutputStream fos;    
                try    
                {    
                	 Log.i(LOG_TAG, "-----<<<<<<<<thread run fos创建");
                    fos = new FileOutputStream(bitmapFile); 
                    Log.i(LOG_TAG, "-----<<<<<<<<thread run fos创建成功");
                    if(bitmap!=null){
                    	bitmap.compress(Bitmap.CompressFormat.PNG,     
                                100, fos);   
                    }
                    fos.close();    
                }    
                catch (FileNotFoundException e)    
                {    
                    // TODO Auto-generated catch block    
                	Log.i(LOG_TAG, "-----<<<<<<<<thread run fos创建有问题FileNotFoundException");
                    e.printStackTrace();    
                }    
                catch (IOException e)    
                {    
                    // TODO Auto-generated catch block  
                	Log.i(LOG_TAG, "-----<<<<<<<<thread run fos创建有问题IOException");
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