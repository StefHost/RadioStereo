package nl.stefhost.radiostereo.functies;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;

public class laad_plaatje extends AsyncTask<String, Void, Bitmap> {
	
	  ImageView imageview;

	  public laad_plaatje(ImageView imageview) {
	      this.imageview = imageview;
	  }

	  protected Bitmap doInBackground(String... urls) {
	      String link = urls[0];
	      Bitmap plaatje = null;
	      try {
	        InputStream in = new java.net.URL(link).openStream();
	        plaatje = BitmapFactory.decodeStream(in);
	      } catch (Exception e) {
	          e.printStackTrace();
	      }
	      return plaatje;
	  }

	  protected void onPostExecute(Bitmap result) {

          imageview.setBackgroundResource(android.R.color.transparent);
          imageview.setImageBitmap(result);
	  }
	}
