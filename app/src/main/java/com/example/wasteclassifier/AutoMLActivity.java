package com.example.wasteclassifier;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.graphics.BitmapFactory.decodeFile;
import static android.graphics.BitmapFactory.decodeStream;

public class AutoMLActivity extends BaseActivity {

    // view components
    private ImageView mImageView;
    private TextView mTextView;
    private Button mSendButton;
//    private EditText mWasteTypeEditText;
    private RadioGroup wasteTypeGroup;

    // constants
    private static final String REMOTE_MODEL_NAME = "Waste_2019510164317";
    // TODO clarify what is my_local_model
    private static final String LOCAL_MODEL_NAME = "my_local_model";

    private FirebaseVisionImageLabeler labeler;

    // firebase database
//    public static class MessageViewHolder extends RecyclerView.ViewHolder {
//        ImageView uploadImageView;
//        TextView typeTextView;
//
//        public MessageViewHolder(View v) {
//            super(v);
////            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
////            messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
////            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
////            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
//        }
//    }
//
//    // Firebase instance variables
//    // database
//    private DatabaseReference mFirebaseDatabaseReference;
//    private FirebaseRecyclerAdapter<ImageAndType, MessageViewHolder>
//            mFirebaseAdapter;

    FirebaseStorage storage = FirebaseStorage.getInstance();

    // Create a storage reference from our app
    StorageReference storageReference = storage.getReference();

    private Uri filePath;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // connect to view
        setContentView(R.layout.activity_automl);
        mImageView = findViewById(R.id.image_view);
        mTextView = findViewById(R.id.text_view);
        mSendButton = findViewById(R.id.send_button);

        wasteTypeGroup = findViewById(R.id.waste_type_group);
        mSendButton.setVisibility(View.INVISIBLE);
        wasteTypeGroup.setVisibility(View.INVISIBLE);

        // configure firebase-hosted model source

        // set ml model conditions
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();

        // set remote ml model
        FirebaseRemoteModel remoteModel = new FirebaseRemoteModel.Builder(REMOTE_MODEL_NAME)
                .enableModelUpdates(true)
                .setInitialDownloadConditions(conditions)
                .setUpdatesDownloadConditions(conditions)
                .build();

        // set model manager
        FirebaseModelManager modelManager = FirebaseModelManager.getInstance();
        // register remote model to the manager
        modelManager.registerRemoteModel(remoteModel);



        // set local ml model
        FirebaseLocalModel localModel = new FirebaseLocalModel.Builder(LOCAL_MODEL_NAME)
                .setAssetFilePath("manifest.json")
                .build();

        // register local model to the manager used by remote manager as well
        modelManager.registerLocalModel(localModel);

        // run the image labeler
        FirebaseVisionOnDeviceAutoMLImageLabelerOptions labelerOptions =
                new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder()
                        .setLocalModelName(LOCAL_MODEL_NAME)
                        .setRemoteModelName(REMOTE_MODEL_NAME)
                        .setConfidenceThreshold(0.65f)
                        .build();

        try {
            labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(labelerOptions);
        }
        catch (FirebaseMLException e) {
            mTextView.setText(e.getMessage());
            mTextView.setTextColor(Color.RED);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case RC_STORAGE_PERMS1:
                case RC_STORAGE_PERMS2:
                    checkStoragePermission(requestCode);
                    break;
                case RC_SELECT_PICTURE:
                    Uri dataUri = data.getData();
                    String path = getPath(this, dataUri);
                    if (null == path) {
                        bitmap = resizeImage(this, imageFile, dataUri, mImageView);
                    }
                    else {
                        bitmap = resizeImage(imageFile, path, mImageView);
                    }
                    if (null != bitmap) {

                        mTextView.setText("Sucessfully get image from album.\n");
                        mTextView.setTextColor(Color.BLACK);
                        mImageView.setImageBitmap(bitmap);
                        executeMLModel(bitmap);
                    }
                    break;
                case RC_TAKE_PICTURE:
                    bitmap = resizeImage(imageFile, imageFile.getPath(), mImageView);
                    if (null != bitmap) {
                        mTextView.setText("Successfully take image by camera.\n");
                        mImageView.setImageBitmap(bitmap);
                        executeMLModel(bitmap);
                    }
                    break;
            }
        }
    }

    /*  write a compressed version of the bitmap to the specified outputstream
        input: imageFile and bitmap to compress
        output: compressed bitmap
     */
    private static Bitmap compressImage(File imageFile, Bitmap bitmap) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream);
            fileOutputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private static Bitmap resizeImage(Context context, File imageFile, Uri uri, ImageView view) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            decodeStream(context.getContentResolver().openInputStream(uri), null, options);
            int photoWidth = options.outWidth;
            int photoHeight = options.outHeight;
            options.inSampleSize = Math.min(photoHeight/view.getHeight(), photoWidth/view.getWidth());
            return compressImage(imageFile, BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap resizeImage(File imageFile, String path, ImageView view) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        decodeFile(path, options);

        int photoWidth = options.outWidth;
        int photoHeight = options.outHeight;

        options.inJustDecodeBounds = false;
        options.inSampleSize = Math.min( photoHeight/view.getHeight(), photoWidth/view.getWidth());
        return compressImage(imageFile, BitmapFactory.decodeFile(path, options));
    }

    private static String getPath(Context context, Uri uri) {
        String path = "";
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (null != cursor) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
            cursor.close();
        }
        return path;
    }

    /* extract object type and confidence of the prediction from classified label from the labeler
       input: labels from labelers
       output: void
     */
    private void extractLabel(List<FirebaseVisionImageLabel> labels, Bitmap bitmap) {
        for (FirebaseVisionImageLabel label : labels) {
            mTextView.setText("Object is " + label.getText() + "\n");
            mTextView.append("Confidence is " + label.getConfidence() + "\n");
            mTextView.setTextColor(Color.BLACK);
        }

        // if no label detected, the classifier cannot predict the waste's type
        if (0 == labels.size()) {
            mTextView.setText("Unable to predict the type of waste.");
            mTextView.setTextColor(Color.RED);

            mSendButton.setVisibility(View.VISIBLE);
            wasteTypeGroup.setVisibility(View.VISIBLE);
            uploadImageAndTypeToStorage(bitmap);


        }
    }

    private void uploadImageAndTypeToStorage(final Bitmap bitmap) {
//        getWasteTypeFromEdittext();
        getWasteType();

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int selectedId = wasteTypeGroup.getCheckedRadioButtonId();
                RadioButton radioButton = wasteTypeGroup.findViewById(selectedId);
                String wasteType = radioButton.getText().toString();

                // Create a reference to type folder and image path
                Date date = new Date();
                DateFormat dateFormat = new SimpleDateFormat("yyyy_mm_dd_hhmmss");
                String strDate = dateFormat.format(date);
                String refPath = "/" + wasteType + "/" + wasteType + "_" + strDate + ".jpg";

                StorageReference imageRef = storageReference.child(refPath);

                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setCustomMetadata("wasteType", wasteType)
                        .build();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = imageRef.putBytes(data, metadata);
                radioButton.setChecked(false);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mTextView.setText("Successfully uploaded the image and waste type to our database.");
                        mTextView.append("Thank you!");
                        mTextView.setTextColor(Color.BLACK);
                        mSendButton.setVisibility(View.INVISIBLE);
                        wasteTypeGroup.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
    }

    private void getWasteType () {

        wasteTypeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == -1) {
                    mSendButton.setEnabled(false);
                }
                else {
                    mSendButton.setEnabled(true);
                }
                RadioButton radioButton = radioGroup.findViewById(i);
            }
        });

    }

//    private void getWasteTypeFromEdittext(){
//        mWasteTypeEditText = findViewById(R.id.waste_type_edittext);
//        mWasteTypeEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (charSequence.toString().trim().length() > 0) {
//                    mSendButton.setEnabled(true);
//                }
//                else {
//                    mSendButton.setEnabled(false);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
//    }

//    public void onRadioButtonClicked() {
//        // Is the button now checked?
//        boolean checked = ((RadioButton) view).isChecked();
//
//        // Check which radio button was clicked
//        switch(view.getId()) {
//            case R.id.recycle_radio_button:
//                if (checked)
//                    wasteType = "recycle";
//                    break;
//            case R.id.compost_radio_button:
//                if (checked)
//                    wasteType = "compost";
//                break;
//            case R.id.trash_radio_button:
//                if (checked)
//                    wasteType = "trash";
//                break;
//        }
//    }



    /* get image and pass the image to the model labeler
       input: image in Bitmap format
       output: void
     */
//    private void executeMLModel (Bitmap bitmap) {
//        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
//        labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
//            @Override
//            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
//                extractLabel(firebaseVisionImageLabels);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                mTextView.setText("Unable to predict the type of waste.");
////                mTextView.setTextColor(Color.RED);
//            }
//        });
//
//    }
    private void executeMLModel (final Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                extractLabel(firebaseVisionImageLabels, bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mTextView.setText("Unable to predict the type of waste.");
//                mTextView.setTextColor(Color.RED);
            }
        });

    }

//    private void uploadImage() {
//
//        if(filePath != null)
//        {
//            final ProgressDialog progressDialog = new ProgressDialog(this);
//            progressDialog.setTitle("Uploading...");
//            progressDialog.show();
//
//            StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());
//            ref.putFile(filePath)
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            progressDialog.dismiss();
//                            Toast.makeText(AutoMLActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            progressDialog.dismiss();
//                            Toast.makeText(AutoMLActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
//                                    .getTotalByteCount());
//                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
//                        }
//                    });
//        }
//    }


}