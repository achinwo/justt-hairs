package com.justthairs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class LoadImagesDialog extends DialogFragment implements OnClickListener{

	public final static String TITLE = "Load Image";
	public final static CharSequence[] CHOICES = new CharSequence[]{"Take photo", "Choose from gallery"};

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(TITLE)
		.setItems(CHOICES, this)
		.setNegativeButton("Cancel", null);
		// Create the AlertDialog object and return it
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		Log.d("WHICH", "Clicked "+ CHOICES[which]);
		switch (which) {
		case 0:
			// Take photo
			Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			getActivity().startActivityForResult(takePicture, 0);//zero can be replaced with any action code
			break;

		case 1:
			// Load from gallery
			Intent pickPhoto = new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			getActivity().startActivityForResult(pickPhoto , 1);
			break;

		default:
			break;
		}

	}

	

}
