package nl.stefhost.radiostereo;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Switch;
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
import nl.stefhost.radiostereo.functies.Muziekspeler_offline;
import nl.stefhost.radiostereo.functies.laad_plaatje;

public class Album extends Fragment implements View.OnClickListener {

    static ProgressDialog progressDialog;
    public String nummer = "";
    public String resultaat = "";
    public String artiest = "";
    public String titel = "";
    public String album = "";
    public String status = "";
    public String link = "";
    public String naam;
    public String nummers;

    int aantal_nummers = 0;
    String[] alle_nummers;

    public ImageView imageView1;
    public ImageView imageView2;
    public TextView textView1;
    public TextView textView2;
    public TextView textView3;
    public TextView textView4;

    public boolean offline = false;

    Switch aSwitch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View View = inflater.inflate(R.layout.fragment_album, container, false);

        imageView1 = (ImageView) View.findViewById(R.id.plaatje);
        imageView2 = (ImageView) View.findViewById(R.id.plaatje_start_stop);

        textView1 = (TextView) View.findViewById(R.id.artiest);
        textView2 = (TextView) View.findViewById(R.id.titel);
        textView3 = (TextView) View.findViewById(R.id.nummers);
        textView4 = (TextView) View.findViewById(R.id.tekst_start_stop);

        aSwitch = (Switch) View.findViewById(R.id.Switch);

        aSwitch.setOnClickListener(this);
        imageView2.setOnClickListener(this);
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

        nummer = getArguments().getString("nummer");

        progressDialog = ProgressDialog.show(this.getContext(), "Informatie laden", "Even geduld aub..", true, false);
        new info_laden().execute();
    }

    private class info_laden extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params)  {

            URL url = null;
            URLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                url = new URL("http://www.radiostereo.nl/paginas/app%202.0/album_info.php?id="+nummer);
            } catch (MalformedURLException e) {
                System.out.println("MalformedURLException");
            }

            if (url != null){
                try{
                    urlConnection = url.openConnection();
                }catch (IOException e){
                    System.out.println("java.io.IOException");
                }
            }

            if (urlConnection != null){
                try{
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }catch (IOException e) {
                    System.out.println("java.io.IOException");
                }
            }

            if (inputStream != null){

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                try{
                    resultaat = bufferedReader.readLine();
                }catch (IOException e) {
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
        album = tokens.nextToken();
        String plaatje = tokens.nextToken();
        aantal_nummers = Integer.valueOf(tokens.nextToken());
        nummers = tokens.nextToken();

        alle_nummers = new String[aantal_nummers];

        int tellen = 0;
        StringTokenizer tokens2 = new StringTokenizer(nummers, ",");
        while (tokens2.hasMoreElements()){
            String titel = tokens2.nextToken();
            alle_nummers[tellen] = titel;
            int nummer = tellen+1;
            String nummer_tekst;
            if (tellen < 9) {
                nummer_tekst = "0"+nummer;
            }else{
                nummer_tekst = ""+nummer;
            }
            textView3.append(nummer_tekst+". "+artiest+" - "+titel+"\n");
            tellen++;
        }

        artiest = artiest.replace("[e]", "é");
        titel = titel.replace("[e]", "é");

        if (plaatje.matches("NEE")){
            imageView1.setBackgroundResource(R.drawable.albumart);
        }else{
            plaatje = plaatje.replace("&amp;", "&");
            new laad_plaatje(imageView1).execute(plaatje);
        }

        textView1.setText(artiest);
        textView2.setText(album);

        File opslag = Environment.getExternalStorageDirectory();
        final File map = new File(opslag,"/Radio Stereo/"+artiest+"/"+album+"/");
        if (map.exists()) {
            offline = true;
            aSwitch.setChecked(true);
        }

    }

    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.plaatje_start_stop || id == R.id.tekst_start_stop){

            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("opties", 0);
            String computer = sharedPreferences.getString("computer", "UIT");
            if (computer.equals("UIT")) {
                afspelen();
            }else{
                progressDialog = ProgressDialog.show(this.getContext(), "Afspelen op RadioStereo.nl", "Even geduld aub..", true, false);
                new playlist().execute();
            }

        }else if (id == R.id.Switch){
            aSwitch.setChecked(false);
            download();
        }

    }

    public void afspelen(){
        /*if (status.matches("")){
            new Muziekspeler().execute(link);
            textView4.setText("Stoppen");
            imageView2.setImageResource(R.drawable.stop);
            status = "UIT";
            NotificationCompat.Builder Builder = new NotificationCompat.Builder(this.getContext()).setSmallIcon(R.drawable.icon).setContentTitle("Radio Stereo").setContentText(artiest+" - "+titel);
            NotificationManager NotificationManager = (NotificationManager) getActivity().getSystemService(Beginscherm.NOTIFICATION_SERVICE);
            NotificationManager.notify(1, Builder.build());
        }else if (status.matches("AAN")){
            Muziekspeler.start();
            textView4.setText("Stoppen");
            imageView2.setImageResource(R.drawable.stop);
            status = "UIT";
            NotificationCompat.Builder Builder = new NotificationCompat.Builder(this.getContext()).setSmallIcon(R.drawable.icon).setContentTitle("Radio Stereo").setContentText(artiest+" - "+titel);
            NotificationManager NotificationManager = (NotificationManager) getActivity().getSystemService(Beginscherm.NOTIFICATION_SERVICE);
            NotificationManager.notify(1, Builder.build());
        }else{
            Muziekspeler.stop();
            textView4.setText("Afspelen");
            imageView2.setImageResource(R.drawable.start);
            status = "AAN";
            NotificationManager NotificationManager = (NotificationManager) getActivity().getSystemService(Beginscherm.NOTIFICATION_SERVICE);
            NotificationManager.cancel(1);
        }*/

        if (offline){

            SQLiteDatabase SQLiteDatabase = this.getContext().openOrCreateDatabase("Database", Context.MODE_PRIVATE, null);

            int tellen = 1;
            while (tellen < aantal_nummers + 1) {

                String titel = alle_nummers[tellen - 1];
                String nummers;
                if (tellen < 9) {
                    nummers = "0" + tellen;
                }else{
                    nummers = "" + tellen;
                }
                titel = titel.replace("'", "[komma]");
                SQLiteDatabase.execSQL("INSERT INTO playlist (artiest, titel, album, nummer, online, album_id) VALUES ('"+artiest+"', '"+titel+"', '"+album+"', '"+nummers+"', 'offline', '"+nummer+"')");
                tellen++;
            }

            Beginscherm.test_functie();
        }else{
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Online afspelen is nog in de maak!", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private class playlist extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params)  {

            URL url = null;
            URLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                url = new URL("http://www.radiostereo.nl/paginas/app%202.0/playlist.php?gebruiker="+naam+"&id="+nummer);
            } catch (MalformedURLException e) {
                System.out.println("MalformedURLException");
            }

            if (url != null){
                try{
                    urlConnection = url.openConnection();
                }catch (IOException e){
                    System.out.println("java.io.IOException");
                }
            }

            if (urlConnection != null){
                try{
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }catch (IOException e) {
                    System.out.println("java.io.IOException");
                }
            }

            if (inputStream != null){

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                try{
                    resultaat = bufferedReader.readLine();
                }catch (IOException e) {
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
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Het nummer wordt afgespeeld op RadioStereo.nl!", Toast.LENGTH_LONG);
            toast.show();
        }

    }

    int download_nummer = 1;

    public void download(){

        titel = alle_nummers[download_nummer-1];
        link = "http://muziek.radiostereo.nl/"+artiest+" - "+titel+".mp3";
        link = link.replace(" ", "%20");

        download_nummer++;
        progressDialog = new ProgressDialog(this.getContext());
        progressDialog.setMessage("Downloaden 1 van "+aantal_nummers+"\nEven geduld aub...");
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();
        new downloaden().execute();
    }

    @SuppressWarnings("all")
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
                final File map = new File(opslag,"/Radio Stereo/"+artiest+"/"+album+"/");
                if (!map.exists()) {
                    map.mkdirs();
                }
                int nummer = download_nummer -1;
                String nummer_nul;
                if (nummer < 10){
                    nummer_nul = "0"+nummer;
                }else{
                    nummer_nul = ""+nummer;
                }
                File file = new File(map, nummer_nul+". "+artiest+" - "+titel+".mp3");
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

            } catch (MalformedURLException e) {
                Log.d("RadioStereo", "MalformedURLException");
            } catch (IOException e) {
                Log.d("RadioStereo", "IOException");
            }

            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {

            if (download_nummer < aantal_nummers+1){
                titel = alle_nummers[download_nummer-1];
                link = "http://muziek.radiostereo.nl/"+artiest+" - "+titel+".mp3";
                link = link.replace(" ", "%20");
                progressDialog.setMessage("Downloaden "+download_nummer+" van "+aantal_nummers+"\nEven geduld aub...");
                //progressDialog.setIndeterminate(false);
                //progressDialog.setMax(100);
                //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                //progressDialog.show();
                download_nummer++;
                new downloaden().execute();
            }else{
                aSwitch.setChecked(true);
                offline = true;
                progressDialog.dismiss();
            }
        }

    }

}