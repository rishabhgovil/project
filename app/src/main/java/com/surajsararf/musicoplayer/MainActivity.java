package com.surajsararf.musicoplayer;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.florent37.viewanimator.AnimationListener;
import com.github.florent37.viewanimator.ViewAnimator;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.surajsararf.musicoplayer.Adapters.Tracklist_items;
import com.surajsararf.musicoplayer.Custom.GetBlur;
import com.surajsararf.musicoplayer.Custom.GetValues;
import com.surajsararf.musicoplayer.Custom.LinearLayoutManagerWithSmoothScroll;
import com.surajsararf.musicoplayer.Custom.MediaStoreAccessHelper;
import com.surajsararf.musicoplayer.Custom.RecyclerTouchListener;
import com.surajsararf.musicoplayer.service.SongPlayback;
import com.surajsararf.musicoplayer.util.MediaItem;
import com.surajsararf.musicoplayer.util.PlayerConstants;
import com.surajsararf.musicoplayer.util.UtilFunctions;

import java.io.IOException;
import java.util.ArrayList;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

import static com.surajsararf.musicoplayer.service.SongPlayback.mp;

public class MainActivity extends AppCompatActivity{


    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        // multi window changed

    }

    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;

    private final static int mMiniMusicplayerheight = 49;
    private final static int mAnimationDuration = 400;
    private final static float mSlideHeight = 150;
    private final static int mToolbar_height=48;
    private final static int mToolbarAnimationDuration=10000;
    private final static int mToolbarAnimationAlphaDuration=300;

    public final static int forNextKey=0;
     // MediaPlayer mp;
    public final static int forPreviousKey=1;
    public final static int forSkipTrack=2;
    public final static int forNothingKey=3;

    private AdView mAdView;
    FloatingActionButton image;
    FloatingActionButton equal;

    private Boolean IsUp;
    private static RelativeLayout mBothmusicPlayer, mMusicPlayer, mMiniPrevious, mMiniNext, mRoot, mAlbumArt_Area,mPrevious,mNext,mRootofList,mContentArea;
    private static LinearLayout mMiniMusicPlayer;
    private static ImageView mControl_bg,mControl_bg1,mAlbumArt,mAlbumArt1,mGoDown, mActivity_bg_image,mActivity_bg_image1,mMiniAlbumArt,mMiniAlbumArt1;
    private static TextView mMiniSongName,mMiniArtistAlbum, mPresentTime, mTotalTime,mSongName,mArtistAlbum;
    private static Toolbar mToolbar;
    private static ProgressBar mMiniProgressBar;
    private static float ScreenHeight, ScreenWidth;
    private static GetValues getValues;
    private float dX, dY, lastPosition, nowPosition;
    private static RelativeLayout mMiniPlayer_inner,mMiniPlay,mMiniPause,mPlay,mPause;
    private static SeekBar mMPSeekBar;
    private static RecyclerView mRecyclerView;
    private static VerticalRecyclerViewFastScroller mFastScroller;
    private static Context mContext;
    private static Picasso picasso;
    private static Handler mHandler;
    private static Activity activity;
    private Boolean isAdLoaded=false;

    private Tracker mTracker;
    private Boolean isTouch =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
//        Intent service = new Intent(this,SongPlayback.class);
//        startService(service);

       //  mp = new MediaPlayer();

        equal=(FloatingActionButton)findViewById(R.id.equal);

        // floating permission
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        }
        else {
            initializeView();
        }


        // equalizer

        equal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,EqualizerActivity.class);
                startActivity(i);
            }
        });


        mHandler=new Handler();
       /* mp = MediaPlayer.create(this,R.raw.hardwell);
        mp.start();*/
        mContext =this;
        activity=this;
        picasso =Picasso.with(mContext);
        BindingView();
        Setup();




        //Local Notifications

        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,1,intent,0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Music is ready to play")
                .setContentText("Play Music")
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.music_player,"open app",pendingIntent)
                .setSmallIcon(R.drawable.music_player)
                .build();

        NotificationManager noti = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        noti.notify(1,notification);




        //multi screen window


        if(isInMultiWindowMode()){
            // in window mode
        }

        if(isInPictureInPictureMode()){
            // in picture mode
        }




        // Firebase Connection



        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference music = db.getReference("music");

        DatabaseReference zero = music.child("0");
        zero.child("genre").setValue("happy");
        zero.child("duration").setValue("500");

        DatabaseReference one = music.child("1");
        one.child("genre").setValue("happy");
        one.child("duration").setValue("478");

        DatabaseReference two = music.child("2");
        two.child("genre").setValue("sad");
        two.child("duration").setValue("512");

        DatabaseReference three = music.child("3");
        three.child("genre").setValue("neutral");
        three.child("duration").setValue("450");

        DatabaseReference four = music.child("4");
        four.child("genre").setValue("neutral");
        four.child("duration").setValue("452");

        DatabaseReference five = music.child("5");
        five.child("genre").setValue("happy");
        five.child("duration").setValue("462");

        DatabaseReference six = music.child("6");
        six.child("genre").setValue("suprise");
        six.child("duration").setValue("465");

        DatabaseReference seven = music.child("7");
        seven.child("genre").setValue("suprise");
        seven.child("duration").setValue("469");

        DatabaseReference eight = music.child("8");
        eight.child("genre").setValue("neutral");
        eight.child("duration").setValue("472");

        DatabaseReference nine = music.child("9");
        nine.child("genre").setValue("sad");
        nine.child("duration").setValue("479");

        DatabaseReference ten = music.child("10");
        ten.child("genre").setValue("happy");
        ten.child("duration").setValue("412");

        DatabaseReference eleven = music.child("11");
        eleven.child("genre").setValue("happy");
        eleven.child("duration").setValue("415");

        DatabaseReference twelve = music.child("12");
        twelve.child("genre").setValue("sad");
        twelve.child("duration").setValue("420");

        DatabaseReference thirteen = music.child("13");
        thirteen.child("genre").setValue("suprise");
        thirteen.child("duration").setValue("422");

        DatabaseReference fourteen = music.child("14");
        fourteen.child("genre").setValue("neutral");
        fourteen.child("duration").setValue("422");

        DatabaseReference fifteen = music.child("15");
        fifteen.child("genre").setValue("neutral");
        fifteen.child("duration").setValue("463");

        DatabaseReference sixteen = music.child("16");
        sixteen.child("genre").setValue("sad");
        sixteen.child("duration").setValue("456");

        DatabaseReference seventeen = music.child("17");
        seventeen.child("genre").setValue("happy");
        seventeen.child("duration").setValue("300");

        DatabaseReference eighteen = music.child("18");
        eighteen.child("genre").setValue("happy");
        eighteen.child("duration").setValue("312");

        DatabaseReference nineteen = music.child("19");
        nineteen.child("genre").setValue("disgust");
        nineteen.child("duration").setValue("296");

        DatabaseReference twenty = music.child("20");
        twenty.child("genre").setValue("fear");
        twenty.child("duration").setValue("289");

        DatabaseReference twentyone = music.child("21");
        twentyone.child("genre").setValue("fear");
        twentyone.child("duration").setValue("290");

        DatabaseReference twentytwo = music.child("22");
        twentytwo.child("genre").setValue("suprise");
        twentytwo.child("duration").setValue("275");

        DatabaseReference twentythree = music.child("23");
        twentythree.child("genre").setValue("neutral");
        twentythree.child("duration").setValue("230");

        DatabaseReference twentyfour = music.child("24");
        twentyfour.child("genre").setValue("sad");
        twentyfour.child("duration").setValue("245");

        DatabaseReference twentyfive = music.child("25");
        twentyfive.child("genre").setValue("happy");
        twentyfive.child("duration").setValue("168");

        DatabaseReference twentysix = music.child("26");
        twentysix.child("genre").setValue("sad");
        twentysix.child("duration").setValue("275");

        DatabaseReference twentyseven = music.child("27");
        twentyseven.child("genre").setValue("suprise");
        twentyseven.child("duration").setValue("287");

        DatabaseReference twentyeight = music.child("28");
        twentyeight.child("genre").setValue("fear");
        twentyeight.child("duration").setValue("187");

        DatabaseReference twentynine = music.child("29");
        twentynine.child("genre").setValue("disgust");
        twentynine.child("duration").setValue("190");

        DatabaseReference thirty = music.child("30");
        thirty.child("genre").setValue("neutral");
        thirty.child("duration").setValue("194");


        DatabaseReference thirtyone = music.child("31");
        thirtyone.child("genre").setValue("neutral");
        thirtyone.child("duration").setValue("187");

        DatabaseReference thirtytwo = music.child("32");
        thirtytwo.child("genre").setValue("happy");
        thirtytwo.child("duration").setValue("188");

        DatabaseReference thirtythree = music.child("33");
        thirtythree.child("genre").setValue("happy");
        thirtythree.child("duration").setValue("200");

        DatabaseReference thirtyfour = music.child("34");
        thirtyfour.child("genre").setValue("neutral");
        thirtyfour.child("duration").setValue("215");

        DatabaseReference thirtyfive = music.child("35");
        thirtyfive.child("genre").setValue("fear");
        thirtyfive.child("duration").setValue("220");

        DatabaseReference thirtysix = music.child("36");
        thirtysix.child("genre").setValue("happy");
        thirtysix.child("duration").setValue("221");

        DatabaseReference thirtyseven = music.child("37");
        thirtyseven.child("genre").setValue("disgust");
        thirtyseven.child("duration").setValue("225");

        DatabaseReference thirtyeight = music.child("38");
        thirtyeight.child("genre").setValue("suprise");
        thirtyeight.child("duration").setValue("178");

        DatabaseReference thirtynine = music.child("39");
        thirtynine.child("genre").setValue("neutral");
        thirtynine.child("duration").setValue("193");

        DatabaseReference forty = music.child("40");
        forty.child("genre").setValue("happy");
        forty.child("duration").setValue("201");

        DatabaseReference fortyone = music.child("41");
        fortyone.child("genre").setValue("sad");
        fortyone.child("duration").setValue("214");

        DatabaseReference fortytwo = music.child("42");
        fortytwo.child("genre").setValue("happy");
        fortytwo.child("duration").setValue("216");

        DatabaseReference fortythree = music.child("43");
        fortythree.child("genre").setValue("sad");
        fortythree.child("duration").setValue("223");

        DatabaseReference fortyfour = music.child("44");
        fortyfour.child("genre").setValue("neutral");
        fortyfour.child("duration").setValue("229");

        DatabaseReference fortyfive = music.child("45");
        fortyfive.child("genre").setValue("disgust");
        fortyfive.child("duration").setValue("231");

        DatabaseReference fortysix = music.child("46");
        fortysix.child("genre").setValue("fear");
        fortysix.child("duration").setValue("323");

        DatabaseReference fortyseven = music.child("47");
        fortyseven.child("genre").setValue("angry");
        fortyseven.child("duration").setValue("333");

        DatabaseReference fortyeight = music.child("48");
        fortyeight.child("genre").setValue("neutral");
        fortyeight.child("duration").setValue("189");

        DatabaseReference fortynine = music.child("49");
        fortynine.child("genre").setValue("angry");
        fortynine.child("duration").setValue("199");

        DatabaseReference fifty = music.child("50");
        fifty.child("genre").setValue("angry");
        fifty.child("duration").setValue("160");


        music.push();

        music.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });










    }

    // floating button



    private void initializeView() {
        findViewById(R.id.noti).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(MainActivity.this, FloatingViewService.class));
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {

            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
                initializeView();
            } else { //Permission is not available
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }




    private void BindingView() {
        mActivity_bg_image = (ImageView) findViewById(R.id.bg_image);
        mActivity_bg_image1 = (ImageView) findViewById(R.id.bg_image1);

        mRoot = (RelativeLayout) findViewById(R.id.root);

        mRootofList= (RelativeLayout) findViewById(R.id.rootoflist);

        mBothmusicPlayer = (RelativeLayout) findViewById(R.id.bothmusicplayer);

        mAdView= (AdView) findViewById(R.id.adView_ma);
        mContentArea= (RelativeLayout) findViewById(R.id.contentarea);

        mMiniMusicPlayer = (LinearLayout) mBothmusicPlayer.findViewById(R.id.minimusicplayer);
        mMiniPlay = (RelativeLayout) mMiniMusicPlayer.findViewById(R.id.play);
        mMiniPause= (RelativeLayout) mMiniMusicPlayer.findViewById(R.id.pause);
        mMiniNext = (RelativeLayout) mMiniMusicPlayer.findViewById(R.id.next);
        mMiniPrevious = (RelativeLayout) mMiniMusicPlayer.findViewById(R.id.previous);
        mMiniSongName = (TextView) mMiniMusicPlayer.findViewById(R.id.songname);
        mMiniProgressBar = (ProgressBar) mMiniMusicPlayer.findViewById(R.id.progressBar);
        mMiniArtistAlbum= (TextView) mMiniMusicPlayer.findViewById(R.id.artist_album_name);
        mMiniAlbumArt= (ImageView) mMiniMusicPlayer.findViewById(R.id.album_art);
        mMiniAlbumArt1= (ImageView) mMiniMusicPlayer.findViewById(R.id.album_art1);

        mMusicPlayer = (RelativeLayout) mBothmusicPlayer.findViewById(R.id.musicplayer);
        mSongName= (TextView) mMusicPlayer.findViewById(R.id.songname);
        mArtistAlbum= (TextView) mMusicPlayer.findViewById(R.id.artist_album_name);
        mPlay= (RelativeLayout) mMusicPlayer.findViewById(R.id.play);
        mPause= (RelativeLayout) mMusicPlayer.findViewById(R.id.pause);
        mPrevious= (RelativeLayout) mMusicPlayer.findViewById(R.id.previous);
        mNext= (RelativeLayout) mMusicPlayer.findViewById(R.id.next);
        mMiniPlayer_inner= (RelativeLayout) mMiniMusicPlayer.findViewById(R.id.miniplayer_inner);

        mGoDown= (ImageView) mMusicPlayer.findViewById(R.id.godown);
        mControl_bg= (ImageView) mMusicPlayer.findViewById(R.id.control_bg);
        mControl_bg1= (ImageView) mMusicPlayer.findViewById(R.id.control_bg1);
        mMPSeekBar= (SeekBar) mMusicPlayer.findViewById(R.id.seekbar);

        mAlbumArt = (ImageView) mMusicPlayer.findViewById(R.id.album_art);
        mAlbumArt1= (ImageView) mMusicPlayer.findViewById(R.id.album_art1);
        mAlbumArt_Area = (RelativeLayout) mMusicPlayer.findViewById(R.id.album_art_area);

        mPresentTime = (TextView) mMusicPlayer.findViewById(R.id.present_time);
        mTotalTime = (TextView) mMusicPlayer.findViewById(R.id.total_time);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mFastScroller = (VerticalRecyclerViewFastScroller) findViewById(R.id.fast_scroller);
        mToolbar= (Toolbar) findViewById(R.id.toolbar);

    }

    private void Setup() {

        SetupGetValue();
        SetupRoot();
        SetupBothmusicplayer();
        SetupClickListener();
        Setuptextview();
        SetupAlbumArtheight();
        SetupRecyclerView();
        SetupToolbar();
        SetupSeekBar();
        mHandler.post(isOnline);
        LoadAds();
        SetupAnalytics();

    }

    private void SetupAnalytics(){
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
    }

    private Runnable isOnline=new Runnable() {
        @Override
        public void run() {
            isOnline();
            mHandler.postDelayed(isOnline,1000);
        }
    };

    private Boolean isOnline(){
        final ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {

            if (!isUp() && !isTouch) {
                mBothmusicPlayer.setTranslationY(getValues.ScreenHeight() - getValues.dpToPx(mMiniMusicplayerheight) - mAdView.getHeight());
            }

            if (!isAdLoaded) {
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }
            mAdView.setVisibility(View.VISIBLE);

            isTouch = true;
            return true;
        } else {
            isAdLoaded=false;
            SetupOnAdFailed();
            isTouch =false;
            return false;
        }
    }

    private void LoadAds(){
        mAdView.setVisibility(View.INVISIBLE);
        //mAdView.getLayoutParams().height=getValues.dpToPx(50);
        
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                SetupOnAdFailed();
                isAdLoaded=false;
            }

            @Override
            public void onAdLoaded() {
                SetupOnAdLoaded();
                isAdLoaded=true;
            }

        });
    }

    private void SetupOnAdLoaded(){
        mAdView.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 0, 0, mAdView.getHeight());
        mContentArea.setLayoutParams(layoutParams);

        ScreenHeight=getValues.ScreenHeight()-mAdView.getHeight()+getValues.dpToPx(4);

        if (!isUp() && !isTouch)
        {
            mBothmusicPlayer.setTranslationY(ScreenHeight - getValues.dpToPx(mMiniMusicplayerheight));
        }

    }

    private void SetupOnAdFailed(){
        SetupGetValue();
        mAdView.setVisibility(View.INVISIBLE);

        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 0, 0, 0);
        mContentArea.setLayoutParams(layoutParams);

        if (!isUp() && !isTouch) {
            mBothmusicPlayer.setTranslationY(ScreenHeight - getValues.dpToPx(mMiniMusicplayerheight));
        }
    }


    private void SetupToolbar(){
        setSupportActionBar(mToolbar);
        mToolbar.getLayoutParams().height=getValues.GetStatusBarHeight()+getValues.dpToPx(mToolbar_height);
        mToolbar.setPadding(0, getValues.GetStatusBarHeight(), 0, 0);
    }

    private void SetupAlbumArtheight(){
        mAlbumArt_Area.getLayoutParams().height = (int) (ScreenHeight / 2 + getValues.dpToPx(mMiniMusicplayerheight));
    }

    private void SetupGetValue() {
        getValues = new GetValues(this);
        ScreenHeight = getValues.ScreenHeight();
        ScreenWidth = getValues.ScreenWidth();
    }

    private void SetupRoot() {
        mRoot.getLayoutParams().height = (int) (ScreenHeight + getValues.dpToPx(mMiniMusicplayerheight));
    }



    private void SetupBothmusicplayer() {
        mBothmusicPlayer.setTranslationY(ScreenHeight - getValues.dpToPx(mMiniMusicplayerheight));
        mBothmusicPlayer.setOnTouchListener(new MyTouchListener());
    }

    private void Setuptextview(){
        mMiniSongName.setSelected(true);
        mMiniArtistAlbum.setSelected(true);
        mSongName.setSelected(true);
        mArtistAlbum.setSelected(true);
    }

    private void SetupClickListener(){
        mNext.setOnClickListener(onNextClickListener);
        mMiniNext.setOnClickListener(onNextClickListener);
        mPrevious.setOnClickListener(onPreviousClickListener);
        mMiniPrevious.setOnClickListener(onPreviousClickListener);
        mMiniPlay.setOnClickListener(onPlayClickListener);
        mPlay.setOnClickListener(onPlayClickListener);
        mPause.setOnClickListener(onPauseClickListener);
        mMiniPause.setOnClickListener(onPauseClickListener);
        mGoDown.setOnClickListener(onGoDownClickListener);
    }

    private View.OnClickListener onNextClickListener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (RunServiceIfnot(SongPlayback.isPlayFromMainTrue,MainActivity.forNextKey,0))
                PlayerConstants.mSongPlayback.skipToNextTrack();

        }
    };

    private View.OnClickListener onPreviousClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (RunServiceIfnot(SongPlayback.isPlayFromMainTrue,MainActivity.forPreviousKey,0))
                PlayerConstants.mSongPlayback.skipToPreviousTrack(false);


        }
    };

    private View.OnClickListener onPlayClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (RunServiceIfnot(SongPlayback.isPlayFromMainTrue,MainActivity.forNothingKey,0)) {
                PlayerConstants.mSongPlayback.startPlayback();
            }

            mp.stop();
            mp.release();
            changeButton();

        }
    };

    private View.OnClickListener onPauseClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(RunServiceIfnot(SongPlayback.isPlayFromMainFalse,MainActivity.forNothingKey,0)){
                PlayerConstants.mSongPlayback.pausePlayback();
            }
            changeButton();
        }
    };

    private View.OnClickListener onGoDownClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            goDown();
        }
    };

    private void SetupSeekBar(){
        mMPSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    private static void setSeekbarDuration(int duration) {
        mMPSeekBar.setMax(duration);
        mMPSeekBar.setProgress(PlayerConstants.mSongPlayback.getCurrentMediaPlayer().getCurrentPosition() / 1000);
        mMiniProgressBar.setMax(duration);
        mMiniProgressBar.setProgress(PlayerConstants.mSongPlayback.getCurrentMediaPlayer().getCurrentPosition() / 1000);
        mHandler.postDelayed(seekbarUpdateRunnable, 0);
    }

    public static Runnable seekbarUpdateRunnable = new Runnable() {
        public void run() {
            try {
                long currentPosition = PlayerConstants.mSongPlayback.getCurrentMediaPlayer().getCurrentPosition();
                //smoothScrollSeekbar(currentPosition);
                mPresentTime.setText(UtilFunctions.getDuration(currentPosition));
                mMPSeekBar.setProgress((int) (currentPosition / 1000));
                mMiniProgressBar.setProgress(PlayerConstants.mSongPlayback.getCurrentMediaPlayer().getCurrentPosition());
                mHandler.postDelayed(seekbarUpdateRunnable, 100);
            } catch (Exception e) {
            }
        }
    };

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int seekBarPosition, boolean changedByUser) {

            try {
                long currentSongDuration = PlayerConstants.mSongPlayback.getCurrentMediaPlayer().getDuration();
                seekBar.setMax((int) currentSongDuration / 1000);
                if (changedByUser)
                    mPresentTime.setText(UtilFunctions.getDuration(seekBar.getProgress() * 1000));
            } catch (Exception e) {
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeCallbacks(seekbarUpdateRunnable);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int seekBarPosition = seekBar.getProgress();
            PlayerConstants.mSongPlayback.getCurrentMediaPlayer().seekTo(seekBarPosition * 1000);
            //Reinitiate the handler.
            mHandler.post(seekbarUpdateRunnable);
        }

    };

    public static void SetupRecyclerViewAdapter(Context context, ArrayList<MediaItem> mediaItems){
        final Tracklist_items tracklist_items=new Tracklist_items(context,mediaItems);
        mRecyclerView.setAdapter(tracklist_items);
    }

    private void SetupRecyclerView(){
        mRecyclerView.setHasFixedSize(true);
        mFastScroller.setRecyclerView(mRecyclerView);




        mRecyclerView.setOnScrollListener(mFastScroller.getOnScrollListener());





        setRecyclerViewLayoutManager(mRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<MediaItem> mediaItems=PlayerConstants.SONGS_LIST;
        MainActivity.SetupRecyclerViewAdapter(getApplicationContext(), mediaItems);

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                int Action=MainActivity.forSkipTrack;
                if (PlayerConstants.SONG_NUMBER==position)
                {
                    Action=MainActivity.forNothingKey;
                }
                if (RunServiceIfnot(SongPlayback.isPlayFromMainTrue,Action,position)){

                    if (PlayerConstants.SONG_NUMBER == position) {
                        changeListButton(position);
                        if (PlayerConstants.mSongPlayback.isPlay) {
                            PlayerConstants.mSongPlayback.pausePlayback();
                        }
                        else {
                            PlayerConstants.mSongPlayback.startPlayback();
                        }
                    } else {
                        setSongPlay(PlayerConstants.SONG_NUMBER, position);
                        PlayerConstants.SONG_NUMBER = position;
                        PlayerConstants.mSongPlayback.skipToTrack(PlayerConstants.SONG_NUMBER);
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
    }

    public static void setSongPlay(int lastPosition,int currentPosition){
        if(lastPosition>=0) {
            try{
                mRecyclerView.findViewHolderForLayoutPosition(lastPosition).itemView.findViewById(R.id.isplay).setVisibility(View.INVISIBLE);
            }
            catch (Exception e) {
            }
        }
        try{
            mRecyclerView.findViewHolderForLayoutPosition(currentPosition).itemView.findViewById(R.id.isplay).setVisibility(View.VISIBLE);
        }
        catch (Exception e){}
    }

    public static void changeListButton(int position){
        try {
            ImageView imageView = (ImageView) mRecyclerView.findViewHolderForLayoutPosition(position).itemView.findViewById(R.id.isplay);
            if (PlayerConstants.mSongPlayback.isPlay) {
                imageView.setImageResource(R.drawable.pause);
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setImageResource(R.drawable.play);
                imageView.setVisibility(View.VISIBLE);
            }
        }
        catch (Exception e) {}
    }

    public void setRecyclerViewLayoutManager(RecyclerView recyclerView) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition =
                    ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        }

        LinearLayoutManagerWithSmoothScroll linearLayoutManagerWithSmoothScroll =new LinearLayoutManagerWithSmoothScroll(this);


        recyclerView.setLayoutManager(linearLayoutManagerWithSmoothScroll);
        recyclerView.scrollToPosition(scrollPosition);

    }

    private static Runnable updateProgressBar=new Runnable() {
        @Override
        public void run() {
            try {
                if (PlayerConstants.SONG_NUMBER>-1) {
                    if (PlayerConstants.mSongPlayback.mCurrentMediaPlayer == 1)
                        if (PlayerConstants.mSongPlayback.isMediaPlayerPrepared())
                            setSeekbarDuration((int) PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getDuration());
                        else
                            mHandler.postDelayed(updateProgressBar, 200);

                    else if (PlayerConstants.mSongPlayback.isMediaPlayer2Prepared())
                        setSeekbarDuration((int) PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getDuration());
                    else
                        mHandler.postDelayed(updateProgressBar, 200);
                }
            }
            catch (Exception e){}
        }
    };

    private static final void SetupMargin(Boolean isMiniPlayerVisible){
        RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        if (isMiniPlayerVisible){
            layoutParams.setMargins(0,getValues.GetStatusBarHeight()+getValues.dpToPx(mToolbar_height),0,2*getValues.dpToPx(mMiniMusicplayerheight));
        }
        else {
            layoutParams.setMargins(0,getValues.GetStatusBarHeight() + getValues.dpToPx(mToolbar_height),0,getValues.dpToPx(mMiniMusicplayerheight));
        }
        mRootofList.setLayoutParams(layoutParams);
    }

    public static void changeUI(){
        updateUI();
        changeButton();
        setupMusicPlayer();
    }

    public static final void setupMusicPlayer(){
        if (PlayerConstants.SONG_NUMBER>-1)
        {
            if (mBothmusicPlayer.getVisibility()==View.INVISIBLE)
            {
                ViewAnimator.animate(mBothmusicPlayer)
                        .alpha(0,1)
                        .onStart(new AnimationListener.Start() {
                            @Override
                            public void onStart() {
                                mBothmusicPlayer.setVisibility(View.VISIBLE);
                            }
                        })
                        .duration(400)
                        .start();
                SetupMargin(true);
            }
            else
            {

                SetupMargin(true);
            }
        }
        else {
            mBothmusicPlayer.setVisibility(View.INVISIBLE);
            SetupMargin(false);
        }
    }

    public static void updateUI() {
        try{
            mHandler.post(updateProgressBar);
            ViewAnimator.animate(mMiniSongName,mSongName,mMiniArtistAlbum,mArtistAlbum,mTotalTime)
                    .alpha(0)
                    .duration(300)
                    .onStop(new AnimationListener.Stop() {
                        @Override
                        public void onStop() {
                            String songName = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getTitle();
                            String artist = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getArtist();
                            String album = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getAlbum();
                            long TotalDuration = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getDuration();

                            mMiniSongName.setText(songName);
                            mSongName.setText(songName);
                            mMiniArtistAlbum.setText(artist + " | " + album);
                            mArtistAlbum.setText(artist + " | " + album);
                            mTotalTime.setText(UtilFunctions.getDuration(TotalDuration));
                        }
                    })
                    .start();

            ViewAnimator.animate(mMiniSongName, mSongName, mMiniArtistAlbum, mArtistAlbum, mTotalTime)
                    .alpha(1)
                    .duration(300)
                    .startDelay(300)
                    .start();
        }catch(Exception e){
        }
    }

    public static void updateImage() {
        try {
            final Uri uri = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getAlbumArtPath();
            new AsyncTask<Void, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Void... params) {
                    Bitmap bitmap = null;
                    try {
                        bitmap = picasso.load(uri).get();
                    } catch (IOException e) {
                        bitmap=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.default_album_art);
                    }
                    return bitmap;
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    changeImageAnimation(bitmap);
                    changebgImageAnimation(bitmap);
                }
            }.execute();

        }
        catch(Exception e){}
    }

    private static void changeImageAnimation(final Bitmap bitmap){
        ViewAnimator.animate(mMiniAlbumArt1, mAlbumArt1)
                .alpha(1, 0)
                .duration(600)
                .onStart(new AnimationListener.Start() {
                    @Override
                    public void onStart() {
                        mAlbumArt.setImageBitmap(bitmap);
                        mMiniAlbumArt.setImageBitmap(bitmap);
                    }
                })
                .onStop(new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        mAlbumArt1.setImageBitmap(bitmap);
                        mMiniAlbumArt1.setImageBitmap(bitmap);
                    }
                }).start();
    }

    private static void changebgImageAnimation(final Bitmap bitmap){
        new AsyncTask<Bitmap, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Bitmap... params) {
                try {
                    return GetBlur.FastBlur(params[0],1.1f,25);
                }
                catch (Exception e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final Bitmap bitmap) {
                ViewAnimator.animate(mControl_bg1)
                        .alpha(1,0)
                        .onStart(new AnimationListener.Start() {
                            @Override
                            public void onStart() {
                                mControl_bg.setImageBitmap(bitmap);
                            }
                        })
                        .onStop(new AnimationListener.Stop() {
                            @Override
                            public void onStop() {
                                mControl_bg1.setImageBitmap(bitmap);
                            }
                        })
                        .duration(600)
                        .start();
            }
        }.execute(bitmap);
        new AsyncTask<Bitmap, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Bitmap... params) {
                try{
                    return GetBlur.FastBlur(params[0],1.1f,16);
                }
                catch (Exception e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final Bitmap bitmap) {
                ViewAnimator.animate(mActivity_bg_image1)
                        .alpha(1, 0)
                        .onStart(new AnimationListener.Start() {
                            @Override
                            public void onStart() {
                                mActivity_bg_image.setImageBitmap(bitmap);
                            }
                        })
                        .onStop(new AnimationListener.Stop() {
                            @Override
                            public void onStop() {
                                mActivity_bg_image1.setImageBitmap(bitmap);
                            }
                        })
                        .duration(600)
                        .start();
            }
        }.execute(bitmap);
    }

    public static void changeButton() {
        changeListButton(PlayerConstants.SONG_NUMBER);
        if(!PlayerConstants.mSongPlayback.isPlay){
            ViewAnimator.animate(mPause, mMiniPause)
                    .alpha(0)
                    .scale(0)
                    .duration(200)
                    .onStop(new AnimationListener.Stop() {
                        @Override
                        public void onStop() {
                            mPause.setVisibility(View.GONE);
                            mMiniPause.setVisibility(View.GONE);
                        }
                    })
                    .start();
            ViewAnimator.animate(mPlay, mMiniPlay)
                    .alpha(0, 1)
                    .scale(0, 1.2f, 1)
                    .duration(400)
                    .onStart(new AnimationListener.Start() {
                        @Override
                        public void onStart() {
                            mPlay.setVisibility(View.VISIBLE);
                            mMiniPlay.setVisibility(View.VISIBLE);
                        }
                    })
                    .startDelay(100)
                    .start();
        }else{
            ViewAnimator.animate(mPlay, mMiniPlay)
                    .alpha(0)
                    .scale(0)
                    .onStop(new AnimationListener.Stop() {
                        @Override
                        public void onStop() {
                            mPlay.setVisibility(View.GONE);
                            mMiniPlay.setVisibility(View.GONE);
                        }
                    })
                    .duration(200)
                    .start();
            ViewAnimator.animate(mPause, mMiniPause)
                    .alpha(0,1)
                    .scale(0,1.2f, 1)
                    .duration(400)
                    .onStart(new AnimationListener.Start() {
                        @Override
                        public void onStart() {
                            mPause.setVisibility(View.VISIBLE);
                            mMiniPause.setVisibility(View.VISIBLE);
                        }
                    })
                    .startDelay(100)
                    .start();
        }
    }


    private Boolean RunServiceIfnot(int isPlayFromMain,int isNextorPre,int position){
        Cursor mCursor= MediaStoreAccessHelper.getAllSongs(mContext, null, null);
        if(!UtilFunctions.isServiceRunning(SongPlayback.class.getName(), mContext)){

            Intent i = new Intent(getApplicationContext(),SongPlayback.class);

            if (isNextorPre==MainActivity.forSkipTrack) {
                setSongPlay(PlayerConstants.SONG_NUMBER,position);
                PlayerConstants.SONG_NUMBER=position;
            }
            else if (isNextorPre==MainActivity.forNothingKey || isNextorPre==MainActivity.forPreviousKey) {
                mHandler.post(loadLastPosition);
            }

            //i.putExtra(SongPlayback.isStartFromMain, true);

            i.putExtra(SongPlayback.RestartServiceAction, isNextorPre);
            i.putExtra(SongPlayback.isPlayFromMain, isPlayFromMain);

            startService(i);
            return false;
        }
        else if (PlayerConstants.SONGS_LIST.size()!=mCursor.getCount())
        {
            Intent i = new Intent(getApplicationContext(),WelcomeActivity.class);
            i.putExtra(SongPlayback.isPlayFromMain, isPlayFromMain);
            i.putExtra(SongPlayback.RestartServiceAction, isNextorPre);
            startActivity(i);
            activity.finish();
            return false;
        }
        return true;
    }

    private Runnable loadLastPosition=new Runnable() {
        @Override
        public void run() {
            lastPosition();
        }
    };

    private void lastPosition(){
        int lastSong= UtilFunctions.getSharedPreferenceint(mContext,UtilFunctions.LastSongNumber,-1);
        if (lastSong>-1)
        {
            PlayerConstants.SONG_NUMBER=lastSong;
            PlayerConstants.lastDuration=UtilFunctions.getSharedPreferenceint(mContext,UtilFunctions.LastDuration, 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.gotocurrentsong)
        {
            if (PlayerConstants.SONG_NUMBER>-1 && PlayerConstants.SONG_NUMBER+1<=PlayerConstants.SONGS_LIST.size())
                mRecyclerView.smoothScrollToPosition(PlayerConstants.SONG_NUMBER);

            return true;
        }
        else if (item.getItemId()==R.id.openmusicplayer){
            if (!isUp()) {
                goUp();
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mHandler.postDelayed(callChangeUI, 500);
        mAdView.resume();

        mTracker.setScreenName("mainActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdView.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdView.pause();
    }

    private Runnable callChangeUI=new Runnable() {
        @Override
        public void run() {
            if (PlayerConstants.SONG_NUMBER>-1)
            {
                changeUI();
                updateImage();
            }
            setupMusicPlayer();
        }
    };

    @Override
    public void onBackPressed() {
        if (isUp()) {
            goDown();
        }
        else {
            super.onBackPressed();}
    }




    private void goUp() {
        Animation(-getValues.dpToPx(mMiniMusicplayerheight), mAnimationDuration, 0);
        mTracker.setScreenName("musicplayer");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void goDown() {
        Animation(ScreenHeight - getValues.dpToPx(mMiniMusicplayerheight), mAnimationDuration, 1);
    }

    private void Animation(float y, int duration, float alpha) {
        mBothmusicPlayer.animate()
                .y(y)
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .start();
        mMiniMusicPlayer.animate()
                .alpha(alpha)
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .start();
    }

    private static Boolean getSlideUporBottom(float last, float now) {
        Boolean isSlideTop = false;
        float getSilde = now - last;
        if (getSilde < 0) {
            getSilde = -getSilde;
        }
        if (getSilde < mSlideHeight) {
            isSlideTop = false;
        } else {
            if (getSilde > mSlideHeight)
                isSlideTop = true;
        }
        return isSlideTop;
    }

    private static Boolean isUp() {
        Boolean isup = false;
        float getY = mBothmusicPlayer.getTranslationY();
        if (getY < 0) {
            isup = true;
        }
        return isup;
    }

    private class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent event) {

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    isTouch =true;
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    lastPosition = (int) view.getY();
                    IsUp = isUp();
                    break;
                case MotionEvent.ACTION_UP:
                    isTouch =false;
                    nowPosition = (int) view.getY();
                    if (lastPosition==nowPosition)
                    {
                        if (!IsUp)
                        {goUp();}
                    }
                    else
                    if (getSlideUporBottom(lastPosition, nowPosition)) {
                        if (IsUp)
                            goDown();
                        else
                            goUp();
                    } else {
                        if (ScreenHeight / 2 < view.getY()) {
                            goDown();
                        } else
                            goUp();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    isTouch =true;
                    int gety = (int) (event.getRawY() + dY);
                    if (gety < -getValues.dpToPx(mMiniMusicplayerheight)) {
                        gety = -getValues.dpToPx(mMiniMusicplayerheight);
                    }
                    if (gety > ScreenHeight - getValues.dpToPx(mMiniMusicplayerheight)) {
                        gety = (int) (ScreenHeight - getValues.dpToPx(mMiniMusicplayerheight));
                    }
                    view.animate()
                            .y(gety)
                            .setDuration(0)
                            .setInterpolator(new AccelerateInterpolator())
                            .start();
                    if (view.getY() < ((ScreenHeight + getValues.dpToPx(mMiniMusicplayerheight)) / 2)) {
                        float percentage = Math.abs(view.getY()+getValues.dpToPx(mMiniMusicplayerheight)) / ((ScreenHeight + getValues.dpToPx(mMiniMusicplayerheight)) / 2);
                        //percentage=percentage*2;
                        mMiniMusicPlayer.animate()
                                .alpha(percentage)
                                .setDuration(0)
                                .setInterpolator(new AccelerateInterpolator())
                                .start();
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }

    }

}