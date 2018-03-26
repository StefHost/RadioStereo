package nl.stefhost.radiostereo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import nl.stefhost.radiostereo.functies.Muziekspeler_offline;

public class Notificaties extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action.equals("stop")){
            Muziekspeler_offline.stop();
            Beginscherm.notifcatie_stop();
        }else if (action.equals("volgende")){
            Log.d("radiostereo", "volgende werkt!");
            Beginscherm.test_functie();
        }else if (action.equals("start")){
            Muziekspeler_offline.start();
            Beginscherm.notifcatie_start();
        }
    }

}