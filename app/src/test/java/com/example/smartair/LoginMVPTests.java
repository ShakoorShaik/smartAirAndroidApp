package com.example.smartair;


import static android.text.TextUtils.isEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class LoginMVPTests {

    @Mock
    LoginActivityView view;
    @Mock
    LoginActivityModel model;
    @Mock
    FirebaseUser user;
    @Mock
    Editable emailInput;
    @Mock
    Editable passInput;

    private LoginActivityPresenter presenter;

    @Before
    public void instantiate() {
        presenter = new LoginActivityPresenter(view, model);
    }

    @Test
    public void testPresenterConstructor1() {

        assertEquals(presenter.view, view);
    }

    @Test
    public void testPresenterConstructor2() {
        assertEquals(presenter.model, model);
    }

    @Test
    public void testAutoLoginUserLoggedIn() {
        when(model.getCurrUser()).thenReturn(user);

        presenter.checkAutoLogin();

        verify(model).getAccountType(presenter);
    }

    @Test
    public void testAutoLoginUserLoggedOut() {
        when(model.getCurrUser()).thenReturn(null);

        presenter.checkAutoLogin();

        verify(model, never()).getAccountType(presenter);
    }

    @Test
    public void testAccountTypeRetrievalParent() {
        presenter.onAccountTypeRetrieved("Parent");

        verify(view).sendToParentHome();
    }

    @Test
    public void testAccountTypeRetrievalChild() {
        presenter.onAccountTypeRetrieved("Child");

        verify(view).sendToChildHome();
    }

    @Test
    public void testAccountTypeRetrievalElse() {
        presenter.onAccountTypeRetrieved("Provider");

        verify(view).sendToMainActivity();
    }

    @Test
    public void testOnRegisterClick() {
        presenter.onRegisterClick();
        verify(view).sendToRegistration();
    }

    @Test
    public void testOnForgetPasswordClick() {
        presenter.onForgetPasswordClick();
        verify(view).sendToPasswordReset();
    }

    @Test
    public void testAttemptLoginAllNull() {
        presenter.attemptLogin(null, null);
        verify(view).sendToast("Enter Email");
    }

    @Test
    public void testAttemptLoginPasswordNull() {
        when(emailInput.toString()).thenReturn("testemail@testing.com");

        presenter.attemptLogin(emailInput, null);
        verify(view).sendToast("Enter Password");
    }

    @Test
    public void testAttemptLoginValid() {
        when(emailInput.toString()).thenReturn("testemail@testing.com");
        when(passInput.toString()).thenReturn("testingpassword");
        String email, password;
        email = emailInput != null ? emailInput.toString().trim() : "";
        password = passInput != null ? passInput.toString() : "";

        presenter.attemptLogin(emailInput, passInput);
        verify(model).DBLogin(email, password, presenter);
    }

    @Test
    public void testOnAccountTypeFailed() {
        presenter.onAccountTypeFailed(new Exception());
        verify(view).sendToast("Failed to retrieve account type");
    }

    @Test
    public void testOnLoginFail1() {
        presenter.onLoginFail(null);
        verify(view).sendToast("Authentication failed.");
    }

    @Test
    public void testOnLoginFail2() {
        presenter.onLoginFail(new Exception());
        verify(view).sendToast("Authentication failed.");
    }

    @Test
    public void testOnLoginFail3() {
        presenter.onLoginFail(new Exception("INVALID_PASSWORD"));
        verify(view).sendToast("Incorrect password. Please try again.");
    }

    @Test
    public void testOnLoginFail4() {
        presenter.onLoginFail(new Exception("wrong-password"));
        verify(view).sendToast("Incorrect password. Please try again.");
    }

    @Test
    public void testOnLoginFail5() {
        presenter.onLoginFail(new Exception("USER_NOT_FOUND"));
        verify(view).sendToast("No account found with this email. Please register first.");
    }

    @Test
    public void testOnLoginFail6() {
        presenter.onLoginFail(new Exception("user-not-found"));
        verify(view).sendToast("No account found with this email. Please register first.");
    }

    @Test
    public void testOnLoginFail7() {
        presenter.onLoginFail(new Exception("INVALID_EMAIL"));
        verify(view).sendToast("Invalid email format. Please check your email.");
    }

    @Test
    public void testOnLoginFail8() {
        presenter.onLoginFail(new Exception("invalid-email"));
        verify(view).sendToast("Invalid email format. Please check your email.");
    }

    @Test
    public void testOnLoginFail9() {
        presenter.onLoginFail(new Exception("too-many-requests"));
        verify(view).sendToast("Too many failed attempts. Please try again later.");
    }

    @Test
    public void testOnLoginFail10() {
        presenter.onLoginFail(new Exception("Unknown error"));
        verify(view).sendToast("Authentication failed: Unknown error");
    }



}
