package com.example.photolabeling;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;

import java.util.List;

public class MainActivity extends ImageLabel implements View.OnClickListener {


    private Bitmap mBitmap;
    private ImageView mImageView;
    private TextView mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.textView);
        mImageView = findViewById(R.id.imageView);
        findViewById(R.id.phone).setOnClickListener(this);
        findViewById(R.id.cloud).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        mTextView.setText(null);
        switch (v.getId()){
            case R.id.phone:
                if (mBitmap != null){
                    FirebaseVisionOnDeviceImageLabelerOptions options = new FirebaseVisionOnDeviceImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build();
                    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);
                    FirebaseVisionImageLabeler detector = FirebaseVision.getInstance().getOnDeviceImageLabeler(options);
                    detector.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> ImageLabels) {
                            for (FirebaseVisionImageLabel label : ImageLabels) {
                                mTextView.append(label.getText() + "\n");
                                mTextView.append(label.getConfidence() + "\n\n");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mTextView.setText(e.getMessage());
                        }
                    });
                }
                break;
            case R.id.cloud:
                if (mBitmap != null) {
                    DataHelper.showDialog(this);
                    FirebaseVisionCloudDetectorOptions options = new FirebaseVisionCloudDetectorOptions.Builder().setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL).setMaxResults(5).build();
                    FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(mBitmap);

                    FirebaseVisionImageLabeler detector = FirebaseVision.getInstance().getCloudImageLabeler();


                    detector.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override

                        public void onSuccess(List<FirebaseVisionImageLabel> ImageLabels) {
                            DataHelper.dismissDialog();
                            for (FirebaseVisionImageLabel label : ImageLabels) {
                                mTextView.append(label.getText() + ": " + label.getConfidence() + "\n\n");
                                mTextView.append(label.getEntityId() + "\n");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            DataHelper.dismissDialog();
                            mTextView.setText(e.getMessage());
                        }
                    });
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_STORAGE_PERMS1:
                    checkStoragePermission(requestCode);
                    break;
                case RC_SELECT_PICTURE:
                    Uri dataUri = data.getData();
                    String path = DataHelper.getPath(this, dataUri);
                    if (path == null) {
                        mBitmap = DataHelper.resizeImage(imageFile, this, dataUri, mImageView);
                    } else {
                        mBitmap = DataHelper.resizeImage(imageFile, path, mImageView);
                    }
                    if (mBitmap != null) {
                        mTextView.setText(null);
                        mImageView.setImageBitmap(mBitmap);
                    }

            }
        }
    }
}

