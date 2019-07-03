package com.example.textrecogniser;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    Button processButton;
    EditText processedTextView;
    Bitmap bitmap;
    Uri resultUri;
    public static final int PERMISSION_CODE = 1111;
    public static final String TAG = "MainActivity";
    String processedText;
    MyView mv;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, new OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
//                    tts.setLanguage(Locale.ENGLISH);


                }

            }
        });

        imageView = findViewById(R.id.imageView);
        processButton = findViewById(R.id.processImage);
        processedTextView = findViewById(R.id.processedTextView);

        mv = new MyView(this);
        mv.setDrawingCacheEnabled(true);

        String[] PERMISSIONS = {
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED))) {
            requestPermissions(PERMISSIONS, PERMISSION_CODE);
        }

        processButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTextRecognition();
            }

        });


    }

    public void startTextRecognition() {
        if (bitmap != null) {
            FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();

            processButton.setEnabled(false);
            Task<FirebaseVisionText> result =
                    detector.processImage(firebaseVisionImage)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    processButton.setEnabled(true);
                                    processTextRecognitionResult(firebaseVisionText);
                                    processedTextView.setText(processedText + "");
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            e.getMessage();
                                            e.getCause();
                                            e.getStackTrace();
                                        }
                                    });
        } else {
            Toast.makeText(MainActivity.this, "Please select an image first", Toast.LENGTH_SHORT).show();
        }

    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        processedText = "";
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(this, "No Text Found", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            processedText += "\n";
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    Log.d(TAG, "processTextRecognitionResult: " + elements.get(k).getText());
                    processedText += elements.get(k).getText() + " ";
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            permissions[i])) {
                        //telling the user the importance of this permission
                        AlertDialog.Builder alertdialog = new AlertDialog.Builder(this);
                        alertdialog.setTitle("Important Permission required")
                                .setMessage("Permissions are required to capture the images and store them");
                        alertdialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        permissions, PERMISSION_CODE);
                            }
                        });
                        alertdialog.setNegativeButton("NO THANKS", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(MainActivity.this, "NO THANKS button is clicked"
                                        , Toast.LENGTH_SHORT).show();

                            }
                        });
                        alertdialog.show();
                    } else {
                        Toast.makeText(this, "Checked never show again and denied permission"
                                , Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.camera) {
//            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//            startActivityForResult(cameraIntent, CAMERA_REQUEST);


// start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
//        if (item.getItemId() == R.id.gallery) {
//
////            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
////            startActivityForResult(Intent.createChooser(galleryIntent, "Select Image from"), GALLERY_REQUEST);
//            CropImage.activity(imageUri)
//                    .start(this);
//        }
        if (item.getItemId() == R.id.pdf) {
            SaveAsPdf();
        }
        if (item.getItemId() == R.id.photo) {
            SaveAsPng();
        }
        if (item.getItemId() == R.id.tts) {
            ConvertTTS(processedText);
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
//            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
//                postImageUri = data.getData();
//                Log.d(TAG, "onActivityResult1: postImageUri " + postImageUri);
////                imageView.setImageURI(postImageUri);
//                CropImage();
//
//            } else {
//
//                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                String path = MediaStore.Images.Media.insertImage(getApplication().getContentResolver(), bitmap, "Title", null);
//                postImageUri = Uri.parse(path);
//                Log.d(TAG, "onActivityResult2: postImageUri " + postImageUri);
////                imageView.setImageURI(postImageUri);
//                CropImage();
//            }
//        }
//        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
//            postImageUri = data.getData();
//            Log.d(TAG, "onActivityResult3: postImageUri " + postImageUri);
////            imageView.setImageURI(postImageUri);
//            CropImage();
//        }
//        if (requestCode == CROP_PIC_REQUEST_CODE && requestCode == RESULT_OK) {
//            Bundle bundle = data.getExtras();
//            Bitmap bitmap = bundle.getParcelable("data");
//
//            imageView.setImageBitmap(bitmap);
//        }
//    }

    public void ConvertTTS(String text) {

        tts.setLanguage(new Locale("en-IN"));

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void SaveAsPdf() {
        android.support.v7.app.AlertDialog.Builder editalert = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        editalert.setTitle("Please Enter the name with which you want to Save");
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        editalert.setView(input);
        editalert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String name = input.getText().toString();
                Log.d(TAG, "onClick: name" + name);


                String finalText = processedTextView.getText().toString().trim();
                // create a new document
                PdfDocument document = new PdfDocument();
                // crate a page description
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(500, 1000, 5).create();
                // start a page
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                bitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true);
                canvas.drawBitmap(bitmap, 0, 0, paint);

                //Static layout which will be drawn on canvas
                //textOnCanvas - text which will be drawn
                //text paint - paint object
                //bounds.width - width of the layout
                //Layout.Alignment.ALIGN_CENTER - layout alignment
                //1 - text spacing multiply
                //1 - text spacing add
                //true - include padding
                TextPaint textPaint = new TextPaint();
                textPaint.setColor(Color.BLACK);
                canvas.translate(5, 550);
                StaticLayout sl = new StaticLayout(finalText, textPaint, 480,
                        Layout.Alignment.ALIGN_NORMAL, 2, 1, true);
                canvas.save();
//                float textHeight = getTextHeight(finalText, textPaint);
//                int numberOfTextLines = sl.getLineCount();
//                float textYCoordinate = bounds.exactCenterY() -
//                        ((numberOfTextLines * textHeight) / 2);
//
//                //text will be drawn from left
//                float textXCoordinate = bounds.left;
//
//                canvas.translate(textXCoordinate, textYCoordinate);

                //draws static layout on canvas
                sl.draw(canvas);
                canvas.restore();


                // finish the page
                document.finishPage(page);
                // write the document content
                String directory_path = Environment.getExternalStorageDirectory().getPath() + "/mypdf/";
                File file = new File(directory_path);
                if (!file.exists()) {
                    file.mkdirs();
                }
                String targetPdf = directory_path + "test-2.pdf";
                File filePath = new File(targetPdf);
                try {
                    document.writeTo(new FileOutputStream(filePath));
                    Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Log.e("main", "error " + e.toString());
                    Toast.makeText(MainActivity.this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
                }
                // close the document
                document.close();
            }
        });
        editalert.show();
    }

//
//    private float getTextHeight(String text, Paint paint) {
//
//        Rect rect = new Rect();
//        paint.getTextBounds(text, 0, text.length(), rect);
//        return rect.height();
//    }

    public void SaveAsPng() {
        android.support.v7.app.AlertDialog.Builder editalert = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        editalert.setTitle("Please Enter the name with which you want to Save");
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        editalert.setView(input);
        editalert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                String name = input.getText().toString();
                Log.d(TAG, "onClick: name" + name);
                Bitmap bitmap = mv.getDrawingCache();

                String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                File file = new File("/sdcard/" + name + ".png");
                Log.d(TAG, "onClick: " + file);
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                        Toast.makeText(MainActivity.this, "File created at" + path, Toast.LENGTH_SHORT).show();
                    }
                    FileOutputStream ostream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 10, ostream);
                    ostream.close();
                    mv.invalidate();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                    mv.setDrawingCacheEnabled(false);
                }
            }
        });

        editalert.show();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                Log.d(TAG, "onActivityResult: resultUri" + resultUri);
                imageView.setImageURI(resultUri);
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG, "onActivityResult: " + error);
            }
        }
    }

    public class MyView extends View {

        private static final float MINP = 0.25f;
        private static final float MAXP = 0.75f;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mBitmapPaint;
        Context context;

        public MyView(Context c) {
            super(c);
            context = c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}

