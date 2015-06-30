package jp.romerome.roplayer;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by roman on 2015/06/28.
 */
public class ListAlbumTrackAdapter extends ArrayAdapter<Track> {

	LayoutInflater mInflater;
	MediaMetadataRetriever mmr;
	Context mContext;

	public ListAlbumTrackAdapter(Context context, List item){
		super(context, 0, item);
		mInflater =  (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		mmr = new MediaMetadataRetriever();
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView,ViewGroup parent){

		Track item = getItem(position);
		ViewHolder holder;

		if(convertView==null){
			convertView = mInflater.inflate(R.layout.album_track_item, null);
			holder = new ViewHolder();
			holder.trackNumTextView = (TextView)convertView.findViewById(R.id.track_num);
			holder.artistTextView   = (TextView)convertView.findViewById(R.id.artist);
			holder.titleTextView = (TextView)convertView.findViewById(R.id.title);
			holder.durationTextView = (TextView)convertView.findViewById(R.id.duration);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.artistTextView.setText(item.artist);
		holder.trackNumTextView.setText(String.valueOf(position + 1));
		holder.titleTextView.setText(item.title);
		holder.durationTextView.setText(RoLibrary.getDuration(item));

		return convertView;
	}

	static class ViewHolder{
		TextView trackNumTextView;
		TextView  artistTextView;
		TextView titleTextView;
		TextView durationTextView;
	}
}
