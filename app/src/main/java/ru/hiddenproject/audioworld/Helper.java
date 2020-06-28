package ru.hiddenproject.audioworld;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Helper {
    public static final String API_VERSION = "KING";
    public static final String HISTORY_PREFS_FILE = "AudioWorldHistory";
    public static final String ARCHIVE_PREFS_FILE = "AudioWorldArchive";
    public AlertDialog makeAlert(Context c, String title, String message, String negative){
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.ic_launcher_background)
                .setCancelable(false)
                .setNegativeButton(negative,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        return alert;
    }
    public String formatTime(int milliseconds){
        String result = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
        );
        return result;
    }
    public void addToArchive(Context c, int bookID){
        SharedPreferences prefs = c.getSharedPreferences(ARCHIVE_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(String.valueOf(bookID), bookID);
        editor.apply();
        editor.commit();
    }
    public boolean inArchive(Context c, int bookID){
        SharedPreferences prefs = c.getSharedPreferences(ARCHIVE_PREFS_FILE, Context.MODE_PRIVATE);
        int id = prefs.getInt(String.valueOf(bookID),-1);
        Log.d("HELPER", String.valueOf(id));
        if(id!=-1)return true;
        else return false;
    }
    public void removeFromArchive(Context c, int bookID){
        SharedPreferences prefs = c.getSharedPreferences(ARCHIVE_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Log.d("HELPER", "TRYING TO REMOVE "+String.valueOf(bookID));
        editor.remove(String.valueOf(bookID)).commit();
    }
    public String getArchive(Context c){
        SharedPreferences prefs = c.getSharedPreferences(ARCHIVE_PREFS_FILE, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        String bookIDs = "";
        int i =0;
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if(i!=0)bookIDs+=",";
            bookIDs+=entry.getValue().toString();
            i++;
        }
        return bookIDs;
    }
    public void saveHistory(Context c, int bookID, int trackID, int position){
        ArrayList<History> history = new ArrayList<History>();
        history.add(new History(bookID, trackID, position));
        // save the task list to preference
        SharedPreferences prefs = c.getSharedPreferences(HISTORY_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(String.valueOf(bookID),String.valueOf(trackID)+":"+String.valueOf(position));
        editor.apply();
        editor.commit();
    }
    public History getHistory(Context c, int bookID){
        SharedPreferences prefs = c.getSharedPreferences(HISTORY_PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String data = prefs.getString(String.valueOf(bookID), null);

        if(data!=null){
            String[] info = data.split(":");
            try {
                Log.d("PLAYER", "HELPER TRACK="+info[0]+" POSITION="+info[1]);
                History history = new History(bookID, Integer.parseInt(info[0]), Integer.parseInt(info[1]));
                return history;
            }catch (NumberFormatException e){
                return null;
            }
        }else{
            return null;
        }
    }
}
