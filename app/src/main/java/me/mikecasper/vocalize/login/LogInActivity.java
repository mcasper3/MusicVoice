package me.mikecasper.vocalize.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import me.mikecasper.vocalize.MusicVoiceActivity;
import me.mikecasper.vocalize.R;
import me.mikecasper.vocalize.api.services.LogInService;
import me.mikecasper.vocalize.login.events.LogInEvent;
import me.mikecasper.vocalize.MainActivity;
import me.mikecasper.vocalize.onboarding.OnboardingActivity;
import me.mikecasper.vocalize.services.eventmanager.EventManagerProvider;
import me.mikecasper.vocalize.services.eventmanager.IEventManager;
import me.mikecasper.vocalize.services.musicplayer.events.GetPlayerStatusEvent;
import me.mikecasper.vocalize.util.Logger;

public class LogInActivity extends MusicVoiceActivity {

    public static final String IS_LOGGED_IN = "isLoggedIn";
    public static final String FIRST_LOGIN = "firstLogin";

    private static final int EULA_VERSION = 1;
    private static final String EULA_AGREED = "eulaAgreedV";
    private static final String TAG = "LogInActivity";
    private IEventManager mEventManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mEventManager = EventManagerProvider.getInstance(this);

        ImageView logInBackground = (ImageView) findViewById(R.id.logInBackground);

        Picasso.with(this)
                .load(R.drawable.log_in_background_blurred)
                .into(logInBackground, new Callback() {
                    @Override
                    public void onSuccess() {
                        showBackgroundGradient();
                        showLogInButton();
                    }

                    @Override
                    public void onError() {
                        // Ignore
                        Log.e(TAG, "Failed to load image resource");
                        showBackgroundGradient();
                        showLogInButton();
                    }
                });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.contains(EULA_AGREED + EULA_VERSION)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("vocalize v" + EULA_VERSION)
                    .setMessage(R.string.vocalize_eula)
                    .setPositiveButton(R.string.i_agree, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LogInActivity.this);
                            sharedPreferences.edit()
                                    .putBoolean(EULA_AGREED + EULA_VERSION, true)
                                    .apply();
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LogInActivity.this.onBackPressed();
                        }
                    });
            builder.create().show();
        }
    }

    private void showBackgroundGradient() {
        View backgroundGradient = findViewById(R.id.background_gradient);
        backgroundGradient.setVisibility(View.VISIBLE);
    }

    private void showLogInButton() {
        View logInButton = findViewById(R.id.log_in_button);
        logInButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // TODO look into this:
        // ViewCompat.setElevation(view, 4dp);

        mEventManager.postEvent(new GetPlayerStatusEvent());
    }

    public void onLogIn(View view) {
        mEventManager.postEvent(new LogInEvent(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LogInService.LOGIN_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                case TOKEN:
                    Logger.i(TAG, "Logged in");
                    determineNextView();
                    break;
                case ERROR:
                    Logger.e(TAG, response.getError());
                    Toast.makeText(this, R.string.login_failure, Toast.LENGTH_SHORT).show();
                    break;
                default:
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    private void determineNextView() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstLogin = sharedPreferences.getBoolean(FIRST_LOGIN, true);

        if (firstLogin) {
            sharedPreferences.edit()
                    .putBoolean(FIRST_LOGIN, false)
                    .apply();

            moveToOnboarding();
        } else {
            moveToMainView();
        }
    }

    private void moveToOnboarding() {
        Intent intent = new Intent(this, OnboardingActivity.class);
        startActivity(intent);
    }

    private void moveToMainView() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        mEventManager = null;

        super.onDestroy();
    }
}
