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

import java.util.List;

/**
 * Created by roman on 2015/06/28.
 */
public class AlbumFragment extends Fragment{

	public AlbumFragment(){

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_list, container, false);
		String title = getResources().getString(R.string.album);
		getActivity().setTitle(title);

		ListView listView = (ListView) rootView.findViewById(R.id.listview);
		List albums = RoLibrary.getAlbums(getActivity());
		ListAlbumAdapter adapter = new ListAlbumAdapter(getActivity(), albums);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Album album = RoLibrary.getAlbums(getActivity()).get(position);
				Intent intent =  new Intent(getActivity(),AlbumTrackActivity.class);
				intent.putExtra(AlbumTrackActivity.INTENT_KEY,album.id);
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
