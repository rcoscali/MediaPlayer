package com.nagravision.mediaplayer;

import com.nagravision.mediaplayer.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
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

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	private DrawerLayout mDrawer;
	private VideoView mVideoHolder;
	private MediaController mControlsView;
	private ListView mMoviesList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		
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
				   		View.SYSTEM_UI_FLAG_FULLSCREEN |
				   		View.SYSTEM_UI_FLAG_IMMERSIVE;
		decorView.setSystemUiVisibility(uiOptions);
		// Remember that you should never show the action bar if the
		// status bar is hidden, so hide that too if necessary.
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		
		mVideoHolder.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
										   View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
										   View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
										   View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
										   View.SYSTEM_UI_FLAG_FULLSCREEN |
										   View.SYSTEM_UI_FLAG_IMMERSIVE);
		OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
		    public void onItemClick(@SuppressWarnings("rawtypes") AdapterView parent, View v, int position, long id) {
		    	mDrawer.closeDrawers();
				mVideoHolder.setVideoURI(Uri.parse(MOVIES_URLS[position]));
				mVideoHolder.requestFocus();
				mVideoHolder.start();		        
		    }
		};

		mMoviesList.setOnItemClickListener(mMessageClickedHandler);		
		
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		mControlsView = new MediaController(this);
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
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
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
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	@Override
	public void onDrawerClosed(View arg0) {
		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN 
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
		decorView.setSystemUiVisibility(uiOptions);
		if (mVideoHolder.canPause()) mVideoHolder.start();
	}

	@Override
	public void onDrawerOpened(View arg0) {
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDrawerStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}
}
