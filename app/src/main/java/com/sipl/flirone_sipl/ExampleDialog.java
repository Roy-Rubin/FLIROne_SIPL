package com.sipl.flirone_sipl;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.sipl.flirone_sipl.R;


public class ExampleDialog extends AppCompatDialogFragment {

    //var's
    private EditText editTextExtraInfo;
    private ExampleDialogListener listener;


    // we wrote this
    public interface ExampleDialogListener {
        // we wrote this too..
        // this will be overriden in the main activity
        void applyTexts(String info);      //todo: i wrote a different function instead of this function..


        void useTextToSaveFile(String info);


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (ExampleDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement exampleDialogListener");
            //e.printStackTrace();
        }

    }

    private int choice = 0;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //
        //android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //
        LayoutInflater inflater = null;
        try {
            inflater = getActivity().getLayoutInflater();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //
        View view = inflater.inflate(R.layout.layout_dialog2, null);  //todo !? <--- idk what this line does... but this is the first place in which we assign layout_dialog to the view
                                                                            //todo: note this possible exception is not yet catched....
                                                                            //ALSO NOTE: i changed the layout to 'layout2' - it has les stuff in it now

        //* testing:   */  //todo: note <---

//        byte[] byteArray = getArgument().getByteArrayExtra("image");
//        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);


//        Bitmap bitmapimage = getIntent().getExtras().getParcelable("BitmapImage");



//        Bitmap bitmapimage = savedInstanceState.getParcelable("BitmapImage");

//todo: take this block comment off, trying recieving the data  differently
/*
        Bitmap bitmapimage = null;
        Bundle dialog_bundle = this.getArguments();
        if (dialog_bundle != null) {
            bitmapimage = dialog_bundle.getParcelable("BitmapImage");
        }

        //
        if (bitmapimage != null) {
            final ImageView dialog_imageView = getActivity().findViewById(R.id.dialogImageView); //todo: this is patched. MIGHT NOT WORK. and might cause exception
            dialog_imageView.setImageBitmap(bitmapimage); //todo: note this important line <----
        }
*/

        //* end of testing   */ //todo: note <---


        //this important var is declared outside of this overriden method.
        // it will now be assigned with the "text edditing view" and later in the method we will extract the text from it
        editTextExtraInfo = view.findViewById(R.id.edit_filename);  //todo !? <---


        builder.setView(view)
                .setTitle(R.string.DialogTitleMsg)

//                .setItems(R.array.pictureable_strings, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // The 'which' argument contains the index position
//                        // of the selected item
//                    }
//                })

                .setSingleChoiceItems(R.array.pictureable_strings, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item

                        choice = which;

                    }
                })

                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //we will be leaving this empty because this is where the dialog ends
                    }
                })
                .setPositiveButton("Keep", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //get data that the user entered
                        String info = "i init'd this !";
//                        info = editTextExtraInfo.getText().toString();

                        // send the info to our activity;
//                        listener.applyTexts(info);

                        switch(choice) {
                            case 0:  //meaning cats
                                info = "Cats";
                                break;
                            case 1: // meaning people
                                info = "People";
                                break;
                            case 2:  // meaning cars
                                info = "Cars";
                                break;
                            case 3: //meaning other
                                //get data that the user entered
                                info = editTextExtraInfo.getText().toString();
                                break;
                        }


                        // todo: my testing
                        listener.useTextToSaveFile(info);





                    }
                });

        // this line is just so it wont yell at me
        choice = 0;

        return builder.create();  //NOTE !!! this is actually where the dialoug is created. outside of the dialog class, in the main activity, it will be shown with ".show"

    }



}
