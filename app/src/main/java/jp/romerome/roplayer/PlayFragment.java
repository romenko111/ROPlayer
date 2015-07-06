package jp.romerome.roplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by roman on 2015/06/30.
 */
public class PlayFragment extends Fragment implements PlayerService.StateChangeListener {

	private Track mTrack;
	private ImageView mAlbumart;
	private ImageButton mPlayButton;
	private ImageButton mNextButton;
	private ImageButton mPreviousButton;
	private ImageButton mRepeatButton;
	private TextView mTrackNoView;
	private TextView mDurationView;
	private TextView mElpsedView;
	private SeekBar mSeekBar;
	private PlayerService mService;
	private ServiceConnection mServiceConnection;
	private int mState = PlayerService.STATE_STOP;
	private PlayerService.StateChangeListener mListener;
	private Timer mTimer;
	private Handler mHandler = new Handler();
	private boolean isSeeking = false;

	public PlayFragment(){

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_play, container, false);
		mTrack = RoLibrary.getCurrentTrack(getActivity());

		mElpsedView = (TextView) rootView.findViewById(R.id.elapsed);
		mDurationView = (TextView) rootView.findViewById(R.id.duration);
		mTrackNoView = (TextView) rootView.findViewById(R.id.trackNo);

		mSeekBar = (SeekBar) rootView.findViewById(R.id.seekbar);
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(isSeeking){
					mElpsedView.setText(RoLibrary.getStringTime(seekBar.getProgress()));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				isSeeking = true;
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mElpsedView.setText(RoLibrary.getStringTime(seekBar.getProgress()));
				mService.seekTo(seekBar.getProgress());
				isSeeking = false;
			}
		});
		mPlayButton = (ImageButton) rootView.findViewById(R.id.btn_play);
		mPlayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mService.playpause();
			}
		});

		mNextButton = (ImageButton) rootView.findViewById(R.id.btn_next);
		mNextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mService.next();
			}
		});

		mPreviousButton = (ImageButton) rootView.findViewById(R.id.btn_previous);
		mPreviousButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mService.previous();
			}
		});

		mRepeatButton = (ImageButton) rootView.findViewById(R.id.btn_repeat);
		mRepeatButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mService.toggleRepeatMode();
			}
		});

		mAlbumart = (ImageView) rootView.findViewById(R.id.album_art);
		initService();

		return rootView;
	}

	@Override
	public void onResume(){
		super.onResume();
		if(mService != null){
			if (mTimer == null) {
				mTimer = new Timer(true);
				mTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								if (!isSeeking) {
									int time = mService.getElpsedTime();
									mElpsedView.setText(RoLibrary.getStringTime(time));
									mSeekBar.setProgress(time);
								}
							}
						});
					}
				}, 100, 100);
			}
		}
	}

	@Override
	public void onPause(){
		super.onPause();
		if(mService != null){
			if(mTimer != null){
				mTimer.cancel();
				mTimer = null;
			}
		}
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		mService.removeStateChangeListener(this);
		mListener = null;
		if(mServiceConnection != null){
			getActivity().unbindService(mServiceConnection);
		}
	}

	private void updateView(int no,int playlistSize){
		if(mTrack != null) {
			mElpsedView.setText("0:00");
			mDurationView.setText(RoLibrary.getStringTime(mTrack));
			mTrackNoView.setText(no + "/" + playlistSize);
			mSeekBar.setMax((int) mTrack.duration);

			MediaMetadataRetriever mmr = new MediaMetadataRetriever();
			Bitmap bitmap;
			try {
				mmr.setDataSource(mTrack.path);
				byte[] data = mmr.getEmbeddedPicture();
				if (data == null) {
					bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.ic_launcher);
				} else {
					bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				}
			} catch (Exception e) {
				bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.ic_launcher);
			}
			mAlbumart.setImageBitmap(bitmap);
		}
	}

	private void setRepeatMode(int repeatMode){
		switch (repeatMode){
			case RoLibrary.REPEAT_OFF:
				mRepeatButton.setBackgroundResource(R.drawable.repeat_off);
				break;

			case RoLibrary.REPEAT_NORMAL:
				mRepeatButton.setBackgroundResource(R.drawable.repeat_normal);
				break;

			case RoLibrary.REPEAT_TRACK:
				mRepeatButton.setBackgroundResource(R.drawable.repeat_track);
				break;

			case RoLibrary.REPEAT_OFF_TRACK:
				mRepeatButton.setBackgroundResource(R.drawable.repeat_off_track);
				break;

			case RoLibrary.REPEAT_NEXT_ALBUM:
				mRepeatButton.setBackgroundResource(R.drawable.repeat_next_album);
				break;
		}
	}

	private void initService(){
		Intent service = new Intent(getActivity().getApplication(),PlayerService.class);
		getActivity().startService(service);

		mServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				PlayerService.RoBinder binder = (PlayerService.RoBinder)service;
				mService = binder.getService();
				mService.addStateChangeListener(PlayFragment.this);
				mState = mService.getState();
				switch (mState){
					case PlayerService.STATE_PLAY:
						mPlayButton.setBackgroundResource(R.drawable.pause);
						break;

					case PlayerService.STATE_PAUSE:
						mPlayButton.setBackgroundResource(R.drawable.play);
						break;
				}
				setRepeatMode(mService.getRepeatMode());
				if(mState != PlayerService.STATE_STOP) {
					updateView(RoLibrary.getNo(getActivity()), RoLibrary.getCurrentPlaylist(getActivity()).size());
					if(mTimer == null) {
						mTimer = new Timer(true);
						mTimer.schedule(new TimerTask() {
							@Override
							public void run() {
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										if (!isSeeking) {
											int time = mService.getElpsedTime();
											mElpsedView.setText(RoLibrary.getStringTime(time));
											mSeekBar.setProgress(time);
										}
									}
								});
							}
						}, 100, 100);
					}
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {

			}
		};
		getActivity().bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStateChange(int state) {
		mState = state;
		switch (mState){
			case PlayerService.STATE_PLAY:
				mPlayButton.setBackgroundResource(R.drawable.pause);
				break;

			case PlayerService.STATE_PAUSE:
				mPlayButton.setBackgroundResource(R.drawable.play);
				break;

			case PlayerService.STATE_STOP:
				getActivity().finish();
				break;
		}
	}

	@Override
	public void onTrackChange(Track track,int no,int playlistSize){
		mTrack = track;
		updateView(no,playlistSize);
		if(mListener != null){
			mListener.onTrackChange(track,no,playlistSize);
		}
	}

	@Override
	public void onRepeatModeChange(int repeatMode) {
		setRepeatMode(repeatMode);
	}

	public  void setStateChangeListener(PlayerService.StateChangeListener listener){
		mListener = listener;
	}

}
