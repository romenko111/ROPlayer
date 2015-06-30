package jp.romerome.roplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

/**
 * Created by roman on 2015/06/28.
 */
public class ArtistFragment extends Fragment{

	public ArtistFragment(){

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_list, container, false);
		String title = getResources().getString(R.string.artist);
		getActivity().setTitle(title);

		ListView listView = (ListView) rootView.findViewById(R.id.listview);
		List artists = RoLibrary.getArtists();
		ListArtistAdapter adapter = new ListArtistAdapter(getActivity(), artists);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Artist artist = RoLibrary.getArtists().get(position);
				Intent intent = new Intent(getActivity(),ArtistAlbumActivity.class);
				intent.putExtra(ArtistAlbumActivity.INTENT_KEY,artist.id);
				startActivity(intent);
			}
		});

		return rootView;

	}
}
