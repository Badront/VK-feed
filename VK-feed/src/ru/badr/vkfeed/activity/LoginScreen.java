package ru.badr.vkfeed.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.vk.sdk.*;
import com.vk.sdk.api.VKError;
import ru.badr.vkfeed.Constants;
import ru.badr.vkfeed.R;
import ru.badr.vkfeed.utils.DialogUtils;
import ru.badr.vkfeed.utils.WebUtils;

public class LoginScreen extends VKActivity {
    private static final String[] sMyScope = new String[] {
            VKScope.FRIENDS,
            VKScope.WALL,
            VKScope.NOHTTPS
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        VKSdk.initialize(
                sdkListener,
                Constants.VK_API_KEY,
                VKAccessToken.tokenFromSharedPreferences(this,Constants.VK_ACCESS_TOKEN));
        if (!VKSdk.wakeUpSession()) {
            /*openNewsFeed();
        }
        else {*/
            checkNetworkAndAuthorize();
        }
    }

    private void checkNetworkAndAuthorize() {
        if(WebUtils.isNetworkAvailable(this))
        {
            VKSdk.authorize(sMyScope, true, true);
        }
        else {
            DialogUtils.showAlert(
                    this,
                    getString(R.string.attention),
                    getString(R.string.network_error),
                    getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    },
                    getString(R.string.repeat),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            checkNetworkAndAuthorize();
                        }
                    }
            );
        }
    }

    private final VKSdkListener sdkListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {
            new VKCaptchaDialog(captchaError).show();
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            VKSdk.authorize(sMyScope);
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            new AlertDialog.Builder(LoginScreen.this)
                    .setMessage(authorizationError.errorMessage)
                    .setCancelable(false)
                    .setPositiveButton(
                            android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .show();
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            newToken.saveTokenToSharedPreferences(LoginScreen.this, Constants.VK_ACCESS_TOKEN);
            openNewsFeed();
        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
            openNewsFeed();
        }
    };

    private void openNewsFeed() {
        startActivity(new Intent(this,NewsFeedList.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_CANCELED){
            finish();
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
