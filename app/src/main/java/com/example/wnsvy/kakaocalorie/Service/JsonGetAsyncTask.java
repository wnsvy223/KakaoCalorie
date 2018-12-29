package com.example.wnsvy.kakaocalorie.Service;

import android.os.AsyncTask;
import android.util.Log;

import com.example.wnsvy.kakaocalorie.Model.RankModel;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


//GET 방식
public class JsonGetAsyncTask extends AsyncTask<String,String,String> {

    private String restUrl;
    private JSONObject jsonObj;

    public JsonGetAsyncTask(String restUrl, JSONObject jsonObj) {
        this.restUrl = restUrl;   // 생성자로 넘겨받은 url에 따라 기능이 분류되므로 백그라운드 작업 메소드에서 url에 따라 분기처리
        this.jsonObj = jsonObj; // 생성자로 넘겨받은 jsonObject 데이터 -> 기능별 url에 따라 넘어오는 데이터들 분기
    }


    @Override
    protected String doInBackground(String... urls) {

        try {
            HttpURLConnection con = null;
            BufferedReader reader = null;

            try{
                //URL url = new URL(urls[0]);//url을 가져온다.
                URL url = new URL(restUrl);//url을 가져온다.
                con = (HttpURLConnection) url.openConnection();
                con.connect();//연결 수행

                //입력 스트림 생성
                InputStream stream = con.getInputStream();

                //속도를 향상시키고 부하를 줄이기 위한 버퍼를 선언한다.
                reader = new BufferedReader(new InputStreamReader(stream));

                //실제 데이터를 받는곳
                StringBuffer buffer = new StringBuffer();

                //line별 스트링을 받기 위한 temp 변수
                String line = "";

                //아래라인은 실제 reader에서 데이터를 가져오는 부분이다. 즉 node.js서버로부터 데이터를 가져온다.
                while((line = reader.readLine()) != null){
                    buffer.append(line);
                }

                //다 가져오면 String 형변환을 수행한다. 이유는 protected String doInBackground(String... urls) 니까
                return buffer.toString();

                //아래는 예외처리 부분이다.
            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //종료가 되면 disconnect메소드를 호출한다.
                if(con != null){
                    con.disconnect();
                }
                try {
                    //버퍼를 닫아준다.
                    if(reader != null){
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }//finally 부분
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(result != null){
            Log.d("서버 응답(GET) :",result);
        }

    }
}
