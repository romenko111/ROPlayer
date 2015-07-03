package jp.romerome.roplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


public class AlbumTrackActivity extends AppCompatActivity{

	public static final String INTENT_KEY = "ALBUM_ID";
	private Album mAlbum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_toolbar);
		Intent intent = getIntent();
		mAlbum = RoLibrary.getAlbum(this,intent.getLongExtra(INTENT_KEY,-1));

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setLogo(R.mipmap.ic_launcher);
		toolbar.setTitle(mAlbum.album);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		Fragment fragment = new AlbumTrackFragment();
		Bundle args = new Bundle();
		args.putLong(INTENT_KEY, mAlbum.id);
		fragment.setArguments(args);
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.body, fragment)
				.commit();

		fragment = new PlaySmallFragment();
		fragmentManager.beginTransaction()
				.replace(R.id.play_small, fragment)
				.commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
			case android.R.id.home:
				onBackPressed();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
