package ru.hiddenproject.audioworld;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Rating;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.RemoteViews;
import java.io.IOException;
import java.util.List;
import android.support.v4.app.NotificationCompat.Action;

public class Player extends Service {

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";
    IBinder mBinder = new Player.LocalBinder();
    private MediaPlayer mMediaPlayer;
    private MediaSessionManager mManager;
    private MediaSessionCompat mSession;
    private MediaControllerCompat mController;
    private int position = 0;
    private int bookID;
    private String title;
    private List<String> tracks;
    private int track;
    private String cover;
    private boolean isStarted = false;
    private Helper helper = new Helper();
    private History history;
    private Handler handler = new Handler();
    private void handleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null)
            return;

        String action = intent.getAction();

        if (action.equalsIgnoreCase(ACTION_PLAY)) {
            mController.getTransportControls().play();
        } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
            mController.getTransportControls().pause();
        } else if (action.equalsIgnoreCase(ACTION_FAST_FORWARD)) {
            mController.getTransportControls().fastForward();
        } else if (action.equalsIgnoreCase(ACTION_REWIND)) {
            mController.getTransportControls().rewind();
        } else if (action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            mController.getTransportControls().skipToPrevious();
        } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
            mController.getTransportControls().skipToNext();
        } else if (action.equalsIgnoreCase(ACTION_STOP)) {
            mController.getTransportControls().stop();
        }
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), Player.class);
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent).build();
    }

    private void buildNotification(NotificationCompat.Action action, String title) {
        android.support.v4.media.app.NotificationCompat.MediaStyle style = new android.support.v4.media.app.NotificationCompat.MediaStyle();
        Intent intent = new Intent(getApplicationContext(), Player.class);
        intent.setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        String NOTIFICATION_CHANNEL_ID = "ru.hiddenproject.audioworld";
        String channelName = "AudioWorldPlayer";
        NotificationCompat.Builder builder = null;
        Intent open = new Intent(getApplicationContext(),BookActivity.class);
        open.putExtra("bookID", bookID);
        open.putExtra("notify", true);
        PendingIntent openIntent = PendingIntent.getActivity(getApplicationContext(),0, open, PendingIntent.FLAG_UPDATE_CURRENT);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(title)
                    .setDeleteIntent(pendingIntent)
                    .setContentIntent(openIntent)
                    .setContentText("Часть " + String.valueOf(this.track + 1))
                    .setStyle(style);
        }else{
            builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(title)
                    .setDeleteIntent(pendingIntent)
                    .setContentIntent(openIntent)
                    .setContentText("Часть " + String.valueOf(this.track + 1))
                    .setStyle(style);
        }

        builder.addAction(generateAction(R.drawable.previous, "Previous", ACTION_PREVIOUS));
        //builder.addAction(generateAction(android.R.drawable.ic_media_rew, "Rewind", ACTION_REWIND));
        builder.addAction(action);
        //builder.addAction(generateAction(android.R.drawable.ic_media_ff, "Fast Foward", ACTION_FAST_FORWARD));
        builder.addAction(generateAction(R.drawable.next, "Next", ACTION_NEXT));
        style.setShowActionsInCompactView(0, 1, 2, 3, 4);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(chan);
        }

        Notification notification = builder.build();
        startForeground(1, notification);
        //notificationManager.notify(1, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mManager == null) {
            if (intent.getExtras() != null) {
                this.title = intent.getStringExtra("bookName");
                this.tracks = intent.getStringArrayListExtra("tracks");
                this.bookID = intent.getIntExtra("bookID", 0);
                this.track = intent.getIntExtra("trackID", 0);
                initMediaSessions(title);
                changePlaylist(tracks);
                setTrack(track);
            }
            Log.d("PLAYER", "onSTART");
        }

        handleIntent(intent);

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void changePlaylist(List<String> tracks) {
        this.tracks = tracks;
        Intent intent = new Intent(BookActivity.BROADCAST_ACTION);
        intent.putExtra("action", "change");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendBroadcast(intent);
        Log.d("PLAYER", "CHANGE PLAYLIST: ");
    }
    public int getBookID(){
        return this.bookID;
    }
    public void setTrackID(int track){
        this.track = track;
    }
    public int getTrackID(){
        return this.track;
    }
    public void setTrack(final int track) {
        handler.removeCallbacksAndMessages(null);
        Log.d("PLAYER", "SET TRACK: " + String.valueOf(track));
        if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
        mMediaPlayer.reset();
        this.track = track;
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                History history = helper.getHistory(getApplicationContext(), bookID);
                Intent intent = new Intent(BookActivity.BROADCAST_ACTION);
                mMediaPlayer.start();
                if(history!=null){
                    if (history.trackID == track) {
                        mMediaPlayer.seekTo(history.position);
                        mediaPlayer.start();
                        intent.putExtra("position", history.position);
                        Log.d("PLAYER", "SAVED POSITION "+String.valueOf(history.position));
                        startPlayProgressUpdater();
                    }
                }else {
                    mMediaPlayer.seekTo(0);
                }
                intent.putExtra("action", "start");
                intent.putExtra("track", track+1);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendBroadcast(intent);
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mController.getTransportControls().skipToNext();
            }
        });
        try {
            mMediaPlayer.setDataSource(this.tracks.get(track));
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("PLAYER", e.getLocalizedMessage());
            Log.d("PLAYER", "SET SOURCE FAILURE: "+this.tracks.get(track));
        }
    }
    public int getDuration(){
        return mMediaPlayer.getDuration();
    }
    public void seekTo(int position, boolean start){
        mMediaPlayer.pause();
        mMediaPlayer.seekTo(position);
        this.position = position;
        if(start)
        mMediaPlayer.start();
    }
    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }
    public int getPosition(){
        return mMediaPlayer.getCurrentPosition();
    }
    public void startPlayProgressUpdater() {
        if (mMediaPlayer.isPlaying()) {
            helper.saveHistory(getApplicationContext(), bookID, getTrackID(), mMediaPlayer.getCurrentPosition());
            Log.d("PLAYER", "POSITION SAVED "+mMediaPlayer.getCurrentPosition());
        }
        Runnable notification = new Runnable() {
            public void run() {
                startPlayProgressUpdater();
            }
        };
        handler.postDelayed(notification,2000);
    }
    private void initMediaSessions(final String title) {
        if(!isStarted) {
            isStarted = true;
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mSession = new MediaSessionCompat(getApplicationContext(), "simple player session");
            try {
                mController = new MediaControllerCompat(getApplicationContext(), mSession.getSessionToken());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mSession.setCallback(new MediaSessionCompat.Callback() {
                                 @Override
                                 public void onPlay() {
                                     super.onPlay();
                                     Log.e("MediaPlayerService", "onPlay");
                                     mMediaPlayer.seekTo(position);
                                     mMediaPlayer.start();
                                     Intent intent = new Intent(BookActivity.BROADCAST_ACTION);
                                     intent.putExtra("action", "play");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     sendBroadcast(intent);
                                     startPlayProgressUpdater();
                                     buildNotification(generateAction(R.drawable.pause, "Pause", ACTION_PAUSE), title);
                                 }

                                 @Override
                                 public void onPause() {
                                     super.onPause();
                                     Log.e("MediaPlayerService", "onPause");
                                     mMediaPlayer.pause();
                                     position = mMediaPlayer.getCurrentPosition();
                                     helper.saveHistory(getApplicationContext(), bookID, track, mMediaPlayer.getCurrentPosition());
                                     Intent intent = new Intent(BookActivity.BROADCAST_ACTION);
                                     intent.putExtra("action", "pause");
                                     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                     sendBroadcast(intent);
                                     handler.removeCallbacksAndMessages(null);
                                     buildNotification(generateAction(R.drawable.play, "Play", ACTION_PLAY), title);
                                 }

                                 @Override
                                 public void onSkipToNext() {
                                     super.onSkipToNext();
                                     Log.e("MediaPlayerService", "onSkipToNext");
                                     setTrackID(getTrackID()+1);
                                     if(getTrackID()<tracks.size()){
                                         setTrack(getTrackID());
                                     }else{
                                         setTrackID(getTrackID()-1);
                                     }
                                     buildNotification(generateAction(R.drawable.pause, "Pause", ACTION_PAUSE), title);
                                 }

                                 @Override
                                 public void onSkipToPrevious() {
                                     super.onSkipToPrevious();
                                     Log.e("MediaPlayerService", "onSkipToPrevious");
                                     setTrackID(getTrackID()-1);
                                     if(getTrackID()>=0){
                                         setTrack(getTrackID());
                                     }else{
                                         setTrackID(getTrackID()+1);
                                     }
                                     buildNotification(generateAction(R.drawable.pause, "Pause", ACTION_PAUSE), title);
                                 }

                                 @Override
                                 public void onFastForward() {
                                     super.onFastForward();
                                     Log.e("MediaPlayerService", "onFastForward");
                                     //Manipulate current media here
                                 }

                                 @Override
                                 public void onRewind() {
                                     super.onRewind();
                                     Log.e("MediaPlayerService", "onRewind");
                                     //Manipulate current media here
                                 }

                                 @Override
                                 public void onStop() {
                                     super.onStop();
                                     Log.e("MediaPlayerService", "onStop");
                                     helper.saveHistory(getApplicationContext(), bookID, track, position);
                                     NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                     notificationManager.cancel(1);
                                     Intent intent = new Intent(getApplicationContext(), Player.class);
                                     stopService(intent);
                                 }

                                 @Override
                                 public void onSeekTo(long pos) {
                                     super.onSeekTo(pos);
                                 }

                                 @Override
                                 public void onSetRating(RatingCompat rating) {
                                     super.onSetRating(rating);
                                 }
                             }
        );
    }

    @Override
    public boolean onUnbind(Intent intent) {
//        mSession.release();

        return super.onUnbind(intent);
    }

    public class LocalBinder extends Binder {
        public Player getServerInstance() {
            return Player.this;
        }
    }
}