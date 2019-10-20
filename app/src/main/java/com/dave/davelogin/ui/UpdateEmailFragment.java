package com.dave.davelogin.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Patterns;
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
 * Activities that contain this fragment must implement the
 * {@link UpdateEmailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UpdateEmailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpdateEmailFragment extends Fragment {

    LinearLayout passwordLayout;
    LinearLayout updateEmailLayout;
    EditText password_edit_text, email_edit_text;
    Button but_update, but_Authenticate;
    ProgressBar progressbar;

    FirebaseUser currentUser;


    public UpdateEmailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_update_email, container, false);

        passwordLayout = root.findViewById(R.id.layoutPassword);
        updateEmailLayout = root.findViewById(R.id.layoutUpdateEmail);
        password_edit_text = root.findViewById(R.id.edit_text_password);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        but_update = root.findViewById(R.id.button_update);
        but_Authenticate = root.findViewById(R.id.button_authenticate);
        email_edit_text = root.findViewById(R.id.edit_text_email);
        progressbar = root.findViewById(R.id.progressbar);


        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        passwordLayout.setVisibility(View.VISIBLE);
        updateEmailLayout.setVisibility(View.INVISIBLE);

        but_update.setOnClickListener(v -> UpdateEmail(v));
        but_Authenticate.setOnClickListener(v -> AuthenticateUser(v));


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
            currentUser.reauthenticate(credentials).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    passwordLayout.setVisibility(View.INVISIBLE);
                    updateEmailLayout.setVisibility(View.VISIBLE);
                } else if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                    //Toast.makeText(this.getActivity(), "Invalid password", Toast.LENGTH_LONG);
                    password_edit_text.setError("Invalid password");
                    password_edit_text.requestFocus();
                    return;
                } else {
                    Toast.makeText(this.getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG);
                }
            });

        }
    }

    private void UpdateEmail(View v) {
        String emailVal = email_edit_text.getText().toString();
        if(emailVal.isEmpty()){
            email_edit_text.setError("Email required");
            email_edit_text.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(emailVal).matches()){
            email_edit_text.setError("Valid Email required");
            email_edit_text.requestFocus();
            return;
        }

        if(currentUser != null){
            progressbar.setVisibility(View.VISIBLE);
            currentUser.updateEmail(emailVal)
                    .addOnCompleteListener(task -> {
                        progressbar.setVisibility(View.GONE);
                        if(task.isSuccessful()){
                            Navigation.findNavController(v).navigate(R.id.action_emailUpdated);
                        } else {
                            String err = task.getException().getMessage();
                            Toast.makeText(this.getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}
