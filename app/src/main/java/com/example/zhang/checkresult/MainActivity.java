package com.example.zhang.checkresult;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
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

public class MainActivity extends AppCompatActivity {
    private EditText editun;
    private EditText editpw;
    private ListView listView;
    private String username;
    private String password;
    private Spinner spinner;
    private List<Map<String, String>> lvList;
    private List<Map<String, String>> lvList1;
    private List<String> semesterList;
    private SimpleAdapter simpleAdapter;
    private ArrayAdapter spinnerAdapter;
    private InputMethodManager imm;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("西北工业大学本科生成绩查询");
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        editpw = (EditText) findViewById(R.id.editpw);
        editun = (EditText) findViewById(R.id.editun);
        listView = (ListView) findViewById(R.id.listView);
        lvList = new ArrayList<>();
        lvList1 = new ArrayList<>();
        semesterList = new ArrayList<>();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        spinner = (Spinner) findViewById(R.id.spinner);
        spinnerAdapter = new ArrayAdapter(this, R.layout.myspinner_layout, semesterList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        simpleAdapter = new SimpleAdapter(MainActivity.this, lvList1, android.R.layout.simple_list_item_2,
                new String[]{"name", "score"}, new int[]{android.R.id.text1, android.R.id.text2});
        listView.setAdapter(simpleAdapter);

        getUserInfo(MainActivity.this);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spinner.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                lvList.removeAll(lvList);
                lvList1.removeAll(lvList1);
                simpleAdapter.notifyDataSetChanged();
                username = editun.getText().toString();
                password = editpw.getText().toString();
                saveUserInfo(MainActivity.this, username, password);
                semesterList.removeAll(semesterList);
                spinnerAdapter.notifyDataSetChanged();
                new myAsyncTask().execute();
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                lvList1.removeAll(lvList1);
                for (Map<String, String> map : lvList){
                    if (map.get("semester").equals(semesterList.get(i))){
//                        System.out.println(map.get("name") + "\n" + map.get("score") + "\n");
                        lvList1.add(map);
                    }
                }
                simpleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public static boolean saveUserInfo(Context context, String username, String password){

        SharedPreferences preferences = context.getSharedPreferences("user", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.commit();
        return true;
    }

    private void getUserInfo(Context context){
        SharedPreferences preferences = context.getSharedPreferences("user", context.MODE_PRIVATE);
        editun.setText(preferences.getString("username", null));
        editpw.setText(preferences.getString("password", null));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("关于");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder normalDialog = new AlertDialog.Builder(MainActivity.this);
        normalDialog.setTitle("关于");
        normalDialog.setMessage("By 豆腐 QQ:523213189\n本项目已开源，地址：https://github.com/rty813/Checkresult");
        normalDialog.setPositiveButton("确定", null);
        normalDialog.show();
        return super.onOptionsItemSelected(item);
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
                double sum = 0;
                double GPA = 0;
                double credit = 0;
                for (Element trTag : trTags){
                    Elements tdTags = trTag.select("td");
                    if (tdTags.size()==0){
                        continue;
                    }
                    if (tdTags.get(0).text().length() < 10){
                        continue;
                    }
                    int i = 1;
                    builder = new StringBuilder();
                    Map map = new HashMap<String, String>();
                    boolean gpaEnable = true;
                    credit = 0;
                    for (Element tdTag : tdTags){
                        switch (i){
                            case 1:
                                map.put("semester", tdTag.text());
                                if (!semesterList.contains(tdTag.text())){
                                    if (semesterList.size() > 0) {
                                        Map mapGPA = new HashMap<String, String>();
                                        mapGPA.put("name", "学分绩（不计选修课）");
                                        mapGPA.put("score", String.valueOf((float) GPA / sum));
                                        mapGPA.put("semester", semesterList.get(0));
                                        lvList.add(mapGPA);
                                        mapGPA = new HashMap<String, String>();
                                        mapGPA.put("name", "总学分（不计选修课）");
                                        mapGPA.put("score", String.valueOf(sum));
                                        mapGPA.put("semester", semesterList.get(0));
                                        lvList.add(mapGPA);
                                        GPA = 0;
                                        sum = 0;
                                    }
                                    semesterList.add(0, tdTag.text());
                                }
                                break;
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
//                                System.out.println(tdTag.text());
                                break;
                            case 6:
                                if (!gpaEnable){
                                    map.put("name", map.get("name") + "\t（选修）");
                                    i++;
                                    continue;
                                }
                                credit = Double.valueOf(tdTag.text());
                                map.put("name", map.get("name") + "\t" + credit);
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
//                                System.out.println("加入GPA");
                                try{
                                    GPA = GPA + credit * Double.valueOf(tdTag.text());
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                                break;
                        }
                        i++;
                    }
                    map.put("score", builder.toString());
//                    System.out.println(builder.toString());
                    lvList.add(map);
                }
                if (semesterList.size() == 0){
                    publishProgress();
                    return null;
                }
//                System.out.println((float)GPA / sum);
                Map mapGPA = new HashMap<String, String>();
                mapGPA.put("name", "学分绩（不计选修课）");
                mapGPA.put("score", String.valueOf((float) GPA / sum));
                mapGPA.put("semester", semesterList.get(0));
                lvList.add(mapGPA);
                mapGPA = new HashMap<String, String>();
                mapGPA.put("name", "总学分（不计选修课）");
                mapGPA.put("score", String.valueOf(sum));
                mapGPA.put("semester", semesterList.get(0));
                lvList.add(mapGPA);

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
            if (lvList.size() == 0){
                progressBar.setVisibility(View.GONE);
                return;
            }
            spinnerAdapter.notifyDataSetChanged();
            for (Map<String, String> map : lvList){
                if (map.get("semester").equals(semesterList.get(0))){
                    System.out.println(map.get("name") + "\n" + map.get("score") + "\n");
                    lvList1.add(map);
                }
            }

            simpleAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
        }
    }
}
