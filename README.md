# Checkresult（西工大本科生成绩查询APP）项目
其实这个项目在今年放寒假之前就已经完成了一部分了，当时只能查询2016-2017 秋季的成绩，昨天我进行了改进，现在可以查询所有学期的成绩。下面详细介绍一下这个APP涉及到的技术。

## 一、AsyncTask
整个程序的核心内容都在doInBackfround方法里面。里面分为如下几个步骤：

1. 模拟登陆<br>
因为教务系统的登陆不需要输入验证码，所以，要容易很多。首先获取cookie，然后再设置HttpURLConnection的cookie为之前获取的cookie值。之后，将用户和密码通过输出流写到HttpURLConnection里面，然后再次建立连接的时候就不用登陆了。
            
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
2. 利用JSoup解析HTML<br>
这部分的代码比较多，主要就是创建一个哈希映射，然后将得到的从网页上读取到的数据按照"name", "score", "semester"字段保存到map里面，然后读取完一条成绩信息以后，将这条Map保存到一个列表里面，这个列表也就是simpleAdapter绑定的list，最终，所有成绩信息全部保存到了lvlist这个列表中。最初，如果只需要获取某一学期的成绩的话，只需要将lvlist直接绑定到simpleAdapter上面，然后直接执行listview.setAdapter()就可以了，然后就可以将成绩信息一条条显示到listview上面了。但是后来要查看所有学期的成绩，就不能这么做。我采取的方法是，在AsyncTask线程中只得到lvlist这个列表，而simpleAdapter并不与lvlist这个列表绑定，而是在spinner控件的onItemSelectedListener方法中绑定的。
## 二、Spinner
这里用了Spinner控件来选择学期，Spinner绑定的数据源为一个list，在JSoup解析中查询到的每一个课程的学期都加入到这个list里面，并且去除重复。一开始用的是字符串数组，但是很难解决数组的去重和长度的问题。后来才想到list。然后，在spiner的onItemSelectListener方法中，便利lvlist中的映射，找到semester等于所选中的学期，把它加入到lvlist1这个列表中，然后，simpleAdapter与lvlist1绑定，界面上便显示出相应学期的成绩了。
        
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            lvList1.removeAll(lvList1);
            for (Map<String, String> map : lvList){
                if (map.get("semester").equals(semesterList.get(i))){
    //              System.out.println(map.get("name") + "\n" + map.get("score") + "\n");
                    lvList1.add(map);
                }
            }
            simpleAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    });
基本就是这样咯~