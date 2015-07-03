package jp.romerome.roplayer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getSimpleName();

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawer;
    private String[] mDrawerItemTitles;
    private LinearLayout mDrawerLayout;
    private TextView mDrawerArtist;
    private TextView mDrawerAlbum;
    private TextView mDrawerTitle;
	private TextView mDrawerSetting;
	private TextView[] mDrawerItems;
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set Toolbar as ActionBar.
        // Use Toolbar in support library.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        // Set DrawerToggle.
        readyDrawerToggle(toolbar);

        // initialize drawer list.
        mDrawerItemTitles = getResources().getStringArray(R.array.menu_title);
        mDrawerLayout = (LinearLayout) findViewById(R.id.slide_menu);
		mDrawerArtist = (TextView) findViewById(R.id.artist);
		mDrawerArtist.setTag(0);
		mDrawerArtist.setOnClickListener(new DrawerItemClickListener());
		mDrawerAlbum = (TextView) findViewById(R.id.album);
		mDrawerAlbum.setTag(1);
		mDrawerAlbum.setOnClickListener(new DrawerItemClickListener());
		mDrawerTitle = (TextView) findViewById(R.id.title);
		mDrawerTitle.setTag(2);
		mDrawerTitle.setOnClickListener(new DrawerItemClickListener());
		mDrawerSetting = (TextView) findViewById(R.id.setting);
		mDrawerSetting.setTag(3);
		mDrawerSetting.setOnClickListener(new DrawerItemClickListener());
		mDrawerItems = new TextView[]{mDrawerArtist,mDrawerAlbum,mDrawerTitle,mDrawerSetting};

		PlaySmallFragment fragment = new PlaySmallFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.play_small, fragment)
                .commit();

        // Drawer listの一つ目をデフォルトで表示する場合
        selectItem(0);
    }
    private void readyDrawerToggle(Toolbar toolbar){
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawer,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_open){
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // ActionBarDrawerToggleクラス内の同メソッドでアイコンのアニメーションをしている
                // overrideするときは気を付けなさい
                super.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.d(TAG, "onDrawerOpened()");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.d(TAG, "onDrawerClosed()");
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // 表示済み、閉じ済みの状態：0
                // ドラッグ中状態：1
                // ドラッグを離したあとのアニメーション中：2
                super.onDrawerStateChanged(newState);
            }
        };

        // リスナー登録
        mDrawer.setDrawerListener(mDrawerToggle);
        // Drawerの矢印表示有り
        mDrawerToggle.setDrawerIndicatorEnabled(true);

        //
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Toolbarを使わない時と同じ
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
			int position = (int) view.getTag();
            selectItem(position);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Create a new fragment and specify the planet to show based on position
        Fragment fragment;
		Bundle args;
        switch (position){
			case 0:
				fragment = new ArtistFragment();
				break;

			case 1:
				fragment = new AlbumFragment();
				break;

            case 2:
                fragment = new TrackFragment();
                break;

			case 3:
				args = new Bundle();
				args.putInt(PlanetFragment.ARG_TITLE_NUMBER, position);
				fragment = new PlanetFragment();
				fragment.setArguments(args);
				break;

            default:
                args = new Bundle();
                args.putInt(PlanetFragment.ARG_TITLE_NUMBER,position);
                fragment = new PlanetFragment();
                fragment.setArguments(args);
        }

        // Insert the fragment by replacing any existing fragment
        // support libraryのFragmentを使用する場合は
        // getFragemtnManagerではなくgetSupportFragmentManager()でなければならない
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.body, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
		for(TextView tv : mDrawerItems){
			tv.setActivated(false);
		}
		mDrawerItems[position].setActivated(true);
        setTitle(mDrawerItemTitles[position]);
        mDrawer.closeDrawer(mDrawerLayout);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    public static class PlanetFragment extends Fragment {
        public static final String ARG_TITLE_NUMBER = "title_number";

        public PlanetFragment() {
            // Empty constructor required for fragment subclasses
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Drawerのリストと同じ順番に表示するfragmentのリソースIDを保持
            int[] fragmentId = { R.layout.fragment_section1,
                    R.layout.fragment_section2,
                    R.layout.fragment_section3,
					R.layout.fragment_section1};

            // タッチされたDrawerリストのindexを取得
            int i = getArguments().getInt(ARG_TITLE_NUMBER);

            // タッチされたDrawerリストのindexに対応するfragmentを取得
            View rootView = inflater.inflate(fragmentId[i], container, false);

            // タッチされたDrawerリストのタイトルを取得,
            String title = getResources().getStringArray(R.array.menu_title)[i];
            getActivity().setTitle(title);

            return rootView;
        }
    }
}
