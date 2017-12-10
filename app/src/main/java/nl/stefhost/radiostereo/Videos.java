package nl.stefhost.radiostereo;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Videos extends Fragment {

    static ProgressDialog progressDialog;
    public ArrayAdapter<String> arrayAdapter;
    ArrayList<String> stringList1 = new ArrayList<>();
    ArrayList<String> stringList2 = new ArrayList<>();
    public String resultaat;
    public ListView listView;

    public String computer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View View = inflater.inflate(R.layout.fragment_videos, container, false);
        setHasOptionsMenu(true);

        listView = (ListView) View.findViewById(R.id.listView);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("opties", 0);
        computer = sharedPreferences.getString("computer", "UIT");

        progressDialog = android.app.ProgressDialog.show(this.getContext(), "Video's laden", "Even geduld aub..", true, false);
        new videos_laden().execute();

        return View;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_muziek, menu);
        super.onCreateOptionsMenu(menu,inflater);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuItem1 = menu.findItem(R.id.computer_uit);
        MenuItem menuItem2 = menu.findItem(R.id.computer_aan);

        if (computer.equals("UIT")) {
            menuItem1.setVisible(true);
            menuItem2.setVisible(false);
        }else{
            menuItem1.setVisible(false);
            menuItem2.setVisible(true);
        }
    }

    private class videos_laden extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params)  {

            stringList1.clear();
            stringList2.clear();

            URL url = null;
            URLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                url = new URL("http://www.radiostereo.nl/paginas/app%202.0/videos_laden.php");
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
            videos_laden_klaar();
        }

    }

    public void videos_laden_klaar(){

        if (resultaat.matches("ERROR")) {
            //ERROR
        }else{
            arrayAdapter = new ArrayAdapter<>(this.getContext(),android.R.layout.simple_list_item_1, stringList1);
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    String video = stringList2.get((int) id);
                    if (!video.matches("0")){

                        Bundle bundle = new Bundle();
                        bundle.putString("video", video);
                        Fragment fragment = new Video();
                        fragment.setArguments(bundle);

                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.beginTransaction().add(R.id.frame_layout, fragment, "NUMMER").addToBackStack(null).commit();
                    }
                }
            });
        }

    }

}