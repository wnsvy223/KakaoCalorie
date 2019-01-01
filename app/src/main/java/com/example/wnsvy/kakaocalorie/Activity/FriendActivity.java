package com.example.wnsvy.kakaocalorie.Activity;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.example.wnsvy.kakaocalorie.Adapter.FriendsListAdapter;
import com.example.wnsvy.kakaocalorie.Fragment.RankFragment;
import com.example.wnsvy.kakaocalorie.Model.RankModel;
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


