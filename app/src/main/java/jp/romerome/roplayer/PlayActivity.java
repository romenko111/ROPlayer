package jp.romerome.roplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by roman on 2015/06/30.
 */
public class PlayActivity extends AppCompatActivity {

	public static final String INTENT_KEY = "TRACK_ID";
	private Track mTrack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plane);
		Intent intent = getIntent();
		mTrack = RoLibrary.getTrack(intent.getLongExtra(INTENT_KEY,-1));

		Fragment fragment = new PlayFragment();
		Bundle args = new Bundle();
		args.putLong(INTENT_KEY, mTrack.id);
		fragment.setArguments(args);
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.body, fragment)
				.commit();
	}

}
