/**
 * Copyright (C) 2013 Romain Guefveneu.
 *   
 *  This file is part of naonedbus.
 *   
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.naonedbus.activity;

import net.naonedbus.BuildConfig;
import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.helper.SlidingMenuHelper;
import net.naonedbus.intent.IIntentParamKey;
import net.simonvt.menudrawer.MenuDrawer;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public abstract class SlidingMenuActivity extends SherlockFragmentActivity implements TabListener {

	private static final String LOG_TAG = "SlidingMenuActivity";
	private static final boolean DBG = BuildConfig.DEBUG;

	/**
	 * Default delay from {@link #peekDrawer()} is called until first animation
	 * is run.
	 */
	private static final long DEFAULT_PEEK_START_DELAY = 1000;

	/**
	 * Default delay between each subsequent animation, after
	 * {@link #peekDrawer()} has been called.
	 */
	private static final long DEFAULT_PEEK_DELAY = 8000;

	private static String BUNDLE_TABS_CURRENT = "tabsCurrent";
	private static String BUNDLE_TABS_TITLES = "tabsTitles";
	private static String BUNDLE_TABS_CLASSES = "tabsClasses";

	/** Layout de l'activitée courante. */
	private final int mLayoutId;
	/** Sert à la détection du changement de thème. */
	private final int mCurrentTheme = NBApplication.THEME;

	/** Titres des fragments. */
	private int[] mTitles;
	/** Classes des fragments */
	private String[] mClasses;
	/** Bundles des fragments. */
	private Bundle[] mBundles;
	/** Fragments tags. */
	private String[] mFragmentsTags;

	/** Gestion du menu latéral. */
	private MenuDrawer mMenuDrawer;
	/** Gestion du menu latéral. */
	private SlidingMenuHelper mSlidingMenuHelper;

	/** The {@link ViewPager} that will host the section contents. */
	private ViewPager mViewPager;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;

	public SlidingMenuActivity(final int layoutId) {
		this.mLayoutId = layoutId;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		if (DBG)
			Log.d(LOG_TAG, "onCreate");

		setTheme(NBApplication.THEMES_MENU_RES[NBApplication.THEME]);
		getWindow().setBackgroundDrawable(null);

		super.onCreate(savedInstanceState);

		setContentView(mLayoutId);

		mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);

		mSlidingMenuHelper = new SlidingMenuHelper(this);
		mSlidingMenuHelper.setupActionBar(getSupportActionBar());
		mSlidingMenuHelper.setupSlidingMenu(mMenuDrawer);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(final int position) {
				getSupportActionBar().setSelectedNavigationItem(position);
			}
		});
	}

	@Override
	public void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mSlidingMenuHelper.onPostCreate(getIntent(), mMenuDrawer, savedInstanceState);
	}

	/**
	 * Show the menu when home icon is clicked.
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			mMenuDrawer.toggleMenu();
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
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(BUNDLE_TABS_CURRENT, getSupportActionBar().getSelectedNavigationIndex());
		outState.putIntArray(BUNDLE_TABS_TITLES, mTitles);
		outState.putStringArray(BUNDLE_TABS_CLASSES, mClasses);
		mSlidingMenuHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		if (savedInstanceState.containsKey(BUNDLE_TABS_TITLES)) {
			final int[] titles = savedInstanceState.getIntArray(BUNDLE_TABS_TITLES);
			final String[] classes = savedInstanceState.getStringArray(BUNDLE_TABS_CLASSES);
			final int selectedPosition = savedInstanceState.getInt(BUNDLE_TABS_CURRENT);

			addFragments(titles, classes);

			getSupportActionBar().setSelectedNavigationItem(selectedPosition > -1 ? selectedPosition : 0);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onWindowFocusChanged(final boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		// Gérer le changement de thème;
		if (hasFocus && (mCurrentTheme != NBApplication.THEME)) {
			final Intent intent = new Intent(this, this.getClass());
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			overridePendingTransition(0, 0);
			startActivity(intent);
			finish();
		}

		mSlidingMenuHelper.onWindowFocusChanged(hasFocus, mMenuDrawer);
	}

	/**
	 * Animates the sliding menu slightly open.
	 */
	protected void peekSlidingMenu() {
		mMenuDrawer.peekDrawer(DEFAULT_PEEK_START_DELAY, DEFAULT_PEEK_DELAY);
	}

	/**
	 * Show the menu when menu button pressed, hide it when back is pressed
	 */
	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU || (mMenuDrawer.isMenuVisible() && keyCode == KeyEvent.KEYCODE_BACK)) {
			mMenuDrawer.toggleMenu();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onTabSelected(final Tab tab, final FragmentTransaction ft) {
		if (DBG)
			Log.d(LOG_TAG, "onTabSelected " + tab.getPosition());

		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {
		if (DBG)
			Log.d(LOG_TAG, "onTabUnselected " + tab.getPosition());
	}

	@Override
	public void onTabReselected(final Tab tab, final FragmentTransaction ft) {
		if (DBG)
			Log.d(LOG_TAG, "onTabReselected " + tab.getPosition());
	}

	protected void addFragments(final int[] titles, final String[] classes) {
		if (DBG)
			Log.d(LOG_TAG, "addFragments " + titles.length);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mClasses = classes;
		mTitles = titles;
		mFragmentsTags = new String[classes.length];

		mViewPager.setOffscreenPageLimit(classes.length);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		for (int i = 0; i < titles.length; i++) {
			actionBar.addTab(actionBar.newTab().setText(titles[i]).setTabListener(this));
		}
	}

	/**
	 * Ajouter les informations de fragments.
	 * 
	 * @param titles
	 *            Les titres (ressources).
	 * @param classes
	 *            Les classes des fragments.
	 */
	protected void addFragments(final int[] titles, final Class<?>[] classes) {
		mClasses = new String[classes.length];
		for (int i = 0; i < classes.length; i++) {
			mClasses[i] = classes[i].getName();
		}
		addFragments(titles, mClasses);
	}

	protected void addDelayedFragments(final int[] titles, final Class<?>[] classes) {
		mClasses = new String[classes.length];
		for (int i = 0; i < classes.length; i++) {
			mClasses[i] = classes[i].getName();
		}
		mTitles = titles;
	}

	protected void loadDelayedFragments() {
		addFragments(mTitles, mClasses);
	}

	protected void setSelectedTab(final int position) {
		if (DBG)
			Log.d(LOG_TAG, "setSelectedTab " + position);
		getSupportActionBar().setSelectedNavigationItem(position);
	}

	/**
	 * Get the current Fragment.
	 * 
	 * @return the current Fragment, or <code>null</code> if we can't find it.
	 */
	private Fragment getCurrentFragment() {
		final Tab tab = getSupportActionBar().getSelectedTab();
		if (tab != null) {
			return getSupportFragmentManager().findFragmentByTag(mFragmentsTags[tab.getPosition()]);
		}
		return null;
	}

	/**
	 * Renvoyer la valeur du paramètre de l'intent
	 * 
	 * @param key
	 * @return
	 */
	protected Object getParamValue(final IIntentParamKey key) {
		return getIntent().getSerializableExtra(key.toString());
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private static final String LOG_TAG = SlidingMenuActivity.LOG_TAG + "$SectionsPagerAdapter";

		public SectionsPagerAdapter(final FragmentManager fm) {
			super(fm);
		}

		@Override
		public Object instantiateItem(final ViewGroup container, final int position) {
			if (DBG)
				Log.d(LOG_TAG, "instantiateItem " + position);

			final Fragment fragment = (Fragment) super.instantiateItem(container, position);
			fragment.setRetainInstance(true);
			mFragmentsTags[position] = fragment.getTag();
			return fragment;
		}

		@Override
		public Fragment getItem(final int position) {
			if (DBG)
				Log.d(LOG_TAG, "getItem " + position);
			final Fragment fragment = Fragment.instantiate(SlidingMenuActivity.this, mClasses[position]);
			fragment.setRetainInstance(true);
			return fragment;
		}

		@Override
		public int getCount() {
			return mClasses.length;
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			if (DBG)
				Log.d(LOG_TAG, "getPageTitle " + position);

			return getString(mTitles[position]);
		}

		public String makeFragmentName(final int viewId, final long id) {
			return "android:switcher:" + viewId + ":" + id;
		}
	}

}
