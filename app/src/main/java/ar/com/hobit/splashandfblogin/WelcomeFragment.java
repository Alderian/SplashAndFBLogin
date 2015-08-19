package ar.com.hobit.splashandfblogin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.internal.ImageDownloader;
import com.facebook.internal.ImageRequest;
import com.facebook.internal.ImageResponse;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

/**
 * A placeholder fragment containing a simple view.
 */
public class WelcomeFragment extends Fragment {

    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String PICTURE = "picture";
    private static final String FIELDS = "fields";

    private static final String REQUEST_FIELDS =
            TextUtils.join(",", new String[]{ID, NAME, PICTURE});

    private AccessTokenTracker accessTokenTracker;
    private CallbackManager callbackManager;

    private LoginButton loginButton;
    private TextView connectedStateLabel;
    private JSONObject user;
    private Drawable userProfilePic;
    private String userProfilePicID;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                fetchUserInfo();
                updateUI();

            }
        };
        callbackManager = CallbackManager.Factory.create();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);
        loginButton = (LoginButton) view.findViewById(R.id.welcome_fragment_login_button);
        loginButton.setFragment(this);

        connectedStateLabel = (TextView) view.findViewById(R.id.welcome_fragment_profile_name);

        // if no background is set for some reason, then default to Facebook blue
        if (view.getBackground() == null) {
            view.setBackgroundColor(getResources().getColor(R.color.com_facebook_blue));
        } else {
            view.getBackground().setDither(true);
        }
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * @throws com.facebook.FacebookException if errors occur during the loading of user information
     */
    @Override
    public void onResume() {
        super.onResume();
        fetchUserInfo();
        updateUI();
    }

    private void fetchUserInfo() {
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            GraphRequest request = GraphRequest.newMeRequest(
                    accessToken, new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject me, GraphResponse response) {
                            user = me;
                            updateUI();
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString(FIELDS, REQUEST_FIELDS);
            request.setParameters(parameters);
            GraphRequest.executeBatchAsync(request);
        } else {
            user = null;
        }
    }

    private void updateUI() {
        if (!isAdded()) {
            return;
        }
        if (AccessToken.getCurrentAccessToken() != null) {
            connectedStateLabel.setTextColor(getResources().getColor(
                    R.color.welcome_fragment_connected_text_color));
            connectedStateLabel.setShadowLayer(1f, 0f, -1f,
                    getResources().getColor(
                            R.color.welcome_fragment_connected_shadow_color));

            if (user != null) {
                ImageRequest request = getImageRequest();
                if (request != null) {
                    Uri requestUri = request.getImageUri();
                    // Do we already have the right picture? If so, leave it alone.
                    if (!requestUri.equals(connectedStateLabel.getTag())) {
                        if (user.optString("id").equals(userProfilePicID)) {
                            connectedStateLabel.setCompoundDrawables(
                                    null, userProfilePic, null, null);
                            connectedStateLabel.setTag(requestUri);
                        } else {
                            ImageDownloader.downloadAsync(request);
                        }
                    }
                }
                connectedStateLabel.setText(user.optString("name"));
            } else {
                connectedStateLabel.setText(getResources().getString(
                        R.string.welcome_fragment_logged_in));
                Drawable noProfilePic = getResources().getDrawable(
                        R.mipmap.profile_default_icon);
                assert noProfilePic != null;
                noProfilePic.setBounds(0, 0,
                        getResources().getDimensionPixelSize(
                                R.dimen.welcome_fragment_profile_picture_width),
                        getResources().getDimensionPixelSize(
                                R.dimen.welcome_fragment_profile_picture_height));
                connectedStateLabel.setCompoundDrawables(null, noProfilePic, null, null);
            }
        } else {
            int textColor = getResources().getColor(
                    R.color.welcome_fragment_not_connected_text_color);
            connectedStateLabel.setTextColor(textColor);
            connectedStateLabel.setShadowLayer(0f, 0f, 0f, textColor);
            connectedStateLabel.setText(getResources().getString(
                    R.string.welcome_fragment_not_logged_in));
            connectedStateLabel.setCompoundDrawables(null, null, null, null);
            connectedStateLabel.setTag(null);
        }
    }

    private ImageRequest getImageRequest() {
        ImageRequest request;
        ImageRequest.Builder requestBuilder = new ImageRequest.Builder(
                getActivity(),
                ImageRequest.getProfilePictureUri(
                        user.optString("id"),
                        getResources().getDimensionPixelSize(
                                R.dimen.welcome_fragment_profile_picture_width),
                        getResources().getDimensionPixelSize(
                                R.dimen.welcome_fragment_profile_picture_height)));

        request = requestBuilder.setCallerTag(this)
                .setCallback(
                        new ImageRequest.Callback() {
                            @Override
                            public void onCompleted(ImageResponse response) {
                                processImageResponse(user.optString("id"), response);
                            }
                        })
                .build();
        return request;
    }

    private void processImageResponse(String id, ImageResponse response) {
        if (response != null) {
            Bitmap bitmap = response.getBitmap();
            if (bitmap != null) {
                BitmapDrawable drawable = new BitmapDrawable(
                        WelcomeFragment.this.getResources(), bitmap);
                drawable.setBounds(0, 0,
                        getResources().getDimensionPixelSize(
                                R.dimen.welcome_fragment_profile_picture_width),
                        getResources().getDimensionPixelSize(
                                R.dimen.welcome_fragment_profile_picture_height));
                userProfilePic = drawable;
                userProfilePicID = id;
                connectedStateLabel.setCompoundDrawables(null, drawable, null, null);
                connectedStateLabel.setTag(response.getRequest().getImageUri());
            }
        }
    }
}
