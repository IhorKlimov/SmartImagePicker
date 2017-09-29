package com.myhexaville.smartimagepicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.theartofdev.edmodo.cropper.CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.getCameraIntents;
import static com.theartofdev.edmodo.cropper.CropImage.getGalleryIntents;

/**
 * Usage: Create new instance, call {@link #choosePicture(boolean)} or {@link #openCamera()}
 * override {@link Activity#onActivityResult}, call {@link #handleActivityResult(int, int, Intent)} in it
 * override {@link Activity#onRequestPermissionsResult}, call {@link #handlePermission(int, int[])} in it
 * get picked file with {@link #getImageFile()}
 * <p>
 * If calling from Fragment, override {@link Activity#onActivityResult(int, int, Intent)}
 * and call {@link Fragment#onActivityResult(int, int, Intent)} for your fragment to delegate result
 */
public class ImagePicker {
    private static final String LOG_TAG = "PhotoPicker";
    private static final int CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITH_CAMERA = 100;
    private OnImagePickedListener listener;
    private Activity activity;
    private Fragment fragment;

    private Uri cropImageUri;
    private File imageFile;
    private int aspectRatioX;
    private int aspectRatioY;
    private boolean withCrop;

    public ImagePicker(Activity activity, @Nullable Fragment fragment, OnImagePickedListener listener) {
        this.activity = activity;
        this.fragment = fragment;
        this.listener = listener;
    }

    public ImagePicker setWithImageCrop(int aspectRatioX, int aspectRatioY) {
        withCrop = true;
        this.aspectRatioX = aspectRatioX;
        this.aspectRatioY = aspectRatioY;
        return this;
    }

    @SuppressLint("NewApi")
    public void choosePicture(boolean includeCamera) {
        if (includeCamera) {
            if (CropImage.isExplicitCameraPermissionRequired(activity)) {
                if (fragment != null) {
                    fragment.requestPermissions(new String[]{Manifest.permission.CAMERA},
                            CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITH_CAMERA);
                } else {
                    activity.requestPermissions(new String[]{Manifest.permission.CAMERA},
                            CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITH_CAMERA);
                }
            } else {
                CropImage.startPickImageActivity(activity);
            }
        } else {
            List<Intent> allIntents = new ArrayList<>();
            PackageManager packageManager = activity.getPackageManager();

            List<Intent> galleryIntents = getGalleryIntents(packageManager, Intent.ACTION_GET_CONTENT, false);
            if (galleryIntents.size() == 0) {
                // if no intents found for get-content try pick intent action (Huawei P9).
                galleryIntents = getGalleryIntents(packageManager, Intent.ACTION_PICK, false);
            }

            allIntents.addAll(galleryIntents);

            Intent target;
            if (allIntents.isEmpty()) {
                target = new Intent();
            } else {
                target = allIntents.get(allIntents.size() - 1);
                allIntents.remove(allIntents.size() - 1);
            }

            // Create a chooser from the main  intent
            Intent chooserIntent = Intent.createChooser(target, activity.getString(R.string.select_source));

            // Add all other intents
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));
            activity.startActivityForResult(chooserIntent, PICK_IMAGE_CHOOSER_REQUEST_CODE);
        }
    }

    @SuppressLint("NewApi")
    public void openCamera() {
        if (CropImage.isExplicitCameraPermissionRequired(activity)) {
            if (fragment != null) {
                fragment.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
            } else {
                activity.requestPermissions(new String[]{Manifest.permission.CAMERA},
                        CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
            }
        } else {
            List<Intent> allIntents = new ArrayList<>();
            PackageManager packageManager = activity.getPackageManager();

            allIntents.addAll(getCameraIntents(activity, packageManager));

            Intent target;
            if (allIntents.isEmpty()) {
                target = new Intent();
            } else {
                target = allIntents.get(allIntents.size() - 1);
                allIntents.remove(allIntents.size() - 1);
            }

            // Create a chooser from the main  intent
            Intent chooserIntent = Intent.createChooser(target, activity.getString(R.string.select_source));

            // Add all other intents
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));
            activity.startActivityForResult(chooserIntent, PICK_IMAGE_CHOOSER_REQUEST_CODE);
        }
    }

    public void handlePermission(int requestCode, int[] grantResults) {
        Log.d(LOG_TAG, "handlePermission: " + requestCode);
        if (requestCode == CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITH_CAMERA) {
            Log.d(LOG_TAG, "handlePermission: 1");
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(activity);
            } else {
                Toast.makeText(activity, R.string.canceling, Toast.LENGTH_SHORT).show();
            }
        }else  if (requestCode == CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            Log.d(LOG_TAG, "handlePermission: 1");
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(activity, R.string.canceling, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            Log.d(LOG_TAG, "handlePermission: 2");
            if (cropImageUri != null && grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                if (withCrop) {
                    CropImage.activity(cropImageUri)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(aspectRatioX, aspectRatioY)
                            .start(activity);
                } else {
                    listener.onImagePicked(cropImageUri);
                }
            } else {
                Toast.makeText(activity, R.string.canceling, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void handleActivityResult(int resultCode, int requestCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Log.d(LOG_TAG, "handleActivityResult: 1");
            if (requestCode == PICK_IMAGE_CHOOSER_REQUEST_CODE) {
                Log.d(LOG_TAG, "handleActivityResult: 2");
                handlePickedImageResult(data);
            } else {
                if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    handleCroppedImageResult(data);
                }
            }
        } else {
            Log.d(LOG_TAG, "handleActivityResult: " + resultCode);
            if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.d(LOG_TAG, "onActivityResult: Image picker Error");
            }
        }
    }

    public File getImageFile() {
        return imageFile;
    }

    private void handleCroppedImageResult(Intent data) {
        Log.d(LOG_TAG, "handleCroppedImageResult: ");
        CropImage.ActivityResult result = CropImage.getActivityResult(data);
        Uri croppedImageUri = result.getUri();
        imageFile = new File(croppedImageUri.getPath());
        listener.onImagePicked(croppedImageUri);
    }

    @SuppressLint("NewApi")
    private void handlePickedImageResult(Intent data) {
        Uri imageUri = CropImage.getPickImageResultUri(activity, data);
        if (CropImage.isReadExternalStoragePermissionsRequired(activity, imageUri)) {
            Log.d(LOG_TAG, "handlePickedImageResult: 1");
            cropImageUri = imageUri;
            activity.requestPermissions(new String[]{READ_EXTERNAL_STORAGE},
                    CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
        } else {
            Log.d(LOG_TAG, "onActivityResult: " + imageUri);
            if (withCrop) {
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(aspectRatioX, aspectRatioY)
                        .start(activity);
            } else {
                listener.onImagePicked(imageUri);
            }
        }
    }
}
