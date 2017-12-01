package com.example.lenovo.storeimageinsdcardtest;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button buttonLoadImage, buttonSaveImage, buttonScreenShot;
    Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        buttonLoadImage = findViewById(R.id.buttonLoadImage);


        buttonSaveImage = findViewById(R.id.buttonSaveImage);


        //first load the image
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageChooser();
            }
        });

        //second save the image
        buttonSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              /*  File file = getImage("/myAppDir/myImages/myimage1.png");
                String path = file.getAbsolutePath();
                Bitmap picture = BitmapFactory.decodeFile(path);
                imageView.setImageBitmap(picture);
                Log.d("aa", "MainActivity: " + "onClick: " + "[view]: " + "path: " + path);*/
                if (bitmap != null) {
                    Log.d("MYLOG", "MainActivity: " + "onClick: " + "bitmap: " + bitmap);
                    try {
                        Uri savedImageInInternalStorage = saveImageInInternalStorage(getApplicationContext(), bitmap, "abcdef.png");
                        Log.d("MYLOG", "MainActivity: " + "onClick: " + "saveImageInInternalStorage: " + savedImageInInternalStorage);
                        imageView.setImageURI(savedImageInInternalStorage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "bitmap is null", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //taking screenshots
        buttonScreenShot = findViewById(R.id.buttonScreenShot);
        buttonScreenShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("MYLOG", "MainActivity: "+"onClick: "+"getScreenShot: "+getScreenShot());
                imageView.setImageBitmap(getScreenShot());
            }
        });
    }

    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 111);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == 111) {
                Uri selectedImageUri = data.getData();
                String pathOfOriginalLocationImage = selectedImageUri.getPath();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(bitmap);
                // String imageSavedPath = saveImageInExternalStorage(bitmap, "myimage2.png");
                // Log.d("aa", "MainActivity: " + "onActivityResult: " + "[requestCode, resultCode, data]: " + "status: " + imageSavedPath);
            }
        }
    }



    public static File getImage(String imagename) {

        File mediaImage = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            mediaImage = new File(myDir.getPath() + imagename);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mediaImage;
    }

    public static Bitmap getBitmap(String filePath, String imageName) {
        File sd = Environment.getExternalStorageDirectory();
        File image = new File(sd + filePath, imageName);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
        bitmap = Bitmap.createBitmap(bitmap, 1, 1, 10, 10);
        return bitmap;
    }

    private void takeScreenshot() {
        Log.d("MYLOG", "MainActivity: " + "takeScreenshot: " + "takeScreenshot: ");
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        Log.d("MYLOG", "MainActivity: " + "openScreenshot: " + "uri: " + uri);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    public Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }


    //ALL FINAL METHODS

    /**
     * save image in internal storage and return the imageuri
     *
     * @param context
     * @param bitmap
     * @param fileName
     * @return uri
     */
    public static Uri saveImageInInternalStorage(Context context, Bitmap bitmap, String fileName) throws IOException {


        ContextWrapper wrapper = new ContextWrapper(context);
        File file = wrapper.getDir("Images", MODE_PRIVATE);
        file = new File(file, fileName);
        OutputStream stream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        stream.flush();
        stream.close();

        // Parse the gallery image url to uri
        Uri savedImageURI = Uri.parse(file.getAbsolutePath());


        return savedImageURI;
    }

    /**
     * store the image in bitmap
     *
     * @param bitmap
     * @param filename
     * @return stored image path
     */
    public static String saveImageInExternalStorage(Bitmap bitmap, String filename) {

//        String stored = null;
//
//        File sdcard = Environment.getExternalStorageDirectory();
//        File file = new File(sdcard, filename + ".png");
//
//        if (file.exists())
//            file.delete();
//
//        try {
//            FileOutputStream out = new FileOutputStream(file);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
//            out.flush();
//            out.close();
//            stored = "success";
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return stored;


        //get path to external storage (SD card)
        // String iconsStoragePath = Environment.getExternalStorageDirectory() + "/myAppDir/myImages/";
        // File sdIconStorageDir = new File(iconsStoragePath);

        File sdIconStorageDir = new File(Environment.getExternalStorageDirectory() + "/myAppDir/myImages/");

        //create storage directories, if they don't exist
        sdIconStorageDir.mkdirs();
        String pathOfStoredImage = "error";
        try {
            Log.d("aa", "MainActivity: " + "storeImage: " + "[bitmap, filename]: " + "value: " + sdIconStorageDir.toString());
            pathOfStoredImage = sdIconStorageDir + "/" + filename;
            //Log.d("aa", "MainActivity: " + "storeImage: " + "[bitmap, filename]: " + "filePath: " + pathOfStoredImage);
            FileOutputStream fileOutputStream = new FileOutputStream(pathOfStoredImage);

            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

            //choose another format if PNG doesn't suit you
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);

            bos.flush();
            bos.close();

        } catch (FileNotFoundException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            return "error";
        } catch (IOException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            return "error";
        }

        return pathOfStoredImage;
    }

    /**
     * this is a method to get the screenshot bitmap image
     * @return Bitmap
     */
    public Bitmap getScreenShot() {
        View v1 = getWindow().getDecorView().getRootView();
        v1.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);
        return bitmap;
    }
}
