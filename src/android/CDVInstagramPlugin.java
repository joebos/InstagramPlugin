/*
    The MIT License (MIT)
    Copyright (c) 2013 Vlad Stirbu
    
    Permission is hereby granted, free of charge, to any person obtaining
    a copy of this software and associated documentation files (the
    "Software"), to deal in the Software without restriction, including
    without limitation the rights to use, copy, modify, merge, publish,
    distribute, sublicense, and/or sell copies of the Software, and to
    permit persons to whom the Software is furnished to do so, subject to
    the following conditions:
    
    The above copyright notice and this permission notice shall be
    included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
    EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
    MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
    NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
    OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
    WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.vladstirbu.cordova;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.FROYO)
public class CDVInstagramPlugin extends CordovaPlugin {

    private static final FilenameFilter OLD_IMAGE_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.startsWith("instagram");
        }
    };

    private static final FilenameFilter OLD_VIDEO_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.startsWith("instagram_video");
        }
    };

    CallbackContext cbContext;
	
	@Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		
		this.cbContext = callbackContext;
		
        if (action.equals("share")) {
            String imageString = args.getString(0);
            String captionString = args.getString(1);
		
	    PluginResult result = new PluginResult(Status.NO_RESULT);
            result.setKeepCallback(true);
		
            this.share(imageString, captionString);
            return true;
        } else if (action.equals("shareVideo")) {
            String videoUrl = args.getString(0);
            String captionString = args.getString(1);
		
    	    PluginResult result = new PluginResult(Status.NO_RESULT);
            result.setKeepCallback(true);
		
            this.shareVideo(videoUrl, captionString);
            return true;
        } else if (action.equals("isInstalled")) {
        	this.isInstalled();
        } else {
        	callbackContext.error("Invalid Action");
        }
        return false;
    }
	
	private void isInstalled() {
		try {
			this.webView.getContext().getPackageManager().getApplicationInfo("com.instagram.android", 0);
			this.cbContext.success(this.webView.getContext().getPackageManager().getPackageInfo("com.instagram.android", 0).versionName);
		} catch (PackageManager.NameNotFoundException e) {
			this.cbContext.error("Application not installed");
		}
	}

    private void share(String imageString, String captionString) {
        if (imageString != null && imageString.length() > 0) { 
        	byte[] imageData = Base64.decode(imageString, 0);
        	
        	File file = null;  
            FileOutputStream os = null;
        	
        	File parentDir = this.webView.getContext().getExternalFilesDir(null);
            File[] oldImages = parentDir.listFiles(OLD_IMAGE_FILTER);
            for (File oldImage : oldImages) {
                oldImage.delete();
            }

            try {
                file = File.createTempFile("instagram", ".png", parentDir);
                os = new FileOutputStream(file, true);
            } catch (Exception e) {
                e.printStackTrace();
            }

        	try {
        		os.write(imageData);
				os.flush();
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	Intent shareIntent = new Intent(Intent.ACTION_SEND);
        	shareIntent.setType("image/*");
        	shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        	shareIntent.putExtra(Intent.EXTRA_TEXT, captionString);
        	shareIntent.setPackage("com.instagram.android");
        	
        	this.cordova.startActivityForResult((CordovaPlugin) this, Intent.createChooser(shareIntent, "Share to"), 12345);
        	
        } else {
            this.cbContext.error("Expected one non-empty string argument.");
        }
    }

    private void shareVideo(String videoFile, String captionString) {
        if (videoFile != null && videoFile.length() > 0) {

            try{

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("video/mp4");

                //File media = new File(file);
                //Uri uri = Uri.fromFile(media);

                // Add the URI to the Intent.
                //share.putExtra(Intent.EXTRA_STREAM, uri);

                // Broadcast the Intent.
                //startActivity(Intent.createChooser(share, "Share to"));

                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(videoFile));
                shareIntent.putExtra(Intent.EXTRA_TEXT, captionString);
                shareIntent.setPackage("com.instagram.android");

                this.cordova.startActivityForResult((CordovaPlugin) this, Intent.createChooser(shareIntent, "Share to"), 12345);

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
            this.cbContext.error("Expected one non-empty string argument.");
        }
    }
    /*
    private void shareVideo(String videoUrl, String captionString) {
        if (videoUrl != null && videoUrl.length() > 0) {

            byte[] videoData = null;

            try{

                URL url = new URL(videoUrl);

                URLConnection ucon = url.openConnection();
                InputStream is = ucon.getInputStream();
                videoData = new byte[is.available()];
                is.read(videoData);

                File file = null;
                FileOutputStream os = null;

                File parentDir = this.webView.getContext().getExternalFilesDir(null);
                File[] oldVideos = parentDir.listFiles(OLD_VIDEO_FILTER);
                for (File oldVideo : oldVideos) {
                    oldVideo.delete();
                }

                try {
                    file = File.createTempFile("instagram_video", ".mp4", parentDir);
                    os = new FileOutputStream(file, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        		os.write(videoData);
				os.flush();
				os.close();
                is.close();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("video/mp4");

                //File media = new File(file);
                //Uri uri = Uri.fromFile(media);

                // Add the URI to the Intent.
                //share.putExtra(Intent.EXTRA_STREAM, uri);

                // Broadcast the Intent.
                //startActivity(Intent.createChooser(share, "Share to"));

                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file));
                shareIntent.putExtra(Intent.EXTRA_TEXT, captionString);
                shareIntent.setPackage("com.instagram.android");

                this.cordova.startActivityForResult((CordovaPlugin) this, shareIntent, 12345);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
            this.cbContext.error("Expected one non-empty string argument.");
        }
    }
    */

    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == Activity.RESULT_OK) {
    		Log.v("Instagram", "shared ok");
    		this.cbContext.success();
    	} else if (resultCode == Activity.RESULT_CANCELED) {
    		Log.v("Instagram", "share cancelled");
    		this.cbContext.error("Share Cancelled");
    	}
    }
}
