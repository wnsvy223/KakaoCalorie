package com.example.wnsvy.kakaocalorie.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.example.wnsvy.kakaocalorie.Interface.AsyncTaskEventListener;
import com.example.wnsvy.kakaocalorie.R;
import com.example.wnsvy.kakaocalorie.Service.JsonPostAsyncTask;
import com.kakao.auth.AccessTokenCallback;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private ISessionCallback callback;
    private static String TAG = "KAKAO_TAG";
    private String email;
    private JsonPostAsyncTask jsonPostAsyncTask;
    public Button customKakaoLogin;
    public LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        getHashKey(mContext); // 카카오 API 연동시 필요한 해시키값 생성
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        callback = new ISessionCallback() {
            @Override
            public void onSessionOpened() {
                goToLoginActivity();
                Log.d(TAG,"세션오픈 <성공>");
            }

            @Override
            public void onSessionOpenFailed(KakaoException exception) {
                setContentView(R.layout.activity_main);
                Log.d(TAG,"세션오픈 <실패>");
            }
        };
        Session.getCurrentSession().addCallback(callback);

        if(!Session.getCurrentSession().checkAndImplicitOpen()){
            setContentView(R.layout.activity_main); // 세션이 유지된 상태가 아니면 메인 액티비티 화면 세팅
            customKakaoLogin = findViewById(R.id.buttonkakao);
            loginButton = findViewById(R.id.btn_kakao_login);
            customKakaoLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loginButton.performClick();
                }
            });
        }else{
            goToLoginActivity(); // 세션이 유지된 상태면 다음 액티비티 이동
        }
    }

    private void goToLoginActivity() {
        UserManagement.getInstance().me(new MeV2ResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Log.d(TAG,"카카오 세션 Close!");
            }

            @Override
            public void onSuccess(MeV2Response result) {
                String url = result.getProfileImagePath();
                String id = result.getNickname();
                long token = result.getId();
                Log.d("토큰", String.valueOf(token));
                UserAccount userAccount = result.getKakaoAccount();
                checkNeedScopes(userAccount,url,id,token);
            }
        });

    }

    private void checkNeedScopes(UserAccount account, String url, String id, long token) {
        OptionalBoolean hasEmail = account.hasEmail();
        if(hasEmail == OptionalBoolean.TRUE){
            email = account.getEmail();
            Log.d("이메일",email);
        }
        List<String> neededScopes = new ArrayList<>(); // 동의 화면에서 요청하는 추가 요구사항 항목 리스트
        if (account.needsScopeAccountEmail()) {
            neededScopes.add("account_email");
        }
        if (account.needsScopeGender()) {
            neededScopes.add("gender");
        }
        Session.getCurrentSession().updateScopes(this, neededScopes, new AccessTokenCallback() {
                    @Override
                    public void onAccessTokenReceived(AccessToken accessToken) {
                        // 유저에게 성공적으로 동의를 받음. 토큰을 재발급 받게 됨.

                        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("email", email);
                        editor.putLong("token",token);
                        editor.putString("profileUrl",url);
                        editor.apply();   // 서비스 클래스에서 유저 정보값 쓰기위해 로그인 할 때 SharedPreference에 저장

                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.accumulate("userID", id );
                            jsonObject.accumulate("userPhoto",  url);
                            jsonObject.accumulate("userEmail",  email);
                            jsonObject.accumulate("token",  token);

                            // 유저 테이블 생성 및 로그인 관련 노드서버와 통신
                            // 통신에 필요한 url과 데이터값을 JsonObject 만들어 생성자에 넘겨줌.
                            jsonPostAsyncTask = new JsonPostAsyncTask(
                                    "http://192.168.0.29:3000/create-user"
                                    , jsonObject
                                    ,getApplicationContext(), new AsyncTaskEventListener<String>() {
                                @Override
                                public void onSuccess(String result) {
                                    // 성공
                                    Log.d("AsyncTaskCallBack","success" + result);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    // 실패 - 세션종료, 비동기 실행 취소
                                    Session.getCurrentSession().removeCallback(callback);
                                    jsonPostAsyncTask.cancel(true);
                                    Log.d("AsyncTaskCallBack","fail"+ e);
                                }
                            });
                            jsonPostAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Intent intent = new Intent(MainActivity.this, UserDataActivity.class);
                        intent.putExtra("profileImage",url);
                        intent.putExtra("id",id);
                        intent.putExtra("email",email);
                        intent.putExtra("token",token);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                    }

                    @Override
                    public void onAccessTokenFailure(ErrorResult errorResult) {
                        // 동의 얻기 실패
                    }
                });
    }

    @Nullable
    public static String getHashKey(Context context) {
        final String TAG = "KeyHash";
        String keyHash = null;
        try {
            PackageInfo info =
                    context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                keyHash = new String(Base64.encode(md.digest(), 0));
                Log.d(TAG, keyHash);
            }
        } catch (Exception e) {
            Log.e("name not found", e.toString());
        }

        if (keyHash != null) {
            return keyHash;
        } else {
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
        try
        {
            if (jsonPostAsyncTask.getStatus() == AsyncTask.Status.RUNNING)
            {
                jsonPostAsyncTask.cancel(true);
                Log.d("jsonPostAsyncTask(Login)","Success cancel AsyncTask");
            }
        }
        catch (Exception e)
        {
            Log.d("jsonPostAsyncTask(Login)", String.valueOf(e));
        }
    }

}
