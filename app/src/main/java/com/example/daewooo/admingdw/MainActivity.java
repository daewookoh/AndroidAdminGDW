package com.example.daewooo.admingdw;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

// 왼쪽 슬라이드 메뉴를 포함한 메인엑티비티
public class MainActivity extends Activity implements WebViewFragment.UiListener {
    WebView webView;
    DrawerLayout dLayout;
    View dView;
    int year;
    int month;
    int day;
    boolean datePickerDialog_visible;
    boolean isDataSet;
    TextView textViewMemberName;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        getWindow().requestFeature(Window.FEATURE_PROGRESS); // 웹로딩 프로그레스바
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 세로모드로 고정

        dLayout = (DrawerLayout) findViewById(R.id.drawer_layout); // 슬라이딩메뉴 총괄
        dView = findViewById(R.id.left_drawer); // 슬라이딩메뉴 화면

        // 사용자 이름세팅
        SharedPreferences sp_login = getSharedPreferences("login_data", MODE_PRIVATE);
        String member_name = sp_login.getString("member_name","");
        textViewMemberName = (TextView) findViewById(R.id.member_name);
        textViewMemberName.setText(member_name);


        GregorianCalendar calendar = new GregorianCalendar();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day= calendar.get(Calendar.DAY_OF_MONTH);



        // 초기실행시 기본설정값
        gocScheduleBtnClicked(dView); // 옹달샘 일정실행
        //emailBtnClicked(dView);
    }

    // Fragment가 활성화되지 않은 상태에서도
    // Back 버튼을 누를 경우 앱 종료 확인
    @Override
    public void onBackPressed(){
        appFinishCheck();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if(dLayout.isDrawerOpen(dView))
            {
                dLayout.closeDrawer(dView);
            }
            else
            {
                dLayout.openDrawer(dView);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // *** uiCallback 함수
    // Fragment에서 Back 버튼을 누를경우
    // 웹페이지 Back이 가능할경우 웹페이지 이동
    // 웹페이지 Back이 불가능할 경우 앱 종료 확인
    public void appFinishCheck(){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.kor_app_finish)
                .setMessage(R.string.kor_app_finish_text)
                .setPositiveButton(R.string.kor_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton(R.string.kor_no, null)
                .show();
    }

    // *** uiCallback 함수
    // Fragment 에서 메뉴(이미지)버튼을 누를경우
    public void onMenuButtonClicked(){
        dLayout.openDrawer(dView);
    }

    // *** uiCallback 함수
    // 옹달샘일정에서 날짜변경을 클릭한경우
    public void openDatePicker(WebView selWebView){

        //DatePickerDialog는 취소를 처리할수 없는 오류가 있어서 FixedDatePickerDialog로 대체
        FixedDatePickerDialog mDatePicker = new FixedDatePickerDialog(MainActivity.this, 0, dateSetListener, year, month, day);
        mDatePicker.show();

        webView = selWebView;

    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            // TODO Auto-generated method stub
            String selDate = String.format("%d-%02d-%02d", year,monthOfYear+1, dayOfMonth);
            gocScheduleChangeDate(selDate);

        }

    };

    //DatePickerDialog는 취소를 처리할수 없는 오류가 있어서 FixedDatePickerDialog 생성
    public class FixedDatePickerDialog extends DatePickerDialog {

        public FixedDatePickerDialog(Context context, OnDateSetListener callBack, int year, int month, int day) {
            super(context, null, year, month, day);
            initializePicker(callBack);
        }

        public FixedDatePickerDialog(Context context, int theme,
                                     OnDateSetListener callBack, int year, int month, int day) {
            super(context, theme, null,year, month, day);
            initializePicker(callBack);
        }

        private void initializePicker(final OnDateSetListener callback) {
            try {
                //If you're only using Honeycomb+ then you can just call getDatePicker() instead of using reflection
                Field pickerField = DatePickerDialog.class.getDeclaredField("mDatePicker");
                pickerField.setAccessible(true);
                final DatePicker picker = (DatePicker) pickerField.get(this);
                this.setCancelable(true);
                this.setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getText(android.R.string.cancel), (OnClickListener) null);
                this.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getText(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                picker.clearFocus(); //Focus must be cleared so the value change listener is called
                                callback.onDateSet(picker, picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
                            }
                        });
            } catch (Exception e) { /* Reflection probably failed*/ }
        }
    }

    // Fragment 화면전환
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void replaceFragment(String title, String layout, String url){
        String sUrl;
        String encrypted = null;
        String gdw_mem_no;
        String goc_mem_no;


        SharedPreferences sp_login = getSharedPreferences("login_data", MODE_PRIVATE);
        gdw_mem_no = sp_login.getString("gdw_mem_no","");
        goc_mem_no = sp_login.getString("goc_mem_no","");

        // 네트워크 연결이 안되어 있으면 sUrl 값을 비워서 넘긴다.
        if(isNetWorkAvailable() == false) {
            Toast.makeText(this, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
            sUrl = "";
        } else {
            String encodeUrl = Uri.encode(url);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String currentDateandTime = sdf.format(new Date());
            MCrypt MCrypt = new MCrypt();
            try {
                encrypted = MCrypt.bytesToHex(MCrypt.encrypt(currentDateandTime));
            } catch (Exception e) {
                e.printStackTrace();
            }

            //보안키 적용할경우
            //sUrl = "http://www.godowon.com/m/surl.gdw?url=" + encodeUrl + "&gdw_mem_no=" + getString(R.string.gdw_mem_no) + "&goc_mem_no=" + getString(R.string.goc_mem_no) + "&enc_key=" + encrypted;
            sUrl = "http://www.godowon.com/m/surl.gdw?url=" + encodeUrl + "&gdw_mem_no=" + gdw_mem_no + "&goc_mem_no=" + goc_mem_no;
        }

        if(gdw_mem_no.isEmpty() || goc_mem_no.isEmpty()) {
            Fragment detail = new LoginFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, detail).commit();
        }
        else if(layout == "web_page") {

            Bundle args = new Bundle();
            args.putString("Title", title);
            args.putString("Layout", layout);
            args.putString("Url", sUrl);
            Fragment detail = new WebViewFragment();
            detail.setArguments(args);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, detail).commit();

        }
        else if(layout == "os_web_page") {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(sUrl));
            startActivity(i);
        }
    }


    public void closeBtnClicked(View view){
        dLayout.closeDrawer(dView);
    }

    public void gocScheduleChangeDate(String selDate) {
        String url = "http://www.godowoncenter.com/admingoc/report/m/today.goc?Ymd=" + selDate;
        webView.loadUrl(url);
        Toast.makeText(MainActivity.this, selDate + " 날짜로 이동", Toast.LENGTH_SHORT).show();
    }

    public void gocScheduleBtnClicked(View view) {
        String title = getResources().getString(R.string.kor_goc_schedule);
        replaceFragment(title, "web_page", "http://www.godowoncenter.com/admingoc/report/m/today.goc");
        //test용
        //replaceFragment(title, "web_page", "http://beta3.godowon.com/test.gdw");

        dLayout.closeDrawer(dView);
    }

    public void gocSchedulePCBtnClicked(View view) {
        String title = getResources().getString(R.string.kor_goc_schedule_pc);
        replaceFragment(title, "web_page", "http://www.godowoncenter.com/admingoc/program/program_calendar.goc");
        dLayout.closeDrawer(dView);
    }

    public void emailBtnClicked(View view) {
        String title = getResources().getString(R.string.kor_email);
        replaceFragment(title, "os_web_page", "http://www.godowon.com/admingdw/index.gdw?redirect=mail");
        dLayout.closeDrawer(dView);
    }

    public void admingdwBtnClicked(View view) {
        String title = getResources().getString(R.string.kor_admingdw);
        replaceFragment(title, "os_web_page", "http://www.godowon.com/m/admingdw/");
        dLayout.closeDrawer(dView);
    }

    public void admingdwPcBtnClicked(View view) {
        String title = getResources().getString(R.string.kor_admingdw_pc);
        replaceFragment(title, "os_web_page", "http://www.godowon.com/admingdw/index.gdw");
        dLayout.closeDrawer(dView);
    }

    public void scheduleBtnClicked(View view) {
        String title = getResources().getString(R.string.kor_schedule);
        replaceFragment(title, "web_page", "http://www.godowon.com/board/gdwboard.gdw?id=admin_Schedule");
        dLayout.closeDrawer(dView);
    }

    public void checkLetterBtnClicked(View view) {
        String title = getResources().getString(R.string.kor_check_letter);
        replaceFragment(title, "web_page", "http://www.godowon.com/m/admingdw/check_tomorrow_letter.gdw");
        dLayout.closeDrawer(dView);
    }

    public void statBtnClicked(View view){
        String title = getResources().getString(R.string.kor_stat);
        replaceFragment(title, "web_page", "http://www.godowon.com/m/admingdw/main_stat.gdw");
        dLayout.closeDrawer(dView);
    }

    public void otherBoardBtnClicked(View view){
        String title = getResources().getString(R.string.kor_other_board);
        replaceFragment(title, "web_page", "http://www.godowon.com/m/admingdw/main_board_list.gdw");
        dLayout.closeDrawer(dView);
    }
/*
    public void morningLetterCalendarBtnClicked(View view){
        String title = getResources().getString(R.string.kor_morning_calendar);
        replaceFragment(title, "web_page", "http://www.godowon.com/admingdw/letter_calendar.gdw");
        dLayout.closeDrawer(dView);
    }
*/
    public void gdwHomeBtnClicked(View view){
        String title = getResources().getString(R.string.kor_gdw_home);
        replaceFragment(title, "web_page", "http://www.godowon.com");
        dLayout.closeDrawer(dView);
    }

    public void gocHomeBtnClicked(View view){
        String title = getResources().getString(R.string.kor_goc_home);
        replaceFragment(title, "web_page", "http://www.godowoncenter.com");
        dLayout.closeDrawer(dView);
    }

    public void loginBtnClicked(View view) {

        if(isNetWorkAvailable() == false) {
            Toast.makeText(this, "네트워크 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText inputName = (EditText)findViewById(R.id.inputName);
        String name = inputName.getText().toString();

        String sUrl = "http://www.godowon.com/m/check_admin_member_no.gdw?member_name=" + name;
        new HttpAsyncTask().execute(sUrl);


        //Log.d("FFF", sUrl);
        //String title = getResources().getString(R.string.kor_email);
        //replaceFragment(title, "os_web_page", "http://www.godowon.com/admingdw/index.gdw?redirect=mail");
        //dLayout.closeDrawer(dView);
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            try {
                result = URLDecoder.decode(result, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                JSONObject jObject = new JSONObject(result);
                String msg = jObject.getString("msg");
                String member_name = jObject.getString("member_name");
                String gdw_mem_no = jObject.getString("gdw_mem_no");
                String goc_mem_no = jObject.getString("goc_mem_no");
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();

                if(msg.equals("SUCCESS"))
                {
                    SharedPreferences sp_login = getSharedPreferences("login_data", MODE_PRIVATE);
                    SharedPreferences.Editor editor_login = sp_login.edit();
                    editor_login.putString("member_name",member_name);
                    editor_login.putString("gdw_mem_no", gdw_mem_no);
                    editor_login.putString("goc_mem_no", goc_mem_no);
                    editor_login.commit();

                    mRegistrationBroadcastReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            SharedPreferences sharedPreferences =
                                    PreferenceManager.getDefaultSharedPreferences(context);
                            boolean sentToken = sharedPreferences
                                    .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);

                        }
                    };

                    //Registering BroadcastReceiver
                    registerReceiver();
                    registerToken();


                    //gocScheduleBtnClicked(dView);
                    rebootApp();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void registerToken()
    {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public void rebootApp() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    // 네트워크 연결상태 확인
    private boolean isNetWorkAvailable(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        boolean isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        boolean isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();
        boolean isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

        if ((isWifiAvailable && isWifiConnect) || (isMobileAvailable && isMobileConnect)){
            return true;
        }else{
            return false;
        }
    }

}
