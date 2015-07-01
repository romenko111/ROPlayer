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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by roman on 2015/06/30.
 */
public class PlayFragment extends Fragment implements PlayerService.StateChangeListener {

	private Track mTrack;
	private ImageView mAlbumart;
	private ImageButton mPlayButton;
	private PlayerService mService;
	private ServiceConnection mServiceConnection;
	private int mState = PlayerService.STATE_STOP;

	public PlayFragment(){

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_play, container, false);

		Bundle args = getArguments();
		long trackId = args.getLong(PlayActivity.INTENT_KEY);
		mTrack = RoLibrary.getTrack(getActivity(),trackId);

		TextView tv = (TextView) rootView.findViewById(R.id.elapsed);
		tv.setText("0:00");

		tv = (TextView) rootView.findViewById(R.id.duration);
		tv.setText(RoLibrary.getDuration(mTrack));

		mPlayButton = (ImageButton) rootView.findViewById(R.id.btn_play);
		mPlayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (mState){
					case PlayerService.STATE_PAUSE:
						mService.play();
						break;

					case PlayerService.STATE_PLAY:
						mService.pause();
						break;
				}
			}
		});

		mAlbumart = (ImageView) rootView.findViewById(R.id.album_art);
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
		initService();

		return rootView;
	}

	private void initService(){
		Intent service = new Intent(getActivity().getApplication(),PlayerService.class);
		getActivity().startService(service);

		mServiceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				PlayerService.RoBinder binder = (PlayerService.RoBinder)service;
				mService = binder.getService();
				mService.setOnStateChangeListener(PlayFragment.this);
				mService.play(mTrack);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {

			}
		};
		getActivity().bindService(service,mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStateChange(int state) {
		mState = state;
		switch (mState){
			case PlayerService.STATE_PLAY:
				mPlayButton.setBackgroundResource(R.drawable.ic_media_pause);
				break;

			case PlayerService.STATE_PAUSE:
				mPlayButton.setBackgroundResource(R.drawable.ic_media_play);
				break;
		}
	}
}
