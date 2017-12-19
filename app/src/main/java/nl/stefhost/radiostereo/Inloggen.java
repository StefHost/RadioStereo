package nl.stefhost.radiostereo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class Inloggen extends AppCompatActivity {

    public ProgressDialog ProgressDialog;
    public String naam;
    public String wachtwoord;
    public String resultaat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inloggen);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        getSupportActionBar().setTitle("   Radio Stereo");
        getSupportActionBar().setIcon(ContextCompat.getDrawable(this, R.drawable.icon));

    }

    public void inloggen(View view) {

        EditText editText1 = (EditText) findViewById(R.id.editText1);
        EditText editText2 = (EditText) findViewById(R.id.editText2);
        naam = editText1.getText().toString();
        wachtwoord = editText2.getText().toString();

        try{
            naam = URLEncoder.encode(naam, "UTF-8");
        }catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try{
            wachtwoord = URLEncoder.encode(wachtwoord, "UTF-8");
        }catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (naam.matches("")){
            foutmelding("Geen naam opgegeven", "Je hebt geen naam opgegeven..\nProbeer het nog een keer!");
        }else if (wachtwoord.matches("")){
            foutmelding("Geen wachtwoord opgegeven", "Je hebt geen wachtwoord opgegeven..\nProbeer het nog een keer!");
        }else{
            InputMethodManager InputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            InputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            ProgressDialog = android.app.ProgressDialog.show(this, "Inloggen", "Even geduld aub..", true, false);
            new inloggen().execute();
        }
    }

    public void foutmelding(String titel, String bericht) {

        AlertDialog.Builder builder = new AlertDialog.Builder(Inloggen.this);
        builder.setTitle(titel)
                .setMessage(bericht)
                .setIcon(R.drawable.foutmelding);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private class inloggen extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params)  {

            URL url = null;
            URLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                url = new URL("http://www.radiostereo.nl/paginas/app%202.0/inloggen.php?naam="+naam+"&wachtwoord="+wachtwoord);
            } catch (MalformedURLException e) {
                System.out.println("MalformedURLException");
            }

            if (url != null){
                try{
                    urlConnection = url.openConnection();
                }catch (java.io.IOException e){
                    System.out.println("java.io.IOException");
                }
            }

            if (urlConnection != null){
                try{
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }catch (java.io.IOException e) {
                    System.out.println("java.io.IOException");
                }
            }

            if (inputStream != null){

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                try{
                    resultaat = bufferedReader.readLine();
                }catch (java.io.IOException e) {
                    System.out.println("java.io.IOException");
                }

            }else{
                resultaat = "ERROR";
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            ProgressDialog.dismiss();
            inloggen_klaar();
        }

    }

    private void inloggen_klaar() {

        if (resultaat.matches("NAAM")){
            foutmelding("Geen bestaande naam", "Je hebt een niet bestaande naam opgegeven..\nProbeer het nog een keer!");
        }else if (resultaat.matches("WACHTWOORD")){
            foutmelding("Verkeerd wachtwoord", "Je hebt een verkeerd wachtwoord opgegeven..\nProbeer het nog een keer!");
        }else{
            SharedPreferences sharedPreferences = getSharedPreferences("opties", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("naam", naam);
            editor.apply();

            //Database maken
            SQLiteDatabase SQLiteDatabase = this.openOrCreateDatabase("Database", Context.MODE_PRIVATE, null);
            SQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS playlist (id INTEGER PRIMARY KEY, artiest text not null, titel text not null, album text not null, nummer text not null, online text not null, album_id text not null)");

            Intent intent = new Intent(this, Beginscherm.class);
            startActivity(intent);
            finish();
        }
    }

    public void registreren(View view) {
        Intent registreren = new Intent(this, Registreren.class);
        startActivity(registreren);
    }

}
