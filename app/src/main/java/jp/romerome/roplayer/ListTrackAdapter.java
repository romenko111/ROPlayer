package jp.romerome.roplayer;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ListTrackAdapter extends ArrayAdapter<Track> {

    LayoutInflater mInflater;
	MediaMetadataRetriever mmr;
	Context mContext;

    public ListTrackAdapter(Context context, List item){
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
            convertView = mInflater.inflate(R.layout.track_item, null);
            holder = new ViewHolder();
            holder.trackTextView    = (TextView)convertView.findViewById(R.id.title);
            holder.artistTextView   = (TextView)convertView.findViewById(R.id.artist);
            holder.durationTextView = (TextView)convertView.findViewById(R.id.duration);
            holder.albumartView = (ImageView) convertView.findViewById(R.id.album_art);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.artistTextView.setText(item.artist);
        holder.trackTextView.setText(item.title);
        holder.durationTextView.setText(RoLibrary.getStringTime(item));
		holder.albumartView.setImageBitmap(null);
		holder.albumartView.setTag(item.path);
		ImageGetTask task = new ImageGetTask(mContext, holder.albumartView);
		task.execute(item);

        return convertView;
    }

    static class ViewHolder{
        TextView  trackTextView;
        TextView  artistTextView;
        TextView  durationTextView;
        ImageView albumartView;
    }

}
