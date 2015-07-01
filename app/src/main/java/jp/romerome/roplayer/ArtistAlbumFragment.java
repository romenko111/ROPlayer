package jp.romerome.roplayer;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class ArtistAlbumFragment extends Fragment{

	private ArrayList<Album> mAlbums;

	public ArtistAlbumFragment(){

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_list, container, false);

		ListView listView = (ListView) rootView.findViewById(R.id.listview);
		Bundle args = getArguments();
		long artistId = args.getLong(ArtistAlbumActivity.INTENT_KEY);
		mAlbums = RoLibrary.getAlbumsInArtists(getActivity(),artistId);
		ListAlbumAdapter adapter = new ListAlbumAdapter(getActivity(), mAlbums);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Album album = mAlbums.get(position);
				Intent intent = new Intent(getActivity(), AlbumTrackActivity.class);
				intent.putExtra(AlbumTrackActivity.INTENT_KEY, album.id);
				String transitionName = getString(R.string.album_art);
				ActivityOptionsCompat options =
						ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
								view.findViewById(R.id.album_art),   // 遷移がはじまるビュー
								transitionName    // 遷移先のビューの transitionName
						);
				ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
			}
		});

		return rootView;

	}

}
