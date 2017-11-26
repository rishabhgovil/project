package com.surajsararf.musicoplayer.Adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.surajsararf.musicoplayer.Custom.GetValues;
import com.surajsararf.musicoplayer.R;
import com.surajsararf.musicoplayer.util.MediaItem;
import com.surajsararf.musicoplayer.util.PlayerConstants;

import java.net.URI;
import java.util.ArrayList;

/**
 * Created by surajsararf on 17/2/16.
 */
public class  Tracklist_items extends RecyclerView.Adapter<Tracklist_items.MyViewHolder> {



    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference music = db.getReference("music");
    DatabaseReference ghgh = music;

    private GetValues getValues;
    private Context context;
    public static Uri uri=null;
    int i=0;
    String artist=new String("hi");
    public static ArrayList<MediaItem> mItemsList;

    public Tracklist_items(Context context,ArrayList<MediaItem> items){
        getValues=new GetValues(context);
        mItemsList=items;
        this.context=context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.tracklist_items,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        //DatabaseReference song = music.child("one");
        //DatabaseReference ghgh = music.child("one").child("ghgh");
       /* DatabaseReference n=music.child(Integer.toString(i));
        i++;*/
        MediaItem detail=mItemsList.get(position);
        holder.SongName.setText(detail.getTitle());
        uri = detail.getAlbumArtPath();
        holder.ArtistAlbumName.setText(detail.getArtist() + " | " + detail.getAlbum());
/*
        n.child("name").setValue(detail.getTitle());
        n.child("artist").setValue(detail.getArtist());*/

        if (PlayerConstants.mSongPlayback.isPlay){
            holder.isPlayImage.setImageResource(R.drawable.pause);
        }
        else
        {
            holder.isPlayImage.setImageResource(R.drawable.play);
        }

        holder.isPlayImage.setVisibility(View.INVISIBLE);
        if (PlayerConstants.SONG_NUMBER>-1 && PlayerConstants.SONG_NUMBER==position)
        {
            holder.isPlayImage.setVisibility(View.VISIBLE);
        }
    }



    @Override
    public int getItemCount() {
        return mItemsList.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView SongName, ArtistAlbumName;
        public ImageView isPlayImage;
        public MyViewHolder(View itemView) {
            super(itemView);
            SongName= (TextView) itemView.findViewById(R.id.songname);
            ArtistAlbumName = (TextView) itemView.findViewById(R.id.artist_album_name);
            isPlayImage= (ImageView) itemView.findViewById(R.id.isplay);
        }
    }

    


}
