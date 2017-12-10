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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class Registreren extends AppCompatActivity {
	
	public ProgressDialog ProgressDialog;
	public String naam;
	public String wachtwoord;
	public String wachtwoord_herhalen;
	public String email;
	public String resultaat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registreren);
	}
    
	public void registreren(View view) {
		
		EditText editText1 = (EditText) findViewById(R.id.editText1);
		EditText editText2 = (EditText) findViewById(R.id.editText2);
		EditText editText3 = (EditText) findViewById(R.id.editText3);
		EditText editText4 = (EditText) findViewById(R.id.editText4);
		naam = editText1.getText().toString();
		wachtwoord = editText2.getText().toString();
		wachtwoord_herhalen = editText3.getText().toString();
		email = editText4.getText().toString();

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

        try{
            wachtwoord_herhalen = URLEncoder.encode(wachtwoord_herhalen, "UTF-8");
        }catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
		
		if (naam.matches("")){
			foutmelding("Geen naam opgegeven", "Je hebt geen naam opgegeven..\nProbeer het nog een keer!");
		}else if (wachtwoord.matches("")){
			foutmelding("Geen wachtwoord opgegeven", "Je hebt geen wachtwoord opgegeven..\nProbeer het nog een keer!");
		}else if (wachtwoord_herhalen.matches("")){
			foutmelding("Wachtwoord niet herhaald", "Je hebt je wachtwoord niet herhaald..\nProbeer het nog een keer!");
		}else if (!wachtwoord.equals(wachtwoord_herhalen)){
			foutmelding("Wachtwoorden komen niet overeen", "De wachtwoorden komen niet overeen..\nProbeer het nog een keer!");
		}else if (email.matches("")){
			foutmelding("Geen email opgegeven", "Je hebt geen e-mailadres opgegeven..\nProbeer het nog een keer!");
		}else{
			InputMethodManager InputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			InputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
			ProgressDialog = android.app.ProgressDialog.show(this, "Registreren", "Even geduld aub..", true, false);
			new registreren().execute();
		}

	}
	
	public void foutmelding(String titel, String bericht) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(Registreren.this);
		builder.setTitle(titel)
			   .setMessage(bericht)
			   .setIcon(R.drawable.foutmelding);
		builder.setPositiveButton("OK", null);
		builder.show();
	}

    private class registreren extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params)  {

            URL url = null;
            URLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                url = new URL("http://www.radiostereo.nl/paginas/app%202.0/registreren.php?naam="+naam+"&wachtwoord="+wachtwoord+"&email="+email);
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
            registreren_klaar();
        }

    }
	
	private void registreren_klaar() {
	    
		if (resultaat.matches("NAAM")){
			foutmelding("Naam in gebruik", "Deze naam is al in gebruik..\nProbeer het nog een keer!");
		}else if (resultaat.matches("EMAIL")){
			foutmelding("Email in gebruik", "Dit e-mailadres is al in gebruik..\nProbeer het nog een keer!");
		}else{
            onBackPressed();
    	    finish();
		}
	}

}
