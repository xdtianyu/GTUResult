package org.xdty.gturesult;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

    final static String errorCaptcha = "ERROR: Incorrect captcha code, try again.";
    private final static String TAG = "MainActivity";
    private final static String HOST = "http://result1.gtu.ac.in/";
    private final static String PAGE = "Default.aspx?ext=archive";

    ArrayAdapter<String> currentAdapter;
    ArrayAdapter<String> sessionAdapter;
    ArrayAdapter<String> optionsAdapter;
    ArrayAdapter<String> groupsAdapter;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    private TextView nameTextView;
    private TextView resultTextView;
    private TextView subjectResultTextView;
    private ListView listView;

    private EditText captcha;
    private EditText seatNumber;
    private EditText number;

    private Spinner currentSpinner;
    private Spinner sessionSpinner;
    private Spinner optionsSpinner;
    private Spinner groupsSpinner;

    private ImageView captchaImageView;

    private ArrayList<String> groups = new ArrayList<>();
    private HashMap<String, String> options = new HashMap<>();
    private HashMap<String, String> sessions = new HashMap<>();

    private String captchaText = "";
    private String ddlsessionText = "";
    private String ddlbatchText = "";

    private String __VIEWSTATEGENERATOR = "";
    private String __VIEWSTATE = "";
    private String __EVENTARGUMENT = "";
    private String __EVENTTARGET = "";
    private boolean isFetching = false;
    private String defaultSession = "";

    private ArrayList<Subject> subjects = new ArrayList<>();
    private Result result = new Result();

    private ArrayAdapter<Subject> listAdapter;

    private boolean isInit = true;

    private boolean isCurrent = false;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        nameTextView = (TextView) findViewById(R.id.name);
        resultTextView = (TextView) findViewById(R.id.result);
        subjectResultTextView = (TextView) findViewById(R.id.subject_result);
        listView = (ListView) findViewById(R.id.list);

        captcha = (EditText) findViewById(R.id.captcha);
        number = (EditText) findViewById(R.id.number);
        seatNumber = (EditText) findViewById(R.id.seat_number);
        currentSpinner = (Spinner) findViewById(R.id.current);
        sessionSpinner = (Spinner) findViewById(R.id.session);
        optionsSpinner = (Spinner) findViewById(R.id.option);
        groupsSpinner = (Spinner) findViewById(R.id.group);
        captchaImageView = (ImageView) findViewById(R.id.captcha_image);
        Button submitButton = (Button) findViewById(R.id.submit);

        number.setText(prefs.getString("number", ""));
        seatNumber.setText(prefs.getString("seatNumber", ""));

        defaultSession = prefs.getString("session", "");
        ddlsessionText = prefs.getString("session", "");

        isCurrent = prefs.getBoolean("isCurrent", false);

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, subjects);

        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editor = getSharedPreferences(getPackageName(), MODE_PRIVATE).edit();
                editor.putString("number", number.getText().toString());
                editor.putString("seatNumber", seatNumber.getText().toString());
                editor.commit();

                result = new Result();
                subjects.clear();
                (new SubmitTask()).execute(HOST, SubmitTask.FROM_CLICK);
            }
        });

        currentAdapter = new ArrayAdapter<>(this, R.layout.spinner_item);
        sessionAdapter = new ArrayAdapter<>(this, R.layout.spinner_item);
        optionsAdapter = new ArrayAdapter<>(this, R.layout.spinner_item);
        groupsAdapter = new ArrayAdapter<>(this, R.layout.spinner_item);

        currentSpinner.setAdapter(currentAdapter);
        sessionSpinner.setAdapter(sessionAdapter);
        groupsSpinner.setAdapter(groupsAdapter);
        optionsSpinner.setAdapter(optionsAdapter);

        currentAdapter.add("Current");
        currentAdapter.add("Archive");

        currentSpinner.setSelection(isCurrent ? 0 : 1);

        currentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isFetching) {
                    String s = currentSpinner.getSelectedItem().toString();

                    if (s.equals("Current")) {
                        isCurrent = true;
                    } else if (s.equals("Archive")) {
                        isCurrent = false;
                    }

                    editor = getSharedPreferences(getPackageName(), MODE_PRIVATE).edit();
                    editor.putBoolean("isCurrent", isCurrent);
                    editor.commit();

                    sessions.clear();
                    options.clear();
                    groups.clear();

                    sessionAdapter.clear();
                    optionsAdapter.clear();
                    groupsAdapter.clear();
                    sessionAdapter.notifyDataSetChanged();
                    optionsAdapter.notifyDataSetChanged();
                    groupsAdapter.notifyDataSetChanged();

                    (new HttpTask()).execute(HOST);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sessionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (!isFetching) {
                    String s = sessionSpinner.getSelectedItem().toString();

                    for (Map.Entry<String, String> entry : sessions.entrySet()) {
                        if (entry.getValue().equals(s)) {
                            ddlsessionText = entry.getKey();

                            editor = getSharedPreferences(getPackageName(), MODE_PRIVATE).edit();
                            editor.putString("session", ddlsessionText);
                            editor.commit();

                            defaultSession = ddlsessionText;

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

                    (new SubmitTask()).execute(HOST);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        groupsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                setOptions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        optionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String s = optionsSpinner.getSelectedItem().toString();

                if (isInit) {

                    s = prefs.getString("optionsValue", groupsSpinner.getSelectedItem().toString());

                    if (!optionsSpinner.getSelectedItem().toString().equals(s)) {

                        for (int i = 0; i < optionsAdapter.getCount(); i++) {
                            if (s != null && s.equals(optionsAdapter.getItem(i))) {
                                optionsSpinner.setSelection(i);
                                break;
                            }
                        }
                    }

                } else {

                    for (Map.Entry<String, String> entry : options.entrySet()) {
                        if (entry.getValue().equals(s)) {
                            ddlbatchText = entry.getKey();

                            editor = getSharedPreferences(getPackageName(), MODE_PRIVATE).edit();
                            editor.putString("options", ddlbatchText);
                            editor.putString("optionsValue", s);
                            editor.commit();

                            break;
                        }
                    }
                }
                isInit = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        (new HttpTask()).execute(HOST);
    }

    private void setOptions() {
        optionsAdapter.clear();

        String s = groupsSpinner.getSelectedItem().toString();

        if (isInit) {
            s = prefs.getString("groups", groupsSpinner.getSelectedItem().toString());

            if (!groupsSpinner.getSelectedItem().toString().equals(s)) {

                int count = 0;
                for (int i = 0; i < groups.size(); i++) {
                    if (s != null && s.equals(groups.get(i))) {
                        groupsSpinner.setSelection(i);
                        break;
                    }
                    count++;
                }
                if (count == groups.size()) {
                    s = groupsSpinner.getSelectedItem().toString();
                }
            }

        } else {
            editor = getSharedPreferences(getPackageName(), MODE_PRIVATE).edit();
            editor.putString("groups", s);
            editor.commit();
        }

        for (String option : options.keySet()) {
            if (options.get(option).startsWith(s)) {
                optionsAdapter.add(options.get(option));
            }
        }

        optionsAdapter.notifyDataSetChanged();
    }

    private void parseHtml(String html, boolean isFromClick) {
        Document document = Jsoup.parse(html);

        Element sessionElement = document.getElementById("ddlsession");

        if (sessionElement != null) {
            Elements sessionElements = sessionElement.getElementsByTag("option");

            for (int i = 0; i < sessionElements.size(); i++) {
                Element optionElement = sessionElements.get(i);
                if (optionElement.attr("selected").equals("selected")) {

                    if (defaultSession.isEmpty()) {
                        defaultSession = optionElement.val();
                    }
                }

                sessions.put(optionElement.val(), optionElement.text());
            }
        } else {
            if (defaultSession.isEmpty()) {
                defaultSession = "";
            }
            sessionElement = document.getElementById("lblSession");
            sessions.put("", sessionElement.text());
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

        if (isFromClick) {
            // parse result
            parseResult(document);
            updateName();
        }
    }

    private void parseResult(Document document) {
        Element name = document.getElementById("lblName");

        if (name == null) {
            return;
        }

        Element messageElement = document.getElementById("lblmsg");
        result.message = messageElement.text();

        makeToast(result.message);

        if (result.message.equals(errorCaptcha)) {
            return;
        }

        Element enrollmentNo = document.getElementById("lblEnrollmentNo");
        Element exam = document.getElementById("lblExam");
        Element declared = document.getElementById("lblDeclaredOn");
        Element examName = document.getElementById("lblExamName");
        Element branch = document.getElementById("lblBranchName");

        result.name = name.text();
        result.enrollmentNumber = enrollmentNo.text();
        result.examSeat = exam.text();
        result.declaredDate = declared.text();
        result.exam = examName.text();
        result.branch = branch.text();

        Elements resultTables = document.getElementsByAttributeValue("class", "Rgrid");

        if (resultTables.size() == 0) {
            return;
        }

        Element subjectTable = resultTables.get(1);

        Elements results = subjectTable.getElementsByAttributeValue("width", "8%");

        for (int i = 0; i < results.size(); i++) {
            Subject subject = new Subject();
            Element element = results.get(i);
            subject.code = element.text();
            element = element.nextElementSibling();
            subject.name = element.text();
            element = element.nextElementSibling();

            Element theoryElement = element.child(0).child(0).child(0).child(0);
            subject.theory.ESE = theoryElement.text();
            theoryElement = theoryElement.nextElementSibling().nextElementSibling();
            subject.theory.PA = theoryElement.text();
            theoryElement = theoryElement.nextElementSibling().nextElementSibling();
            subject.theory.TOTAL = theoryElement.text();

            element = element.nextElementSibling();
            Element practicalElement = element.child(0).child(0).child(0).child(0);
            subject.practical.ESE = practicalElement.text();
            practicalElement = practicalElement.nextElementSibling().nextElementSibling();
            subject.practical.PA = practicalElement.text();
            practicalElement = practicalElement.nextElementSibling().nextElementSibling();
            subject.practical.TOTAL = practicalElement.text();

            element = element.nextElementSibling();
            subject.grade = element.text();

            subjects.add(subject);
        }

        Element resultElement = resultTables.get(2);
        Element currentBacklog = resultElement.getElementById("lblCUPBack");
        Element totalBacklog = resultElement.getElementById("lblTotalBack");
        Element SPI = resultElement.getElementById("lblSPI");
        Element CPI = resultElement.getElementById("lblCPI");

        result.currentBacklog = currentBacklog.text();
        result.totalBacklog = totalBacklog.text();
        result.SPI = SPI.text();
        result.CPI = CPI.text();
    }

    private void makeToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                resultTextView.setText(message);
            }
        });
    }

    private void updateName() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                nameTextView.setText(result.name);
                seatNumber.setText(result.examSeat);
            }
        });
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
                listAdapter.notifyDataSetChanged();
                Utils.setListViewHeightBasedOnChildren(listView);
                subjectResultTextView.setText(result.toString());
            }
        }, 1000);
    }

    class HttpTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            isFetching = true;

            String url = params[0];

            if (!isCurrent) {
                url += PAGE;
            }

            String result = "";

            Response response = null;
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try {
                response = client.newCall(request).execute();
                result = response.body().string();
                parseHtml(result, false);

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
            //(new SubmitTask()).execute(HOST, PAGE);

            sessions.clear();
            options.clear();
            groups.clear();

            optionsAdapter.clear();
            groupsAdapter.clear();
            optionsAdapter.notifyDataSetChanged();
            groupsAdapter.notifyDataSetChanged();

            (new SubmitTask()).execute(HOST);
        }
    }

    class SubmitTask extends AsyncTask<String, Void, String> {

        public final static String FROM_CLICK = "from_click";

        @Override
        protected String doInBackground(String... params) {

            String result = "";

            String url = params[0];

            if (!isCurrent) {
                url += PAGE;
            }

            isInit = true;

            try {
                OkHttpClient client = new OkHttpClient();

                String numberString = number.getText().toString();
                String seatNumberString = seatNumber.getText().toString();
                String captchaString = captcha.getText().toString();

                RequestBody body = new FormEncodingBuilder()
                        .add("__EVENTTARGET", __EVENTTARGET)
                        .add("__EVENTARGUMENT", __EVENTARGUMENT)
                        .add("__VIEWSTATE", __VIEWSTATE)
                        .add("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR)
                        .add("ddlsession", ddlsessionText)
                        .add("ddlbatch", ddlbatchText)
                        .add("txtenroll", numberString)
                        .add("txtSheetNo", seatNumberString)
                        .add("CodeNumberTextBox", captchaString)
                        .add("btnSearch", "Search")
                        .build();

                Log.d(TAG, body.toString());

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                result = response.body().string();

                boolean isFromClick = false;

                if (params.length > 1 && FROM_CLICK.equals(params[1])) {
                    isFromClick = true;
                }

                parseHtml(result, isFromClick);

            } catch (IOException e) {
                e.printStackTrace();
                makeToast(getString(R.string.error));
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {

            updateUI();

        }
    }
}
