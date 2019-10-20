package com.dave.davelogin.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dave.davelogin.R;
import com.dave.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    TextView registerTextView, forgotPasswordTextView;
    Button butLogin;
    FirebaseAuth mAuth;
    EditText edit_text_email, edit_text_password;
    Utils utilsInstance;
    ProgressBar progressBar;
    Button butFacebookLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        utilsInstance = Utils.getInstance();

        mAuth = FirebaseAuth.getInstance();
        butLogin = findViewById(R.id.button_sign_in);
        butLogin.setOnClickListener(v -> logIntheUser());
        progressBar = findViewById(R.id.progressbar);
        butFacebookLogin = findViewById(R.id.butLoginFacebook);
        forgotPasswordTextView = findViewById(R.id.text_view_forget_password);

        butFacebookLogin.setOnClickListener(v -> StartHomeWithNavActivity());

        registerTextView = findViewById(R.id.text_view_register);
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartSignUpActivity();
            }
        });

        forgotPasswordTextView.setOnClickListener(v -> {
            startActivity(new Intent(this, PasswordResetActivity.class));
        });
    }

    private void logIntheUser() {
        edit_text_email = findViewById(R.id.edit_text_email);
        edit_text_password = findViewById(R.id.edit_text_password);
        String emailText = edit_text_email.getText().toString();
        String pwText = edit_text_password.getText().toString();
        if(emailText.isEmpty()){
            edit_text_email.setError("Email required");
            edit_text_email.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()){
            edit_text_email.setError("Valid Email required");
            edit_text_email.requestFocus();
            return;
        }

        if(pwText.isEmpty() || edit_text_password.length() < 8){
            edit_text_password.setError("Minimum 8 characters Password required");
            edit_text_password.requestFocus();
            return;
        }

        LoginToFirebaseWithCredentials(emailText, pwText, this);

    }

    private void LoginToFirebaseWithCredentials(String emailText, String pwText, LoginActivity loginActivity) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(emailText, pwText)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        //User registered and take to profile page
                        //StartProfileActivity();
                        StartHomeWithNavActivity();
                        finish();
                    } else {
                        //Registration failed
                        //Show the exception
                        utilsInstance.ShowToastLong(this, "User registration failed: " + task.getException().getMessage());
                    }
                });
    }

    private void StartSignUpActivity() {
        Intent signUpIntent = new Intent(this, SignupActivity.class);
        startActivity(signUpIntent);
    }

    private void StartProfileActivity(){
        //User registered
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        startActivity(profileIntent);
        finish();
    }

    private void StartHomeWithNavActivity() {
        Intent navHomeIntent = new Intent(this, HomeActivity.class);
        startActivity(navHomeIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            //Todo: take him to application home page when app is complete
            StartHomeWithNavActivity();
            finish();
        }
    }
}
