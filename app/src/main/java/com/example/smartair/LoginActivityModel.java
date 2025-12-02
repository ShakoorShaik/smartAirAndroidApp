package com.example.smartair;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import utils.DatabaseManager;

public class LoginActivityModel {

    FirebaseAuth mAuth;
    FirebaseFirestore db;




    public LoginActivityModel() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }

    public FirebaseUser getCurrUser() {
        return mAuth.getCurrentUser();
    }

    public void getAccountType(LoginActivityPresenter presenter) {
        DatabaseManager.getData("accountType", new DatabaseManager.DataSuccessFailCallback() {
            @Override
            public void onSuccess(String accountType) {
                presenter.onAccountTypeRetrieved(accountType);
            }

            @Override
            public void onFailure(Exception e) {
                mAuth.signOut();
                presenter.onAccountTypeFailed(e);
            }
        });
    }

    public void DBLogin(String email, String password, LoginActivityPresenter presenter) {
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
        
        DatabaseManager.accountLogin(email, password, new DatabaseManager.SuccessFailCallback() {
            @Override
            public void onSuccess() {
                presenter.sendMsg("Login Successful");
                getAccountType(presenter);
            }


            @Override
            public void onFailure(Exception e) {
                presenter.onLoginFail(e);

            }
        });
    }

}
