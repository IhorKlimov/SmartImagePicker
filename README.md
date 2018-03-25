### Simplified abstraction over [Android Image Cropper](https://github.com/ArthurHub/Android-Image-Cropper) library to pick images from gallery or camera and crop them if needed.

## Usage
Add this line to build.gradle
```groovy
compile 'com.myhexaville:smart-image-picker:1.0.4'
```


### Create new instance and save it as field
```java
imagePicker = new ImagePicker(this, /* activity non null*/
                null, /* fragment nullable*/
                imageUri -> {/*on image picked */
                    imageView.setImageURI(imageUri);
                })
                .setWithImageCrop(
                        1 /*aspect ratio x*/
                        1 /*aspect ratio y*/);
```

### If calling from Activity
Override Activity's methods to delegate permissions to ImagePicker and resulting image
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    imagePicker.handleActivityResult(resultCode,requestCode, data);
}
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    imagePicker.handlePermission(requestCode, grantResults);
}
```
### Open Picker
There's two methods available
```java
imagePicker.openCamera();
imagePIcker.choosePicture(true /*show camera intents*/);
```
First one opens camera directly, second shows an intent picker, where user picks from desired application. You can include/exclude camera intents with boolean.

That's it, if you don't need to crop image, don't call *setImageCrop()*  in the chain. By default it's disabled. And if you want to get a file after you picked image, you can get it with this method
```java
File file = imagePicker.getImageFile();
```
### If calling from Fragment 
Create instance 
```java
imagePicker = new ImagePicker(getActivity(),
                this,
                imageUri -> {/*on image picked */
                    imageView.setImageURI(imageUri);
                })
                .setWithImageCrop(
                        1 /*aspect ratio x*/
                        1 /*aspect ratio y*/);
```

Overriden methods should be in your Fragment

```java
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
```
But your fragment won't get activity result callback itself. You need to call it manually. Add this code to activity that hosts your fragment

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    List<Fragment> fragments = getSupportFragmentManager().getFragments();
    if (fragments != null) {
        for (Fragment f : fragments) {
            if (f instanceof YourFragment) {
                f.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
```

### You can get a sample app [here](https://github.com/IhorKlimov/SmartImagePicker/tree/master/app)
### You don't need to add any permissions to manifest, everything is merged automatically from library's manifest file
