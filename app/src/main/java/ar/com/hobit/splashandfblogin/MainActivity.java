package ar.com.hobit.splashandfblogin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.appevents.AppEventsLogger;

/**
 * Main Activity
 * <p/>
 * - Shows login fragment if user is not logger in
 * - Shows Main Fragment when user is logged in
 * - Shows settings
 */
public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";

    private static final int LOGIN_FRAGMENT = 0;
    private static final int WELCOME_FRAGMENT = 1;
    private static final int FRAGMENT_COUNT = WELCOME_FRAGMENT + 1;

    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    private boolean isResumed = false;
    private AccessTokenTracker accessTokenTracker;
    private CallbackManager callbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (isResumed) {
                    FragmentManager manager = getSupportFragmentManager();
                    int backStackSize = manager.getBackStackEntryCount();
                    for (int i = 0; i < backStackSize; i++) {
                        manager.popBackStack();
                    }
                    if (currentAccessToken != null) {
                        showFragment(WELCOME_FRAGMENT, false);
                    } else {
                        showFragment(LOGIN_FRAGMENT, false);
                    }
                }
            }
        };

        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        LoginFragment loginFragment = (LoginFragment) fm.findFragmentById(R.id.loginFragment);
        fragments[LOGIN_FRAGMENT] = loginFragment;
        fragments[WELCOME_FRAGMENT] = fm.findFragmentById(R.id.welcomeFragment);

        FragmentTransaction transaction = fm.beginTransaction();
        for (Fragment fragment : fragments) {
            transaction.hide(fragment);
        }
        transaction.commit();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Store something in saved state for future use
        // outState.putBoolean(SOMETHING_KEY, something);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        if (AccessToken.getCurrentAccessToken() != null) {
            // if the user already logged in, try to show the selection fragment
            showFragment(WELCOME_FRAGMENT, false);
        } else {
            // otherwise present the login screen and ask the user to login,
            // unless the user explicitly skipped.
            showFragment(LOGIN_FRAGMENT, false);
        }
    }

    public void showLoginFragment() {
        showFragment(LOGIN_FRAGMENT, true);
    }

    private void showFragment(int fragmentIndex, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    // FACEBOOK EVENTS

    @Override
    public void onResume() {
        super.onResume();
        isResumed = true;

        // Call the 'activateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onResume methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.activateApp(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        isResumed = false;

        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onPause methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.deactivateApp(this);
    }

}
