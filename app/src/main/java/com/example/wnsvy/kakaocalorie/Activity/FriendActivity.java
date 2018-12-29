package com.example.wnsvy.kakaocalorie.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.example.wnsvy.kakaocalorie.Adapter.FriendsListAdapter;
import com.example.wnsvy.kakaocalorie.Model.RankModel;
import com.example.wnsvy.kakaocalorie.Service.JsonGetAsyncTask;
import com.example.wnsvy.kakaocalorie.Service.JsonPostAsyncTask;
import com.example.wnsvy.kakaocalorie.Utils.Logger;
import com.example.wnsvy.kakaocalorie.R;
import com.kakao.friends.AppFriendContext;
import com.kakao.friends.response.AppFriendsResponse;
import com.kakao.friends.response.model.AppFriendInfo;
import com.kakao.kakaotalk.callback.TalkResponseCallback;
import com.kakao.kakaotalk.v2.KakaoTalkService;
import com.kakao.network.ErrorResult;
import com.example.wnsvy.kakaocalorie.Adapter.FriendsListAdapter.IFriendListCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FriendActivity extends AppCompatActivity implements IFriendListCallback {

    private FriendsInfo friendsInfo = null;
    private FriendsListAdapter adapter = null;
    private ListView listFriend;
    private final static String TAG = "FriendActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        listFriend = findViewById(R.id.listFriend);
        requestFriends();
    }

    public void testServer(List<Long> token){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("friendTokenList", token);
            JsonPostAsyncTask jsonPostAsyncTask = new JsonPostAsyncTask("http://192.168.0.29:3000/show-rank",jsonObject);
            try {
                String res = jsonPostAsyncTask.execute().get();
                Log.d("제이슨", String.valueOf(res));
                JSONArray jsonArray = new JSONArray(res);
                for(int i=0; i<jsonArray.length(); i++){ // 생성된 jsonArray를 순회하며 각 키값에 접근하여 데이터를 가져옴.
                    JSONObject object = jsonArray.getJSONObject(i);
                    String id = object.getString("userEmail");
                    String photo = object.getString("userPhoto");
                    String footstep = object.getString("footstep");
                    String distance = object.getString("distance");
                    Log.d("파싱값(아이디)", id);
                    Log.d("파싱값(사진)", photo);
                    Log.d("파싱값(걸음)", footstep);
                    Log.d("파싱값(거리)", distance);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void requestFriends() {

        friendsInfo = new FriendsInfo();
        final IFriendListCallback callback = this;
        // offset = 0, limit = 100
        AppFriendContext appFriendContext = new AppFriendContext(true, 0, 100, "asc");

        KakaoTalkService.getInstance().requestAppFriends(appFriendContext,
                new TalkResponseCallback<AppFriendsResponse>() {
                    @Override
                    public void onNotKakaoTalkUser() {
                        Toast.makeText(getApplicationContext(),"카카오톡 가입 유저가 아닙니다.",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSessionClosed(ErrorResult errorResult) {
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onNotSignedUp() {
                        //redirectSignupActivity();
                    }

                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        Logger.e("onFailure: " + errorResult.toString());
                    }

                    @Override
                    public void onSuccess(AppFriendsResponse result) {
                        // 친구 목록
                        Logger.e("Friends: " + result.getFriends().toString());
                        // context의 beforeUrl과 afterUrl이 업데이트 된 상태.
                        if(result != null){
                            friendsInfo.merge(result);
                            if(adapter ==null){
                                adapter = new FriendsListAdapter(friendsInfo.getFriendInfoList(), callback);
                                listFriend.setAdapter(adapter);
                            }else{
                                adapter.setItem(friendsInfo.getFriendInfoList());
                                adapter.notifyDataSetChanged();
                            }
                        }

                        SharedPreferences sharedPreferences = getSharedPreferences("user",MODE_PRIVATE );
                        long myToken = sharedPreferences.getLong("token", 0);
                        List<AppFriendInfo> list = result.getFriends();
                        List<Long> tokenList = new ArrayList<>();
                        tokenList.add(myToken);
                        for (int i=0; i<list.size(); i++){
                            long token = list.get(i).getId();
                            tokenList.add(token);
                        }
                        testServer(tokenList);
                        Log.d("토큰값", String.valueOf(tokenList));
                    }
                });
    }

    @Override
    public void onItemSelected(int position, AppFriendInfo friendInfo) {

    }

    @Override
    public void onPreloadNext() {

    }

    private static class FriendsInfo {
        private final List<AppFriendInfo> friendInfoList = new ArrayList<>();
        private int totalCount;
        private String id;

        public FriendsInfo() {
        }

        public List<AppFriendInfo> getFriendInfoList() {
            return friendInfoList;
        }

        public void merge(AppFriendsResponse response) {
            this.id = response.getResultId();
            this.totalCount = response.getTotalCount();
            this.friendInfoList.addAll(response.getFriends());
        }

        public String getId() {
            return id;
        }

        public int getTotalCount() {
            return totalCount;
        }
    }
}


