package com.dave.userauth.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dave.userauth.R;
import com.dave.utils.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    static final int GOOGLE_SIGN = 123;

    TextView registerTextView, forgotPasswordTextView;
    Button butLogin;
    FirebaseAuth mAuth;
    EditText edit_text_email, edit_text_password;
    Utils utilsInstance;
    ProgressBar progressBar;
    Button butFacebookLogin;
    Button butGoogleLogin;
    GoogleSignInClient mGoogleSignInClient;

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
        butGoogleLogin = findViewById(R.id.butLoginGoogle);
        forgotPasswordTextView = findViewById(R.id.text_view_forget_password);

        //Google signon
        butGoogleLogin.setOnClickListener(v -> ConnectThroughGoogle(v));
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

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

    private void ConnectThroughGoogle( View v) {
        progressBar.setVisibility(View.VISIBLE);
        Intent googleSignInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(googleSignInIntent, GOOGLE_SIGN);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GOOGLE_SIGN){
            Task<GoogleSignInAccount> gTask = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount gAccount = gTask.getResult(ApiException.class);
                if(gAccount != null){
                    firebaseAuthWithGoogle(gAccount);
                }
            } catch (ApiException e){
                e.printStackTrace();
            }

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount gAccount){
        Log.d("TAG", "firebaseAuthWithGoogle: " + gAccount.getId());

        AuthCredential gCredentials = GoogleAuthProvider.getCredential(gAccount.getIdToken(), null);

        mAuth.signInWithCredential(gCredentials).addOnCompleteListener(this, task -> {
            if(task.isSuccessful()){
                Log.d("TAG", "Login Successful");
                progressBar.setVisibility(View.INVISIBLE);

                FirebaseUser user = mAuth.getCurrentUser();

                StartHomeWithNavActivity();
                finish();
            }
            else {
                progressBar.setVisibility(View.INVISIBLE);
                Log.w("Failure", "Sign in with Google failed", task.getException());
                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT);
            }
        });
    }
}
