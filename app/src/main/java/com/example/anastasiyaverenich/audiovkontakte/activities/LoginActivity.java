package com.example.anastasiyaverenich.audiovkontakte.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.anastasiyaverenich.audiovkontakte.R;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

public class LoginActivity extends FragmentActivity implements CompoundButton.OnCheckedChangeListener {
    private boolean isResumed = false;
    private static final String[] sMyScope = new String[]{
            VKScope.AUDIO
    };
    public static boolean useAudioVkontakteWithSdk;
    int isOpenLogoutFragment = -1;
    public Switch mSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        VKSdk.wakeUpSession(this, new VKCallback<VKSdk.LoginState>() {
            @Override
            public void onResult(VKSdk.LoginState res) {
                Log.d("TAG", "onResult");
                Log.d("TAG", String.valueOf(res));
                if (isResumed) {
                    switch (res) {
                        case LoggedOut:
                            showLogin();
                            break;
                        case LoggedIn:
                            if (isOpenLogoutFragment == 1) {
                                showLogout();
                                isOpenLogoutFragment = -1;
                            } else if (isOpenLogoutFragment == 2) {
                                finish();
                            } else {
                                LoginActivity.useAudioVkontakteWithSdk = true;
                                startAudioActivity();
                            }
                            break;
                        case Pending:
                            break;
                        case Unknown:
                            break;
                    }
                }
            }

            @Override
            public void onError(VKError error) {

            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.e("TAG", "Check " + isChecked);
        Toast.makeText(this, "Отслеживание переключения: " + (isChecked ? "on" : "off"),
                Toast.LENGTH_SHORT).show();
    }

    private void showLogout() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new LogoutFragment())
                .commitAllowingStateLoss();
    }

    private void showLogin() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, new LoginFragment())
                .commitAllowingStateLoss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("TAG", "onResume");
        isResumed = true;
        if (VKSdk.isLoggedIn()) {
            if (isOpenLogoutFragment == 1) {
                showLogout();
                isOpenLogoutFragment = -1;
            } else if (isOpenLogoutFragment == 2) {
                finish();
            } else {
                LoginActivity.useAudioVkontakteWithSdk = true;
                startAudioActivity();
            }
        } else {
            showLogin();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void startAudioActivity() {
        Intent i = new Intent(getApplicationContext(), AudioActivity.class);
        i.putExtra("key", useAudioVkontakteWithSdk);
        startActivityForResult(i, 1);
        //startActivity(i);
    }

    public static class LoginFragment extends android.support.v4.app.Fragment {
        public LoginFragment() {
            super();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_login, container, false);
            v.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VKSdk.login(getActivity(), sMyScope);
                }
            });
            return v;
        }

    }

    public static class LogoutFragment extends android.support.v4.app.Fragment {
        public LogoutFragment() {
            super();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View v = inflater.inflate(R.layout.fragment_logout, container, false);
            final SwitchCompat mSwitch = (SwitchCompat) v.findViewById(R.id.monitored_switch);
            mSwitch.setChecked(true);
            if (!mSwitch.isActivated()) {
                playAudioWithSdk((LoginActivity) getActivity(), v);

            } else {
                playAudioWithoutSdk((LoginActivity) getActivity(), v);
            }
            mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    if (isChecked) {
                        playAudioWithSdk((LoginActivity) getActivity(), v);


                    } else {
                        playAudioWithoutSdk((LoginActivity) getActivity(), v);
                    }
                }
            });

            v.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VKSdk.logout();
                    if (!VKSdk.isLoggedIn()) {
                        ((LoginActivity) getActivity()).showLogin();
                    }
                }
            });
            return v;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

        Log.d("TAG", "onActivityResult");
        if (data == null) {
            return;
        }
        isOpenLogoutFragment = data.getIntExtra("date", isOpenLogoutFragment);
        Log.d("TAG", String.valueOf(isOpenLogoutFragment));

        // User passed Authorization
               /* LoginActivity.useAudioVkontakteWithSdk = true;
                startAudioActivity(LoginActivity.useAudioVkontakteWithSdk);*/
        //LoginActivity.useAudioVkontakteWithSdk = true;
        //startAudioActivity();
        //showLogout();
    }

    private static void playAudioWithSdk(final LoginActivity actiyity, View v) {
        //Toast.makeText(actiyity, "SET ON", Toast.LENGTH_SHORT).show();
        LoginActivity.useAudioVkontakteWithSdk = true;
        //((LoginActivity) getActivity()).startAudioActivity(LoginActivity.useAudioVkontakteWithSdk);
        v.findViewById(R.id.button_vkontakte).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actiyity.startAudioActivity();
            }
        });
    }

    private static void playAudioWithoutSdk(final LoginActivity actiyity, View v) {
        Toast.makeText(actiyity, "SET OFF", Toast.LENGTH_SHORT).show();
        LoginActivity.useAudioVkontakteWithSdk = false;
        v.findViewById(R.id.button_vkontakte).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //LoginActivity.useAudioVkontakteWithSdk = false;
                actiyity.startAudioActivity();
            }
        });

    }

}
