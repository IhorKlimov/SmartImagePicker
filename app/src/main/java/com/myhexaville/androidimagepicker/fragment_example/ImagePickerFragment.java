package com.myhexaville.androidimagepicker.fragment_example;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myhexaville.androidimagepicker.R;
import com.myhexaville.androidimagepicker.databinding.MainLayoutBinding;
import com.myhexaville.smartimagepicker.ImagePicker;

public class ImagePickerFragment extends Fragment {

    private ImagePicker imagePicker;
    private MainLayoutBinding binding;

    public ImagePickerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_layout, container, false);

        binding.openCamera.setOnClickListener(v -> openCamera());
        binding.showAll.setOnClickListener(v -> showAll());
        binding.showGallery.setOnClickListener(v -> chooseFromGallery());

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imagePicker.handleActivityResult(resultCode, requestCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        imagePicker.handlePermission(requestCode, grantResults);
    }

    public void showAll() {
        refreshImagePicker();
        imagePicker.choosePicture(true);
    }

    public void chooseFromGallery() {
        refreshImagePicker();
        imagePicker.choosePicture(false);
    }

    public void openCamera() {
        refreshImagePicker();
        imagePicker.openCamera();
    }

    private void refreshImagePicker() {
        imagePicker = new ImagePicker(getActivity(),
                this,
                imageUri -> {
                    binding.image.setImageURI(imageUri);
                });
        if (binding.withCrop.isChecked()) {
            imagePicker.setWithImageCrop(
                    Integer.parseInt(binding.aspectRatioX.getText().toString()),
                    Integer.parseInt(binding.aspectRatioY.getText().toString())
            );
        }
    }

}
