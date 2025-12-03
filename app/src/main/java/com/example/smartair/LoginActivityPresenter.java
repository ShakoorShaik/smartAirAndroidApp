package com.example.smartair;

import android.text.Editable;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;

public class LoginActivityPresenter {

    LoginActivityModel model;

    LoginActivityView view;


    public LoginActivityPresenter(LoginActivityView view, LoginActivityModel model) {
        this.view = view;
        this.model = model;
    }

    public void checkAutoLogin() {

        FirebaseUser currentUser = model.getCurrUser();
        if(currentUser != null){
            model.getAccountType(this);
        }
    }


    public void onAccountTypeRetrieved(String accountType) {

        if ("Parent".equals(accountType)) {
            view.sendToParentHome();
        } else if ("Child".equals(accountType)) {
            view.sendToChildHome();
        } else if ("Provider".equals(accountType)) {
            view.sendToProviderCodeLinking();
        } else {
            view.sendToMainActivity();
        }
    }

    public void onRegisterClick() {
        view.sendToRegistration();
    }

    public void onForgetPasswordClick() {
        view.sendToPasswordReset();
    }

    public void attemptLogin(Editable emailInput, Editable passInput) {
        String email, password;
        email = emailInput != null ? emailInput.toString().trim() : "";
        password = passInput != null ? passInput.toString() : "";

        if (email.isEmpty())
        {
            sendMsg("Enter Email");
            return;
        }

        if(password.isEmpty())
        {
           sendMsg("Enter Password");
            return;
        }

        model.DBLogin(email, password, this);
    }


    public void sendMsg(String msg) {
        view.sendToast(msg);
    }

    public void onAccountTypeFailed(Exception e) {
        view.sendToast("Failed to retrieve account type");
    }

    public void onLoginFail(Exception e) {
        String errorMessage = "Authentication failed.";
        if (e != null && e.getMessage() != null) {
            String firebaseError = e.getMessage();
            if (firebaseError.contains("INVALID_PASSWORD") || firebaseError.contains("wrong-password")) {
                errorMessage = "Incorrect password. Please try again.";
            } else if (firebaseError.contains("USER_NOT_FOUND") || firebaseError.contains("user-not-found")) {
                errorMessage = "No account found with this email. Please register first.";
            } else if (firebaseError.contains("INVALID_EMAIL") || firebaseError.contains("invalid-email")) {
                errorMessage = "Invalid email format. Please check your email.";
            } else if (firebaseError.contains("too-many-requests")) {
                errorMessage = "Too many failed attempts. Please try again later.";
            } else {
                errorMessage = "Authentication failed: " + firebaseError;
            }
        }
        view.sendToast(errorMessage);
    }


}
