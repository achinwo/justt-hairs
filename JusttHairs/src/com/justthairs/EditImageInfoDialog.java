package com.justthairs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class EditImageInfoDialog extends DialogFragment implements DialogInterface.OnClickListener{

	private AlertDialog altDialog;
	private ImageWrapper imgWrapper;
	
	public void setImgWrapper(ImageWrapper imgWrapper) {
		this.imgWrapper = imgWrapper;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        if(imgWrapper == null){
        	imgWrapper = new ImageWrapper("Untitled", null, "", 0.0);
        }
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_image_info, null);
        EditText titleTV = (EditText) view.findViewById(R.id.edit_dialog_title_edittext);
        EditText descTV = (EditText) view.findViewById(R.id.edit_dialog_desc_edittext);
        EditText priceTV = (EditText) view.findViewById(R.id.edit_dialog_price_edittext);
        
        titleTV.setText(imgWrapper.getTitle());
        descTV.setText(imgWrapper.description);
        priceTV.setText(String.format("%.2f", imgWrapper.getPrice()));
        
        //view.on
        builder.setTitle(imgWrapper.getTitle())
        	   .setView(view)
               .setPositiveButton("Ok", this)
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        altDialog = builder.create();
        Log.d("ACTION", "Created dialog!" + altDialog);
        // Create the AlertDialog object and return it
        return altDialog;
    }
	
	public AlertDialog getAlertDialog(){
		return altDialog;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if(which == DialogInterface.BUTTON_POSITIVE){
			EditText titleTV = (EditText) getAlertDialog().findViewById(R.id.edit_dialog_title_edittext);
	        EditText descTV = (EditText) getAlertDialog().findViewById(R.id.edit_dialog_desc_edittext);
	        EditText priceTV = (EditText) getAlertDialog().findViewById(R.id.edit_dialog_price_edittext);
	        
	        String title = titleTV.getText().toString();
	        double price = Double.parseDouble(priceTV.getText().toString());
	        
	        imgWrapper.setDescription(descTV.getText().toString());
	        
	        if(imgWrapper.getTitle() != title){
	        	imgWrapper.setTitle(title);
	        }
	        
	        if(imgWrapper.getPrice() != price){
	        	imgWrapper.setPrice(price);
	        }
		}
	}

	

}
