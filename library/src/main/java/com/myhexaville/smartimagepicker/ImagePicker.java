package com.myhexaville.smartimagepicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.theartofdev.edmodo.cropper.CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE;
import static com.theartofdev.edmodo.cropper.CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE;
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
public class ImagePicker implements ImagePickerContract {
    private static final String TAG = "ImagePicker";
    private static final int CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITH_CAMERA = 100;
    private static final int CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITHOUT_CAMERA = 101;

    private static String currentCameraFileName = "";
    private OnImagePickedListener listener;
    private Activity activity;
    private Fragment fragment;

    private File imageFile;
    private int aspectRatioX, aspectRatioY;
    private boolean withCrop;

    public ImagePicker(Activity activity, @Nullable Fragment fragment, OnImagePickedListener listener) {
        this.activity = activity;
        this.fragment = fragment;
        this.listener = listener;
    }

    @SuppressWarnings("UnusedReturnValue")
    @Override
    public ImagePicker setWithImageCrop(int aspectRatioX, int aspectRatioY) {
        withCrop = true;
        this.aspectRatioX = aspectRatioX;
        this.aspectRatioY = aspectRatioY;
        return this;
    }

    @SuppressLint("NewApi")
    @Override
    public void choosePicture(boolean includeCamera) {
        if (needToAskPermissions()) {
            String[] neededPermissions = getNeededPermissions();
            int requestCode = includeCamera
                    ? CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITH_CAMERA
                    : CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITHOUT_CAMERA;
            if (fragment != null) {
                fragment.requestPermissions(neededPermissions, requestCode);
            } else {
                activity.requestPermissions(neededPermissions, requestCode);
            }
        } else {
            startImagePickerActivity(includeCamera);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void openCamera() {
        if (needToAskPermissions()) {
            if (fragment != null) {
                fragment.requestPermissions(getNeededPermissions(), CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
            } else {
                activity.requestPermissions(getNeededPermissions(), CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
            }
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Intent cameraIntent = getCameraIntent();
            if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(cameraIntent, PICK_IMAGE_CHOOSER_REQUEST_CODE);
            }
        }
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    @NonNull
    @Override
    public File getImageFile() {
        return imageFile;
    }

    @Override
    public void handlePermission(int requestCode, int[] grantResults) {
        Log.d(TAG, "handlePermission: " + requestCode);
        if (requestCode == CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITH_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                startImagePickerActivity(true);
            } else {
                Toast.makeText(activity, R.string.canceling, Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE_WITHOUT_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                startImagePickerActivity(false);
            } else {
                Toast.makeText(activity, R.string.canceling, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(activity, R.string.canceling, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void handleActivityResult(int resultCode, int requestCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Log.d(TAG, "handleActivityResult: 1");
            if (requestCode == PICK_IMAGE_CHOOSER_REQUEST_CODE) {
                Log.d(TAG, "handleActivityResult: 2");
                handlePickedImageResult(data);
            } else {
                if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    handleCroppedImageResult(data);
                }
            }
        } else {
            Log.d(TAG, "handleActivityResult: " + resultCode);
            if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.d(TAG, "onActivityResult: Image picker Error");
            }
        }
    }

    private String[] getNeededPermissions() {
        if (withCrop) {
            return new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        } else {
            return new String[]{Manifest.permission.CAMERA};
        }
    }

    private boolean needToAskPermissions() {
        if (withCrop) {
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
        }
    }

    private void handleCroppedImageResult(Intent data) {
        Log.d(TAG, "handleCroppedImageResult: ");
        CropImage.ActivityResult result = CropImage.getActivityResult(data);
        Uri croppedImageUri = result.getUri();
        deletePreviouslyCroppedFiles(croppedImageUri);
        imageFile = new File(croppedImageUri.getPath());
        listener.onImagePicked(croppedImageUri);
    }

    @SuppressLint("NewApi")
    private void handlePickedImageResult(Intent data) {
        boolean isCamera = true;
        if (data != null && data.getData() != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        Uri imageUri = isCamera || data.getData() == null ? getCameraFileUri(activity) : data.getData();
        if (isCamera) {
            deletePreviousCameraFiles();
        }
        Log.d(TAG, "handlePickedImageResult: " + imageUri);
        if (withCrop) {
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(aspectRatioX, aspectRatioY)
                    .start(activity);
        } else {
            imageFile = new File(imageUri.getPath());
            listener.onImagePicked(imageUri);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deletePreviousCameraFiles() {
        File imagePath = new File(activity.getFilesDir(), "images");
        if (imagePath.exists() && imagePath.isDirectory()) {
            if (imagePath.listFiles().length > 0) {
                for (File file : imagePath.listFiles()) {
                    if (!file.getName().equals(currentCameraFileName)) {
                        file.delete();
                    }
                }
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deletePreviouslyCroppedFiles(Uri currentCropImageUri) {
        Log.d(TAG, "deletePreviouslyCroppedFiles: " + currentCropImageUri);
        String croppedImageName = currentCropImageUri.getLastPathSegment();
        File imagePath = activity.getCacheDir();
        Log.d(TAG, "deletePreviouslyCroppedFiles: " + imagePath.exists() + " " + imagePath.isDirectory());
        if (imagePath.exists() && imagePath.isDirectory()) {
            Log.d(TAG, "deletePreviouslyCroppedFiles: " + imagePath.toString());
            Log.d(TAG, "deletePreviouslyCroppedFiles: " + imagePath.listFiles().length);
            if (imagePath.listFiles().length > 0) {
                for (File file : imagePath.listFiles()) {
                    Log.d(TAG, "deletePreviouslyCroppedFiles: " + file.getName());
                    if (!file.getName().equals(croppedImageName)) {
                        file.delete();
                    }
                }
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @NonNull
    private Intent getCameraIntent() {
        currentCameraFileName = "outputImage" + System.currentTimeMillis() + ".jpg";
        File imagesDir = new File(activity.getFilesDir(), "images");
        imagesDir.mkdirs();
        File file = new File(imagesDir, currentCameraFileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            Log.d(TAG, "openCamera: coudln't crate ");
            e.printStackTrace();
        }
        Log.d(TAG, "openCamera: file exists " + file.exists() + " " + file.toURI().toString());
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String authority = activity.getPackageName() + ".smart-image-picket-provider";
        final Uri outputUri = FileProvider.getUriForFile(
                activity.getApplicationContext(),
                authority,
                file);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        activity.grantUriPermission(
                "com.google.android.GoogleCamera",
                outputUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION
        );
        return cameraIntent;
    }

    private Uri getCameraFileUri(Activity activity) {
        File imagePath = new File(activity.getFilesDir(), "images/" + currentCameraFileName);
        return Uri.fromFile(imagePath);
    }

    private void startImagePickerActivity(boolean includeCamera) {
        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = activity.getPackageManager();

        List<Intent> galleryIntents = getGalleryIntents(packageManager, Intent.ACTION_GET_CONTENT, false);
        if (galleryIntents.size() == 0) {
            // if no intents found for get-content try pick intent action (Huawei P9).
            galleryIntents = getGalleryIntents(packageManager, Intent.ACTION_PICK, false);
        }

        if (includeCamera) {
            allIntents.add(getCameraIntent());
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
