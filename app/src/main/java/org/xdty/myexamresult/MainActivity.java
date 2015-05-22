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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";

    private final static String HOST = "http://gturesults.in/";
    private final static String PAGE = "Default.aspx?ext=w1024";
    ArrayAdapter<String> optionsAdapter;
    ArrayAdapter<String> groupsAdapter;
    private EditText captcha;
    private EditText seatNumber;
    private EditText number;
    private String captchaText;
    private String ddlbatchText;
    private Spinner optionsSpinner;
    private Spinner groupsSpinner;
    private ImageView captchaImageView;
    private WebView webView;
    private Button submitButton;
    private HashSet<String> groups = new HashSet<>();
    private HashMap<String, String> options = new HashMap<>();
    private String __VIEWSTATEGENERATOR = "";
    private String __VIEWSTATE = "";
    private String __EVENTARGUMENT = "";
    private String __EVENTTARGET = "";

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

        optionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        groupsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);

        optionsSpinner.setAdapter(optionsAdapter);
        groupsSpinner.setAdapter(groupsAdapter);

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
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        (new HttpTask()).execute(HOST + PAGE);
    }

    class HttpTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String result = "";

            Response response = null;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(params[0])
                    .build();

            try {
                response = client.newCall(request).execute();

                Document document = Jsoup.parse(response.body().string());

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
                    options.put(optionElement.val(), optionElement.text().replaceAll("\\.", ""));
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

                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //text.setText(s);

            Picasso.with(MainActivity.this).load(HOST + captchaText).into(captchaImageView);

            for (String group : groups) {
                groupsAdapter.add(group);
            }
            groupsAdapter.notifyDataSetChanged();
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
                Response response = null;
                response = client.newCall(request).execute();
                result = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadDataWithBaseURL("", s, "text/html", "UTF-8", "");
            webView.setVisibility(View.VISIBLE);
        }
    }
}
