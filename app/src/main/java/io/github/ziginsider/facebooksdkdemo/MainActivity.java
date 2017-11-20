package io.github.ziginsider.facebooksdkdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    CallbackManager callbackManager;
    TextView txtEmail, txtBirthday, txtFriends;
    ProgressDialog mDialog;
    ImageView imgAvatar;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //printKeyHash();

        callbackManager = CallbackManager.Factory.create();

        txtBirthday = (TextView) findViewById(R.id.txt_birthday);
        txtEmail = (TextView) findViewById(R.id.txt_email);
        txtFriends = (TextView) findViewById(R.id.txt_friends);

        imgAvatar = (ImageView) findViewById(R.id.avatar);

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

        loginButton.setReadPermissions(Arrays.asList("public_profile",
                "email",
                "user_birthday",
                "user_friends"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mDialog = new ProgressDialog(MainActivity.this);
                mDialog.setMessage("Retrieving data...");
                mDialog.show();

                String accessToken = loginResult.getAccessToken().getToken();

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        mDialog.dismiss();

                        Log.d(TAG, "onCompleted: " + object.toString());
                        
                        getData(object);
                    }
                });

                //Request Graph API
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, email, birthday, friends");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        // If already login
        if (AccessToken.getCurrentAccessToken() != null) {

            //Just set User ID
            GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                     getData(object);
                }
            });

            //Request Graph API
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id, email, birthday, friends");
            request.setParameters(parameters);
            request.executeAsync();
        }
    }

    private void getData(JSONObject object) {
        try {
            URL profile_picture
                    = new URL("https://graph.facebook.com/"
                    + object.getString("id")
                    + "/picture?width=250&height=250");

            Picasso.with(this).load(profile_picture.toString()).into(imgAvatar);

            txtEmail.setText(object.getString("email"));
            txtBirthday.setText(object.getString("birthday"));
            txtFriends.setText("Friends: " + object.getJSONObject("friends")
                    .getJSONObject("summary")
                    .getString("total_count"));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void printKeyHash() {
        try {
            PackageInfo info = getPackageManager()
                    .getPackageInfo("io.github.ziginsider.facebooksdkdemo",
                            PackageManager.GET_SIGNATURES);
            for (Signature signature:info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d(TAG, "KeyHash: " + Base64.encodeToString(md.digest(),Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
