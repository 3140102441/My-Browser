package com.example.asus.mybrowser;

import android.Manifest;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Picture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.sax.StartElementListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;

import android.text.TextWatcher;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;



import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import java.util.Locale;
import java.util.logging.Logger;


public class MainActivity extends AppCompatActivity {
    public static final int SHOW_LOCATION = 0;
    public static String currentposition = "";
    private Button button;
    private Button button2;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText editText;
    private WebView webview;
    private EditText editText2;
    private TextView textview;
    private ProgressBar progressbar;
    private Button button_back;
    private Button button_forward;
    private Button button_origin;
    private Button button_label;
    private Button button_setting;
    private SensorManager sensorManager;
    private int currentindex = 0;
    private int flag = 0, flag1 = 0;
    private ListView listview;
    private ListView listview_setting;
    private ListView listview_history;
    private int flag_load = 0;
    private ArrayList<String> labellist = new ArrayList<String>();
    private ArrayList<String> URLlist = new ArrayList<String>();
    private ArrayList<String> setlist = new ArrayList<String>();
    private ArrayList<String> history = new ArrayList<String>();
    private ArrayList<String> history_title = new ArrayList<String>();
    private URL url;
    private String urlsource;
    private TextView textView_empty;
    private int flag_his = 0, flag_label = 0, flag_set = 0;
    public static final int SHOW_RESPONSE = 0;
    private FrameLayout frame;
    private LocationManager locationManager;
    private String provider;
    public double Latitude, Longiture;
    public String title;
    private int flag_x = 0,flag_quit = 0;
    private int Operation = 0;
    private ArrayList<Integer>  op = new ArrayList<Integer>();

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_RESPONSE:
                    final String responce = (String) msg.obj;
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle(title);
                    dialog.setMessage(responce);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("Copy", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            // 将文本内容放到系统剪贴板里。
                            cm.setText(responce);
                        }
                    });
                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dialog.show();

            }
        }
    };


  private void showLocation(Location location){
         Latitude = location.getLatitude();
      Longiture = location.getLongitude();
  }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void sendRequestWithHttpURLConnection(final String s) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(s);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept-Language", "zh-CN");//设置消息的类型
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);
                    InputStream in = conn.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder responce = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responce.append(line);
                    }
                    if (s.contains("&sensor=true")) {
                        JSONObject jsonObject = new JSONObject(responce.toString());
                        //获取results节点下的位置信息
                        JSONArray resultArray = jsonObject.getJSONArray("results");
                        if (resultArray.length() > 0) {
                            JSONObject obj = resultArray.getJSONObject(0);
                            //取出格式化后的位置数据
                            String address = obj.getString("formatted_address");

                            address = "纬度： "+ Latitude + "\n" + "经度："+ Longiture + "\n" + address;
                            Message message = new Message();
                            message.what = SHOW_RESPONSE;
                            message.obj = address;
                            handler.sendMessage(message);
                        }
                    } else {
                        Message message = new Message();
                        message.what = SHOW_RESPONSE;
                        message.obj = responce.toString();
                        handler.sendMessage(message);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (conn != null)
                        conn.disconnect();
                }
            }
        }).start();
    }


    public void UpdateUI(View view) {
        if (!(view instanceof EditText) && !(view instanceof ListView)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    // TODO Auto-generated method stub
                    flag_label = 0;
                    flag_set = 0;
                    editText.clearFocus();
                    editText2.clearFocus();
                    listview.setVisibility(listview.INVISIBLE);
                    listview_setting.setVisibility(listview_setting.INVISIBLE);
                    HideSoftKeyboard.hideSoftKeyboard(MainActivity.this);
                    frame.setVisibility(frame.INVISIBLE);
                    flag1 = 0;
                    flag_quit = 0;

                    if(Operation == 1 || Operation == 2 ||Operation ==5) {
                        Operation = 0;
                        op.remove(op.size() - 1);
                    }

                    if (currentindex == 0) {
                        editText.getText().clear();
                        editText2.getText().clear();
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editText2.getLayoutParams();
                        params.topMargin = 700;
                        editText2.setLayoutParams(params);
                    } else {
                        if (flag_his == 1) {
                            editText.setText("历史记录");
                        } else
                            editText.setText(webview.getTitle());
                    }

                    return false;
                }
            });
        } else if (!(view instanceof ListView)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    // TODO Auto-generated method stub
                    listview.setVisibility(listview.INVISIBLE);
                    listview_setting.setVisibility(listview_setting.INVISIBLE);
                    listview_history.setVisibility(listview_history.INVISIBLE);
                    return false;
                }
            });
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                if (!(innerView instanceof Button))
                    UpdateUI(innerView);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(listener);
        }
        super.onDestroy();
        save(history,history_title);
    }

    private void save(ArrayList<String> history, ArrayList<String> history_title) {
        int i =0;
        SharedPreferences.Editor editor = getSharedPreferences("lutaotao",MODE_PRIVATE).edit();

        editor.putInt("labelsize",labellist.size());
        for(i=0;i<labellist.size()-2;i++)
            editor.putString("labeltitle"+i,labellist.get(i));
        editor.putInt("labelurlsize",URLlist.size());
        for(i=0;i<URLlist.size()-2;i++){
            editor.putString("labelurl"+i,URLlist.get(i));
        }
        editor.putInt("historysize",history.size());
        for(i=0;i<history.size();i++)
        editor.putString("history"+i,history.get(i));
        editor.putInt("historytitlesize",history_title.size());
        for(i=0;i<history_title.size();i++)
            editor.putString("history_title"+i,history_title.get(i));
        editor.commit();
    }

    private SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float xValue = Math.abs(event.values[0]);
            float yValue = Math.abs(event.values[1]);
            float zValue = Math.abs(event.values[2]);
            if (xValue > 18 || yValue > 18 || zValue > 18) {
                save(history,history_title);
                System.exit(0);
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            if (op.size() > 0) {
                flag_quit = 0;
                Operation = op.get(op.size()-1);
                op.remove(op.size() - 1);
                if (Operation == 1) {
                    HideSoftKeyboard.hideSoftKeyboard(MainActivity.this);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editText2.getLayoutParams();
                    params.topMargin = 700;
                    editText2.setLayoutParams(params);
                } else if (Operation == 2) {
                    flag_label = 0;
                    frame.setVisibility(frame.INVISIBLE);
                    listview.setVisibility(listview.INVISIBLE);
                } else if (Operation == 3) {
                    labellist.remove(0);
                    button_back.performClick();
                } else if (Operation == 4) {
                    button_back.performClick();
                }else if(Operation ==5){
                    flag_set = 0;
                    listview_setting.setVisibility(listview_setting.INVISIBLE);
                }
                Operation = 0;
            }
            else{
                if(!webview.getUrl().equals("about:blank")||editText.getText().toString().equals("历史记录")){
                    button_back.performClick();
                }
                else {
                    if (flag_quit == 0) {
                        Toast.makeText(MainActivity.this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
                        flag_quit = 1;
                    } else {
                        save(history,history_title);
                        System.exit(0);
                    }
                }
            }
        }
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        button = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        editText = (EditText) findViewById(R.id.edit_text);
        editText2 = (EditText) findViewById(R.id.EditText2);
        textview = (TextView) findViewById(R.id.textView);
        progressbar = (ProgressBar) findViewById(R.id.progressBar);
        button_back = (Button) findViewById(R.id.button_back);
        button_forward = (Button) findViewById(R.id.button_forward);
        button_label = (Button) findViewById(R.id.button_label);
        button_setting = (Button) findViewById(R.id.button_setting);
        button_origin = (Button) findViewById(R.id.button_origin);
        frame = (FrameLayout) findViewById(R.id.frame);
        frame.setVisibility(frame.INVISIBLE);
        progressbar.setVisibility(progressbar.INVISIBLE);
        listview = (ListView) findViewById(R.id.listView);
        listview_setting = (ListView) findViewById(R.id.listView2);
        listview_history = (ListView) findViewById(R.id.listView3);
        textView_empty = (TextView) findViewById(R.id.textView2);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        labellist.add("主页");
        labellist.add("                               新建标签");
        listview.setVisibility(listview.INVISIBLE);
        listview_setting.setVisibility(listview_setting.INVISIBLE);
        listview_history.setVisibility(listview_setting.INVISIBLE);
        textView_empty.setVisibility(textView_empty.INVISIBLE);
        URLlist.add("");
        URLlist.add("");
        setlist.add("    查看源码");
        setlist.add("    历史记录");
        setlist.add("    删除浏览记录");
        setlist.add("    查看位置");
        setlist.add("    网页截图");
        setlist.add("    指南针");
        setlist.add("    生成二维码");

        if(flag_load ==0) {
            load();
            flag_load = 1;
        }


        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_container);
               //设置刷新时动画的颜色，可以设置4个
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                     @Override
                     public void onRefresh() {
                         webview.loadUrl(webview.getUrl());
                      // TODO Auto-generated method stub
                        /*new Handler().postDelayed(new Runnable() {
                  @Override
                    public void run() {
                      swipeRefreshLayout.setRefreshing(false);
                                           }
                          }, 6000);*/
           }
                   });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);



        webview = (WebView) findViewById(R.id.web_view);
        webview.getSettings().setJavaScriptEnabled(true);
        final WebSettings webSettings = webview.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webview.setWebViewClient(new WebViewClient());
        webview.loadUrl("");
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressbar.setVisibility(View.INVISIBLE);
                } else {
                    if (View.INVISIBLE == progressbar.getVisibility()) {
                        progressbar.setVisibility(View.VISIBLE);
                    }
                    progressbar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }


        });

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefreshLayout.setRefreshing(false);
                flag_label = 0;
                flag_set = 0;
                editText2.getText().clear();
                textView_empty.setVisibility(textView_empty.INVISIBLE);
                frame.setVisibility(frame.INVISIBLE);

                if(op.size()>0) {
                    if (op.get(op.size() - 1).equals(1)) {
                        op.remove(op.size() - 1);
                        Operation = 0;
                    }
                }

                if (!history.contains(webview.getUrl()) && !webview.getUrl().equals("about:blank") && !webview.getTitle().equals(webview.getUrl()) && webview.getTitle().length() < 20) {
                    history.add(webview.getUrl());
                    history_title.add(webview.getTitle());
                }
                if (webview.getUrl().equals("about:blank")) {
                    if (flag_his == 1) {
                        listview_history.setVisibility(listview_history.VISIBLE);
                        editText.setText("历史记录");
                        if (history.size() == 0)
                            textView_empty.setVisibility(textView_empty.VISIBLE);
                    } else {
                        currentindex = 0;
                        editText2.setVisibility(editText2.VISIBLE);
                        button2.setVisibility(button2.VISIBLE);
                        textview.setVisibility(textview.VISIBLE);
                        editText.getText().clear();
                        editText.setHint("主页");
                    }
                } else {
                    flag = 0;
                    currentindex = 1;
                    editText.setHint("");
                    editText.setText(webview.getTitle());
                }
                super.onPageFinished(view, url);
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button:
                        flag_label = 0;
                        flag_set = 0;
                        String inputText = editText.getText().toString();
                        if (!inputText.contains(".com") && !inputText.contains(".cn"))
                            inputText = "http://www.baidu.com/s?&ie=utf-8&oe=UTF-8&wd=" + inputText;
                        else if (!inputText.contains("www.") && !inputText.contains("http://"))
                            inputText = "http://www." + inputText;
                        else if (inputText.contains("www.") && !inputText.contains("http://"))
                            inputText = "http://" + inputText;
                        else if (!inputText.contains("www.") && inputText.contains("http://"))
                            inputText.replace("http://", "http://www.");

                        editText.clearFocus();
                        HideSoftKeyboard.hideSoftKeyboard(MainActivity.this);
                        if (editText.getText().toString().contains("- 百度")) {
                            webview.loadUrl(webview.getUrl());
                        } else
                            webview.loadUrl(inputText);

                        editText2.setVisibility(editText2.GONE);
                        button2.setVisibility(button2.GONE);
                        textview.setVisibility(textview.INVISIBLE);
                        listview.setVisibility(listview.INVISIBLE);
                        listview_history.setVisibility(listview_history.INVISIBLE);
                        listview_setting.setVisibility(listview_setting.INVISIBLE);
                        /*button_back.setVisibility(button_back.GONE);
                        button_forward.setVisibility(button_forward.GONE);
                        button_label.setVisibility(button_label.GONE);
                        button_setting.setVisibility(button_setting.GONE);*/
                        break;
                    default:
                        break;
                }

            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button2:
                        flag_label = 0;
                        flag_set = 0;
                        HideSoftKeyboard.hideSoftKeyboard(MainActivity.this);
                        String inputText = editText2.getText().toString();
                        inputText = "http://www.baidu.com/s?&ie=utf-8&oe=UTF-8&wd=" + inputText;
                        webview.loadUrl(inputText);
                        editText2.setVisibility(editText2.GONE);
                        button2.setVisibility(button2.GONE);
                        textview.setVisibility(textview.INVISIBLE);
                        listview.setVisibility(listview.INVISIBLE);
                        listview_history.setVisibility(listview_history.INVISIBLE);
                        listview_setting.setVisibility(listview_setting.INVISIBLE);
                        /*button_back.setVisibility(button_back.GONE);
                        button_forward.setVisibility(button_forward.GONE);
                        button_label.setVisibility(button_label.GONE);
                        button_setting.setVisibility(button_setting.GONE);*/
                        break;
                    default:
                        break;
                }
            }
        });

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editText.performClick();
                }
            }
        });

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.edit_text:
                        Operation = 1;
                        op.add(1);
                        flag_label = 0;
                        flag_set = 0;
                        frame.setVisibility(frame.VISIBLE);
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editText2.getLayoutParams();
                        params.topMargin = 500;
                        editText2.setLayoutParams(params);
                        if (flag1 == 0) {
                            editText.setText(webview.getUrl());
                            editText.selectAll();
                            flag1 = 1;
                        }
                        break;
                    default:
                        break;
                }

            }
        });

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    HideSoftKeyboard.hideSoftKeyboard(MainActivity.this);
                    button.performClick();
                    return true;
                }
                return false;
            }
        });


        editText2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editText2.performClick();
                }
            }
        });

        editText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.EditText2:
                        Operation = 1;
                        op.add(1);
                        flag_label = 0;
                        flag_set = 0;
                        editText.getText().clear();
                        frame.setVisibility(frame.INVISIBLE);
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editText2.getLayoutParams();
                        params.topMargin = 500;
                        editText2.setLayoutParams(params);
                        break;
                    default:
                        break;
                }

            }
        });
        editText2.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    HideSoftKeyboard.hideSoftKeyboard(MainActivity.this);
                    button2.performClick();
                    return true;
                }
                return false;
            }
        });

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button_back:
                        flag_his--;
                        flag_label = 0;
                        flag_set = 0;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editText2.getLayoutParams();
                        params.topMargin = 700;
                        editText2.setLayoutParams(params);
                        if (webview.canGoBack()) {
                            webview.goBack();
                            textview.setVisibility(textview.INVISIBLE);
                            button2.setVisibility(button2.INVISIBLE);
                            editText2.setVisibility(editText2.INVISIBLE);
                            listview.setVisibility(listview.INVISIBLE);
                            listview_history.setVisibility(listview_history.INVISIBLE);
                            listview_setting.setVisibility(listview_setting.INVISIBLE);
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        button_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button_forward:
                        flag_his++;
                        flag_label = 0;
                        flag_set = 0;
                        if (webview.canGoForward() && flag != 1) {
                            webview.goForward();
                            editText2.setVisibility(editText2.GONE);
                            button2.setVisibility(button2.GONE);
                            textview.setVisibility(textview.INVISIBLE);
                            listview.setVisibility(listview.INVISIBLE);
                            listview_history.setVisibility(listview_history.INVISIBLE);
                            listview_setting.setVisibility(listview_setting.INVISIBLE);
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        button_origin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button_origin:
                        flag_label = 0;
                        flag_set = 0;
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editText2.getLayoutParams();
                        params.topMargin = 700;
                        editText2.setLayoutParams(params);
                        currentindex = 0;
                        flag = 1;
                        flag_his--;
                        webview.loadUrl("");
                        editText2.setVisibility(editText2.VISIBLE);
                        button2.setVisibility(button2.VISIBLE);
                        textview.setVisibility(textview.VISIBLE);
                        listview.setVisibility(listview.INVISIBLE);
                        listview_history.setVisibility(listview_history.INVISIBLE);
                        listview_setting.setVisibility(listview_setting.INVISIBLE);
                        HideSoftKeyboard.hideSoftKeyboard(MainActivity.this);
                        break;
                    default:
                        break;
                }
            }
        });

        button_label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button_label:
                        Operation = 2;
                        op.add(2);
                        flag_set = 0;
                        if (flag_label == 1) {
                            listview.setVisibility(listview.INVISIBLE);
                            frame.setVisibility(frame.INVISIBLE);
                            op.remove(op.size()-1);
                            op.remove(op.size()-1);
                            Operation = 0;
                            flag_label = 0;
                        } else {
                            listview_setting.setVisibility(listview_setting.INVISIBLE);
                            frame.setVisibility(frame.VISIBLE);
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.list_item1, labellist);
                            listview.setAdapter(adapter);
                            listview.setVisibility(listview.VISIBLE);
                            flag_label = 1;
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listview.setVisibility(listview.INVISIBLE);
                flag_label = 0;
                flag_set = 0;
                String label = labellist.get(position);
                if (label.equals("                               新建标签")) {
                    Operation = 3;
                    op.remove(op.size()-1);
                    op.add(3);
                    labellist.add(0, webview.getTitle());
                    URLlist.add(0, webview.getUrl());
                    button_origin.performClick();
                } else if (label.equals("主页")) {
                    Operation = 4;
                    op.remove(op.size()-1);
                    op.add(4);
                    button_origin.performClick();
                } else {
                    Operation = 4;
                    op.remove(op.size()-1);
                    op.add(4);
                    webview.loadUrl(URLlist.get(position));
                    editText2.setVisibility(editText2.GONE);
                    button2.setVisibility(button2.GONE);
                    textview.setVisibility(textview.INVISIBLE);
                }
                Operation = 0;
            }
        });


        button_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button_setting:
                        flag_label = 0;
                        Operation = 5;
                        op.add(5);
                        if (flag_set == 1) {
                            op.remove(op.size()-1);
                            op.remove(op.size()-1);
                            Operation = 0;
                            listview_setting.setVisibility(listview_setting.INVISIBLE);
                            flag_set = 0;
                        } else {
                            listview.setVisibility(listview.INVISIBLE);
                            frame.setVisibility(frame.INVISIBLE);
                            ArrayAdapter<String> adapter_setting = new ArrayAdapter<String>(MainActivity.this, R.layout.list_item1, setlist);
                            listview_setting.setAdapter(adapter_setting);
                            listview_setting.setVisibility(listview_setting.VISIBLE);
                            flag_set = 1;
                        }
                        break;
                    default:
                        break;
                }
            }
        });

        listview_setting.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                flag_label = 0;
                flag_set = 0;
                listview_setting.setVisibility(listview_setting.INVISIBLE);
                String label = setlist.get(position);
                op.remove(op.size()-1);
                Operation = 0;
                if (label.equals("    查看源码")) {
                    title = "网站源码";
                    sendRequestWithHttpURLConnection(webview.getUrl());
                } else if (label.equals("    历史记录")) {
                    flag_his = 1;
                    editText2.setVisibility(editText2.GONE);
                    button2.setVisibility(button2.GONE);
                    textview.setVisibility(textview.INVISIBLE);
                    listview_setting.setVisibility(listview_setting.INVISIBLE);
                    webview.loadUrl("");
                    ArrayAdapter<String> adapter_setting = new ArrayAdapter<String>(MainActivity.this, R.layout.list_item1, history_title);
                    listview_history.setAdapter(adapter_setting);
                    listview_history.setVisibility(listview_history.VISIBLE);
                } else if (label.equals("    删除浏览记录")) {
                    history.clear();
                    history_title.clear();

                } else if (label.equals("    网页截图")) {
                    Bitmap bitmap = captureWebView(webview);
                    File f = new File("/storage/emulated/0/Download/","picture.bmp");
                    if (f.exists()) {
                        f.delete();
                    }
                    try {
                        FileOutputStream out = new FileOutputStream(f);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                        out.flush();
                        out.close();
                        Toast.makeText(MainActivity.this, "图片已保存至/storage/emulated/0/Download/", Toast.LENGTH_SHORT).show();
                    } catch (FileNotFoundException e) {
// TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
// TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (label.equals("    查看位置")) {
                    title = "所在地区";
                    List<String> providerList = locationManager.getProviders(true);
                    if (providerList.contains(LocationManager.GPS_PROVIDER)) {
                        provider = LocationManager.GPS_PROVIDER;
                    } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
                        provider = LocationManager.NETWORK_PROVIDER;
                    } else {
                        return;
                    }
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Location location = locationManager.getLastKnownLocation(provider);
                    if (location != null) {
                        Latitude = location.getLatitude();
                        Longiture = location.getLongitude();
                        sendRequestWithHttpURLConnection("http://maps.google.cn/maps/api/geocode/json?latlng="+ Latitude+","+ Longiture+"&sensor=true,language=zh-CN");
                    }
                    locationManager.requestLocationUpdates(provider, 5000, 1, locationListener);

                }
                else if(label.equals("    指南针")){
                   Intent intent = new Intent(MainActivity.this,Second.class);
                    startActivity(intent);
                }
                else if(label.equals("    生成二维码")){
                    Intent intent = new Intent(MainActivity.this,Third.class);
                    intent.putExtra("extra_data",webview.getUrl().toString());
                    startActivity(intent);
                }
                else{
                    webview.loadUrl(URLlist.get(position));
                    editText2.setVisibility(editText2.GONE);
                    button2.setVisibility(button2.GONE);
                    textview.setVisibility(textview.INVISIBLE);
                }
            }
        });

        listview_history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                flag_label = 0;
                flag_set = 0;
                flag_his++;
                listview_history.setVisibility(listview_history.INVISIBLE);
                webview.loadUrl(history.get(position));
                editText.setText(history_title.get(position));
            }
        });

        listview_history.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub

                listview.setVisibility(listview.INVISIBLE);
                listview_setting.setVisibility(listview_setting.INVISIBLE);
                return false;
            }
        });


        UpdateUI(findViewById(R.id.main));
    }

    private void load() {
        SharedPreferences pref = getSharedPreferences("lutaotao",MODE_PRIVATE);
        int label_size = pref.getInt("labelsize",0);
        for(int i =0;i<label_size-2;i++){
            String temp = pref.getString("labeltitle"+i,"");
            labellist.add(i,temp);
        }
        int labelurl_size = pref.getInt("labelurlsize",0);
        for(int i =0;i<labelurl_size-2;i++){
            String temp = pref.getString("labelurl"+i,"");
            URLlist.add(i,temp);
        }
        int history_size = pref.getInt("historysize",0);
        for(int i=0;i<history_size;i++){
           String temp = pref.getString("history"+i,"");
            history.add(temp);
        }
        int historytitle_size = pref.getInt("historytitlesize",0);
        for(int i=0;i<historytitle_size;i++){
            String temp = pref.getString("history_title"+i,"");
            history_title.add(temp);
        }
    }
    private Bitmap captureWebView(WebView webView){
        Picture snapShot = webView.capturePicture();
        float i = webView.getScale();
        int h = (int)(webView.getContentHeight() * i);

        final Bitmap bmp = Bitmap.createBitmap(webView.getWidth(), h,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        snapShot.draw(canvas);
        return bmp;

    }

}
