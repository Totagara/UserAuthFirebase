package com.dave.davelogin.ui;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dave.davelogin.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpdatePasswordFragment extends Fragment {

    Button but_authenticateUser, but_updatePw;
    EditText password_edit_text, newPassword_edit_text, newPasswordConfirm_edit_text;
    FirebaseUser currentUser;
    LinearLayout passwordLayout, updatePasswordLayout;
    ProgressBar progressbar;

    public UpdatePasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_update_password, container, false);
        but_authenticateUser = root.findViewById(R.id.button_authenticate);
        password_edit_text = root.findViewById(R.id.edit_text_password);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        passwordLayout = root.findViewById(R.id.layoutPassword);
        updatePasswordLayout = root.findViewById(R.id.layoutUpdatePassword);
        but_updatePw = root.findViewById(R.id.button_update);
        newPassword_edit_text = root.findViewById(R.id.edit_text_new_password);
        newPasswordConfirm_edit_text = root.findViewById(R.id.edit_text_new_password_confirm);
        progressbar = root.findViewById(R.id.progressbar);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        passwordLayout.setVisibility(View.VISIBLE);
        updatePasswordLayout.setVisibility(View.INVISIBLE);

        but_authenticateUser.setOnClickListener(v -> AuthenticateUser(v));
        but_updatePw.setOnClickListener(v -> UpdatePassword(v));

    }

    private void UpdatePassword(View v) {
        String passwordVal = newPassword_edit_text.getText().toString(),
                passwordConfirmVal = newPasswordConfirm_edit_text.getText().toString();
        if(passwordVal.isEmpty() || passwordVal.length() < 8){
            newPassword_edit_text.setError("Minimum 8 charactors required");
            newPassword_edit_text.requestFocus();
            return;
        }

        if(!passwordVal.equals(passwordConfirmVal)){
            newPasswordConfirm_edit_text.setError("password did not match");
            newPasswordConfirm_edit_text.requestFocus();
            return;
        } else {
            if(currentUser != null){
                progressbar.setVisibility(View.VISIBLE);
                currentUser.updatePassword(passwordVal).addOnCompleteListener(task -> {
                    progressbar.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        Toast.makeText(this.getActivity(), "Password updated", Toast.LENGTH_LONG);
                        Navigation.findNavController(v).navigate(R.id.actionPasswordUpdated);

                    } else {
                        Toast.makeText(this.getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG);
                    }
                });
            }
        }
    }

    private void AuthenticateUser(View v) {
        String passwordVal = password_edit_text.getText().toString();

        if(passwordVal.isEmpty()){
            password_edit_text.setError("Enter the password");
            password_edit_text.requestFocus();
            return;
        }

        if (currentUser != null){
            AuthCredential credentials = EmailAuthProvider.getCredential(currentUser.getEmail(), passwordVal);
            progressbar.setVisibility(View.VISIBLE);
            currentUser.reauthenticate(credentials).addOnCompleteListener(task -> {
                progressbar.setVisibility(View.GONE);
                if(task.isSuccessful()){
                    passwordLayout.setVisibility(View.GONE);
                    updatePasswordLayout.setVisibility(View.VISIBLE);
                } else if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                    password_edit_text.setError("Invalid password");
                    password_edit_text.requestFocus();
                    return;
                } else {
                    Toast.makeText(this.getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG);
                }
            });

        }
    }
}
