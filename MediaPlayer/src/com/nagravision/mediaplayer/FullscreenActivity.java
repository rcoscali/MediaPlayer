package com.nagravision.mediaplayer;

import com.nagravision.mediaplayer.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.XmlResourceParser;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
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
    	"Titles",
    	"Sintel (mp4)", 
    	"ED (avi)", 
    	"exMPD_BIP_TC1 (mpd)", 
    	"Reel (avi)", 
    	"Big Buck Bunny (m4v)"};
    private static final String MOVIES_URLS[] = {
    	"",
        "http://mirrorblender.top-ix.org/movies/sintel-1280-surround.mp4",
        "http://video.blendertestbuilds.de/download.blender.org/ED/ED_1024.avi",
        "http://dash.edgesuite.net/dash264/TestCases/1a/netflix/exMPD_BIP_TC1.mpd",
        "http://av.vimeo.com/12067/403/106802665.mp4?download=1&token2=1406580578_b736cf91787070149db5c21909d920e3&filename=Reel%25202012-HD.mp4",
        "http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_640x360.m4v"};
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

        setContentView(R.layout.activity_fullscreen);
        mDrawer = (DrawerLayout)findViewById(R.id.drawer_layout);
		mMoviesList = (ListView)findViewById(R.id.start_drawer);
		mUrlsAdapter = new ArrayAdapter<String>(this, 
												android.R.layout.simple_list_item_activated_1, 
												MOVIES_ARR);
		mMoviesList.setAdapter(mUrlsAdapter);

		OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
		    public void onItemClick(AdapterView parent, View v, int position, long id) {
		    	mDrawer.closeDrawers();
				mVideoHolder.setVideoURI(Uri.parse(MOVIES_URLS[position]));
				mVideoHolder.requestFocus();
				mVideoHolder.start();		        
		    }
		};

		mMoviesList.setOnItemClickListener(mMessageClickedHandler);		
		
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		mVideoHolder = (VideoView)findViewById(R.id.fullscreen_content);
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
		// TODO Auto-generated method stub
		if (mVideoHolder.canPause()) mVideoHolder.start();
	}

	@Override
	public void onDrawerOpened(View arg0) {
		// TODO Auto-generated method stub
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