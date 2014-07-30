package com.nagravision.mediaplayer;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import com.nagravision.mediaplayer.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
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
import android.widget.VideoView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
@SuppressLint("CutPasteId")
public class FullscreenActivity extends Activity implements DrawerListener {

	private static final String LOG_TAG = "JAVA(MediaPlayer)";
	
    private static final String MOVIES_ARR[] = {
/*01*/    	"Sintel (mp4)", 
/*02*/    	"Elephants Dream (avi)", 
/*03*/    	"exMPD_BIP_TC1 (dash+xml)", 
/*04*/    	"Reel (avi)", 
/*05*/    	"Big Buck Bunny (m4v)",
/*06*/    	"Elephants Dream (mkv)",
/*07*/    	"Reel (file:mp4)",
/*08*/    	"Project London (file:mp4)",
/*09*/    	"Tears of Steel (mkv)"
    	};
    private static final String MOVIES_URLS[] = {
/*01*/        "http://mirrorblender.top-ix.org/movies/sintel-1280-surround.mp4",
/*02*/        "http://video.blendertestbuilds.de/download.blender.org/ED/ED_1024.avi",
/*03*/        "http://dash.edgesuite.net/dash264/TestCases/1a/netflix/exMPD_BIP_TC1.mpd",
/*04*/        "http://av.vimeo.com/12067/403/106802665.mp4?download=1&token2=1406580578_b736cf91787070149db5c21909d920e3&filename=Reel%25202012-HD.mp4",
/*05*/        "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_640x360.m4v",
/*06*/        "http://download.blender.org/ED/ed3d_sidebyside-RL-2x1920x1038_24fps.mkv",
/*07*/        "file://Reel 2012-SD.mp4",
/*08*/		  "file://Project London- Official Trailer-SD.mp4",
/*09*/		  "http://arcagenis.org/mirror/mango/ToS/tears_of_steel_1080p.mkv"
        };
    private ArrayAdapter<String> mUrlsAdapter;
                                                

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
	
	private static final int REQUEST_DISPLAY = 0;
	private static final int REQUEST_INFOS = 1;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	private DrawerLayout mDrawer;
	private VideoView mVideoHolder;
	private MediaController mControlsView;
	private ListView mMoviesList;
	private Notification.Builder mNotifBuilder;
	private NotificationManager mNotifMgr = null;
	private Notification mLastNotif = null;
	private int mLastPosition = -1;
	private PendingIntent mDefaultIntent, mInfosIntent;
	private Intent mExplicitIntent, mInfosActionIntent;

	@Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.v(LOG_TAG, "FullscreenActivity::onSaveInstanceState - Enter\n");
        super.onSaveInstanceState(outState);
        Log.v(LOG_TAG, "SavingSTATE: LastPosition = " + mLastPosition + "\n");
    	outState.putInt("LastPosition", mLastPosition);
        if (mLastPosition >= 0 && mLastPosition < MOVIES_URLS.length)
        {
            Log.v(LOG_TAG, "SavingSTATE: MediaPosition = " + mVideoHolder.getCurrentPosition() + "\n");
        	outState.putInt("MediaPosition", mVideoHolder.getCurrentPosition());
        }
    }
	
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "FullscreenActivity::onCreate - Enter\n");
		super.onCreate(savedInstanceState);

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
					// If the ViewPropertyAnimator API is available
					// (Honeycomb MR2 and later), use it to animate the
					// in-layout UI controls at the bottom of the
					// screen.
					if (mControlsHeight == 0) {
						mControlsHeight = mControlsView.getHeight();
					}
					if (mShortAnimTime == 0) {
						mShortAnimTime = getResources().getInteger(
								android.R.integer.config_shortAnimTime);
					}
					mControlsView.animate().translationY(visible ? 0 : mControlsHeight).setDuration(mShortAnimTime);
				} 
				else {
					// 	If the ViewPropertyAnimator APIs aren't
					// available, simply show or hide the in-layout UI
					// controls.
					mControlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
				}

				if (visible && AUTO_HIDE) {
					// Schedule a hide().
					delayedHide(AUTO_HIDE_DELAY_MILLIS);
				}
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

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "FullscreenActivity::onPostCreate - Enter\n");
		super.onPostCreate(savedInstanceState);

        Log.v(LOG_TAG, "savedInstanceState = " + savedInstanceState + "\n");
		if (savedInstanceState != null) {
			Log.v(LOG_TAG, "Restoring SavedSTATE\n");
            if (savedInstanceState.containsKey("LastPosition")) {
            	int position = savedInstanceState.getInt("LastPosition");
    			Log.v(LOG_TAG, "SavedSTATE: LastPosition = " + mLastPosition + "\n");
    			int msec = savedInstanceState.getInt("MediaPosition");
    			Log.v(LOG_TAG, "SavedSTATE: MediaPosition = " + msec + "\n");
    			ClickItem(position, msec);
            }
        }		

		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			Log.v(LOG_TAG, "FullscreenActivity.mDelayHideTouchListener.OnTouchListener.onTouch - Enter\n");
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return view.performClick();
		}
	};
	
	Handler mHideHandler = new Handler();
	
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			Log.v(LOG_TAG, "FullscreenActivity.mHideRunnable.Runnable - Enter\n");
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		Log.v(LOG_TAG, "FullscreenActivity::delayedHide - Enter\n");
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
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

	@Override
	public void onDrawerSlide(View arg0, float arg1) {
		Log.v(LOG_TAG, "FullscreenActivity::onDrawerSlide - Enter\n");		
	}

	@Override
	public void onDrawerStateChanged(int arg0) {
		Log.v(LOG_TAG, "FullscreenActivity::onDrawerStateChanged - Enter\n");		
	}
	
}
