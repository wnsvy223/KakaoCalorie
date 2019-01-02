package com.example.wnsvy.kakaocalorie.Service;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.wnsvy.kakaocalorie.Interface.AsyncTaskEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


// POST 방식
public class JsonPostAsyncTask extends AsyncTask<String,String,String> {

    private String restUrl;
    private JSONObject jsonObj;
    private AsyncTaskEventListener<String> callback;
    private Context context;
    public Exception mException;

    public JsonPostAsyncTask(String restUrl, JSONObject jsonObj, Context context, AsyncTaskEventListener callback) {
        this.restUrl = restUrl;   // 생성자로 넘겨받은 url에 따라 기능이 분류되므로 백그라운드 작업 메소드에서 url에 따라 분기처리
        this.jsonObj = jsonObj; // 생성자로 넘겨받은 jsonObject 데이터 -> 기능별 url에 따라 넘어오는 데이터들 분기
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... urls) {

        try {
            //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
            JSONObject jsonObject = jsonObj;
            Log.d("백그라운드", String.valueOf(jsonObject));

            HttpURLConnection con = null;
            BufferedReader reader = null;

            try{
                URL url = new URL(restUrl);
                //URL url = new URL(urls[0]);
                //연결을 함
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");//POST방식으로 보냄
                con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨  주겠다는 의미
                con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                con.connect();

                //서버로 보내기위해서 스트림 만듬
                OutputStream outStream = con.getOutputStream();
                //버퍼를 생성하고 넣음
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                writer.write(jsonObject.toString());
                writer.flush();
                writer.close();//버퍼를 받아줌

                //서버로 부터 데이터를 받음
                InputStream stream = con.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();

                String line = "";
                while((line = reader.readLine()) != null){
                    buffer.append(line);
                }

                return buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임

            } catch (MalformedURLException e){
                e.printStackTrace();
                mException = e;
            } catch (IOException e) {
                e.printStackTrace();
                mException = e;
            } finally {
                if(con != null){
                    con.disconnect();
                }
                try {
                    if(reader != null){
                        reader.close();//버퍼를 닫아줌
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    mException = e;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(callback != null) {
            if (result != null) {
                if (mException == null) {
                    callback.onSuccess(result);
                    Log.d("서버 응답 메시지", result);
                } else {
                    callback.onFailure(mException);
                }
            }
        }
    }
}
