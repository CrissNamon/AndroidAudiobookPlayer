package ru.hiddenproject.audioworld;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class TracksAdapter extends RecyclerView.Adapter<TracksAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private List<String> tracks;
    private Context c;
    private String title;
    Player service;
    private BookActivity ac;
    private History history;
    private int bookID;
    private Helper helper;

    public TracksAdapter(Context context, List<String> tracks, BookActivity ac, String title, int bookID) {
        this.tracks = tracks;
        this.inflater = LayoutInflater.from(context);
        this.c = context;
        this.ac =  ac;
        this.title = title;
        this.bookID = bookID;
        this.helper = new Helper();
        setHasStableIds(true);
    }
    @Override
    public TracksAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.track_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TracksAdapter.ViewHolder holder, final int position) {
        final String track = tracks.get(position);
        String[] split = track.split("/");
        String text = "Часть "+String.valueOf(position+1);
        history = helper.getHistory(c, bookID);
        if(history!=null){
            if(history.trackID==position)
            text+=" (Вы остановились здесь)";
        }
        holder.trackName.setText(text);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ac.playerView.setVisibility(View.GONE);
                ac.loading_player.setVisibility(View.VISIBLE);
                history = helper.getHistory(c, bookID);
                if(history!=null)
                ac.changeTrackName(history.trackID);
                holder.trackName.setText("Часть "+String.valueOf(position+1)+" (Вы остановились здесь)");
                ac.playAudio(position);
                Log.d("PLAYER", "PLAY TRACK "+String.valueOf(position));
            }
        });
    }
    @Override
    public int getItemCount() {
        return tracks.size();
    }
    @Override
    public long getItemId(int position){
        return position;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView trackName;
        ViewHolder(View view){
            super(view);
            trackName = (TextView)view.findViewById(R.id.trackName);
        }
    }
}
