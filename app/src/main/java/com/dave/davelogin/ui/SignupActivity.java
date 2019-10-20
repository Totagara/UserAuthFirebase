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

public class SignupActivity extends AppCompatActivity {

    TextView loginTextView;
    Button butSignUp;
    EditText edit_text_email;
    EditText edit_text_password;
    FirebaseAuth mAuth;
    Utils utilsInstance;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        utilsInstance = Utils.getInstance();

        progressBar = findViewById(R.id.progressbar);

        //Handle login click if account already exists
        loginTextView = findViewById(R.id.text_view_login);
        loginTextView.setOnClickListener(v -> StartLoginActivity());

        //Handle signup button clcik
        butSignUp = findViewById(R.id.button_register);
        butSignUp.setOnClickListener(v -> SignupUser());

        //Initialize mAuth
        mAuth = FirebaseAuth.getInstance();
    }

    private void SignupUser() {
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
            edit_text_password.setError("Minimum 8 charactors Password required");
            edit_text_password.requestFocus();
            return;
        }

        RegisterUserWithEmail(emailText, pwText);

    }

    private void RegisterUserWithEmail(String emailText, String pwText) {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(emailText,pwText)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        //User registered and take to profile page
                        StartProfileActivity();
                    } else {
                        //Registration failed
                        //Show the exception
                        utilsInstance.ShowToastLong(this, "User registration failed" + task.getException().getMessage());
                    }

                });
    }

    private void StartProfileActivity(){
        //User registered
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        startActivity(profileIntent);
        finish();
    }

    private void StartLoginActivity(){
        //User registered
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            //If user is already loggedin take him to profile page.
            //Todo: take him to application home page when app is complete
            StartProfileActivity();
        }
    }
}
