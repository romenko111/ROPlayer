package jp.romerome.roplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by roman on 2015/07/03.
 */
public class PlaySmallFragment extends Fragment implements PlayerService.StateChangeListener{

	private ImageView mAlbumart;
	private ImageButton mPlayButton;
	private ImageButton mNextButton;
	private ImageButton mPreviousButton;
	private TextView mTitleView;
	private TextView mArtistView;
	private Track mTrack;
	private PlayerService mService;
	private ServiceConnection mServiceConnection;
	private int mState = PlayerService.STATE_STOP;

	public PlaySmallFragment(){

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_play_small, container, false);
		rootView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), PlayActivity.class);
				String transitionName = getString(R.string.album_art);
				ActivityOptionsCompat options =
						ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
								mAlbumart,   // 遷移がはじまるビュー
								transitionName    // 遷移先のビューの transitionName
						);
				ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
			}
		});

		mTrack = RoLibrary.getCurrentTrack(getActivity());

		mTitleView = (TextView) rootView.findViewById(R.id.title);
		mArtistView = (TextView) rootView.findViewById(R.id.artist);

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
		mAlbumart = (ImageView) rootView.findViewById(R.id.album_art);
		updateView(RoLibrary.getNo(getActivity()), RoLibrary.getCurrentPlaylist(getActivity()).size());
		initService();
		return rootView;
	}

	private void updateView(int no,int playlistSize){
		if(mTrack != null) {
			mTitleView.setText(mTrack.title);
			mArtistView.setText(mTrack.artist);

			MediaMetadataRetriever mmr = new MediaMetadataRetriever();
			Bitmap bitmap;
			try {
				mmr.setDataSource(mTrack.path);
				byte[] data = mmr.getEmbeddedPicture();
				if (data == null) {
					bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
				} else {
					bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				}
			} catch (Exception e) {
				bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
			}
			mAlbumart.setImageBitmap(bitmap);
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
				mService.addStateChangeListener(PlaySmallFragment.this);
				mState = mService.getState();
				switch (mState){
					case PlayerService.STATE_PLAY:
						mPlayButton.setBackgroundResource(R.drawable.pause_small);
						updateView(RoLibrary.getNo(getActivity()), RoLibrary.getCurrentPlaylist(getActivity()).size());
						break;

					case PlayerService.STATE_PAUSE:
						mPlayButton.setBackgroundResource(R.drawable.play_small);
						updateView(RoLibrary.getNo(getActivity()), RoLibrary.getCurrentPlaylist(getActivity()).size());
						break;
				}

			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Log.d("test","onServiceDisconnected");
			}
		};
		getActivity().bindService(service, mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStateChange(int state) {
		mState = state;
		switch (mState){
			case PlayerService.STATE_PLAY:
				mPlayButton.setBackgroundResource(R.drawable.pause_small);
				break;

			case PlayerService.STATE_PAUSE:
				mPlayButton.setBackgroundResource(R.drawable.play_small);
				break;
		}
	}

	@Override
	public void onTrackChange(Track track, int no, int playlistSize) {
		mTrack = track;
		updateView(no,playlistSize);
	}

	@Override
	public void onRepeatModeChange(int repeatMode) {

	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		if(mServiceConnection != null){
			getActivity().unbindService(mServiceConnection);
		}
	}

}
