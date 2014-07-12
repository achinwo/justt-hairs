package com.justthairs;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

import android.os.AsyncTask;
import android.util.Log;

public class FileDownloadTask extends AsyncTask<String, Void, File> {
	private final WeakReference<Observer> observer;
 
	public FileDownloadTask(Observer observer) {
		this.observer = new WeakReference<Observer>(observer);
	}
 
	@Override
	protected File doInBackground(String... params) {
		return Utils.downloadFile(params[0], params[1]);
	}
 
	@Override
	protected void onPostExecute(File img) {
		if (isCancelled()) {
			img = null;
		}
		
		Observer o = observer.get();
		if(o != null){
			
			ResultContainer container = new ResultContainer(img);
			
			o.update(container, Consts.Update.JSON_DOWNLOADED);
		}else{
			Log.d("onPostExecute", "missing observer object");
		}
	}
	
	public class ResultContainer extends Observable{
		protected Object value;
		
		public ResultContainer(Object value) {
			this.value = value;
		}
		
	}
 
}
