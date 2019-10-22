package com.dave.userauth.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.dave.userauth.R;
import com.dave.userauth.ui.HomeActivity;
import com.dave.utils.Utils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    //Default image if user profile pic is not updated
    private final String DEFAULT_IMAGE_URL = "https://picsum.photos/id/1025/4951/3301";

    private static final int NEW_CAMERA_PERMISSION_REQUEST = 301;
    private static final int NEW_GALLERY_PERMISSION_REQUEST = 302;
    private static final int NEW_CAMERA_INTENT_REQUEST = 401;
    private static final int NEW_GALLERY_INTENT_REQUEST = 402;

    Utils utilsInstance;
    Context currentActivity;
    ImageView profilePicImageView;
    ProgressBar imageUploadProgreesBar, saveProfileProgressbar;
    Uri imageUri;
    ImageButton edit_phoneNumber;
    EditText profileNameEditText;
    FirebaseUser firebaseUser ;
    TextView emailIdTextView, phoneNumberTextView, emailNotVerifiedTextView, verifyEmailTextView;
    Button butSaveProfile, but_edit_email, but_change_password;

    private com.dave.userauth.ui.profile.ProfileViewModel profileViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        utilsInstance = Utils.getInstance();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        profilePicImageView = root.findViewById(R.id.imgView_profilePic);
        imageUploadProgreesBar = root.findViewById(R.id.progressbar_pic);
        profileNameEditText = root.findViewById(R.id.edit_text_name);
        emailIdTextView = root.findViewById(R.id.text_email);
        phoneNumberTextView = root.findViewById(R.id.text_phone);
        emailNotVerifiedTextView = root.findViewById(R.id.text_not_verified);
        butSaveProfile = root.findViewById(R.id.button_save);
        saveProfileProgressbar = root.findViewById(R.id.progressbarSave);
        verifyEmailTextView = root.findViewById(R.id.text_not_verified);
        but_change_password = root.findViewById(R.id.but_change_password);
        edit_phoneNumber = root.findViewById(R.id.but_edit_phonenumber);
        but_edit_email = root.findViewById(R.id.but_edit_email);
        currentActivity = this.getActivity();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profilePicImageView.setOnClickListener(v -> selectPictureData(v));

        //if user is already loggedin, update profile details
        if(firebaseUser != null){
            LoadCurrentUserDetails();
        }

        //Attach Save profile changes handler
        butSaveProfile.setOnClickListener(v -> SaveProfileChanges());

        //attach verify email click listener
        verifyEmailTextView.setOnClickListener(v -> verifyEmail());

        //Handle verify/add phone number
        edit_phoneNumber.setOnClickListener(v -> verifyPhoneNumber(v));
        //phoneNumberTextView.setOnClickListener(v -> verifyPhoneNumber(v));

        //emailIdTextView.setOnClickListener(v -> updateEmailId(v));
        but_edit_email.setOnClickListener(v -> updateEmailId(v));

        but_change_password.setOnClickListener(v -> ChangePassword(v));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap image;
        if (resultCode == RESULT_OK && requestCode == NEW_GALLERY_INTENT_REQUEST && data != null) {
            Uri selectedImage = data.getData();
            try {
                image = MediaStore.Images.Media.getBitmap(this.getActivity().getContentResolver(), selectedImage);
                uploadImageToFirebaseAndSaveURL(image);
            } catch (IOException e) {
                Toast.makeText(this.getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else if (resultCode == RESULT_OK && requestCode == NEW_CAMERA_INTENT_REQUEST && data != null) {
            image = (Bitmap) data.getExtras().get("data");
            uploadImageToFirebaseAndSaveURL(image);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == NEW_CAMERA_PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            OpenImageIntent(Manifest.permission.CAMERA);
        } else if(requestCode == NEW_GALLERY_PERMISSION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            OpenImageIntent(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void selectPictureData(View v) {
        final CharSequence[] options = {"Camera", "Gallery","Cancel"};
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.getActivity());
        dialogBuilder.setTitle("Choose image from");
        dialogBuilder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(options[which].equals("Camera")){
                    //RequestPermissionForImageCam(Manifest.permission.CAMERA);
                    RequestPermissionForImage(Manifest.permission.CAMERA);
                } else if(options[which].equals("Gallery")){
                    //RequestPermissionForImageGal(Manifest.permission.READ_EXTERNAL_STORAGE);
                    RequestPermissionForImage(Manifest.permission.READ_EXTERNAL_STORAGE);
                } else if(options[which].equals("Cancel")){
                    dialog.dismiss();
                }
            }
        });
        dialogBuilder.show();
    }

    private void RequestPermissionForImage(String permission){
        if(Build.VERSION.SDK_INT >= 22){
            CheckForImagePermssions(permission);
        } else {
            OpenImageIntent(permission);
        }
    }

    private void CheckForImagePermssions(String permission) {
        int reqCode = permission.equals(Manifest.permission.CAMERA)?NEW_CAMERA_PERMISSION_REQUEST
                :permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)?NEW_GALLERY_PERMISSION_REQUEST:0;

        if(ContextCompat.checkSelfPermission(this.getActivity(), permission) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), permission)){
                Toast.makeText(this.getActivity(), "Please enable permissions for this app", Toast.LENGTH_LONG).show();
            }
            else {
                //this is for requesting permissions from fragment
                requestPermissions(new String[] {permission}, reqCode);
            }
        }
        else {
            OpenImageIntent(permission);
        }
    }

    private void OpenImageIntent(String permission) {
        Intent imageIntent;
        if(permission.equals(Manifest.permission.CAMERA)) {
            imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(imageIntent, NEW_CAMERA_INTENT_REQUEST);
        } else if(permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
            imageIntent.setType("image/*");
            startActivityForResult(imageIntent, NEW_GALLERY_INTENT_REQUEST);
        }
    }

    private void ChangePassword(View v) {
        Navigation.findNavController(v).navigate(R.id.actionUpdatePassword);
    }

    private void verifyPhoneNumber(View v) {
        Navigation.findNavController(v).navigate(R.id.actionVerifyPhone);
    }

    private void updateEmailId(View v) {
        Navigation.findNavController(v).navigate(R.id.action_updateEmail);
    }

    private void verifyEmail() {
        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                utilsInstance.ShowToastShort(this.getActivity(), "Verification email sent");
            } else {
                utilsInstance.ShowToastShort(this.getActivity(), "Profile Updated");
            }
        });
    }

    private void SaveProfileChanges() {
        Uri uri;
        if (imageUri != null) {
            uri = imageUri;
        } else if(firebaseUser.getPhotoUrl() == null){
            //uri = Uri.parse(DEFAULT_IMAGE_URL);
            uri = null;
        } else {
            uri = firebaseUser.getPhotoUrl();
        }

        String profileName = profileNameEditText.getText().toString();
        if(profileName.isEmpty()){
            profileNameEditText.setError("Name Required");
            profileNameEditText.requestFocus();
            return;
        }

        UserProfileChangeRequest updateBuilder = new UserProfileChangeRequest
                .Builder()
                .setDisplayName(profileName)
                .setPhotoUri(uri).build();

        saveProfileProgressbar.setVisibility(View.VISIBLE);

        firebaseUser.updateProfile(updateBuilder).addOnCompleteListener(task -> {
            saveProfileProgressbar.setVisibility(View.GONE);
            if(task.isSuccessful()){
                utilsInstance.ShowToastShort(this.getActivity(), "Profile Updated");
            } else {
                utilsInstance.ShowToastLong(this.getActivity(), "Profile update failed" + task.getException().getMessage());
            }
        });
    }

    private void LoadCurrentUserDetails() {
        if(firebaseUser.getPhotoUrl() != null){
            Picasso.get().load(firebaseUser.getPhotoUrl()).into(profilePicImageView);
        } else {
            Picasso.get().load(R.mipmap.ic_defaultprofilepic).into(profilePicImageView);
        }

        if(firebaseUser.getEmail() != null){
            emailIdTextView.setText(firebaseUser.getEmail());
        }

        //Update Display name
        if(firebaseUser.getDisplayName() != null &&  !firebaseUser.getDisplayName().isEmpty()) {
            profileNameEditText.setText(firebaseUser.getDisplayName());
        }

        //update phone nunber
        if(firebaseUser.getPhoneNumber() != null && !firebaseUser.getPhoneNumber().isEmpty()){
            phoneNumberTextView.setText(firebaseUser.getPhoneNumber());
        } else {
            phoneNumberTextView.setText("Add phone number");
        }

        //Update email verified info
        if(firebaseUser.isEmailVerified()){
            emailNotVerifiedTextView.setVisibility(View.INVISIBLE);
        } else {
            emailNotVerifiedTextView.setVisibility(View.VISIBLE);
        }
    }

    private void uploadImageToFirebaseAndSaveURL(Bitmap cameraImage) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("pics/" + FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
        cameraImage.compress(Bitmap.CompressFormat.JPEG,100, byteArrayOS);

        byte[] image = byteArrayOS.toByteArray();

        //Show the progressbar while uploading
        imageUploadProgreesBar.setVisibility(View.VISIBLE);
        UploadTask upload = storageRef.putBytes(image);
        upload.addOnCompleteListener(task -> {
            imageUploadProgreesBar  .setVisibility(View.GONE);
            if(task.isSuccessful()){
                storageRef.getDownloadUrl().addOnCompleteListener(urlTask -> {
                    imageUri = urlTask.getResult();
                    profilePicImageView.setImageBitmap(cameraImage);
                    Toast.makeText(this.getActivity(), "Image uploaded, please save the changes", Toast.LENGTH_LONG).show();
                });
            } else {
                utilsInstance.ShowToastLong(this.getActivity(), "Image upload failed: " + task.getException().getMessage());
            }
        });
    }
}