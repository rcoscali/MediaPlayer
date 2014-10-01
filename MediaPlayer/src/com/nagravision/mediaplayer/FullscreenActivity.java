/*
 * Copyright (C) 2014 Nagravision
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nagravision.mediaplayer;

import com.nagravision.mediaplayer.util.SystemUiHider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.media.MediaCryptoException;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
@SuppressLint("CutPasteId")
public class FullscreenActivity 
    extends Activity 
    implements DrawerListener, 
               ActivityLifecycleCallbacks
{
    /*===============================================================*/
    /* Constants                                                     */
    /*===============================================================*/

    /**
     * Tag used for logging through the android log system
     */
    private static final String LOG_TAG = "JAVA(MediaPlayer)";
        
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * Contants for the intent used for displaying application
     */
    private static final int REQUEST_DISPLAY = 0;

    /**
     * Contants for the intent used for displaying movie information
     */
    private static final int REQUEST_INFOS = 1;

    /**
     * Array of strings containing the titles of the movies
     */
    private static final String MOVIES_ARR[] = {
        /*00*/          "", 
        /*01*/          "Sintel (mp4)", 
        /*02*/          "Avatar trailer (mp4)", 
        /*03*/          "Avatar trailer (cenc)", 
        /*04*/          "Reel (avi)", 
        /*05*/          "Big Buck Bunny (m4v)",
        /*06*/          "Elephants Dream (mkv)",
        /*07*/          "Reel (file:mp4)",
        /*08*/          "Project London (file:mp4)",
        /*09*/          "Tears of Steel (mkv)",
        /*10*/			"DASH IF encrypted (m4s)",
        /*11*/			"DASH IF clear (avc)",
        /*12*/			"DASH IF encrypted init (avc)",
        /*13*/			"DASH IF encrypted (avc)",
    };

    /**
     * Array of strings containing the URLs of the movies
     */
    private static final String MOVIES_URLS[] = {
        /*00*/        "",
        /*01*/        "http://mirrorblender.top-ix.org/movies/sintel-1280-surround.mp4",
        /*02*/        "http://home.citycable.ch/rcoscali/Aspire/assets/avatar.mp4",
        /*03*/        "http://home.citycable.ch/rcoscali/Aspire/assets/avatar_cenc.mp4",
        /*04*/        "http://av.vimeo.com/12067/403/106802665.mp4?download=1&token2=1406580578_b736cf91787070149db5c21909d920e3&filename=Reel%25202012-HD.mp4",
        /*05*/        "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_640x360.m4v",
        /*06*/        "http://download.blender.org/ED/ed3d_sidebyside-RL-2x1920x1038_24fps.mkv",
        /*07*/        "file://Reel 2012-SD.mp4",
        /*08*/        "file://Project London- Official Trailer-SD.mp4",
        /*09*/        "http://arcagenis.org/mirror/mango/ToS/tears_of_steel_1080p.mkv",
        /*10*/		  "http://dash.edgesuite.net/dash264/TestCases/6c/envivio/seg_b2000k_v_n9.m4s",
        /*11*/		  "http://dash.edgesuite.net/dash264/TestCases/1a/netflix/ElephantsDream_H264BPL30_0500.264.dash",
        /*12*/		  "http://dash.edgesuite.net/dash264/TestCases/6c/envivio/seg_b1000k_v_init.m4s",
        /*13*/		  "file:///mnt/sdcard/movies/seg_b1000k_v.mp4",
    };
    
    private static final String MOVIES_MDTA[] = {
    	/*00*/			"",
    	/*01*/			"",
    	/*02*/			"",
    	/*03*/			"drm",
    	/*04*/			"",
    	/*05*/			"drm",
    	/*06*/			"",
    	/*07*/			"",
    	/*08*/			"",
    	/*09*/			"",
    	/*10*/			"drm",
    	/*11*/			"drm",
    	/*12*/			"drm",
    	/*13*/			"drm",
    };

    /*===============================================================*/
    /* Fields                                                        */
    /*===============================================================*/

    /**
     * Array adapter for the drawer list providing the choice of the movie to the user
     */
    private ArrayAdapter<String> mUrlsAdapter;                                                

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    /**
     * The instance of the {@link DrawerLayout} for this activity.
     */
    private DrawerLayout mDrawer;

    /**
     * The instance of the {@link VideoView} for this activity.
     */
    private VideoView mVideoHolder;

    /**
     * The instance of the {@link MediaController} for this activity.
     */
    private MediaController mControlsView;

    /**
     * The instance of the {@link ListView} for the drawer of this activity.
     */
    private ListView mMoviesList;

    /**
     * Instance of {@link Notification.Builder} for building notifications for this activity
     */
    private Notification.Builder mNotifBuilder;

    /**
     * Instance of {@link NotificationManager} for this activity
     */
    private NotificationManager mNotifMgr = null;

    /**
     * Instance of {@link Notification} for this activity
     */
    private Notification mLastNotif = null;

    /**
     * Last selected movie item in the drawer's {@link ListView}
     */
    private int mLastPosition = -1;

    /**
     * Instances of {@link PendingIntent} used for the notifications of this activity
     */
    private PendingIntent mDefaultIntent, mInfosIntent;

    /**
     * Instances of {@link Intent} used for the notifications of this activity
     */
    private Intent mExplicitIntent, mInfosActionIntent;

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    @SuppressWarnings("unused")
	private View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.v(LOG_TAG, "FullscreenActivity.mDelayHideTouchListener.OnTouchListener.onTouch - Enter\n");
                if (AUTO_HIDE) {
                    delayedHide(AUTO_HIDE_DELAY_MILLIS);
                }
                return view.performClick();
            }
        };

    /**
     * Instance of the {@link Handler} used for hiding system UI
     */
    private Handler mHideHandler = new Handler();
       
    /**
     * Runnable hiding the system UI
     */ 
    private Runnable mHideRunnable = new Runnable() {
            @Override
            public void run() {
                Log.v(LOG_TAG, "FullscreenActivity.mHideRunnable.Runnable - Enter\n");
                mSystemUiHider.hide();
            }
        };

    /**
     * 
     */
    public FullscreenActivity() {
    	super();
    	Log.v(LOG_TAG, "FullscreenActivity::FullscreenActivity - Enter\n");
    }

    private void saveInstanceState(Bundle out)
    {
        Log.v(LOG_TAG, "FullscreenActivity::saveInstanceState - Enter\n");

        if (out != null) {
            Log.v(LOG_TAG, "FullscreenActivity::saveInstanceState - out = " + out.toString() + "\n");
            Log.v(LOG_TAG, "SavingSTATE: LastPosition = " + mLastPosition + "\n");
            out.putInt("LastPosition", mLastPosition);
            if (mLastPosition >= 0 && mLastPosition < MOVIES_URLS.length)
                {
                    Log.v(LOG_TAG, "SavingSTATE: MediaPosition = " + mVideoHolder.getCurrentPosition() + "\n");
                    out.putInt("MediaPosition", mVideoHolder.getCurrentPosition());
                }
        }
        else {
            Log.v(LOG_TAG, "Cannot save instance state: null out bundle\n");
        }
    }

    private void readInstanceState(Bundle in)
    {
        Log.v(LOG_TAG, "FullscreenActivity::readInstanceState - Enter\n");
        if (in != null) {
            Log.v(LOG_TAG, "FullscreenActivity::readInstanceState - in = " + in.toString() + "\n");
            if (!in.isEmpty()) {
                Log.v(LOG_TAG, "Reading SavedSTATE\n");
                if (in.containsKey("LastPosition")) {
                    int position = in.getInt("LastPosition");
                    Log.v(LOG_TAG, "SavedSTATE: LastPosition = " + mLastPosition + "\n");
                    int msec = in.getInt("MediaPosition");
                    Log.v(LOG_TAG, "SavedSTATE: MediaPosition = " + msec + "\n");
                    ClickItem(position, msec);
                }
            }
            else {
                Log.v(LOG_TAG, "No SavedSTATE available ! Input bundle empty !\n");
            }
        }
        else {
            Log.v(LOG_TAG, "No SavedSTATE available ! Null Input bundle !\n");
        }
    }

    /**
     *
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) 
    {
        Log.v(LOG_TAG, "FullscreenActivity::onSaveInstanceState - Enter\n");
        super.onSaveInstanceState(outState);
        saveInstanceState(outState);
    }
        
    /**
     *
     */
    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle inBundle) 
    {
        Log.v(LOG_TAG, "FullscreenActivity::onCreate - Enter\n");
        super.onCreate(inBundle);
                
        getApplication().registerActivityLifecycleCallbacks(this);

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
                             WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                
        setContentView(R.layout.activity_fullscreen);
        mDrawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        mMoviesList = (ListView)findViewById(R.id.start_drawer);
        mUrlsAdapter = new ArrayAdapter<String>(this, 
                                                android.R.layout.simple_list_item_activated_1, 
                                                MOVIES_ARR);
        mMoviesList.setAdapter(mUrlsAdapter);
        mVideoHolder = (VideoView)findViewById(R.id.fullscreen_content);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= 19) {
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        }
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.hide();
                
        mNotifBuilder = new Notification.Builder(this);
        mNotifMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mExplicitIntent = new Intent(Intent.ACTION_VIEW);
        mExplicitIntent.setClass(this, this.getClass());
        mDefaultIntent = PendingIntent.getActivity(this, 
                                                   REQUEST_DISPLAY, 
                                                   mExplicitIntent, 
                                                   Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                                                   Intent.FLAG_DEBUG_LOG_RESOLUTION);
        mInfosActionIntent = new Intent();
        mInfosActionIntent.setAction("com.nagravision.mediaplayer.VIDEO_INFOS");
        mInfosActionIntent.addCategory(Intent.CATEGORY_DEFAULT);
        mInfosActionIntent.addCategory(Intent.CATEGORY_INFO);
        mInfosIntent = PendingIntent.getActivity(this,
                                                 REQUEST_INFOS,
                                                 mInfosActionIntent,
                                                 Intent.FLAG_ACTIVITY_NEW_TASK |
                                                 Intent.FLAG_DEBUG_LOG_RESOLUTION);
                
        if (Build.VERSION.SDK_INT >= 19) {
            mVideoHolder.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                               View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                               View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                               View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                               View.SYSTEM_UI_FLAG_FULLSCREEN |
                                               View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
        else {
            mVideoHolder.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                               View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                               View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                               View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                               View.SYSTEM_UI_FLAG_FULLSCREEN);                 
        }
        OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
                public void onItemClick(@SuppressWarnings("rawtypes") AdapterView parent, View v, int position, long id) {
                    Log.v(LOG_TAG, "FullscreenActivity.onCreate.OnItemClickListener::onItemClick - Enter\n");
                    ClickItem(position, 0);
                }
            };

        mMoviesList.setOnItemClickListener(mMessageClickedHandler);             
                
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        mControlsView = new MediaController(this, true);
        mControlsView.setPrevNextListeners(new View.OnClickListener() 
            {
                /*
                 * Next listener
                 */
                @Override
                public void onClick(View view) {
                    Log.v(LOG_TAG, "FullscreenActivity.onCreate.OnClickListener::onClick(next) - Enter\n");
                    mVideoHolder.stopPlayback();
                    int position = 0;
                    if (mLastPosition > 0)
                        position = mLastPosition +1;
                    if (position >= MOVIES_URLS.length)
                        position = 0;
                    ClickItem(position, 0);

                    String toaststr = getResources().getString(R.string.next_movie_toast_string) + MOVIES_ARR[position];
                    Toast.makeText(FullscreenActivity.this, toaststr, Toast.LENGTH_LONG).show();
                }
            }, new View.OnClickListener() 
                {
                    /*
                     * Prev listener
                     */
                    @Override
                    public void onClick(View view) {
                        Log.v(LOG_TAG, "FullscreenActivity.onCreate.OnClickListener::onClick(prev) - Enter\n");
                        mVideoHolder.stopPlayback();
                        int position = 0;
                        if (mLastPosition > 0)
                            position = mLastPosition -1;
                        if (position < 0)
                            position = MOVIES_URLS.length -1;
                        ClickItem(position, 0);
                        
                        String toaststr = getResources().getString(R.string.prev_movie_toast_string) + MOVIES_ARR[position];
                        Toast.makeText(FullscreenActivity.this, toaststr, Toast.LENGTH_LONG).show();
                    }
                });
        mVideoHolder.setMediaController(mControlsView);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, mVideoHolder, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() 
            {
                // Cached values.
                int mControlsHeight;
                int mShortAnimTime;

                @Override
                @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                public void onVisibilityChange(boolean visible) {
                    Log.v(LOG_TAG, "FullscreenActivity.OnVisibilityChangeListener::onVisibilityChange - Enter\n");
                                
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                        if (mControlsHeight == 0)
                            mControlsHeight = mControlsView.getHeight();
                        if (mShortAnimTime == 0)
                            mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
                        mControlsView.animate().translationY(visible ? 0 : mControlsHeight).setDuration(mShortAnimTime);
                    } 
                    else
                        mControlsView.setVisibility(visible ? View.VISIBLE : View.GONE);

                    if (visible && AUTO_HIDE)
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                }
            });

        // Set up the user interaction to manually show or hide the system UI.
        mVideoHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (TOGGLE_ON_CLICK) {
                        mSystemUiHider.toggle();
                    } else {
                        mSystemUiHider.show();
                    }
                }
            });

        mDrawer.openDrawer(mMoviesList);
    }

    /**
     *
     */
    protected void onStart() 
    {
        super.onStart();
        Log.v(LOG_TAG, "FullscreenActivity::onStart - Enter\n");
    }    
    
    /**
     *
     */
    protected void onRestart() 
    {
        super.onRestart();
        Log.v(LOG_TAG, "FullscreenActivity::onRestart - Enter\n");
        ClickItem(mLastPosition, 0);
    }    
    
    /**
     *
     */
    protected void onResume() 
    {
        super.onResume();
        Log.v(LOG_TAG, "FullscreenActivity::onResume - Enter\n");
        if (mVideoHolder.canPause()) mVideoHolder.resume();
    }    
   
    /**
     *
     */
    protected void onPause() 
    {
        super.onPause();
        Log.v(LOG_TAG, "FullscreenActivity::onPause - Enter\n");
        if (mVideoHolder.canPause()) mVideoHolder.pause();
    }    
   
    /**
     *
     */
    protected void onStop() 
    {
        super.onStop();
        Log.v(LOG_TAG, "FullscreenActivity::onStop - Enter\n");
        mVideoHolder.stopPlayback();
    }    
   
    /**
     *
     */
    protected void onDestroy() 
    {
        super.onDestroy();
        Log.v(LOG_TAG, "FullscreenActivity::onDestroy - Enter\n");
    }    
   
    /**
     *
     */
    @Override
    protected void onPostCreate(Bundle inBundle) 
    {
        Log.v(LOG_TAG, "FullscreenActivity::onPostCreate - Enter\n");
        super.onPostCreate(inBundle);

        readInstanceState(inBundle);

        delayedHide(100);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        Log.v(LOG_TAG, "FullscreenActivity::delayedHide - Enter\n");
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     *
     */        
    @SuppressWarnings("unused")
	private void ClickItem(int position, int msec) {
        Log.v(LOG_TAG, "FullscreenActivity::ClickItem - Enter\n");

        /* Close the drawer */
        mDrawer.closeDrawers();

        /* Build the notification we are going to display */
        mNotifBuilder.setContentText(getResources().getString(R.string.playing_notification));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        (new PrintWriter(baos)).format(getResources().getString(R.string.long_playing_notification), MOVIES_ARR[position]);
        mNotifBuilder.setContentInfo(baos.toString());
        mNotifBuilder.setContentTitle(getResources().getString(R.string.app_name));
        mNotifBuilder.setSubText(getResources().getString(R.string.app_copyright));
        mNotifBuilder.setSmallIcon(R.drawable.ic_action_play);
        mExplicitIntent.setData(Uri.parse(MOVIES_URLS[position]));
        mNotifBuilder.setContentIntent(mDefaultIntent);
        mNotifBuilder.addAction(R.drawable.ic_action_about,
                                getResources().getString(R.string.infos_action_description),
                                mInfosIntent);
        
        /* If a notif was already sent, cancel it */
        if (mLastNotif != null) {
            if (mLastPosition  != -1) {
                mNotifMgr.cancel(getResources().getString(R.string.playing_notification), 
                                 mLastPosition);
                mLastPosition = -1;
            }
            else
                mNotifMgr.cancelAll();
            mLastNotif = null;
        }
        mLastNotif = mNotifBuilder.build();
        mLastPosition = position;
        
        if (false && MOVIES_MDTA[position].contains("drm")) {
            Log.d(LOG_TAG, "Starting extraction of Media Metadata\n");
            MediaExtractor extractor = new MediaExtractor();
            try {
                Log.d(LOG_TAG, "URL: " + MOVIES_URLS[position]);
                extractor.setDataSource(MOVIES_URLS[position]);
                int numTracks = extractor.getTrackCount();
            	Log.v(LOG_TAG, "Number of tracks: " + numTracks);
            	for (int i = 0; i < numTracks; ++i) {
                    MediaFormat format = extractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    Log.v(LOG_TAG, "Track[" + i + "].mime = " + mime);
                    if (mime.equals("video/avc")) {
                        extractor.selectTrack(i);
//                        byte[] key = new byte[16];
//                        key[0] = (byte) 0x7f; key[1] = (byte) 0x99; key[2] = (byte) 0x06; key[3] = (byte) 0x95;
//                        key[4] = (byte) 0x78; key[5] = (byte) 0xab; key[6] = (byte) 0x4d; key[7] = (byte) 0xae;
//                        key[8] = (byte) 0x8d; key[9] = (byte) 0x6c; key[10] = (byte) 0xe2; key[11] = (byte) 0x4c;
//                        key[12] = (byte) 0xc3; key[13] = (byte) 0x21; key[14] = (byte) 0x02; key[15] = (byte) 0x32;
                        MediaCrypto mediaCrypto;
                        MediaCodec codec = MediaCodec.createDecoderByType("video/avc");
                    	Map<UUID, byte[]> psshInfo = extractor.getPsshInfo();
                    	for (Iterator<UUID> it = psshInfo.keySet().iterator();
                    		 it.hasNext();) {
                    		UUID uuid = it.next();
                    		String psshData = new String(psshInfo.get(uuid));
                    		Log.v(LOG_TAG, 
                    			  "PSSH for UUID: " + uuid.toString() + 
                    			  " has data :" + psshData);
                            try {
                            	mediaCrypto = new MediaCrypto(uuid, psshInfo.get(uuid));
                            	codec.configure(format, 
                            					mVideoHolder.getHolder().getSurface(), 
                            					mediaCrypto,
                            					0);
                            	codec.start();
                            	@SuppressWarnings("unused")
								ByteBuffer[] inputBuffers = codec.getInputBuffers();
                            	@SuppressWarnings("unused")
								ByteBuffer[] outputBuffers = codec.getOutputBuffers();
                            	for (;;) {
                            		   int inputBufferIndex = codec.dequeueInputBuffer(200000);
                            		   if (inputBufferIndex >= 0) {
                            			   //extractor.readSampleData();
                            			   Log.v(LOG_TAG, "Read data from extractor (and crypto) and provide to decoder\n");
                            		   }
                            	}
                            } catch (MediaCryptoException mce) {
                            	Log.e(LOG_TAG, "Could not instanciate MediaCrypto ! " + mce);
                            }
                    	}
                    }
            	}
            } catch (IOException ioe) {
                Log.e(LOG_TAG, "Cannot access URL: " + ioe);
            }
        }
        
        /* Setup media player for playing the movie */
        mVideoHolder.setVideoURI(Uri.parse(MOVIES_URLS[position]));
        mVideoHolder.requestFocus();
        mVideoHolder.start();
                
        if (msec > 0) {
            if (mVideoHolder.canPause()) mVideoHolder.pause();
            mVideoHolder.seekTo(msec);
            if (mVideoHolder.canPause()) mVideoHolder.resume();
        }
                
        /* Player has started ... send notification */
        mNotifMgr.notify(getResources().getString(R.string.playing_notification), 
                         mLastPosition, 
                         mLastNotif);
    }

    /**
     *
     */        
    @Override
    public void onDrawerClosed(View arg0) {
        Log.v(LOG_TAG, "FullscreenActivity::onDrawerClosed - Enter\n");

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN 
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(uiOptions);
        if (mVideoHolder.canPause()) mVideoHolder.resume();
    }

    /**
     *
     */        
    @Override
    public void onDrawerOpened(View arg0) {
        Log.v(LOG_TAG, "FullscreenActivity::onDrawerOpened - Enter\n");
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        if (mVideoHolder.canPause()) mVideoHolder.pause();
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
    }

    /**
     *
     */        
    @Override
    public void onDrawerSlide(View arg0, float arg1) {
        Log.v(LOG_TAG, "FullscreenActivity::onDrawerSlide - Enter\n");          
    }

    /**
     *
     */        
    @Override
    public void onDrawerStateChanged(int arg0) {
        Log.v(LOG_TAG, "FullscreenActivity::onDrawerStateChanged - Enter\n");           
    }

    /**
     *
     */        
    @Override
    public void onActivityCreated(Activity activity, Bundle inBundle) {
        Log.v(LOG_TAG, "FullscreenActivity::onActivityCreated - Enter\n");
        Log.v(LOG_TAG, "FullscreenActivity::onActivityCreated - inBundle = " + inBundle + "\n");
    }

    /**
     *
     */        
    @Override
    public void onActivityStarted(Activity activity) {
        Log.v(LOG_TAG, "FullscreenActivity::onActivityStarted - Enter\n");
    }

    /**
     *
     */        
    @Override
    public void onActivityResumed(Activity activity) {
        Log.v(LOG_TAG, "FullscreenActivity::onActivityResumed - Enter\n");
    }

    /**
     *
     */        
    @Override
    public void onActivityPaused(Activity activity) {
        Log.v(LOG_TAG, "FullscreenActivity::onActivityPaused - Enter\n");
    }

    /**
     *
     */        
    @Override
    public void onActivityStopped(Activity activity) {
        Log.v(LOG_TAG, "FullscreenActivity::onActivityStopped - Enter\n");
    }

    /**
     *
     */        
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outBundle) {
        Log.v(LOG_TAG, "FullscreenActivity::onActivitySaveInstanceState - Enter\n");
        Log.v(LOG_TAG, "FullscreenActivity::onActivitySaveInstanceState - outBundle = " + outBundle + "\n");
    }

    /**
     *
     */        
    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.v(LOG_TAG, "FullscreenActivity::onActivityDestroyed - Enter\n");
    }
        
}
