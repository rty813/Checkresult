package com.example.zhang.checkresult;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

public class MainActivity extends AppCompatActivity {
    private EditText editun;
    private EditText editpw;
    private ListView listView;
    private String username;
    private String password;
    private List<Map<String, String>> lvList;
    private SimpleAdapter simpleAdapter;
    private InputMethodManager imm;
    private ProgressBar progressBar;
    private double GPA;
    private double sum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("2016-2017秋季成绩查询");
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        editpw = (EditText) findViewById(R.id.editpw);
        editun = (EditText) findViewById(R.id.editun);
        listView = (ListView) findViewById(R.id.listView);
        lvList = new ArrayList<>();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        simpleAdapter = new SimpleAdapter(MainActivity.this, lvList, android.R.layout.simple_list_item_2,
                new String[]{"name", "score"}, new int[]{android.R.id.text1, android.R.id.text2});
        listView.setAdapter(simpleAdapter);

        getTxtFileInfo(MainActivity.this);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                lvList.removeAll(lvList);
                simpleAdapter.notifyDataSetChanged();
                username = editun.getText().toString();
                password = editpw.getText().toString();
                saveUserInfo(MainActivity.this, username, password);
                new myAsyncTask().execute();
            }
        });
    }

    public static boolean saveUserInfo(Context context, String username, String password){
        try{
            File file = new File(context.getFilesDir(),"userinfo.txt");
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write((username + "##" + password).getBytes());
            outputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void getTxtFileInfo(Context context){
        try {
            File file = new File(context.getFilesDir(),"userinfo.txt");
            FileInputStream inputStream = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String content = br.readLine();
            String[] contents = content.split("##");
            username = contents[0];
            password = contents[1];
            editpw.setText(password);
            editun.setText(username);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class myAsyncTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String param = "username=" + username + "&password=" + password;
                URL url = new URL("http://us.nwpu.edu.cn/eams/login.action");

                //获取Cookie
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36 OPR/42.0.2393.94 (Edition Baidu)");
                String cookie = connection.getHeaderField("Set-Cookie");
                System.out.println(connection.getHeaderFields());

                //模拟登陆
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Cookie", cookie);

                OutputStream os = connection.getOutputStream();
                os.write(param.getBytes("GBK"));
                os.close();

                //并不知道为什么加这个，但是不加这个下面就无法正常获取数据
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                br.close();


                //获取成绩
                url = new URL("http://us.nwpu.edu.cn/eams/teach/grade/course/person!historyCourseGrade.action?projectType=MAJOR");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Cookie", cookie);

                //获取网页源代码
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line = "";
                while ((line = reader.readLine()) != null){
                    builder.append(line + '\n');
                }
                reader.close();

                //利用JSoup解析
                Document document = Jsoup.parse(builder.toString());
                Elements trTags = document.select("tr");
                if (trTags.size() == 5){
                    publishProgress();
                }
                sum = 0;
                GPA = 0;
                for (Element trTag : trTags){
                    Elements tdTags = trTag.select("td");
                    if (tdTags.size()==0){
                        continue;
                    }
                    if (!tdTags.get(0).text().equals("2016-2017 秋")){
                        continue;
                    }
                    int i = 1;
                    builder = new StringBuilder();
                    Map map = new HashMap<String, String>();
                    boolean gpaEnable = true;
                    double credit = 0;
                    for (Element tdTag : tdTags){
                        switch (i){
                            case 2:
//                                System.out.println(tdTag.text());
                                if (tdTag.text().charAt(3) == 'L'){
                                    gpaEnable = false;
                                }else{
                                    gpaEnable = true;
                                }
                                break;
                            case 4:
                                map.put("name",tdTag.text());
                                System.out.println(tdTag.text());
                                break;
                            case 6:
                                if (!gpaEnable){
                                    i++;
                                    continue;
                                }
                                credit = Double.valueOf(tdTag.text());
                                sum += credit;
                                break;
                            case 7:
                                builder.append("平时成绩：" + tdTag.text() + '\n');
                                break;
                            case 8:
                                builder.append("期中成绩：" + tdTag.text() + '\n');
                                break;
                            case 10:
                                builder.append("期末成绩：" + tdTag.text() + '\n');
                                break;
                            case 11:
                                builder.append("总评成绩：" + tdTag.text());
                                break;
                            case 12:
                                if (!gpaEnable){
                                    i++;
                                    continue;
                                }
                                System.out.println("加入GPA");
                                GPA = GPA + credit * Double.valueOf(tdTag.text());
                                break;
                        }
                        i++;
                    }
                    map.put("score", builder.toString());
                    System.out.println(builder.toString());
                    lvList.add(map);
                }
                System.out.println((float)GPA / sum);
                Map map = new HashMap<String, String>();
                map.put("name","学分绩");
                map.put("score",String.valueOf((float)GPA / sum));
                lvList.add(0,map);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            Toast.makeText(MainActivity.this,"请检查用户名和密码",Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            simpleAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }
    }
}
