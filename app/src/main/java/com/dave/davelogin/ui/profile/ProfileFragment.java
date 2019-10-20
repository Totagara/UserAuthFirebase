package com.dave.davelogin.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.dave.davelogin.R;
import com.dave.davelogin.ui.HomeActivity;
import com.dave.davelogin.ui.VerifyPhoneFragment;
import com.dave.davelogin.ui.profile.ProfileViewModel;
import com.dave.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.UTFDataFormatException;

public class ProfileFragment extends Fragment {

    private static final int CAMERA_IMAGE_REQUEST = 101;

    //Default image if user profile pic is not updated
    private final String DEFAULT_IMAGE_URL = "https://picsum.photos/id/1025/4951/3301";

    ImageView profilePicImageView;
    ProgressBar imageUploadProgreesBar, saveProfileProgressbar;
    Uri imageUri;
    EditText profileNameEditText;
    private Utils utilsInstance;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    TextView emailIdTextView, phoneNumberTextView, emailNotVerifiedTextView, verifyEmailTextView, emailTextView, changePasswordTextView;
    Button butSaveProfile;

    private com.dave.davelogin.ui.profile.ProfileViewModel profileViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        utilsInstance = Utils.getInstance();

        profilePicImageView = root.findViewById(R.id.imgView_profilePic);
        imageUploadProgreesBar = root.findViewById(R.id.progressbar_pic);
        profileNameEditText = root.findViewById(R.id.edit_text_name);
        emailIdTextView = root.findViewById(R.id.text_email);
        phoneNumberTextView = root.findViewById(R.id.text_phone);
        emailNotVerifiedTextView = root.findViewById(R.id.text_not_verified);
        butSaveProfile = root.findViewById(R.id.button_save);
        saveProfileProgressbar = root.findViewById(R.id.progressbarSave);
        verifyEmailTextView = root.findViewById(R.id.text_not_verified);
        changePasswordTextView = root.findViewById(R.id.text_change_password);

        // Inflate the layout for this fragment
        return root;
    }

    /*@Override
    public void onStart() {
        super.onStart();
        if(firebaseUser != null){
            //If user is already loggedin take him to profile page.
            //Todo: take him to application home page when app is complete
            StartHomeWithNavActivity();
            getActivity().finish();
        }
    }

    private void StartHomeWithNavActivity() {
        Intent navHomeIntent = new Intent(this.getActivity(), HomeActivity.class);
        startActivity(navHomeIntent);
    }*/

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profilePicImageView.setOnClickListener(v -> selectPictureIntent());

        //if user is already loggedin, update profile details
        if(firebaseUser != null){
            LoadCurrentUserDetails();
            //Picasso.get().load(firebaseUser.getPhotoUrl()).into(profilePicImageView);
        }

        //Attach Save profile changes handler
        butSaveProfile.setOnClickListener(v -> SaveProfileChanges());

        //attach verify email click listener
        verifyEmailTextView.setOnClickListener(v -> verifyEmail());

        //Handle verify/add phone number
        phoneNumberTextView.setOnClickListener(v -> verifyPhoneNumber(v));

        emailIdTextView.setOnClickListener(v -> updateEmailId(v));

        changePasswordTextView.setOnClickListener(v -> ChangePassword(v));

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
            uri = Uri.parse(DEFAULT_IMAGE_URL);
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
        Picasso.get().load(firebaseUser.getPhotoUrl()).into(profilePicImageView);

        emailIdTextView.setText(firebaseUser.getEmail());

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



    private void selectPictureIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Check the request code for result
        if(requestCode == CAMERA_IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            Bitmap cameraImage = (Bitmap) data.getExtras().get("data");
            uploadImageToFirebaseAndSaveURL(cameraImage);
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
                    utilsInstance.ShowToastShort(this.getActivity(), "Image url: " + imageUri.toString());
                });

            } else {
                utilsInstance.ShowToastLong(this.getActivity(), "Image upload failed: " + task.getException().getMessage());
            }
        });

    }
}