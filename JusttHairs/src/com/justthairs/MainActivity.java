package com.justthairs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity implements Observer, OnMenuItemClickListener {
	
	public static final String JSON_FILE_NAME = "data.json";
	private ImageView selectedImage;
	int selectedImageIndex;
	protected Drawable noImage;
	protected JSONObject json;
	protected ArrayList<ImageWrapper> images;

	@Override
	public void update(Observable arg0, Object arg1) {
		final Consts.Update what = (Consts.Update) arg1;
		Log.d("OBSERVABLE_UPDATE", what.toString());
		ImageWrapper wrapper = null;
		TextView tv = null;
		
		switch (what) {
			case IMAGE_CHANGED:
				wrapper = (ImageWrapper) arg0;
				Log.i(what.name(), wrapper.toString());
				break;
				
			case JSON_DOWNLOADED:
				FileDownloadTask.ResultContainer res = (FileDownloadTask.ResultContainer) arg0;
				File json = (File) res.value;
				Log.i(what.name(), json.getAbsolutePath());
				JSONObject js = Utils.loadJSON(json.getAbsolutePath());
				setJson(js);
				images.addAll(ImageWrapper.getImageWrappers(js, this));
				
				for(ImageWrapper w: images){
					w.setRoot(getExternalCacheDir().getAbsolutePath());
				}
				
				Gallery gallery;
				//gallery.setAdapter(gallery.getAdapter());
				break;
			
			case PRICE_CHANGED:
				wrapper = (ImageWrapper) arg0;
				tv = (TextView) findViewById(R.id.main_view_price);
				tv.setText(""+wrapper.getPrice());
				break;
			
			case IMG_TITLE_CHANGED:
				wrapper = (ImageWrapper) arg0;
				tv = (TextView) findViewById(R.id.main_view_title);
				tv.setText(wrapper.getTitle());
				break;
				
			default:
				break;
		}

	}
	
	public void uploadJson(String fileName){
		Utils.UploadFileTask task = new Utils().new UploadFileTask();
		String[] args = new String[] {fileName, ImageWrapper.URL_BASE_PATH + "upload.php"};
		task.execute(args);
	}
	
	public void setJson(JSONObject json) {
		this.json = json;
		Log.d("JSON_UPDATED", json.toString());
	}

	public void setSelectedImageIndex(int index) {
		if(selectedImageIndex < images.size()){
			selectedImageIndex = index;
		}
		
		if(!images.isEmpty()){
			ImageWrapper imageWrapper = images.get(selectedImageIndex);
			Bitmap b = imageWrapper.getImgBitmap();
			selectedImage.setImageBitmap(b);
			Log.d("WRAPPER_BITMAP", ""+b);
			TextView titleTV = (TextView) findViewById(R.id.main_view_title);
			titleTV.setText(imageWrapper.getTitle());
		}else{
			selectedImage.setImageDrawable(noImage);
		}
	}
	
	public ImageWrapper getSelectedImagWrapper(){
		return images.get(selectedImageIndex);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.d(this.getClass().getSimpleName(), "created the thing!!");

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		noImage = getResources().getDrawable(R.drawable.no_image);
		images = new ArrayList<ImageWrapper>();
		
		File externalCache = getExternalCacheDir();
		
		for(File file: externalCache.listFiles()){
			Log.d("CLEAR_CACHE", "deleted "+ file.getAbsolutePath());
			file.delete();
		}
		
		File jsonFile = new File(externalCache.getAbsolutePath() +"/"+JSON_FILE_NAME);
		
		if(!jsonFile.exists()){
			// attempt to load from endigo website
			FileDownloadTask task = new FileDownloadTask(this);
			
			String[] args = new String[] {ImageWrapper.URL_BASE_PATH + JSON_FILE_NAME, externalCache.getAbsolutePath()};
		    task.execute(args);
		    
			Log.d("JSON_DOWNLOAD", "Result ");
		}
		
		//images = ImageWrapper.getImageWrappers(json, getAssets(), this);
		Log.d("Images", "" + images.toString() +" External storage: ");
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i("MAIN_ACTIVITY", "Resumming...");
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				MainActivity.this.setSelectedImageIndex(MainActivity.this.selectedImageIndex);
				//MainActivity.this.getGallery().setSelection(MainActivity.this.selectedImageIndex, true);
			}
		}, 500);
		
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i("MAIN_ACTIVITY", "Starting...");
		
		if(selectedImage == null){
			
		LinearLayout gallery = getGallery();
		selectedImage = (ImageView) findViewById(R.id.main_view_image);
		selectedImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
		
		selectedImage.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				MainActivity.this.onEditButtonClick(v);
				return false;
			}
		});
		//check storage dir for data.json
		//if not present, download from endigo
		//load imageWrappers
		
		setSelectedImageIndex(0);
		}else{
			Log.i("MAIN_ACTIVITY", "Start aborted!" );
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onEditButtonClick(View obj) {
		EditImageInfoDialog dialog = new EditImageInfoDialog();
		dialog.setImgWrapper(getSelectedImagWrapper());
		
		dialog.show(this.getFragmentManager(), "NoticeDialogFragment");
		Log.d("ACTION", "Dialog " + dialog.getAlertDialog());
		
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		LoadImagesDialog dialog = new LoadImagesDialog();
		dialog.show(this.getFragmentManager(), "LoadImage "+item);
		return false;
	}

	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
		Log.d("ACTIVITY_RESULT", "HIT: " + requestCode +" Result"+ resultCode);
		switch(requestCode) {
			case 0:
				if(resultCode == Activity.RESULT_OK){ 
					Uri selectedImage = imageReturnedIntent.getData();
					Log.d("RESULT", "camera: " + selectedImage.getPath());
					//imageview.setImageURI(selectedImage);
				}
	
				break; 
			case 1:
				if(resultCode == Activity.RESULT_OK){  
					Uri selectedImage = imageReturnedIntent.getData();
					
					Log.d("RESULT", "gallery: " + selectedImage.getPath());
					//imageview.setImageURI(selectedImage);
					String imgPath = Utils.getPath(selectedImage, this);
					Utils.copyFile(imgPath, getExternalCacheDir().getAbsolutePath());
					File newImg = new File(getExternalCacheDir().getAbsolutePath() + "/" + new File(imgPath).getName());
					if(newImg.exists()){
						Log.d("GET_IMG_FROM_GALLERY", "Successfully moved to cache: "+newImg.getAbsolutePath());
						final ImageWrapper wrapper = new ImageWrapper("Untitled", newImg.getName(), "", 0.0);
						wrapper.setRoot(getExternalCacheDir().getAbsolutePath());
						
						Handler handler = new Handler();
						handler.postDelayed(new Runnable() {

						    @Override
						    public void run() {
						        addImageWrapper(wrapper);
						    }

						}, 500);
						
					}else{
						Log.d("GET_IMG_FROM_GALLERY", "Failed to move "+newImg.getAbsolutePath());
					}
				}
				break;
		}
		
	}

	public LinearLayout getGallery(){
		return (LinearLayout) findViewById(R.id.main_view_gallery_layout);
	}
	
	public void addImageWrapper(ImageWrapper imgW){
		imgW.addObserver(this);
		images.add(imgW);
		
		int position = images.size() - 1;
		getGallery();
		setSelectedImageIndex(position);
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}

	}
	

	public class GalleryImageAdapter extends BaseAdapter {
		
		private MainActivity mContext;

		public GalleryImageAdapter(MainActivity context) {
			mContext = context;
		}

		public int getCount() {
			int size = mContext.images.size();
			if(size > 0){
				return size;
			}else{
				return 1;
			}
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int index, View view, ViewGroup viewGroup) {
			Log.d("GET_VIEW", "v:"+view+" vg:"+viewGroup+" index:"+index);
			ImageView i = new ImageView(mContext);
			
			Bitmap img;
			if(mContext.images.isEmpty()){
				img = BitmapFactory.decodeResource(getResources(), R.drawable.no_image);
			}else{
				ImageWrapper wrapper = mContext.images.get(index);
				
				img = wrapper.getImgBitmap();
				
				if(img == null){
					Utils.ImageDownloaderTask task = new Utils().new ImageDownloaderTask(i);
					
					String[] args = new String[]{wrapper.getUrl(), getExternalCacheDir().getAbsolutePath()};
					task.execute(args);
					Log.d("STARTED_IMG_LOADER", ""+args);
				}
				
			}
			
			i.setImageBitmap(img);
			
			i.setLayoutParams(new Gallery.LayoutParams(200, 200));
			i.setScaleType(ImageView.ScaleType.CENTER_CROP);
			return i;
		}
	}


}
