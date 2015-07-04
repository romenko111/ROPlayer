package jp.romerome.roplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by roman on 2015/06/28.
 */
public class AlbumTrackFragment extends Fragment {

	private Album mAlbum;
	private ImageView mAlbumart;

	public AlbumTrackFragment(){

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_album_track, container, false);

		ListView listView = (ListView) rootView.findViewById(R.id.listview);
		Bundle args = getArguments();
		long albumId = args.getLong(AlbumTrackActivity.INTENT_KEY);
		mAlbum = RoLibrary.getAlbum(getActivity(),albumId);
		ArrayList<Track> tracks = RoLibrary.getTracksInAlbum(getActivity(),albumId);
		ListAlbumTrackAdapter adapter = new ListAlbumTrackAdapter(getActivity(), tracks);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Track track = (Track) parent.getItemAtPosition(position);
				RoLibrary.setCurrentPlaylist(getActivity(), track.albumId);
				RoLibrary.setNo(getActivity(), position + 1);

				Intent intent = new Intent(getActivity(), PlayActivity.class);
				String transitionName = getString(R.string.album_art);
				ActivityOptionsCompat options =
						ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
								mAlbumart,   // 遷移がはじまるビュー
								transitionName    // 遷移先のビューの transitionName
						);
				ActivityCompat.startActivity(getActivity(), intent, options.toBundle());

				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(PlayerService.ACTION_NEW_PLAY);
				getActivity().sendBroadcast(broadcastIntent);
			}
		});

		mAlbumart = (ImageView) rootView.findViewById(R.id.album_art);
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		Bitmap bitmap;
		try {
			mmr.setDataSource(tracks.get(0).path);
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

		TextView albumView = (TextView) rootView.findViewById(R.id.album);
		albumView.setText(mAlbum.album);

		TextView artistView = (TextView) rootView.findViewById(R.id.artist);
		artistView.setText(mAlbum.artist);

		return rootView;

	}

}
