package com.example.textrecogniser;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActionBar actionBar;

    ImageView imageView;
    Button processButton;
    Bitmap bitmap;
    Uri resultUri;
    public static final int PERMISSION_CODE = 1111;
    Uri imageUri;
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
//        actionBar = this.getSupportActionBar();
//        actionBar.setTitle("TextRecogniser");
//
        imageView = findViewById(R.id.imageView);
        processButton = findViewById(R.id.processImage);

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

            }
        });
    }

//    public void processTextRecognitionResults(FirebaseVisionText firebaseVisionText) {
//
//        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
//        if (blocks.size() == 0) {
//            Toast.makeText(this, "No Text Found", Toast.LENGTH_SHORT).show();
//            return;
//        } else {
//            String resultText = firebaseVisionText.getText();
//            Log.d(TAG, "processTextRecognitionResults: resultText" + resultText);
//            for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
//                String blockText = block.getText();
//                Float blockConfidence = block.getConfidence();
//                List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
//                Point[] blockCornerPoints = block.getCornerPoints();
//                Rect blockFrame = block.getBoundingBox();
//
//                Log.d(TAG, "processTextRecognitionResults: blockText" + blockText);
//                Log.d(TAG, "processTextRecognitionResults: blockConfidence" + blockConfidence);
//                Log.d(TAG, "processTextRecognitionResults: blockLanguages" + blockLanguages);
//                Log.d(TAG, "processTextRecognitionResults: blockCornerPoints" + blockCornerPoints);
//                Log.d(TAG, "processTextRecognitionResults: blockFrame" + blockFrame);
//
//                for (FirebaseVisionText.Line line : block.getLines()) {
//                    String lineText = line.getText();
//                    Float lineConfidence = line.getConfidence();
//                    List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
//                    Point[] lineCornerPoints = line.getCornerPoints();
//                    Rect lineFrame = line.getBoundingBox();
//
//                    Log.d(TAG, "processTextRecognitionResults2: lineText" + lineText);
//                    Log.d(TAG, "processTextRecognitionResults2: lineConfidence" + lineConfidence);
//                    Log.d(TAG, "processTextRecognitionResults2: lineLanguages" + lineLanguages);
//                    Log.d(TAG, "processTextRecognitionResults2: lineCornerPoints" + lineCornerPoints);
//                    Log.d(TAG, "processTextRecognitionResults2: lineFrame" + lineFrame);
//
//                    for (FirebaseVisionText.Element element : line.getElements()) {
//                        String elementText = element.getText();
//                        Float elementConfidence = element.getConfidence();
//                        List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
//                        Point[] elementCornerPoints = element.getCornerPoints();
//                        Rect elementFrame = element.getBoundingBox();
//
//                        Log.d(TAG, "processTextRecognitionResults2: elementText" + elementText);
//                        Log.d(TAG, "processTextRecognitionResults2: elementConfidence" + elementConfidence);
//                        Log.d(TAG, "processTextRecognitionResults2: elementLanguages" + elementLanguages);
//                        Log.d(TAG, "processTextRecognitionResults2: elementCornerPoints" + elementCornerPoints);
//                        Log.d(TAG, "processTextRecognitionResults2: elementFrame" + elementFrame);
//                    }
//                }
//            }
//        }
//    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(this, "No Text Found", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    Log.d(TAG, "processTextRecognitionResult: " + elements.get(k).getText());
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
        if (item.getItemId() == R.id.gallery) {

//            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(Intent.createChooser(galleryIntent, "Select Image from"), GALLERY_REQUEST);

            CropImage.activity(imageUri)
                    .start(this);
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

//    public void CropImage() {
//        try {
//
//            Intent cropIntent = new Intent("com.android.camera.action.CROP");
//
//            cropIntent.setDataAndType(postImageUri, "image/*");
//            cropIntent.putExtra("crop", "true");
//            cropIntent.putExtra("aspectX", 3);
//            cropIntent.putExtra("aspectY", 4);
//            cropIntent.putExtra("outputX", 180);
//            cropIntent.putExtra("outputY", 180);
//            cropIntent.putExtra("return-data", 180);
//            cropIntent.putExtra("scaleUpIfNeeded", true);
//            startActivityForResult(cropIntent, CROP_PIC_REQUEST_CODE);
//        }
//        // respond to users whose devices do not support the crop action
//        catch (ActivityNotFoundException e) {
//            // display an error message
////            String errorMessage = "";
////            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
////            toast.show();
//
//            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//            alertDialog.setTitle("Device Error")
//                    .setMessage("Whoops - your device doesn't support the crop action!")
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                        }
//                    }).show();
//        }
//    }


}

