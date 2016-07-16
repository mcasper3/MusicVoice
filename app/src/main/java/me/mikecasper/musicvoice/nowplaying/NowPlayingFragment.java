package me.mikecasper.musicvoice.nowplaying;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import me.mikecasper.musicvoice.MusicVoiceApplication;
import me.mikecasper.musicvoice.R;
import me.mikecasper.musicvoice.controllers.MusicButtonsController;
import me.mikecasper.musicvoice.controllers.MusicInfoController;
import me.mikecasper.musicvoice.controllers.NowPlayingMusicControls;
import me.mikecasper.musicvoice.controllers.NowPlayingTrackInfoController;
import me.mikecasper.musicvoice.models.Artist;
import me.mikecasper.musicvoice.models.Track;
import me.mikecasper.musicvoice.nowplaying.events.StartQueueFragmentEvent;
import me.mikecasper.musicvoice.services.eventmanager.EventManagerProvider;
import me.mikecasper.musicvoice.services.eventmanager.IEventManager;
import me.mikecasper.musicvoice.services.musicplayer.events.GetPlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.LostPermissionEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SeekToEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipBackwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SkipForwardEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.SongChangeEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.StopSeekbarUpdateEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.TogglePlaybackEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleRepeatEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.ToggleShuffleEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdatePlayerStatusEvent;
import me.mikecasper.musicvoice.services.musicplayer.events.UpdateSongTimeEvent;
import me.mikecasper.musicvoice.util.DateUtility;

public class NowPlayingFragment extends Fragment {

    // Constants for Saving View State
    private static final String IS_PLAYING = "isPlaying";

    // Services
    private IEventManager mEventManager;

    // Data Members
    private boolean mIsPlayingMusic;
    private boolean mShuffleEnabled;
    private int mRepeatMode;
    private ImageView mAlbumArt;
    private MusicInfoController mMusicInfoController;
    private MusicButtonsController mMusicButtonsController;

    // For swiping
    private float mInitialXValue;
    private float mSecondXValue;
    private static final int MIN_DISTANCE = 200;

    // Target for better image loading
    private final Target mTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mAlbumArt.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            mAlbumArt.setImageDrawable(errorDrawable);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            // do nothing
        }
    };

    public NowPlayingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onPause() {
        mEventManager.unregister(mMusicInfoController);
        mEventManager.unregister(mMusicButtonsController);
        mEventManager.unregister(this);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        mEventManager.register(this);
        mEventManager.register(mMusicButtonsController);
        mEventManager.register(mMusicInfoController);
        mEventManager.postEvent(new GetPlayerStatusEvent());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.view_alternate_now_playing, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEventManager = EventManagerProvider.getInstance(getContext());

        Bundle args = getArguments();
        Track track = args.getParcelable(NowPlayingActivity.TRACK);
        
        mMusicButtonsController = new NowPlayingMusicControls(view);
        mMusicInfoController = new NowPlayingTrackInfoController(view, track);

        boolean shouldPlaySong = args.getBoolean(NowPlayingActivity.SHOULD_PLAY_TRACK, false);
        mIsPlayingMusic = args.getBoolean(NowPlayingActivity.IS_PLAYING_MUSIC, false);

        if (savedInstanceState == null) {
            if (shouldPlaySong) {
                mIsPlayingMusic = true;
            }
        } else {
            mIsPlayingMusic = savedInstanceState.getBoolean(IS_PLAYING);
            mShuffleEnabled = savedInstanceState.getBoolean(NowPlayingActivity.SHUFFLE_ENABLED);
            mRepeatMode = savedInstanceState.getInt(NowPlayingActivity.REPEAT_MODE);
        }

        TextView playlistName = (TextView) view.findViewById(R.id.playlist_name);
        playlistName.setSelected(true);
        playlistName.setSingleLine(true);

        String playlist = args.getString(NowPlayingActivity.PLAYLIST_NAME, null);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        if (playlist == null) {
            playlist = sharedPreferences.getString(NowPlayingActivity.PLAYLIST_NAME, null);
        } else {
            sharedPreferences.edit()
                    .putString(NowPlayingActivity.PLAYLIST_NAME, playlist)
                    .apply();
        }

        playlistName.setText(getString(R.string.playing_from, playlist));



        mAlbumArt = (ImageView) view.findViewById(R.id.album_art);
        mAlbumArt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        mInitialXValue = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        mSecondXValue = event.getX();
                        float deltaX = mSecondXValue - mInitialXValue;
                        if (deltaX > MIN_DISTANCE) {
                            mEventManager.postEvent(new SkipBackwardEvent());
                        } else if (deltaX < -MIN_DISTANCE) {
                            mEventManager.postEvent(new SkipForwardEvent());
                        }
                        break;
                }

                return true;
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(IS_PLAYING, mIsPlayingMusic);
        outState.putBoolean(NowPlayingActivity.SHUFFLE_ENABLED, mShuffleEnabled);
        outState.putInt(NowPlayingActivity.REPEAT_MODE, mRepeatMode);
    }

    @Subscribe
    public void startQueueFragment(StartQueueFragmentEvent event) {
        Fragment fragment = new QueueFragment();
        Bundle args = getArguments();
        fragment.setArguments(args);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.now_playing_content, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Subscribe
    public void onPlayerStatusUpdated(UpdatePlayerStatusEvent event) {
        Track track = event.getTrack();

        if (track != null) {
            Bundle args = getArguments();
            args.putParcelable(NowPlayingActivity.TRACK, track);
        } else {
            getActivity().onBackPressed();
        }
    }

    @Subscribe
    public void onSongChange(SongChangeEvent event) {
        Track track = event.getTrack();

        Bundle args = getArguments();

        args.putParcelable(NowPlayingActivity.TRACK, track);

        Picasso.with(getContext())
                .load(track.getAlbum().getImages().get(0).getUrl())
                .error(R.drawable.default_playlist)
                .into(mTarget);
    }

    @Override
    public void onDestroy() {
        mEventManager = null;
        mAlbumArt = null;
        mMusicButtonsController.tearDown();
        mMusicInfoController.tearDown();

        ((MusicVoiceApplication) getActivity().getApplication()).getRefWatcher().watch(this);

        super.onDestroy();
    }
}
