package nl.stefhost.radiostereo;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

import nl.stefhost.radiostereo.functies.Muziekspeler;
import nl.stefhost.radiostereo.functies.laad_plaatje;

public class Video extends Fragment implements View.OnClickListener {

    static ProgressDialog progressDialog;
    public String video = "";
    public String resultaat = "";
    public String artiest = "";
    public String titel = "";
    public String status = "";
    public String link = "";
    public String naam;

    public ImageView imageView1;
    public ImageView imageView2;
    public ImageView imageView3;
    public ImageView imageView4;
    public ImageView imageView5;
    public TextView textView1;
    public TextView textView2;
    public TextView textView3;
    public TextView textView4;
    public TextView textView5;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View View = inflater.inflate(R.layout.fragment_video, container, false);

        imageView1 = (ImageView) View.findViewById(R.id.plaatje);
        imageView2 = (ImageView) View.findViewById(R.id.plaatje_start_stop);
        imageView3 = (ImageView) View.findViewById(R.id.plaatje_download);

        textView1 = (TextView) View.findViewById(R.id.artiest);
        textView2 = (TextView) View.findViewById(R.id.titel);
        textView3 = (TextView) View.findViewById(R.id.tekst_start_stop);
        textView4 = (TextView) View.findViewById(R.id.tekst_download);

        imageView2.setOnClickListener(this);
        imageView3.setOnClickListener(this);

        textView3.setOnClickListener(this);
        textView4.setOnClickListener(this);

        RelativeLayout relativeLayout = (RelativeLayout) View.findViewById(R.id.relativeLayout);
        relativeLayout.setSoundEffectsEnabled(false);
        relativeLayout.setOnClickListener(this);

        return View;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("opties", 0);
        naam = sharedPreferences.getString("naam", "");

        video = getArguments().getString("video");

        progressDialog = android.app.ProgressDialog.show(this.getContext(), "Informatie laden", "Even geduld aub..", true, false);
        new info_laden().execute();
    }

    private class info_laden extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params)  {

            URL url = null;
            URLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                url = new URL("http://www.radiostereo.nl/paginas/app%202.0/video_info.php?id="+video);
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
            info_laden_klaar();
        }

    }

    private void info_laden_klaar() {

        StringTokenizer tokens = new StringTokenizer(resultaat, "|");

        artiest = tokens.nextToken();
        titel = tokens.nextToken();
        String plaatje = tokens.nextToken();

        artiest = artiest.replace("[e]", "é");
        titel = titel.replace("[e]", "é");
        link = "http://videos.radiostereo.nl/"+artiest+" - "+titel+".mp4";
        link = link.replace(" ", "%20");

        if (plaatje.matches("NEE")){
            imageView1.setBackgroundResource(R.drawable.albumart);
        }else{
            plaatje = plaatje.replace("&amp;", "&");
            new laad_plaatje(imageView1).execute(plaatje);
        }

        textView1.setText(artiest);
        textView2.setText(titel);

    }

    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.plaatje_start_stop || id == R.id.tekst_start_stop){

            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("opties", 0);
            String computer = sharedPreferences.getString("computer", "UIT");
            if (computer.equals("UIT")) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(link), "video/mp4");
                startActivity(intent);
            }else{
                progressDialog = android.app.ProgressDialog.show(this.getContext(), "Afspelen op RadioStereo.nl", "Even geduld aub..", true, false);
                new playlist().execute();
            }

        }else if (id == R.id.plaatje_download || id == R.id.tekst_download){
            download();
        }

    }

    private class playlist extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params)  {

            URL url = null;
            URLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                url = new URL("http://www.radiostereo.nl/paginas/app%202.0/playlist.php?gebruiker="+naam+"&id="+video+"&video");
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
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "De video wordt afgespeeld op RadioStereo.nl!", Toast.LENGTH_LONG);
            toast.show();
        }

    }

    public void download(){
        progressDialog = new ProgressDialog(this.getContext());
        progressDialog.setMessage("Downloaden\nEven geduld aub...");
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        new downloaden().execute();
    }


    class downloaden extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... f_url) {

            try {
                URL url = new URL(link);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                File opslag = Environment.getExternalStorageDirectory();
                final File map = new File(opslag,"/Radio Stereo/");
                if (!map.exists()){
                    map.mkdir();
                }else{

                    File file = new File(map, artiest + " - " + titel + ".mp4");
                    FileOutputStream fileOutput = new FileOutputStream(file);
                    InputStream inputStream = urlConnection.getInputStream();

                    int totalSize = urlConnection.getContentLength();
                    int downloadedSize = 0;

                    byte[] buffer = new byte[1024];
                    int bufferLength;
                    int download;

                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        fileOutput.write(buffer, 0, bufferLength);
                        downloadedSize += bufferLength;
                        download = ((downloadedSize * 100) / totalSize);
                        progressDialog.setProgress(download);
                    }
                    fileOutput.close();
                }

            } catch (MalformedURLException e) {
                Log.d("RadioStereo", "MalformedURLException");
            } catch (IOException e) {
                Log.d("RadioStereo", "IOException");
            }

            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            progressDialog.dismiss();
        }

    }

}