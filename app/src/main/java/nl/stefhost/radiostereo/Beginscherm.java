package nl.stefhost.radiostereo;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.stefhost.radiostereo.functies.Muziekspeler_offline;

public class Beginscherm extends AppCompatActivity {

    private android.support.v4.widget.DrawerLayout DrawerLayout;
    private android.widget.ListView ListView;
    private ActionBarDrawerToggle DrawerToggle;
    public int icon;

    static SQLiteDatabase SQLiteDatabase;
    static NotificationCompat.Builder Builder;
    static NotificationManager NotificationManager;
    static PendingIntent pendingIntent1;
    static PendingIntent pendingIntent2;
    static PendingIntent pendingIntent3;

    static Context context;

    int[] icons = new int[]{R.drawable.chat, R.drawable.muziek, R.drawable.muziekraden, R.drawable.videos, R.drawable.streep, R.drawable.over, R.drawable.uitloggen};
    String[] titles = new String[] {"Chat", "Muziek", "Muziekraden", "Video's", "", "Over", "Uitloggen"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beginscherm);

        SharedPreferences sharedPreferences = getSharedPreferences("opties", 0);
        String naam = sharedPreferences.getString("naam", "");

        if (naam.equals("")){
            Intent intent = new Intent(this, Inloggen.class);
            startActivity(intent);
            finish();
        }else{
            DrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            DrawerToggle = new ActionBarDrawerToggle(this, DrawerLayout, 0, 0) {};
            DrawerLayout.setDrawerListener(DrawerToggle);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
            }

            List<HashMap<String, String>> List = new ArrayList<>();

            for (int i = 0; i < titles.length; i++) {
                HashMap<String, String> HashMap = new HashMap<>();
                HashMap.put("icon", Integer.toString(icons[i]));
                HashMap.put("title", titles[i]);
                List.add(HashMap);
            }

            String[] van = {"icon", "title"};
            int[] naar = {R.id.imageView, R.id.textView};

            SimpleAdapter SimpleAdapter = new SimpleAdapter(getBaseContext(), List, R.layout.listview_home, van, naar);
            ListView = (ListView) findViewById(R.id.list_view);
            ListView.setAdapter(SimpleAdapter);
            ListView.setOnItemClickListener(new DrawerItemClickListener());

            getSupportActionBar().setTitle("   Radio Stereo");
            getSupportActionBar().setIcon(ContextCompat.getDrawable(this, R.mipmap.ic_launcher));

            //DrawerLayout.openDrawer(ListView);

            Intent intent = getIntent();
            Uri data = intent.getData();
            String link = ""+data;
            link = link.replace("http://app.radiostereo.nl/", "");

            if (!link.equals("null")){
                Log.d("Radio Stereo Begin: ", ""+link);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("link", link);
                editor.apply();
                selectItem("Nummer");
            }else{
                selectItem("Radio Stereo");
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }

            SQLiteDatabase = this.openOrCreateDatabase("Database", Context.MODE_PRIVATE, null);
            SQLiteDatabase.execSQL("delete from playlist");

            //Builder = new NotificationCompat.Builder(this);
            NotificationManager = (NotificationManager) this.getSystemService(Beginscherm.NOTIFICATION_SERVICE);

            Intent intent1 = new Intent(this, Notificaties.class);
            intent1.setAction("stop");
            Intent intent2 = new Intent(this, Notificaties.class);
            intent2.setAction("volgende");
            Intent intent3 = new Intent(this, Notificaties.class);
            intent3.setAction("start");

            pendingIntent1 = PendingIntent.getBroadcast(this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
            pendingIntent2 = PendingIntent.getBroadcast(this, 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
            pendingIntent3 = PendingIntent.getBroadcast(this, 0, intent3, PendingIntent.FLAG_UPDATE_CURRENT);

            context = Beginscherm.this;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        new huidig_nummer().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return DrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        DrawerToggle.syncState();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            String keuze = titles[position];
            if (keuze.equals("Uitloggen")){
                AlertDialog.Builder builder = new AlertDialog.Builder(Beginscherm.this);
                builder.setTitle("Uitloggen")
                        .setMessage("Weet je zeker dat je wilt uitloggen?");
                builder.setPositiveButton("JA", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        uitloggen();
                    }
                });
                builder.setNegativeButton("NEE", null);
                builder.show();
            }else{
                if (!keuze.equals("")) {
                    selectItem(keuze);
                }
            }
        }
    }

    public void uitloggen(){
        SharedPreferences sharedPreferences = getSharedPreferences("opties", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("naam", "");
        editor.apply();
        Intent intent = new Intent(this, Inloggen.class);
        startActivity(intent);
        finish();
    }

    private void selectItem(String keuze) {

        Fragment fragment = null;

        switch (keuze){
            case "Chat":
                fragment = new Chat();
                icon = R.drawable.chat;
                break;
            case "Muziek":
                fragment = new Muziek();
                icon = R.drawable.muziek;
                break;
            case "Muziekraden":
                fragment = new Muziekraden();
                icon = R.drawable.muziekraden;
                break;
            case "Radio Stereo":
                fragment = new Nieuws();
                icon = R.drawable.icon;
                break;
            case "Nummer":
                fragment = new Nummer();
                icon = R.drawable.muziek;
                break;
            case "Over":
                fragment = new Over();
                icon = R.drawable.over;
                break;
            case "Video's":
                fragment = new Videos();
                icon = R.drawable.videos;
                break;
        }

        DrawerLayout.closeDrawer(ListView);
        if (getSupportFragmentManager().getBackStackEntryCount() > 0){
            getSupportFragmentManager().popBackStack();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, fragment).commit();
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("   " +keuze);
        }
        getSupportActionBar().setIcon(ContextCompat.getDrawable(this, icon));
    }

    public void onBackPressed() {

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("NUMMER");

        if (fragment != null){
            super.onBackPressed();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(Beginscherm.this);
            builder.setTitle("Afsluiten")
                    .setMessage("Weet je zeker dat je de Radio Stereo App wilt afsluiten?")
                    .setCancelable(false);
            builder.setPositiveButton("JA", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            builder.setNegativeButton("NEE", null);
            builder.show();
        }
    }

    static String notificatie_tekst;

    public static void test_functie(){

        Cursor cursor = SQLiteDatabase.rawQuery("SELECT id, artiest, titel, album, nummer, online FROM playlist", null);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String artiest = cursor.getString(cursor.getColumnIndex("artiest"));
            String titel = cursor.getString(cursor.getColumnIndex("titel"));
            String album = cursor.getString(cursor.getColumnIndex("album"));
            String nummer = cursor.getString(cursor.getColumnIndex("nummer"));
            String online = cursor.getString(cursor.getColumnIndex("online"));
            //Log.d("Radio Stereo", artiest+"|"+titel+"|"+album+"|"+nummer+"|"+online);
            SQLiteDatabase.execSQL("delete from playlist WHERE id='" + id + "'");

            titel = titel.replace("[komma]", "'");

            notificatie_tekst = artiest + " - " + titel;

            Builder = new NotificationCompat.Builder(context);

            Builder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Radio Stereo")
                    .setContentText(notificatie_tekst);

            Builder.addAction(R.drawable.stop, "", pendingIntent1);
            Builder.addAction(R.drawable.start, "", pendingIntent2);

            NotificationManager.notify(1, Builder.build());

            new Muziekspeler_offline().execute(artiest, titel, album, nummer);

        }else{
            Muziekspeler_offline.stop();
            NotificationManager.cancel(1);
        }

        cursor.close();
    }

    public static void notifcatie_stop(){

        Builder = new NotificationCompat.Builder(context);

        Builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Radio Stereo")
                .setContentText(notificatie_tekst);

        Builder.addAction(R.drawable.kleur_paars, "", pendingIntent3);
        Builder.addAction(R.drawable.kleur_blauw, "", pendingIntent2);

        NotificationManager.notify(1, Builder.build());

    }

    public static void notifcatie_start(){

        Builder = new NotificationCompat.Builder(context);

        Builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Radio Stereo")
                .setContentText(notificatie_tekst);

        Builder.addAction(R.drawable.stop, "", pendingIntent1);
        Builder.addAction(R.drawable.start, "", pendingIntent2);

        NotificationManager.notify(1, Builder.build());

    }

    public String resultaat;

    private class huidig_nummer extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params)  {

            URL url = null;
            URLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                url = new URL("http://www.radiostereo.nl/test/paginas/huidig_nummer/Stefan.txt");
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
            if (!resultaat.equals("LEEG")){
                //resultaat = resultaat.replace(" - ", "\n");
                TextView textView = (TextView) findViewById(R.id.textView);
                textView.setText(resultaat);
            }
            //new pauze().execute();
        }

    }

    private class pauze extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params)  {
            try{
                Thread.sleep(10000);
            }catch(InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            new huidig_nummer().execute();
        }
    }

}