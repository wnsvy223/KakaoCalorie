package com.example.wnsvy.kakaocalorie.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import com.example.wnsvy.kakaocalorie.R;
import com.example.wnsvy.kakaocalorie.Service.JsonPostAsyncTask;
import com.kakao.auth.AccessTokenCallback;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.network.ErrorResult;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        getHashKey(mContext); // 카카오 API 연동시 필요한 해시키값 생성

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
        }else{
            goToLoginActivity(); // 세션이 유지된 상태면 다음 액티비티 이동
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }

    private void goToLoginActivity() {
        UserManagement.getInstance().me(new MeV2ResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                //Toast.makeText(getApplicationContext(),"카카오 세션 Close!.",Toast.LENGTH_SHORT).show();
                Log.d(TAG,"카카오 세션 Close!");
            }

            @Override
            public void onSuccess(MeV2Response result) {
                String url = result.getProfileImagePath();
                String id = result.getNickname();
                long token = result.getId();
                Log.d("토큰", String.valueOf(token));
                UserAccount userAccount = result.getKakaoAccount();
                handleScopeError(userAccount,url,id,token);
            }
        });

    }

    private void handleScopeError(UserAccount account,String url, String id, long token) {
        OptionalBoolean hasEmail = account.hasEmail();
        if(hasEmail == OptionalBoolean.TRUE){
            email = account.getEmail();
            Log.d("이메일",email);
        }
        List<String> neededScopes = new ArrayList<>();
        if (account.needsScopeAccountEmail()) {
            neededScopes.add("account_email");
        }
        if (account.needsScopeGender()) {
            neededScopes.add("gender");
        }
        Session.getCurrentSession().updateScopes(this, neededScopes, new
                AccessTokenCallback() {
                    @Override
                    public void onAccessTokenReceived(AccessToken accessToken) {
                        // 유저에게 성공적으로 동의를 받음. 토큰을 재발급 받게 됨.

                        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("email", email);
                        editor.putLong("token",token);
                        editor.apply();   // 서비스 클래스에서 유저 정보값 쓰기위해 로그인 할 때 SharedPreference에 저장

                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.accumulate("userID", id );
                            jsonObject.accumulate("userPhoto",  url);
                            jsonObject.accumulate("userEmail",  email);
                            jsonObject.accumulate("token",  token);
                            //new JsonPostAsyncTask("http://192.168.35.67:3000/create-user",jsonObject).execute();
                            new JsonPostAsyncTask("http://192.168.0.29:3000/create-user",jsonObject).execute();
                            // 유저 테이블 생성 및 로그인 관련 노드서버와 통신
                            // 통신에 필요한 url과 데이터값을 JsonObject 만들어 생성자에 넘겨줌.

                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            intent.putExtra("profileImage",url);
                            intent.putExtra("id",id);
                            intent.putExtra("email",email);
                            intent.putExtra("token",token);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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

}
