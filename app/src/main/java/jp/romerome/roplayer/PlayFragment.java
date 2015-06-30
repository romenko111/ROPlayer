package jp.romerome.roplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by roman on 2015/06/30.
 */
public class PlayFragment extends Fragment{

	private Track mTrack;
	private ImageView mAlbumart;

	public PlayFragment(){

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_play, container, false);

		Bundle args = getArguments();
		long trackId = args.getLong(PlayActivity.INTENT_KEY);
		mTrack = RoLibrary.getTrack(trackId);

		TextView tv = (TextView) rootView.findViewById(R.id.artist);
		tv.setText(mTrack.artist);

		tv = (TextView) rootView.findViewById(R.id.title);
		tv.setText(mTrack.title);

		tv = (TextView) rootView.findViewById(R.id.album);
		tv.setText(mTrack.album);

		tv = (TextView) rootView.findViewById(R.id.elapsed);
		tv.setText("0:00");

		tv = (TextView) rootView.findViewById(R.id.duration);
		tv.setText(RoLibrary.getDuration(mTrack));

		mAlbumart = (ImageView) rootView.findViewById(R.id.album_art);
		mAlbumart.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mAlbumart.getWidth(),mAlbumart.getHeight());
				params.addRule(RelativeLayout.BELOW,R.id.info);
				mAlbumart.setLayoutParams(params);
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
				}
				catch (Exception e){
					bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.ic_launcher);
				}
				mAlbumart.setImageBitmap(bitmap);
			}
		});

		return rootView;

	}

}
