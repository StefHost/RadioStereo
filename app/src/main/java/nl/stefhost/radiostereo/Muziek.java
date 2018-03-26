package nl.stefhost.radiostereo;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Muziek extends Fragment implements View.OnClickListener {

    static ProgressDialog progressDialog;
    public ArrayAdapter<String> arrayAdapter;
    ArrayList<String> stringList1 = new ArrayList<>();
    ArrayList<String> stringList2 = new ArrayList<>();
    public String resultaat;
    public String keuze = "alles";
    public String naam;
    public ListView listView;

    public TextView menu_1;
    public TextView menu_2;
    public TextView menu_3;
    public TextView menu_4;

    public String computer;
    public String mp3_path;
    public String zoekterm = "";
    public String afspeellijst = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View View = inflater.inflate(R.layout.fragment_muziek, container, false);
        setHasOptionsMenu(true);

        listView = (ListView) View.findViewById(R.id.listView);

        menu_1 = (TextView) View.findViewById(R.id.menu_1);
        menu_2 = (TextView) View.findViewById(R.id.menu_2);
        menu_3 = (TextView) View.findViewById(R.id.menu_3);
        menu_4 = (TextView) View.findViewById(R.id.menu_4);

        menu_1.setOnClickListener(this);
        menu_2.setOnClickListener(this);
        menu_3.setOnClickListener(this);
        menu_4.setOnClickListener(this);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("opties", 0);
        naam = sharedPreferences.getString("naam", "");
        computer = sharedPreferences.getString("computer", "UIT");

        progressDialog = android.app.ProgressDialog.show(this.getContext(), "Muziek laden", "Even geduld aub..", true, false);
        new muziek_laden().execute();

        return View;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_muziek, menu);
        if (naam.equals("Stefan") || naam.equals("Ronald")){
            //inflater.inflate(R.menu.menu_muziek_admin, menu);
        }

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setIconifiedByDefault(true);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setQueryHint("Zoeken");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String tekst) {
                zoekterm = tekst;
                zoekterm = zoekterm.replace(" ", "%20");
                zoekterm = zoekterm.replace("é", "[e]");
                zoeken();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String tekst) {
                if (tekst.isEmpty()){
                    zoekterm = "";
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
    }

    public void zoeken(){
        progressDialog = android.app.ProgressDialog.show(this.getContext(), "Muziek zoeken", "Even geduld aub..", true, false);
        new muziek_laden().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.computer_uit:
                SharedPreferences sharedPreferences1 = getActivity().getSharedPreferences("opties", 0);
                SharedPreferences.Editor editor1 = sharedPreferences1.edit();
                editor1.putString("computer", "AAN");
                editor1.apply();
                computer = "AAN";
                getActivity().invalidateOptionsMenu();
                return true;
            case R.id.computer_aan:
                SharedPreferences sharedPreferences2 = getActivity().getSharedPreferences("opties", 0);
                SharedPreferences.Editor editor2 = sharedPreferences2.edit();
                editor2.putString("computer", "UIT");
                editor2.apply();
                computer = "UIT";
                getActivity().invalidateOptionsMenu();
                return true;
            case R.id.playlist:
                progressDialog = android.app.ProgressDialog.show(this.getContext(), "Muziek toevoegen aan playlist", "Even geduld aub..", true, false);
                new playlist().execute();
                return true;
            case R.id.muziek_uploaden:
                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1234);
                return true;
            case R.id.alles_afspelen:
                android.database.sqlite.SQLiteDatabase SQLiteDatabase = this.getContext().openOrCreateDatabase("Database", Context.MODE_PRIVATE, null);

                int aantal = listView.getCount();
                int tellen = 0;
                while (tellen < aantal) {
                    String totaal = listView.getItemAtPosition(tellen).toString();
                    //Log.d("Radio Stereo", "" + totaal);
                    String[] splitsen = totaal.split(" - ");
                    String artiest = splitsen[0];
                    String titel = splitsen[1];
                    SQLiteDatabase.execSQL("INSERT INTO playlist (artiest, titel, online, album_id) VALUES ('"+artiest+"', '"+titel+"', 'online', '0')");
                    tellen++;
                }

                Beginscherm.test_functie();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1234) {
            Uri uri = data.getData();
            String[] path1 = {MediaStore.Audio.Media.DATA};

            Cursor cursor = getContext().getContentResolver().query(uri, path1, null, null, null);
            if (cursor != null){
                cursor.moveToFirst();
                int index = cursor.getColumnIndex(path1[0]);
                mp3_path = cursor.getString(index);
                cursor.close();
            }

            String[] path2 = {MediaStore.Audio.Media.TITLE};
            cursor = getContext().getContentResolver().query(uri, path2, null, null, null);
            if (cursor != null){
                cursor.moveToFirst();
                int index = cursor.getColumnIndex(path2[0]);
                String mp3_title = cursor.getString(index);
                cursor.close();

                String artiest;
                String titel;
                if (mp3_title.contains("-")){
                    StringTokenizer tokens = new StringTokenizer(mp3_title, "-");
                    artiest = tokens.nextToken();
                    titel = tokens.nextToken();
                }else{
                    artiest = mp3_title;
                    titel = mp3_title;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_upload, null);

                EditText editText1 = (EditText)view.findViewById(R.id.editText1);
                EditText editText2 = (EditText)view.findViewById(R.id.editText2);
                editText1.setText(artiest);
                editText2.setText(titel);

                builder.setView(view).setTitle("Muziek Uploaden");
                builder.setPositiveButton("Bevestigen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText editText1 = (EditText)view.findViewById(R.id.editText1);
                        EditText editText2 = (EditText)view.findViewById(R.id.editText2);
                        String artiest = editText1.getText().toString();
                        String titel = editText2.getText().toString();
                        artiest = artiest.replace(" ", "%20");
                        titel = titel.replace(" ", "%20");
                        muziek_uploaden(artiest, titel);
                    }
                });
                builder.show();

            }

        }

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuItem1 = menu.findItem(R.id.computer_uit);
        MenuItem menuItem2 = menu.findItem(R.id.computer_aan);
        MenuItem menuItem3 = menu.findItem(R.id.playlist);
        MenuItem menuItem4 = menu.findItem(R.id.alles_afspelen);

        if (computer.equals("UIT")) {
            menuItem1.setVisible(true);
            menuItem2.setVisible(false);
            menuItem3.setVisible(false);
            if ((keuze.equals("afspeellijsten") && !afspeellijst.equals("")) || keuze.equals("favorieten")){
                menuItem4.setVisible(true);
            }
        }else{
            menuItem1.setVisible(false);
            menuItem2.setVisible(true);
            if (keuze.equals("favorieten")) {
                menuItem3.setVisible(true);
            }
            menuItem4.setVisible(false);
        }
    }

    public void onClick(View v) {

        stringList1.clear();
        stringList2.clear();

        int id = v.getId();

        Drawable selectie = getResources().getDrawable(R.drawable.selectie);

        menu_1.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        menu_2.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        menu_3.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        menu_4.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        if (id == R.id.menu_1){
            keuze = "alles";
            menu_1.setCompoundDrawablesWithIntrinsicBounds(null, null, null, selectie);
        }else if (id == R.id.menu_2){
            keuze = "albums";
            menu_2.setCompoundDrawablesWithIntrinsicBounds(null, null, null, selectie);
        }else if (id == R.id.menu_3){
            keuze = "favorieten";
            menu_3.setCompoundDrawablesWithIntrinsicBounds(null, null, null, selectie);
        }else{
            keuze = "afspeellijsten";
            afspeellijst = "";
            menu_4.setCompoundDrawablesWithIntrinsicBounds(null, null, null, selectie);
        }

        getActivity().invalidateOptionsMenu();

        progressDialog = android.app.ProgressDialog.show(this.getContext(), "Muziek laden", "Even geduld aub..", true, false);
        new muziek_laden().execute();

    }

    private class muziek_laden extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params)  {

            URL url = null;
            URLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                url = new URL("http://www.radiostereo.nl/paginas/app%202.0/muziek_laden.php?keuze="+keuze+"&gebruiker="+naam+"&zoekterm="+zoekterm+"&afspeellijst="+afspeellijst);
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
                resultaat = inputStream.toString();

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line;
                try{
                    while ((line = bufferedReader.readLine()) != null) {
                        line = line.replace("[e]", "é");
                        StringTokenizer tokens = new StringTokenizer(line, "|");
                        String first = tokens.nextToken();
                        String second = tokens.nextToken();
                        stringList1.add(first);
                        stringList2.add(second);
                    }

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
            muziek_laden_klaar();
        }

    }

    public void muziek_laden_klaar(){

        if (!resultaat.matches("ERROR")) {
            arrayAdapter = new ArrayAdapter<>(this.getContext(),android.R.layout.simple_list_item_1, stringList1);
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    String nummer = stringList2.get((int) id);
                    if (!nummer.matches("0")){

                        Bundle bundle = new Bundle();
                        bundle.putString("nummer", nummer);

                        if (keuze.equals("albums")) {
                            Fragment fragment = new Album();
                            fragment.setArguments(bundle);
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            fragmentManager.beginTransaction().add(R.id.frame_layout, fragment, "NUMMER").addToBackStack(null).commit();

                        }else if(keuze.equals("afspeellijsten")){
                            if (afspeellijst.equals("")){
                                afspeellijst = nummer;
                                progressDialog = android.app.ProgressDialog.show(getContext(), "Muziek laden", "Even geduld aub..", true, false);

                                stringList1.clear();
                                stringList2.clear();
                                new muziek_laden().execute();
                            }else{
                                Fragment fragment = new Nummer();
                                fragment.setArguments(bundle);
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction().add(R.id.frame_layout, fragment, "NUMMER").addToBackStack(null).commit();
                            }
                        }else{
                            Fragment fragment = new Nummer();
                            fragment.setArguments(bundle);
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            fragmentManager.beginTransaction().add(R.id.frame_layout, fragment, "NUMMER").addToBackStack(null).commit();
                        }
                    }
                }
            });

            getActivity().invalidateOptionsMenu();
        }

    }

    public void muziek_uploaden(final String artiest, final String titel){
        progressDialog = android.app.ProgressDialog.show(this.getContext(), "Muziek uploaden", "Even geduld aub..", true, false);

        new Thread(new Runnable() {
            public void run() {
                uploadFile(artiest,titel);
            }
        }).start();
    }

    int serverResponseCode = 0;
    public int uploadFile(String artiest, String titel) {

        Log.d("RadioStereo", artiest+"-"+titel);

        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        File sourceFile = new File(mp3_path);

        if (!sourceFile.isFile()) {
            //Source File not exist
            return 0;
        }else{
            try {

                FileInputStream fileInputStream = new FileInputStream(sourceFile);

                URL url = new URL("http://muziek.radiostereo.nl/upload.php?gebruiker="+naam+"&artiest="+artiest+"&titel="+titel);

                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", mp3_path);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename="+ mp3_path + lineEnd);
                dos.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){
                    progressDialog.dismiss();
                }

                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                Log.e("Viewfinder", "MalformedURLException");
            } catch (Exception e) {
                Log.e("Viewfinder", ""+e);
            }
            return serverResponseCode;

        }
    }

    private class playlist extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params)  {

            URL url = null;
            URLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                url = new URL("http://www.radiostereo.nl/paginas/app%202.0/playlist.php?gebruiker="+naam+"&keuze="+keuze);
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
            playlist_klaar();
        }

    }

    public void playlist_klaar() {
        Toast toast = Toast.makeText(this.getContext(), "De muziek is toegevoegd aan je online playlist", Toast.LENGTH_SHORT);
        toast.show();
    }

}