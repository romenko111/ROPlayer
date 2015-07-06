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
import android.widget.ImageView;
import android.widget.ListView;

import java.util.List;

public class TrackFragment extends Fragment {

    public TrackFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_list,container,false);
        String title = getResources().getString(R.string.title);
        getActivity().setTitle(title);

        ListView listView = (ListView) rootView.findViewById(R.id.listview);
        List tracks = RoLibrary.getTracks(getActivity());
        ListTrackAdapter adapter = new ListTrackAdapter(getActivity(), tracks);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Track track = (Track)parent.getItemAtPosition(position);
				RoLibrary.setCurrentPlaylist(getActivity(), RoLibrary.getTracks(getActivity()),RoLibrary.PLAYWITH_TRACK);
				RoLibrary.setNo(getActivity(), position + 1);

				ImageView albumart = (ImageView) view.findViewById(R.id.album_art);
				Intent intent = new Intent(getActivity(), PlayActivity.class);
				String transitionName = getString(R.string.album_art);
				ActivityOptionsCompat options =
						ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
								albumart,   // 遷移がはじまるビュー
								transitionName    // 遷移先のビューの transitionName
						);
				ActivityCompat.startActivity(getActivity(), intent, options.toBundle());

				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(PlayerService.ACTION_NEW_PLAY);
				getActivity().sendBroadcast(broadcastIntent);
			}
		});

        return rootView;

    }
}
