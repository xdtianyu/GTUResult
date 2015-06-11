package org.xdty.myexamresult;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";

    private final static String HOST = "http://gturesults.in/";
    private final static String PAGE = "Default.aspx?ext=archive";
    ArrayAdapter<String> sessionAdapter;
    ArrayAdapter<String> optionsAdapter;
    ArrayAdapter<String> groupsAdapter;
    private EditText captcha;
    private EditText seatNumber;
    private EditText number;
    private String captchaText;
    private String ddlsessionText;
    private String ddlbatchText;
    private Spinner sessionSpinner;
    private Spinner optionsSpinner;
    private Spinner groupsSpinner;
    private ImageView captchaImageView;
    private WebView webView;
    private Button submitButton;
    private ArrayList<String> groups = new ArrayList<>();
    private HashMap<String, String> options = new HashMap<>();
    private HashMap<String, String> sessions = new HashMap<>();
    private String __VIEWSTATEGENERATOR = "";
    private String __VIEWSTATE = "";
    private String __EVENTARGUMENT = "";
    private String __EVENTTARGET = "";

    private boolean isFetching = false;

    private String defaultSession = "";

    @Override
    public void onBackPressed() {
        if (webView.getVisibility() != View.GONE) {
            webView.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captcha = (EditText) findViewById(R.id.captcha);
        number = (EditText) findViewById(R.id.number);
        seatNumber = (EditText) findViewById(R.id.seat_number);
        sessionSpinner = (Spinner) findViewById(R.id.session);
        optionsSpinner = (Spinner) findViewById(R.id.option);
        groupsSpinner = (Spinner) findViewById(R.id.group);
        captchaImageView = (ImageView) findViewById(R.id.captcha_image);
        submitButton = (Button) findViewById(R.id.submit);
        webView = (WebView) findViewById(R.id.webview);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new SubmitTask()).execute(HOST + PAGE);
            }
        });

        sessionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        optionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        groupsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);

        sessionSpinner.setAdapter(sessionAdapter);
        groupsSpinner.setAdapter(groupsAdapter);
        optionsSpinner.setAdapter(optionsAdapter);

        sessionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!isFetching) {
                    String s = sessionSpinner.getSelectedItem().toString();

                    for (Map.Entry<String, String> entry : sessions.entrySet()) {
                        if (entry.getValue().equals(s)) {
                            ddlsessionText = entry.getKey();
                            break;
                        }
                    }

                    sessions.clear();
                    options.clear();
                    groups.clear();

                    optionsAdapter.clear();
                    groupsAdapter.clear();
                    optionsAdapter.notifyDataSetChanged();
                    groupsAdapter.notifyDataSetChanged();

                    (new SubmitTask()).execute(HOST + PAGE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        groupsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                optionsAdapter.clear();
                String s = groupsSpinner.getSelectedItem().toString();
                for (String option : options.keySet()) {
                    if (options.get(option).startsWith(s)) {
                        optionsAdapter.add(options.get(option));
                    }
                }

                optionsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        optionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String s = optionsSpinner.getSelectedItem().toString();

                for (Map.Entry<String, String> entry : options.entrySet()) {
                    if (entry.getValue().equals(s)) {
                        ddlbatchText = entry.getKey();
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        (new HttpTask()).execute(HOST + PAGE);
    }

    private void parseHtml(String html) {
        Document document = Jsoup.parse(html);

        Element sessionElement = document.getElementById("ddlsession");

        Elements sessionElements = sessionElement.getElementsByTag("option");

        for (int i = 0; i < sessionElements.size(); i++) {
            Element optionElement = sessionElements.get(i);
            if (optionElement.attr("selected").equals("selected")) {
                defaultSession = optionElement.val();
            }

            sessions.put(optionElement.val(), optionElement.text());
        }

        Element selectElement = document.getElementById("ddlbatch");

        Elements groupElements = selectElement.getElementsByTag("optgroup");

        for (int i = 0; i < groupElements.size(); i++) {
            Element groupElement = groupElements.get(i);
            String label = groupElement.attr("label");
            groups.add(label);
        }

        Elements optionElements = selectElement.getElementsByTag("option");

        for (int i = 0; i < optionElements.size(); i++) {
            Element optionElement = optionElements.get(i);

            Element previous = optionElement.previousElementSibling();

            for (; previous.attr("label").isEmpty(); ) {
                previous = previous.previousElementSibling();
            }

            String label = previous.attr("label");

            String value = optionElement.text().replaceAll("\\.", "");

            if (!value.startsWith(label)) {
                value = value.replace(value.substring(0, label.length()), label);
            }

            options.put(optionElement.val(), value);
        }

        Elements imageElements = document.getElementsByTag("img");

        for (int i = 0; i < imageElements.size(); i++) {
            Element imageElement = imageElements.get(i);
            if (imageElement.attr("src").startsWith("CaptchaImage")) {
                captchaText = imageElement.attr("src");
            }
        }

        Elements inputElements = document.getElementsByTag("input");

        for (int i = 0; i < inputElements.size(); i++) {
            Element inputElement = inputElements.get(i);

            if (inputElement.val() != null) {
                switch (inputElement.attr("name")) {
                    case "__EVENTTARGET":
                        __EVENTTARGET = inputElement.val();
                        break;
                    case "__EVENTARGUMENT":
                        __EVENTARGUMENT = inputElement.val();
                        break;
                    case "__VIEWSTATE":
                        __VIEWSTATE = inputElement.val();
                        break;
                    case "__VIEWSTATEGENERATOR":
                        __VIEWSTATEGENERATOR = inputElement.val();
                        break;
                }
            }
        }
    }

    private void updateUI() {
        Picasso.with(MainActivity.this).load(HOST + captchaText).into(captchaImageView);
        sessions = (HashMap<String, String>) Utils.sortByValue(sessions);
        options = (HashMap<String, String>) Utils.sortByValue(options);
        Collections.sort(groups);

        sessionAdapter.clear();
        for (String session : sessions.keySet()) {
            sessionAdapter.add(sessions.get(session));
        }

        sessionAdapter.notifyDataSetChanged();

        String[] array = sessions.keySet().toArray(new String[sessions.size()]);
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(defaultSession)) {
                sessionSpinner.setSelection(i);
                break;
            }
        }

        for (String group : groups) {
            groupsAdapter.add(group);
        }
        groupsAdapter.notifyDataSetChanged();

        sessionSpinner.postDelayed(new Runnable() {
            @Override
            public void run() {
                isFetching = false;
            }
        }, 1000);
    }

    class HttpTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            isFetching = true;

            String result = "";

            Response response = null;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(params[0])
                    .build();

            try {
                response = client.newCall(request).execute();
                result = response.body().string();
                parseHtml(result);

                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //text.setText(s);
            updateUI();
        }
    }

    class SubmitTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String result = "";

            try {
                OkHttpClient client = new OkHttpClient();

                RequestBody body = new FormEncodingBuilder()
                        .add("__EVENTTARGET", __EVENTTARGET)
                        .add("__EVENTARGUMENT", __EVENTARGUMENT)
                        .add("__VIEWSTATE", __VIEWSTATE)
                        .add("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR)
                        .add("ddlsession", ddlsessionText)
                        .add("ddlbatch", ddlbatchText)
                        .add("txtenroll", number.getText().toString())
                        .add("txtSheetNo", seatNumber.getText().toString())
                        .add("CodeNumberTextBox", captcha.getText().toString())
                        .add("btnSearch", "Search")
                        .build();

                Log.d(TAG, body.toString());

                Request request = new Request.Builder()
                        .url(params[0])
                        .post(body)
                        .build();
                Response response = response = client.newCall(request).execute();
                result = response.body().string();

                parseHtml(result);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {

            updateUI();

//            webView.getSettings().setJavaScriptEnabled(true);
//            webView.loadDataWithBaseURL("", s, "text/html", "UTF-8", "");
//            webView.setVisibility(View.VISIBLE);
        }
    }
}
