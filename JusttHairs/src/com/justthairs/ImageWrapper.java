package com.justthairs;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.graphics.Bitmap;
import android.util.Log;


public class ImageWrapper extends Observable{

    public static final String DEFAULT_IMAGE = "no_image.png";
    public static final String URL_BASE_PATH = "http://endigosoftware.com/justt_hairs/";
    protected String fileName, description, title, root;
    protected double price;
    protected Bitmap imgBitmap;

    public ImageWrapper(String title, String fileName, String desc, double price) {
        this.fileName = fileName;
        this.description = desc;
        this.price = price;
        this.title = title;
        imgBitmap = null;
        root = "";
    }
    
    public void setRoot(String root) {
		this.root = root;
	}
    
    public String getUrl(){
    	return URL_BASE_PATH + getFileName();
    }
    
    public ImageWrapper() {
        fileName = ImageWrapper.DEFAULT_IMAGE;
        price = 0.0;
        description = "Please enter image description";
        imgBitmap = null;
        title = fileName;
    }
    
    public String getFileName() {
    	return getFile().getName();
    }
    
    public File getFile(){
    	String path = null;
		if(fileName == null){
			path = ImageWrapper.DEFAULT_IMAGE;
		}else{
			path = fileName;
		}
		return new File(root + "/" + path);
    }
    
	public Bitmap getImgBitmap() {
		if(imgBitmap == null && getFile().exists()){
			imgBitmap = Utils.getBitmap(getFile().getAbsolutePath());
		}
		
		return imgBitmap;
	}
	
	public void refresh(){
		imgBitmap = null;
		this.setChanged();
		this.notifyObservers(Consts.Update.IMAGE_CHANGED);
	}
	
	public double getPrice() {
		return price;
	}
	
	public void setPrice(double price){
		this.price = price;
		this.setChanged();
		this.notifyObservers(Consts.Update.PRICE_CHANGED);
	}
	
	public void setTitle(String title) {
		this.title = title;
		this.setChanged();
		this.notifyObservers(Consts.Update.IMG_TITLE_CHANGED);
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setDescription(String desc){
		this.description = desc;
		this.setChanged();
		this.notifyObservers(Consts.Update.DESC_CHANGED);
	}

    public static ArrayList<ImageWrapper> getImageWrappers(JSONObject json, Observer obsvr) {
        ArrayList<ImageWrapper> formList = new ArrayList<ImageWrapper>();
        try {
            JSONArray m_jArry = json.getJSONArray("images");

            for (int i = 0; i < m_jArry.length(); i++) {
                JSONObject jo_inside = m_jArry.getJSONObject(i);

                String desc = jo_inside.getString("description");
                double p = jo_inside.getDouble("price");
                String fn = jo_inside.getString("fileName");
                String title = jo_inside.getString("title");
                
                ImageWrapper imgWrapper = new ImageWrapper(title, fn, desc, p);
                
                if(obsvr != null){
                	imgWrapper.addObserver(obsvr);
                }
                
                formList.add(imgWrapper);
            }
            
        } catch (JSONException ex) {
            Log.e("Error!!: ", ex.toString());
            
        }
        return formList;
    }

    public void allFilesInDir() {
        // Directory path here
        String path = ".";

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for (File f : listOfFiles) {

            if (f.isFile()) {
                System.out.println("" + f.getName());
            }
        }
    }

    public String toString() {
        String s = "{\"fileName\":\"%s\", \"price\":%.2f, \"description\":\"%s\"}";
        return String.format(s, getFileName(), price, description);
    }
	
}
