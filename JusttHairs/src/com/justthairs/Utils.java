package com.justthairs;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

public class Utils {

	public static JSONObject loadJSON(String path) {
		JSONObject json = null;
		try {

			InputStream is = new FileInputStream(path);

			int size = is.available();

			byte[] buffer = new byte[size];

			is.read(buffer);

			is.close();

			json = new JSONObject(new String(buffer, "UTF-8"));

		} catch (JSONException e) {
			Log.e("LOADJSON", e.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
			Log.e("LOADJSON", ex.toString());
			return null;
		}
		return json;
	}

	public static File downloadFile(String urlStr, String destFolder) {
		File f = null;
		try {
			URL url = new URL(urlStr);
			InputStream input = url.openStream();
			FileOutputStream fos = null;
			String localFile = null;
			// Get only file name
			StringTokenizer st = new StringTokenizer(url.getFile(), "/");

			while (st.hasMoreTokens()) {
				localFile = st.nextToken();
			}

			String fullPath = destFolder + "/" + localFile;
			fos = new FileOutputStream(fullPath);

			int oneChar = 0;
			while ((oneChar = input.read()) != -1) {
				fos.write(oneChar);
			}
			input.close();
			fos.close();

			f = new File(fullPath);
		} catch (IOException ex) {
			Log.e("Error: ", ex.toString());
			return null;
		}
		return f;
	}

	public static boolean copyFile(String src, String dst){
		boolean success = false;
		RandomAccessFile s = null;
		RandomAccessFile d = null;
		FileChannel inChannel = null;
	    FileChannel outChannel = null;
	    try {
	    	File tmpDst = new File(dst + "/" + new File(src).getName());
	    	
	    	if(!tmpDst.exists()){
	    		tmpDst.createNewFile();
	    	}
	    	
	    	s = new RandomAccessFile(src, "rw");
			d = new RandomAccessFile(tmpDst, "rw");
		    inChannel = s.getChannel();
		    outChannel = d.getChannel();
	        inChannel.transferTo(0, inChannel.size(), outChannel);
	        success = true;
	    }catch(IOException ex){
	    	Log.e("COPY_FILE", "Error: "+ex.toString());
	    }finally {
	    	try{
	        if (inChannel != null){
	            inChannel.close();
	        }
	        if (outChannel != null){
	            outChannel.close();
	        }
	        if (s != null){
	            s.close();
	        }
	        if (d != null){
	            d.close();
	        }
	    	}catch(Exception e){
	    		Log.e("COPY_FILE", "Error while closing streams: "+e.toString());
	    	}
	    }
	    return success;
	}
	
	public static String getPath(Uri uri, Activity a) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = a.managedQuery(uri, projection, null, null, null);
		a.startManagingCursor(cursor);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
		}
	
	public static Boolean uploadFile(String fileName, String destURLStr) {
		FileInputStream fileInputStream = null;
		String exsistingFileName = fileName;
		URL connectURL = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		String Tag = "3rd";
		Boolean result = false;
		HttpURLConnection conn = null;
		try {
			connectURL = new URL(destURLStr);
			fileInputStream = new FileInputStream(fileName);
			// ------------------ CLIENT REQUEST

			Log.e(Tag, "Starting to bad things");
			// Open a HTTP connection to the URL

			conn = (HttpURLConnection) connectURL.openConnection();

			// Allow Inputs
			conn.setDoInput(true);

			// Allow Outputs
			conn.setDoOutput(true);

			// Don't use a cached copy.
			conn.setUseCaches(false);

			// Use a post method.
			conn.setRequestMethod("POST");

			conn.setRequestProperty("Connection", "Keep-Alive");

			conn.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);
			
			conn.setRequestProperty("Image", "hello world!");
			
			DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
					+ exsistingFileName + "\"" + lineEnd);
			dos.writeBytes(lineEnd);

			Log.e(Tag, "Headers are written" );

			// create a buffer of maximum size

			int bytesAvailable = fileInputStream.available();
			int maxBufferSize = 1024;
			int bufferSize = Math.min(bytesAvailable, maxBufferSize);
			byte[] buffer = new byte[bufferSize];

			// read file and write it into form...

			int bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}

			// send multipart form data necesssary after file data...

			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// close streams
			Log.e(Tag, "File is written");
			fileInputStream.close();
			dos.flush();
			result = true;

			InputStream is = conn.getInputStream();
			// retrieve the response from server
			int ch;

			StringBuffer b = new StringBuffer();
			Log.e(Tag, "Getting response");
			while ((ch = is.read()) != -1) {
				b.append((char) ch);
			}
			String s = b.toString();
			Log.i("Response", s);
			dos.close();
			

		} catch (MalformedURLException ex) {
			Log.e(Tag, "error: " + ex.getMessage(), ex);
		}catch (IOException ioe) {
			Log.e(Tag, "error: " + ioe.getMessage(), ioe);
		}finally{
			if(conn != null){
				conn.disconnect();
			}
		}

		return result;

	}

	public class UploadFileTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			Boolean result = uploadFile(params[0], params[1]);
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			Log.d("UPLOAD_RESULT", ""+result);
		}

	}

	public static Bitmap getBitmap(String pathStr) {
		InputStream istr = null;
		Bitmap bitmap = null;
		try {
			Logger.getLogger("getBitmapFromAsset").info(
					"getting image: " + pathStr);
			istr = new FileInputStream(pathStr);
			bitmap = BitmapFactory.decodeStream(istr);
		} catch (IOException e) {
			e.printStackTrace();
			Logger.getLogger("getBitmapFromAsset").info("ERROR: " + e);
		}
		return bitmap;
	}

	class ImageDownloaderTask extends AsyncTask<String, Void, File> {
		private final WeakReference imageViewReference;

		public ImageDownloaderTask(ImageView imageView) {
			imageViewReference = new WeakReference(imageView);
		}

		@Override
		// Actual download method, run in the task thread
		protected File doInBackground(String... params) {
			// params comes from the execute() call: params[0] is the url.
			return downloadFile(params[0], params[1]);
		}

		@Override
		// Once the image is downloaded, associates it to the imageView
		protected void onPostExecute(File localFile) {
			if (isCancelled()) {
				localFile = null;
			}

			if (imageViewReference != null) {
				ImageView imageView = (ImageView) imageViewReference.get();
				if (imageView != null) {

					if (localFile != null && localFile.exists()) {
						imageView.setImageBitmap(Utils.getBitmap(localFile
								.getAbsolutePath()));
					} else {
						imageView.setImageDrawable(imageView.getContext()
								.getResources()
								.getDrawable(R.drawable.no_image));
					}
				}

			}
		}

	}

	static Bitmap downloadBitmap(String url) {
		final AndroidHttpClient client = AndroidHttpClient
				.newInstance("Android");
		final HttpGet getRequest = new HttpGet(url);
		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.w("ImageDownloader", "Error " + statusCode
						+ " while retrieving bitmap from " + url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					final Bitmap bitmap = BitmapFactory
							.decodeStream(inputStream);
					return bitmap;
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (Exception e) {
			// Could provide a more explicit error message for IOException or
			// IllegalStateException
			getRequest.abort();
			Log.w("ImageDownloader", "Error while retrieving bitmap from "
					+ url);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return null;
	}
	
	
}
