package com.sipl.flirone_sipl;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.flir.flironesdk.*;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;




public class MainActivity extends AppCompatActivity implements Device.Delegate , FrameProcessor.Delegate , ExampleDialog.ExampleDialogListener {

    private static final String TAG = "FLIROne. [Roy's msg]";

    // Flir one class for frame processing
    FrameProcessor frameProcessor;
    //
    private Device.TuningState currentTuningState = Device.TuningState.Unknown;

    //stuff for the floating dialog
    // ?
    String extra_filename_info_from_user;  //todo: check if needed.


    // view variable for displaying the received frame.
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        imageView = (ImageView)findViewById(R.id.imageView);


        //todo: important:
        // How do I get the temperature out of the pixels?
        //In order to get the temperature of a pixel,
        // you’ll need to add ThermalRadiometricKelvinImage FrameType to the image processor.
        // Once you receive a RenderedFrame in this format, you can use the width and height supplied to find the value of a particular pixel in this array.
        // The values represent degrees Kelvin * 100. For example, the value 273.15ºK is represented by 27,315.

        //todo: only BlendedMSXRGBA8888Image realy works... the visualAligned sends back an image, but then for some strange reason, thermalRadiometric values are returned of a different size
        // FLIRONE part
                                    //note: previously the first arg was "BlendedMSXRGBA8888Image", and i switched it to ""
        frameProcessor = new FrameProcessor(this, this, EnumSet.of(RenderedImage.ImageType.VisibleAlignedRGBA8888Image, RenderedImage.ImageType.ThermalRadiometricKelvinImage));  //todo: change image type !!?!?!
        frameProcessor.setImagePalette(RenderedImage.Palette.Hottest);
        // note the "enum set" above. i should be able to change this to more than 1 type if i  want to produce more than 1 type of frame.
        // note: in order to receive different formats, use the setFrameTypes method with a Set of as many formats as you want.
        //Warning: you may not use the BlendedMSXRGBA8888Image and ThermalRGBA8888Image types concurrently.
        //Note: while not critical, it is recommended to use an EnumSet when calling setFrameTypes
    }

    @Override
    protected void onResume() {
        super.onResume();
        Device.startDiscovery(this, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Device.stopDiscovery();
    }

    ////////////////////

    Device flirDevice;


    @Override
    public void onDeviceConnected(Device device) {
        Log.d(TAG, "Entered onDeviceConnected");
        // assignment of device to variable.
        flirDevice = device;
        //
        device.startFrameStream(new Device.StreamDelegate() {
            // This delegate will handle the receiving of Frame
            // objects which represent the raw data received from the device on each frame
            @Override
            public void onFrameReceived(Frame frame) {
                // todo: "frame" is supposed to be the raw data !
                if (currentTuningState != Device.TuningState.InProgress){
                    frameProcessor.processFrame(frame, FrameProcessor.QueuingOption.CLEAR_QUEUED);
//                    thermalSurfaceView.requestRender();
                }


                //?
                //todo: i read this in one of the guides. havent used this yet:
                // Use the Frame method save to save the frame in FLIR’s radiometric JPEG format.
                // This allows the file to be opened by the SDK or other FLIR applications
                // and re-rendered with another palette or additional temperature analysis.
//                frame.save();


            }
        });
    }

    @Override
    public void onDeviceDisconnected(Device device) {
        //?
        device.stopFrameStream();
    }

    RenderedImage last_radiometric_image_rendered; //will be used later when we press the button
    Bitmap last_bitmap_from_rendered_image = null; //will be used later in the dialog screen //todo: delete this if not passing the data to the dialog screen.
    Bitmap last_bitmap_from_rendered_image_when_thermal_data_was_recieved = null; //will be used later in the dialog screen //NOTE: the purpose of this var, is for the students to recieve the closest match to the frame they need  to blend thermal data with msx image

    @Override
    public void onFrameProcessed(RenderedImage renderedImage) {

        if (renderedImage.imageType() == RenderedImage.ImageType.VisibleAlignedRGBA8888Image) {  //todo: might need to change the image type later.
            // then, display the image

            // create a bitmap from the data
            // note for this line [and the next ones] from video: "pixelData() is byte array of ARGB8888 for MSX"  - MSX is the blended thermal and non thermal image
            final Bitmap imageBitmap = Bitmap.createBitmap(renderedImage.width(), renderedImage.height(), Bitmap.Config.ARGB_8888);
            // note from video for the next lines: "use a byte buffer to copy pixel data, and remember to update the UI from the UI thread"
            imageBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(renderedImage.pixelData()));
            //final ImageView imageView = (ImageView)findViewById(R.id.imageView);  //todo: maybe this line isnt supposed to be here, but is supposed to be before "onCreate"????? im trying this now.

            final Bitmap imageBitmap2 = renderedImage.getBitmap();

            last_bitmap_from_rendered_image = imageBitmap2; //todo: check that this <-- doesnt cause a crash

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

//                imageView.setImageBitmap(imageBitmap); //todo: note this important line <----
                    imageView.setImageBitmap(imageBitmap2); //todo: note this important line <----


                }
            });


        } else if (renderedImage.imageType() == RenderedImage.ImageType.ThermalRadiometricKelvinImage) {

            //we'll save the button now so we can use it when the user presses the button.
            last_radiometric_image_rendered = renderedImage;                                                //i hope there's a CTOR to copy this //todo: do i need to use "new" here?
            last_bitmap_from_rendered_image_when_thermal_data_was_recieved = last_bitmap_from_rendered_image; //i hope there's a CTOR to copy this //todo: do i need to use "new" here?
        }


    }

    boolean tuning_state_equals_tuned_boolvar = false;

    @Override
    public void onTuningStateChanged(Device.TuningState tuningState) {
        Log.d(TAG, "Entered onTuningStateChanged");

        currentTuningState = tuningState;
        if (tuningState == Device.TuningState.InProgress){
            runOnUiThread(new Thread(){
                @Override
                public void run() {
                    super.run();
                    findViewById(R.id.tuningProgressBar).setVisibility(View.VISIBLE);
                    findViewById(R.id.tuningTextView).setVisibility(View.VISIBLE);
//                    tuning_state_equals_tuned_boolvar = false;
                }
            });
        }else {
            runOnUiThread(new Thread() {
                @Override
                public void run() {
                    super.run();
                    findViewById(R.id.tuningProgressBar).setVisibility(View.GONE);
                    findViewById(R.id.tuningTextView).setVisibility(View.GONE);
//                    if (currentTuningState != Device.TuningState.Tuned) {
//                        tuning_state_equals_tuned_boolvar = false;
//                    }
//                    if (currentTuningState == Device.TuningState.Tuned && tuning_state_equals_tuned_boolvar != true) {
//                        tuning_state_equals_tuned_boolvar = true;
//                        Toast.makeText(getApplicationContext(), "flirone device tuned !", Toast.LENGTH_SHORT).show();  //todo: NOTE this line MOIGHT cause a crash
//                    }
                }
            });
        }



    }

    @Override
    public void onAutomaticTuningChanged(boolean b) {}


    // note that i didnt use the "button.setOnClickListener ..... way of doing this -
    //i simply created a function as you see below, and added it as what is opened when you press the button in the xml file.
    public void buttonPressedFunction(View view) {
        Log.d(TAG, "Entered buttonPressedFunction");

        // dialog part
        openDialog_keep_or_discard();
        // NOTE: after this function. the var' extra_filename_info_from_user will have the additional info needed for the file name

        Log.d(TAG, "Breakpoint point");

    }



    /// floating dialog window part !!!! from now on ..... -------- ///

    public void openDialog_keep_or_discard() {
        ExampleDialog exampleDialog = new ExampleDialog();

        /*testing: pass bitmap to the dialog via a bundle*/ //todo: <--

 //todo: take this block comment off, trying passing the data through differently. maybe using a puByteArray
/*
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        last_bitmap_from_rendered_image.compress(Bitmap.CompressFormat.PNG, 100, stream);
//        byte[] byteArray = stream.toByteArray();
        Bundle bundle = new Bundle();
//        bundle.putByteArray("image",byteArray);
        bundle.putParcelable("BitmapImage",last_bitmap_from_rendered_image);
        exampleDialog.setArguments(bundle);

*/

        /* end of testing */ //todo: <--

        exampleDialog.show(getSupportFragmentManager(), "example dialog");  //todo: what does the 'tag' mean ?  //NOTE QUESTION: does "show" invoke "onDialogCreate" ?
    }


    @Override  //overriding this form our own ExampleDialog class  //todo: i wrote a different function instead of this function..
    public void applyTexts(String info) {
        extra_filename_info_from_user = info;
    }


    @Override
    public void useTextToSaveFile(String info) {
        // todo: CHECK  - entering this shit supposedly if after pressing "keep" in the dialog window

        extra_filename_info_from_user = info;


        // todo: NOTE: from here on out it is the function that i previously created, just copied to here.

        String textToWrite = null;
        try {  // exception may pop if no rendered image exists [meaning last_radiometric_image_rendered is null, or last_radiometric_image_rendered.thermalPixelValues() return null]
            textToWrite = Arrays.toString(last_radiometric_image_rendered.thermalPixelValues());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String height_string = Integer.toString(last_radiometric_image_rendered.height());
        String width_string = Integer.toString(last_radiometric_image_rendered.width());
        String extra_messege = "height is: " + height_string + " width is: " + width_string;

        Toast.makeText(getApplicationContext(), "saving img data: " + extra_messege, Toast.LENGTH_SHORT).show();  //todo: NOTE this line MOIGHT cause a crash

        //Checking the availability state of the External Storage.
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {

            //If it isn't mounted - we can't write into it.
            //todo: does this mean we need to return an error ?
            return;
        }

        // !! To avoid interfering with users existing pictures and videos, you should create a sub-directory for your application's media files within this directory !! //
        File myMediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "FLIROne_SIPL");  //todo: note the sub-folder's name
        // This location works best if you want the created files to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! myMediaStorageDir.exists()){
            if (! myMediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                // todo: needs to throw / write an error message..
            }
        }

        boolean temp_check_bool = true;
        //not sure if needed, but i'll add this anyway:
        if (myMediaStorageDir.setExecutable(true) == false) {
            // meaning this could not be done
            temp_check_bool = false;
        }
        if (myMediaStorageDir.setReadable(true) == false) {
            // meaning this could not be done
            temp_check_bool = false;
        }if (myMediaStorageDir.setWritable(true) == false) {
            // meaning this could not be done
            temp_check_bool = false;
        }
        if (temp_check_bool == false) {
            Toast.makeText(getApplicationContext(), "problem using dir. ", Toast.LENGTH_SHORT).show();  //todo: NOTE this line MOIGHT cause a crash
            Log.d(TAG, "problem using dir. ");
            return;
        }
        // Create a media file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        // RCKD = Rendered Centi-Kelvin Data
        File textFile = new File(myMediaStorageDir.getPath() + File.separator + "RCKD_"+ extra_filename_info_from_user + "_" + timeStamp + ".txt");

        String full_file_name_w_location_string = textFile.getAbsolutePath();

        FileOutputStream outputStream = null;
        try {
            //second argument of FileOutputStream constructor indicates whether
            //to append or create new file if one exists
            outputStream = new FileOutputStream(full_file_name_w_location_string, true); //todo: might need to pass the file "textFile" instead

            outputStream.write(textToWrite.getBytes()); //todo: check - do i need to change to bytes ?
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // todo: later on, check wich media scanned actually works, and delete the rest
        MediaScannerConnection.scanFile(this, new String[] {full_file_name_w_location_string}, null, null);
        MediaScannerConnection.scanFile(this, new String[] {myMediaStorageDir.getPath()}, null, null);
        MediaScannerConnection.scanFile(this, new String[] {myMediaStorageDir.toString()}, null, null);
        MediaScannerConnection.scanFile(this, new String[] {textFile.toString()}, null, null);



        Toast.makeText(getApplicationContext(), "final file name: " + "RCKD_"+ extra_filename_info_from_user + "_" + timeStamp + ".txt", Toast.LENGTH_SHORT).show();  //todo: NOTE this line MOIGHT cause a crash
        Log.d(TAG, "Saved ! final file name: " + "RCKD_"+ extra_filename_info_from_user + "_" + timeStamp + ".txt");

        Log.d(TAG, "Breakpoint point");

        // --------------- new part: trying to save the second image !!!------------------------------

        // note this has the same time stamp and extra filename info from user from before - to have a correlation with the RCKD file
        File image_file_path = new File(myMediaStorageDir.getPath() + File.separator + "img_"+ extra_filename_info_from_user + "_" + timeStamp + ".png"); // todo: not sure about the file type extension


        FileOutputStream outputStream2 = null;
        try {
            //second argument of FileOutputStream constructor indicates whether
            //to append or create new file if one exists
            outputStream2 = new FileOutputStream(image_file_path.getAbsolutePath());
            last_bitmap_from_rendered_image_when_thermal_data_was_recieved.compress(Bitmap.CompressFormat.PNG, 100, outputStream2);
            // PNG is a lossless format, the compression factor (100) is ignored
            outputStream2.flush();
            outputStream2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // todo: later on, check wich media scanned actually works, and delete the rest
        MediaScannerConnection.scanFile(this, new String[] {image_file_path.getAbsolutePath()}, null, null);
        MediaScannerConnection.scanFile(this, new String[] {image_file_path.getAbsolutePath().toString()}, null, null);

        Log.d(TAG, "Saved ! final file name: " + "img_"+ extra_filename_info_from_user + "_" + timeStamp + ".png");

        Log.d(TAG, "Breakpoint point");
    }


}

/*
NOTES:
 ...You can also use a USB cable, much like your users will use.
 However, bear in mind that what is presented to the USB interface is not what is on external storage…
 but, instead, is what has been indexed on external storage in the MediaStore.
 Hence, unless you take steps to ensure that new files that you create get indexed, they may not be immediately visible.

the best answer is for you to use scanFile() on MediaScannerConnection to update the media database after you close your file. This will make your file immediately available to the user.

 */
