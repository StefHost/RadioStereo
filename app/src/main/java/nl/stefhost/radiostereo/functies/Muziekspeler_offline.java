package nl.stefhost.radiostereo.functies;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;

import nl.stefhost.radiostereo.Beginscherm;

public class Muziekspeler_offline extends AsyncTask<String, Void, String> {
	private static MediaPlayer mediaPlayer = new MediaPlayer();

	protected String doInBackground(String... strings) {

		String artiest = strings[0];
		String titel = strings[1];
		String album = strings[2];
		String nummer = strings[3];

		mediaPlayer.reset();

		String filePath = Environment.getExternalStorageDirectory()+"/Radio Stereo/"+artiest+"/"+album+"/"+nummer+". "+artiest+" - "+titel+".mp3";

		try {
			mediaPlayer.setDataSource(filePath);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			Log.d("Radio Stereo", "IllegalArgumentException");
		} catch (SecurityException e) {
			e.printStackTrace();
			Log.d("Radio Stereo", "SecurityException");
		} catch (IllegalStateException e) {
			e.printStackTrace();
			Log.d("Radio Stereo", "IllegalStateException");
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("Radio Stereo", "IOException");
		}
		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			Log.d("Radio Stereo", "IllegalStateException");
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("Radio Stereo", "printStackTrace");
		}
			
		return null;
	}
	
    @Override
    protected void onPostExecute(String result) {
		mediaPlayer.start();

		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				Log.d("Radio Stereo", "nummer is afgelopen!");
				Beginscherm.test_functie();
			}
		});
    }

	public static void start() {
		mediaPlayer.start();
	}

	public static void stop() {
		mediaPlayer.pause();
		mediaPlayer.seekTo(0);
	}
	
}
