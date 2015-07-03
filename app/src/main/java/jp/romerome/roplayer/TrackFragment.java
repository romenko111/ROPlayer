package jp.romerome.roplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        return rootView;

    }
}
