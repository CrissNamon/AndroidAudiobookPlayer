package ru.hiddenproject.audioworld;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static ru.hiddenproject.audioworld.Helper.API_VERSION;

public class BookActivity extends AppCompatActivity {
    private Retrofit retrofit;
    private API api;
    private Player player;
    public List<String> tracks;
    public String title;
    public String cover;
    boolean mBounded = false;
    public LinearLayout playerView;
    private TextView trackName;
    private Button prev;
    private Button play;
    private Button next;
    private SeekBar seekBar;
    private boolean isPlayerInit = false;
    private boolean isPlaying = true;
    private BroadcastReceiver br;
    private Handler handler = new Handler();
    private int bookID;
    private boolean fromNotify;
    private LinearLayout loading;
    public LinearLayout loading_player;
    private LinearLayout content;
    private TextView playerCurrentTime;
    private TextView playerDuration;
    private Helper helper;
    private int position = 0;
    private TracksAdapter tracksAdapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private boolean instance = false;
    public final static String BROADCAST_ACTION = "ru.hiddenproject.audioworld.player";
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        final CollapsingToolbarLayout toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolbarLayout.setTitle("Загрузка...");
        helper = new Helper();
        bookID = getIntent().getIntExtra("bookID",-1);
        Uri data;
        if(getIntent().getData()!=null){
           data = getIntent().getData();
           String param = data.getLastPathSegment();
           try{
               bookID = Integer.parseInt(param);
           }catch (NumberFormatException e){
               finish();
           }
        }
        fromNotify = getIntent().getBooleanExtra("notify", false);
        loading = (LinearLayout)findViewById(R.id.loading);
        loading_player = (LinearLayout)findViewById(R.id.loading_player);
        content = (LinearLayout)findViewById(R.id.layout);
        playerCurrentTime = (TextView)findViewById(R.id.playerCurrentTime);
        playerDuration = (TextView)findViewById(R.id.playerDuration);
        final TextView bookName = (TextView)findViewById(R.id.bookName);
        final TextView bookAuthor = (TextView)findViewById(R.id.bookAuthor);
        final TextView bookDesc = (TextView)findViewById(R.id.bookDesc);
        final TextView bookReader = (TextView)findViewById(R.id.bookReader);
        final ImageView bookCover = (ImageView)findViewById(R.id.bookCover);
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        playerView = (LinearLayout)findViewById(R.id.playerView);
        trackName = (TextView)findViewById(R.id.trackname);
        prev = (Button)findViewById(R.id.prev);
        play = (Button)findViewById(R.id.play);
        next = (Button)findViewById(R.id.next);
        seekBar = (SeekBar)findViewById(R.id.seekbar);
        retrofit = new Retrofit.Builder()
                .baseUrl("http://185.125.217.104")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(API.class);
        tracks = new ArrayList<String>();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        api.getBook(API_VERSION, "book.get",bookID).enqueue(new Callback<Book>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {
                Log.d("PLAYER", "RESPONSE "+response.message());
                toolbarLayout.setTitle(response.body().title);
                bookName.setText(response.body().title);
                bookAuthor.setText(Html.fromHtml("<b>Автор</b>: "+response.body().author));
                bookDesc.setText(response.body().info);
                bookReader.setText(Html.fromHtml("<b>Читает</b>: "+response.body().reader));
                Glide.with(getApplicationContext()).load(response.body().img).into(bookCover);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                tracksAdapter = new TracksAdapter(getApplicationContext(), response.body().tracks, BookActivity.this, response.body().title, bookID);
                recyclerView.setAdapter(tracksAdapter);
                tracks = response.body().tracks;
                title = response.body().title;
                cover = response.body().img;
                loading.setVisibility(View.GONE);
                content.setVisibility(View.VISIBLE);

                /*if(fromNotify)getPlayerControls();
                if(savedInstanceState!=null) {
                    instance = savedInstanceState.getBoolean("instance");
                    getPlayerControls();
                    Log.d("BOOKACTIVITY", String.valueOf(savedInstanceState.getBoolean("instance")));
                }*/
                getPlayerControls();
                if(helper.inArchive(getApplicationContext(), bookID)){
                    fab.setImageResource(R.drawable.favourite);
                }else{
                    fab.setImageResource(R.drawable.not_favourite);
                }

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String text = "";
                        if(helper.inArchive(getApplicationContext(), bookID)){
                            text = "Удалено из архива";
                            fab.setImageResource(R.drawable.not_favourite);
                            helper.removeFromArchive(getApplicationContext(), bookID);
                        }else{
                            text = "Добавлено в архив";
                            fab.setImageResource(R.drawable.favourite);
                            helper.addToArchive(getApplicationContext(), bookID);
                        }
                        Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    }
                });
            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {

            }
        });
        br = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra("action");
                switch(action){
                    case "start":
                        int track = intent.getIntExtra("track", 0);
                        position = intent.getIntExtra("position", 0);
                        initPlayerControls("Часть "+String.valueOf(track));
                        break;
                    case "play":
                        Play();
                        break;
                    case "pause":
                        Pause();
                        break;
                    case "change":
                        isPlayerInit = false;
                        break;
                }
            }
        };
        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(br, intFilt);
    }
    public void changeTrackName(int track){
        Log.d("PLAYER", "REMOVE MARK ON "+String.valueOf(track));
        TextView text = (TextView)recyclerView.getChildAt(track).findViewById(R.id.trackName);
        text.setText("Часть "+String.valueOf(track+1));
    }
    public void playAudio(int track) {
        if(!fromNotify)seekBar.setProgress(0);
        //if(!mBounded) {
            Intent intent = new Intent(getApplicationContext(), Player.class);
            intent.setAction(Player.ACTION_PLAY);
            intent.putStringArrayListExtra("tracks", (ArrayList<String>) tracks);
            intent.putExtra("bookName", title);
            intent.putExtra("bookCover", cover);
            intent.putExtra("bookID", bookID);
            intent.putExtra("trackID", track);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            }else {
                startService(intent);
            }
            bindService(intent, mConnection, BIND_AUTO_CREATE);
            Log.d("PLAYER","BIND");
        //}else{
            //player.setTrack(track);
            isPlaying=true;
            Log.d("PLAYER", "ALREADY BIND");
        //}

    }
    public void getPlayerControls(){
        //if(fromNotify || instance){
            Intent intent = new Intent(getApplicationContext(), Player.class);
            bindService(intent, mConnection, BIND_AUTO_CREATE);
        //}
    }
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            //Toast.makeText(BookActivity.this, "Service is disconnected", 1000).show();
            mBounded = false;
            player = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Toast.makeText(BookActivity.this, "Service is connected", 1000).show();
            mBounded = true;
            Player.LocalBinder mLocalBinder = (Player.LocalBinder)service;
            player = mLocalBinder.getServerInstance();

            if(fromNotify || instance || player.getBookID()==bookID)initPlayerControls("Часть "+String.valueOf(player.getTrackID()+1));
            Log.d("PLAYER","SERVICE CONNECTED");

        }
    };
    public void Pause(){
        Drawable d = getResources().getDrawable(R.drawable.play_view);
        play.setBackground(d);
        isPlaying = false;
    }
    public void Play(){
        Drawable d = getResources().getDrawable(R.drawable.pause_view);
        play.setBackground(d);
        isPlaying = true;
    }
    public void initPlayerControls(String trackname){
        if(!isPlayerInit) {
            isPlayerInit = true;
            seekBar.setProgress(position);
            if(!fromNotify && !instance && player.getBookID()!=bookID)player.seekTo(position, isPlaying);
            if(bookID==player.getBookID()){
                Drawable d;
                if(player.isPlaying()) {
                    d = getResources().getDrawable(R.drawable.pause_view);
                }else{
                    d = getResources().getDrawable(R.drawable.play_view);
                }
                play.setBackground(d);
            }
            Log.d("PLAYER", "ACTIVITY GET POSITION "+String.valueOf(position));
            seekBar.setMax(player.getDuration());
            playerCurrentTime.setText(helper.formatTime(position));
            playerDuration.setText(helper.formatTime(player.getDuration()));
            //Log.d("PLAYER","DURATION "+String.valueOf(player.getDuration()));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    //Log.d("PLAYER", "SEEKBAR PROGRESS "+String.valueOf(i));
                    if(b) {
                        player.seekTo(i, isPlaying);
                        playerCurrentTime.setText(helper.formatTime(i));
                    }else{
                        seekBar.setProgress(i);
                        playerCurrentTime.setText(helper.formatTime(i));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            trackName.setText(trackname);
            prev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(BookActivity.this, Player.class);
                    i.setAction(Player.ACTION_PREVIOUS);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(i);
                    }else{
                        startService(i);
                    }
                }
            });
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(BookActivity.this, Player.class);
                    Drawable d;
                    if(isPlaying) {
                        isPlaying = false;
                        i.setAction(Player.ACTION_PAUSE);
                        d = getResources().getDrawable(R.drawable.play_view);
                        play.setBackground(d);
                    }else{
                        isPlaying = true;
                        d = getResources().getDrawable(R.drawable.pause_view);
                        i.setAction(Player.ACTION_PLAY);
                        play.setBackground(d);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(i);
                    }else{
                        startService(i);
                    }
                }
            });
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(BookActivity.this, Player.class);
                    i.setAction(Player.ACTION_NEXT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(i);
                    }else{
                        startService(i);
                    }
                }
            });
            startPlayProgressUpdater();
            loading_player.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
        }else{
            Drawable d = getResources().getDrawable(R.drawable.pause_view);
            play.setBackground(d);
            trackName.setText(trackname);
            seekBar.setProgress(position);
            playerCurrentTime.setText(helper.formatTime(0));
            playerDuration.setText(helper.formatTime(player.getDuration()));
            seekBar.setMax(player.getDuration());
            loading_player.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
        }
    }
    public void startPlayProgressUpdater() {

        //Log.d("PLAYER", "SET PROGRESS "+String.valueOf(player.getPosition()));
        if (isPlaying) {
            seekBar.setProgress(player.getPosition());
            playerCurrentTime.setText(helper.formatTime(player.getPosition()));
            //helper.saveHistory(getApplicationContext(), bookID, player.getTrackID(), player.getPosition());
        }
            Runnable notification = new Runnable() {
                public void run() {
                    startPlayProgressUpdater();
                }
            };
            handler.postDelayed(notification,1000);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public void onPause() {
        if(mBounded) {
            unbindService(mConnection);
        }
        unregisterReceiver(br);
        super.onPause();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_book, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "audioworld.app/a/"+String.valueOf(bookID);
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Поделится книгой"));
                break;
            case R.id.action_favourite:
                String text = "";
                if(helper.inArchive(getApplicationContext(), bookID)){
                    text = "Удалено из архива";
                    helper.removeFromArchive(getApplicationContext(), bookID);
                }else{
                    text = "Добавлено в архив";
                    helper.addToArchive(getApplicationContext(), bookID);
                }
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean("instance", true);
    }
}
