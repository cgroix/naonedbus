package net.naonedbus.activity;

import java.util.ArrayList;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.helper.SlidingMenuHelper;
import net.naonedbus.intent.IIntentParamKey;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public abstract class SimpleFragmentActivity extends SherlockFragmentActivity {

	private static String BUNDLE_TABS_CURRENT = "tabsCurrent";

	private int mLayoutId;
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	/** Sert à la détection du changement de thème. */
	private int mCurrentTheme = NBApplication.THEME;
	/** Gestion du menu latéral. */
	private SlidingMenuHelper mSlidingMenuHelper;

	public SimpleFragmentActivity(int layoutId) {
		this.mLayoutId = layoutId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(NBApplication.THEME);
		super.onCreate(savedInstanceState);
		setContentView(mLayoutId);

		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mTabsAdapter = new TabsAdapter(this, getSupportActionBar(), mViewPager);

		mSlidingMenuHelper = new SlidingMenuHelper(this);
		mSlidingMenuHelper.setupActionBar(getSupportActionBar());
		getSupportActionBar().setIcon(R.drawable.ic_launcher);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final Fragment fragment = getCurrentFragment();

		if (fragment instanceof CustomFragmentActions) {
			final CustomFragmentActions customListFragment = (CustomFragmentActions) fragment;
			customListFragment.onCreateOptionsMenu(menu);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final Fragment fragment = getCurrentFragment();

		if (fragment instanceof CustomFragmentActions) {
			final CustomFragmentActions customListFragment = (CustomFragmentActions) fragment;
			customListFragment.onPrepareOptionsMenu(menu);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Show the menu when home icon is clicked.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			final Fragment fragment = getCurrentFragment();
			if (fragment instanceof CustomFragmentActions) {
				final CustomFragmentActions customListFragment = (CustomFragmentActions) fragment;
				return customListFragment.onOptionsItemSelected(item);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(BUNDLE_TABS_CURRENT, getSupportActionBar().getSelectedNavigationIndex());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.containsKey(BUNDLE_TABS_CURRENT)) {
			final int selectedPosition = savedInstanceState.getInt(BUNDLE_TABS_CURRENT);
			getSupportActionBar().setSelectedNavigationItem(selectedPosition);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		// Gérer le changement de thème;
		if (hasFocus && (mCurrentTheme != NBApplication.THEME)) {
			final Intent intent = new Intent(this, this.getClass());
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			overridePendingTransition(0, 0);
			startActivity(intent);
			finish();
		}
	}

	/**
	 * Ajouter les information de fragments.
	 * 
	 * @param titles
	 *            Les titres (ressources).
	 * @param classes
	 *            Les classes des fragments.
	 * @param bundles
	 *            Les bundles des fragments.
	 */
	protected void addFragments(int[] titles, Class<?>[] classes, Bundle[] bundles) {
		final ActionBar actionBar = getSupportActionBar();
		final FragmentManager fragmentManager = getSupportFragmentManager();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// final FragmentTransaction transaction =
		// fragmentManager.beginTransaction();
		for (int i = 0; i < titles.length; i++) {
			final Fragment fragment = Fragment.instantiate(this, classes[i].getName(), bundles[i]);
			// transaction.add(fragment, classes[i].getName());

			mTabsAdapter.addTab(actionBar.newTab().setText(titles[i]), classes[i], bundles[i]);
		}
		// transaction.commit();
		// fragmentManager.executePendingTransactions();

	}

	/**
	 * Ajouter les information de fragments.
	 * 
	 * @param titles
	 *            Les titres (ressources).
	 * @param classes
	 *            Les classes des fragments.
	 */
	protected void addFragments(int[] titles, Class<?>[] classes) {
		final ActionBar actionBar = getSupportActionBar();
		final FragmentManager fragmentManager = getSupportFragmentManager();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// final FragmentTransaction transaction =
		// fragmentManager.beginTransaction();
		for (int i = 0; i < titles.length; i++) {
			final Fragment fragment = Fragment.instantiate(this, classes[i].getName(), null);
			// transaction.add(fragment, classes[i].getName());

			mTabsAdapter.addTab(actionBar.newTab().setText(titles[i]), classes[i], null);
		}
		// transaction.commit();
		// fragmentManager.executePendingTransactions();
	}

	/**
	 * Get the current Fragment.
	 * 
	 * @return the current Fragment, or <code>null</code> if we can't find it.
	 */
	private Fragment getCurrentFragment() {
		final Tab tab = getSupportActionBar().getSelectedTab();
		if (tab != null) {
			return getSupportFragmentManager().findFragmentByTag(tab.getTag().toString());
		}
		return null;
	}

	public static class TabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener,
			ViewPager.OnPageChangeListener {

		private final FragmentActivity mActivity;
		private final ViewPager mViewPager;
		private final ActionBar mActionBar;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		static final class TabInfo {
			private final Object tag;
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(Object _tag, Class<?> _class, Bundle _args) {
				tag = _tag;
				clss = _class;
				args = _args;
			}
		}

		public TabsAdapter(FragmentActivity activity, ActionBar actionBar, ViewPager pager) {
			super(activity.getSupportFragmentManager());
			mActivity = activity;
			mActionBar = actionBar;
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
			final Object tag = tab.getText().toString();
			final TabInfo info = new TabInfo(tag, clss, args);

			tab.setTabListener(this);
			tab.setTag(tag);

			mTabs.add(info);
			mActionBar.addTab(tab);

			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int position) {
			TabInfo info = mTabs.get(position);
			return Fragment.instantiate(mActivity, info.clss.getName(), info.args);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int position) {
			mActionBar.setSelectedNavigationItem(position);
			mActivity.invalidateOptionsMenu();
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			mViewPager.setCurrentItem(tab.getPosition());
			// mActivity.invalidateOptionsMenu();
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {

		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {

		}
	}

	/**
	 * Renvoyer la valeur du paramètre de l'intent
	 * 
	 * @param key
	 * @return
	 */
	protected Object getParamValue(IIntentParamKey key) {
		return getIntent().getSerializableExtra(key.toString());
	}

	protected SlidingMenuHelper getSlidingMenuHelper() {
		return mSlidingMenuHelper;
	}

}
