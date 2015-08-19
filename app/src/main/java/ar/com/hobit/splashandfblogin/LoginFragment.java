package ar.com.hobit.splashandfblogin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 * <p/>
 * Created by oscar.alvarez on 18/08/15.
 */
public class LoginFragment extends Fragment {

    private static final String TAG = "FB Login";

    private static final List<String> permissionNeeds = Arrays.asList("public_profile", "email", "user_friends");

    private LoginButton loginButton;
    private CallbackManager callbackManager;

    private FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            Log.d(TAG, "Login successful " + loginResult.toString());
            Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Log.d(TAG, "Login canceled");
            Toast.makeText(getActivity(), "Login canceled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(FacebookException exception) {
            Log.e(TAG, "Login error ", exception);
            Toast.makeText(getActivity(), "Login error", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions(permissionNeeds);
        loginButton.setFragment(this);
        loginButton.registerCallback(callbackManager, callback);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}