package com.dave.davelogin.ui;


import android.content.Context;
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
import android.widget.Toast;

import com.dave.davelogin.R;
import com.dave.utils.Utils;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class VerifyPhoneFragment extends Fragment {
    LinearLayout layoutPhone, layoutVerification;
    Button but_SendCode, but_verifyCode;
    EditText Phone_edit_text, verifyCode_edit_text;
    CountryCodePicker countryCodePicker;
    private Utils utilsInstance = Utils.getInstance();
    Context currentActivity;
    String globalVerificationId = null;

    public VerifyPhoneFragment() {
        // Required empty public constructor
        this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_verify_phone, container, false);

        currentActivity = this.getActivity();
        layoutPhone = root.findViewById(R.id.layoutPhone);
        layoutVerification = root.findViewById(R.id.layoutVerification);
        but_SendCode = root.findViewById(R.id.button_send_verification);
        Phone_edit_text = root.findViewById(R.id.edit_text_phone);
        countryCodePicker = root.findViewById(R.id.ccp);
        but_verifyCode = root.findViewById(R.id.button_verify);
        verifyCode_edit_text = root.findViewById(R.id.edit_text_code);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layoutPhone.setVisibility(View.VISIBLE);
        layoutVerification.setVisibility(View.INVISIBLE);
        
        but_SendCode.setOnClickListener(v -> sendVerificationCode());
        but_verifyCode.setOnClickListener(v -> verifyCode(v));
    }

    private void verifyCode(View v) {
        String verifyCodeVal = verifyCode_edit_text.getText().toString();
        if(verifyCodeVal.isEmpty()){
            verifyCode_edit_text.setError("enter the verification OTP");
            verifyCode_edit_text.requestFocus();
            return;
        }

        if(globalVerificationId != null){
            PhoneAuthCredential credentials = PhoneAuthProvider.getCredential(globalVerificationId, verifyCodeVal);
            addPhoneNumber(credentials);
        }
        //Navigation.findNavController(this.getView()).navigate(R.id.actionPhoneVerified);
    }

    private void sendVerificationCode() {
        String phoneNumberText = Phone_edit_text.getText().toString();
        if(phoneNumberText.isEmpty() || phoneNumberText.length() != 10){
            Phone_edit_text.setError("Valid phone number required");
            Phone_edit_text.requestFocus();
            return;
        }

        String phoneNumber = "+" + countryCodePicker.getSelectedCountryCode() + phoneNumberText;
        PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(
                        phoneNumber,
                        60,
                        TimeUnit.SECONDS,
                        requireActivity(),
                        phoneAuthCallback
                );

        layoutPhone.setVisibility(View.INVISIBLE);
        layoutVerification.setVisibility(View.VISIBLE);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks phoneAuthCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            if(phoneAuthCredential != null){
                addPhoneNumber(phoneAuthCredential);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(currentActivity, e.getMessage(), Toast.LENGTH_LONG);
        }

        //if user has to enter OTP manually
        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(verificationId, forceResendingToken);
            globalVerificationId = verificationId;
        }
    };

    private void addPhoneNumber(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance()
                .getCurrentUser().updatePhoneNumber(phoneAuthCredential)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        utilsInstance.ShowToastShort(currentActivity, "Adding Phone number");
                        Navigation.findNavController(this.getView()).navigate(R.id.actionPhoneVerified);
                    } else {
                        utilsInstance.ShowToastShort(currentActivity, task.getException().getMessage());
                    }

                });

        //Return to Profile page
        //Navigation.findNavController(this.getView()).navigate(R.id.actionPhoneVerified);
    }
}
