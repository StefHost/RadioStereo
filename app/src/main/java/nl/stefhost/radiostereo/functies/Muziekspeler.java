package nl.stefhost.radiostereo.functies;

import java.io.IOException;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import nl.stefhost.radiostereo.Beginscherm;

public class Muziekspeler extends AsyncTask<String, Void, String> {
	private static MediaPlayer mediaPlayer = new MediaPlayer();

	protected String doInBackground(String... url) {
		
			String test = url[0];
			Log.d("Radio Stereo", test);

			mediaPlayer.reset();
		    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		    try {
				mediaPlayer.setDataSource(test);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				mediaPlayer.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
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
	
	public static void pause() {
		mediaPlayer.pause();
	}
	
	public static void stop() {
		mediaPlayer.pause();
		mediaPlayer.seekTo(0);
	}
	
}
