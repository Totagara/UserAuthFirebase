package com.dave.davelogin.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dave.davelogin.R;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {

    Button resetPassword;
    EditText email_editText;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);
        resetPassword = findViewById(R.id.button_reset_password);
        resetPassword.setOnClickListener(v -> ResetPassword());
        email_editText = findViewById(R.id.text_email);
        progressBar = findViewById(R.id.progressbar);
    }

    private void ResetPassword() {
        String emailVal = email_editText.getText().toString();

        if(emailVal.isEmpty()){
            email_editText.setError("Email required");
            email_editText.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(emailVal).matches()){
            email_editText.setError("Valid Email required");
            email_editText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance()
                .sendPasswordResetEmail(emailVal)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        Toast.makeText(this, "check your email for reset link", Toast.LENGTH_LONG).show();
                        StartLoginActivity();
                        finish();
                    } else {
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(failTask -> {
                    Toast.makeText(this, failTask.getMessage(),Toast.LENGTH_LONG).show();
        });

    }

    private void StartLoginActivity(){
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
    }
}
