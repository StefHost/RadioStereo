package nl.stefhost.radiostereo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Chat extends Fragment implements View.OnClickListener {

    static ProgressDialog progressDialog;
    public String resultaat;
    public String bericht;
    public String naam;

    public TextView textView1;
    public EditText editText1;

    public ArrayList<String> arrayList;
    public String[] stringArray;
    public ImageView imageView1;
    public String kleur = "grijs";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View View = inflater.inflate(R.layout.fragment_chat, container, false);

        textView1 = (TextView) View.findViewById(R.id.chat);
        editText1 = (EditText) View.findViewById(R.id.editText1);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("opties", 0);
        naam = sharedPreferences.getString("naam", "");

        /*editText1.setImeActionLabel("Versturen", KeyEvent.KEYCODE_ENTER);
        editText1.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    verzenden();
                }
                return false;
            }
        });*/

        imageView1 = (ImageView) View.findViewById(R.id.imageView1);
        ImageView imageView2 = (ImageView) View.findViewById(R.id.imageView2);
        imageView1.setOnClickListener(this);
        imageView2.setOnClickListener(this);

        progressDialog = android.app.ProgressDialog.show(this.getContext(), "Chat laden", "Even geduld aub..", true, false);
        new chat_laden().execute();

        return View;
    }

    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.imageView1) {
            arrayList = new ArrayList<>();
            arrayList.add("grijs");
            arrayList.add("rood");
            arrayList.add("groen");
            arrayList.add("blauw");
            arrayList.add("oranje");
            arrayList.add("paars");

            stringArray = arrayList.toArray(new String[arrayList.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setTitle("Chatkleur")
                    .setItems(stringArray, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            kleur = stringArray[which];

                            int id = getResources().getIdentifier("kleur_"+kleur, "drawable", getActivity().getPackageName());
                            imageView1.setImageResource(id);
                        }
                    });
            builder.show();
        }else{
            verzenden();
        }
    }

    public void verzenden(){

        InputMethodManager InputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        InputMethodManager.hideSoftInputFromWindow(editText1.getWindowToken(), 0);

        bericht = editText1.getText().toString();
        bericht = bericht.replace(" ", "%20");
        if (!kleur.equals("grijs")){
            bericht = "["+kleur+"]"+bericht+"[/"+kleur+"]";
        }
        if (!bericht.matches("")){
            editText1.setText("");
            progressDialog = android.app.ProgressDialog.show(this.getContext(), "Bericht verzenden", "Even geduld aub..", true, false);
            new chat_verzenden().execute();
        }
    }

    private class chat_laden extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params)  {

            URL url = null;
            URLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                url = new URL("http://www.radiostereo.nl/paginas/app%202.0/chat_laden.php");
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
            progressDialog.dismiss();
            chat_laden_klaar();
        }

    }


    private void chat_laden_klaar() {
        resultaat = resultaat.replace("[enter]", "<br />");
        resultaat = resultaat.replace("[rood]", "<font color='#ED1C24'>");
        resultaat = resultaat.replace("[/rood]", "</font>");
        resultaat = resultaat.replace("[groen]", "<font color='#22B14C'>");
        resultaat = resultaat.replace("[/groen]", "</font>");
        resultaat = resultaat.replace("[blauw]", "<font color='#00A2E8'>");
        resultaat = resultaat.replace("[/blauw]", "</font>");
        resultaat = resultaat.replace("[oranje]", "<font color='#FF8000'>");
        resultaat = resultaat.replace("[/oranje]", "</font>");
        resultaat = resultaat.replace("[paars]", "<font color='#A349A4'>");
        resultaat = resultaat.replace("[/paars]", "</font>");
        textView1.setText(Html.fromHtml(resultaat));
        progressDialog.dismiss();
        new pauze().execute();
    }

    private class pauze extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params)  {
            try{
                Thread.sleep(1000);
            }catch(InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            new chat_laden().execute();
        }
    }

    private class chat_verzenden extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params)  {

            URL url = null;
            URLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                url = new URL("http://www.radiostereo.nl/paginas/app%202.0/chat_verzenden.php?gebruiker="+naam+"&bericht="+bericht);
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
            progressDialog.dismiss();
        }

    }

}