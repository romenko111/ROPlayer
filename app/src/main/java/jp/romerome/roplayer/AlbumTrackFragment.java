package jp.romerome.roplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by roman on 2015/06/28.
 */
public class AlbumTrackFragment extends Fragment {

	private Album mAlbum;

	public AlbumTrackFragment(){

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_album_track, container, false);

		ListView listView = (ListView) rootView.findViewById(R.id.listview);
		Bundle args = getArguments();
		long albumId = args.getLong(AlbumTrackActivity.INTENT_KEY);
		mAlbum = RoLibrary.getAlbum(albumId);
		ArrayList<Track> tracks = RoLibrary.getTracksInAlbum(albumId);
		ListAlbumTrackAdapter adapter = new ListAlbumTrackAdapter(getActivity(), tracks);
		listView.setAdapter(adapter);

		ImageView albumArt = (ImageView) rootView.findViewById(R.id.album_art);
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
		albumArt.setImageBitmap(bitmap);

		TextView albumView = (TextView) rootView.findViewById(R.id.album);
		albumView.setText(mAlbum.album);

		TextView artistView = (TextView) rootView.findViewById(R.id.artist);
		artistView.setText(mAlbum.artist);

		return rootView;

	}

}
