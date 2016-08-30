package upp.foodonet.material;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import com.facebook.Profile;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import CommonUtilPackage.CommonUtil;
import CommonUtilPackage.InternalRequest;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String MY_TAG = "food_SignIn";

    public static final String FACEBOOK_KEY = "facebook";
    public static final String GOOGLE_KEY = "google";
    public static final String FOODONET_KEY = "foodonet";
    public static final String PHONE_KEY = "phone_number";
    public static final String AVATAR_KEY = "avatar";
    public static final String NETWORKTYPE_KEY = "networktype";
    public static final String USER_NAME = "user_name";

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_REG_PHONE_NUM = 1;
    ImageButton signInGoogle;
    public Profile profile;
    public String facebookEmail;
    ImageButton faceLogIn;
    GoogleSignInAccount account;
    TextView continueWithoutRegistration;
    ProfileTracker profileTracker;
    AccessTokenTracker accessTokenTracker;
    public CallbackManager callbackManager;
    private GoogleApiClient mGoogleApiClient;
    private boolean isFacebookTrackingStarted;
    private InternalRequest ir;

    public FacebookCallback<LoginResult> facebookCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        InitGoogleLogin();

        faceLogIn = (ImageButton) findViewById(R.id.sing_in_btn_facebook);
        faceLogIn.setOnClickListener(this);

        signInGoogle = (ImageButton) findViewById(R.id.sing_in_btn_google);
        signInGoogle.setOnClickListener(this);
        //signInGoogle.setSize(SignInButton.SIZE_STANDARD);
        //signInGoogle.setScopes(gso.getScopeArray());

        continueWithoutRegistration = (TextView) findViewById(R.id.sign_in_tv_continue);
        continueWithoutRegistration.setOnClickListener(this);

        callbackManager = CallbackManager.Factory.create();

        //new way to get SHA1 key
        //printKeyHash();

        if (profile != null) {
            Toast.makeText(this, profile.getId(), Toast.LENGTH_LONG).show();
        }
    }

    private void InitGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()//.requestIdToken()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else if (requestCode == RC_REG_PHONE_NUM) {
            switch (resultCode) {
                case RESULT_OK:
                    if(account != null)
                        CommonUtil.PutCommonPreferencesIsRegisteredGoogleFacebook(this, account);
                    else if (profile != null)
                        CommonUtil.PutCommonPreferencesIsRegisteredGoogleFacebook(this, profile);
                    String phoneNumber = data.getStringExtra(PHONE_KEY);
                    ir.PhoneNumber = phoneNumber;
                    Intent dataUserRegistrationIntent = new Intent();
                    dataUserRegistrationIntent.putExtra(InternalRequest.INTERNAL_REQUEST_EXTRA_KEY, ir);
                    setResult(RESULT_OK, dataUserRegistrationIntent);
                    finish();
                    break;
                case RESULT_CANCELED:
                    setResult(resultCode);
                    finish();
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFacebookTrackingStarted) {
            accessTokenTracker.stopTracking();
            profileTracker.stopTracking();
            isFacebookTrackingStarted = false;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sing_in_btn_facebook:
                //Toast.makeText(this,"face",Toast.LENGTH_LONG).show();
                facebookCallback = InitFacebookCallback();
                LoginManager manager = LoginManager.getInstance();
                manager.registerCallback(callbackManager, facebookCallback);
                manager.logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends", "email"));

                break;
            case R.id.sing_in_btn_google:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
                //Toast.makeText(this,"google",Toast.LENGTH_LONG).show();

                break;
            case R.id.sign_in_tv_continue:
                // Toast.makeText(this,"continue",Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
                break;
        }

    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private FacebookCallback<LoginResult> InitFacebookCallback() {
        FacebookSdk.sdkInitialize(getApplicationContext());

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {

            }
        };
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                //profile changes following
            }
        };

        accessTokenTracker.startTracking();
        profileTracker.startTracking();
        isFacebookTrackingStarted = true;

        return new FacebookCallback<LoginResult>() {
            private ContentResolver contentResolver;

            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                // Application code
                                try {
                                    facebookEmail = object.getString("email");
                                    Log.i(MY_TAG, "facebook email: " + facebookEmail);
                                    profile = Profile.getCurrentProfile();
                                    //CommonUtil.PutCommonPreferencesIsRegisteredGoogleFacebook(this, profile);
                                    CreateFacebookInternalRequest();
                                    ContinueAfterFacebookLogin();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link,email");
                request.setParameters(parameters);
                request.executeAsync();

                if(Profile.getCurrentProfile() == null){
                    profileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                            profileTracker.stopTracking();
                        }
                    };
                }
                else {
                }

                //Toast.makeText(getApplicationContext(), profile.getFirstName() + " is logged to Facebook", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "Login error", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            account = result.getSignInAccount();
            //CommonUtil.PutCommonPreferencesIsRegisteredGoogleFacebook(this, account);
            CreateGoogleInternalRequest();
            Intent regPhoneIntent = new Intent(getApplicationContext(), RegisterPhoneActivity.class);
            regPhoneIntent.putExtra(AVATAR_KEY, ir.PhotoURL);
            regPhoneIntent.putExtra(NETWORKTYPE_KEY, ir.SocialNetworkType);
            regPhoneIntent.putExtra(USER_NAME, ir.UserName);
            startActivityForResult(regPhoneIntent, RC_REG_PHONE_NUM);
            //Toast.makeText(this, acct.getDisplayName(), Toast.LENGTH_LONG).show();
            /*mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            updateUI(true);*/
        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
            Toast.makeText(this, "Login error", Toast.LENGTH_LONG).show();
        }
    }

    private void CreateGoogleInternalRequest()
    {
        ir = new InternalRequest(InternalRequest.ACTION_POST_NEW_USER);
        ir.SocialNetworkType = GOOGLE_KEY;
        ir.SocialNetworkID = account.getId();
        ir.SocialNetworkToken = "token1"; //account.getIdToken();
        ir.PhoneNumber = "0545645";
        ir.Email = account.getEmail();
        ir.UserName = account.getDisplayName();
        ir.IsLoggedIn = true;
        ir.DeviceUUID = CommonUtil.GetIMEI(this);
        ir.ServerSubPath = getString(R.string.server_post_register_user);
        Uri photoUri = account.getPhotoUrl();
        if(photoUri != null)
            ir.PhotoURL = photoUri.toString();
    }


//    @Override
//    public void onSuccess(LoginResult loginResult) {
//    }

    public void ContinueAfterFacebookLogin(){
        Intent regPhoneIntent = new Intent(getApplicationContext(), RegisterPhoneActivity.class);
        regPhoneIntent.putExtra(NETWORKTYPE_KEY, ir.SocialNetworkType);
        regPhoneIntent.putExtra(USER_NAME, ir.UserName);
        regPhoneIntent.putExtra(AVATAR_KEY, ir.PhotoURL);
        startActivityForResult(regPhoneIntent, RC_REG_PHONE_NUM);
    }

    public void CreateFacebookInternalRequest()
    {
        ir = new InternalRequest(InternalRequest.ACTION_POST_NEW_USER);
        ir.SocialNetworkType = FACEBOOK_KEY;
        ir.SocialNetworkID = profile.getId();
        ir.SocialNetworkToken = "token1";
        ir.PhoneNumber = "";
        ir.Email = TextUtils.isEmpty(facebookEmail) ? "" : facebookEmail;
        ir.UserName = profile.getName();
        ir.IsLoggedIn = true;
        ir.DeviceUUID = CommonUtil.GetIMEI(this);
        ir.PhotoURL = profile.getProfilePictureUri(100, 100).getPath();
        ir.ServerSubPath = getString(R.string.server_post_register_user);
    }

//    @Override
//    public void onCancel() {
//
//    }
//
//    @Override
//    public void onError(FacebookException error) {
//
//    }

    private void printKeyHash() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo("upp.foodonet", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d(MY_TAG, "KeyHash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(MY_TAG, e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e(MY_TAG, e.toString());
        }
    }
}

