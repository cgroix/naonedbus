package net.naonedbus.helper;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.AboutActivity;
import net.naonedbus.activity.impl.EquipementsActivity;
import net.naonedbus.activity.impl.InfosTraficActivity;
import net.naonedbus.activity.impl.MainActivity;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.ParkingsActivity;
import net.naonedbus.activity.impl.SearchActivity;
import net.naonedbus.activity.impl.SettingsActivity;
import net.naonedbus.widget.adapter.impl.MainMenuAdapter;
import net.naonedbus.widget.indexer.impl.MainMenuIndexer;
import net.naonedbus.widget.item.impl.LinkMainMenuItem;
import net.naonedbus.widget.item.impl.MainMenuItem;
import net.simonvt.menudrawer.MenuDrawer;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;

public class SlidingMenuHelper {

	private static MainMenuAdapter sAdapter;
	private static int sSavedPosition = -1;
	private static int sSavedListTop;

	/** Menu général. */
	private ListView mMenuListView;

	/**
	 * Contenu du menu général.
	 */
	private static final List<MainMenuItem> MENU_ITEMS;
	static {
		// @formatter:off
		MENU_ITEMS = new ArrayList<MainMenuItem>();
		MENU_ITEMS.add(new MainMenuItem(R.string.menu_accueil, MainActivity.class, R.drawable.ic_action_view_as_grid, 0));
		MENU_ITEMS.add(new MainMenuItem(R.string.menu_info_trafic, InfosTraficActivity.class,R.drawable.ic_action_warning, 0));
//		menuItems.add(new MainMenuItem(R.string.menu_itineraires, ItineraireActivity.class, R.drawable.ic_action_direction, 0));
		MENU_ITEMS.add(new MainMenuItem(R.string.menu_parkings, ParkingsActivity.class, R.drawable.ic_action_parking, 0));
		MENU_ITEMS.add(new MainMenuItem(R.string.menu_equipements, EquipementsActivity.class, R.drawable.ic_action_place, 0));
		MENU_ITEMS.add(new MainMenuItem(R.string.menu_recherche, SearchActivity.class, R.drawable.ic_action_search, 0));
		MENU_ITEMS.add(new MainMenuItem(R.string.menu_carte, MapActivity.class, R.drawable.ic_action_map, 0));
		MENU_ITEMS.add(new MainMenuItem(R.string.menu_parametres, SettingsActivity.class, R.drawable.ic_action_settings, 1));
		MENU_ITEMS.add(new MainMenuItem(R.string.menu_about, AboutActivity.class, R.drawable.ic_action_info, 1));
		MENU_ITEMS.add(new LinkMainMenuItem(R.string.menu_don, "http://t.co/4uKK33eu", R.drawable.ic_action_favourite, 1));
		// @formatter:on
	}

	private Activity mActivity;

	public SlidingMenuHelper(final Activity activity) {
		mActivity = activity;
	}

	public void onPostCreate(final Intent intent, final MenuDrawer slidingMenu, final Bundle savedInstanceState) {
		if (intent.getBooleanExtra("fromMenu", false)
				&& (savedInstanceState == null || !savedInstanceState.containsKey("menuConsumed"))) {
			// Afficher le menu au démarrage, pour la transition.
			slidingMenu.openMenu(false);

			// // Masquer le menu.
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					slidingMenu.closeMenu(true);
				}
			}, 400);

			// Consommer l'affichage du menu pour ne pas réafficher en cas de
			// rotation.
			intent.putExtra("fromMenu", false);
		}
	}

	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("menuConsumed", true);
	}

	public void onWindowFocusChanged(boolean hasFocus, final MenuDrawer slidingMenu) {
		// Gérer le masquage de menu
		if (hasFocus == false && slidingMenu.isMenuVisible()) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					slidingMenu.closeMenu(false);
				}
			}, 800);
		}

		if (hasFocus) {
			// Nouvelle activité
			sAdapter.setCurrentClass(mActivity.getClass());
			sAdapter.notifyDataSetChanged();
		}

	}

	public void setupSlidingMenu(final MenuDrawer slidingMenu) {
		slidingMenu.setMenuView(R.layout.menu);
		slidingMenu.setDropShadow(R.drawable.shadow);

		mMenuListView = (ListView) slidingMenu.findViewById(android.R.id.list);
		if (sAdapter == null) {
			sAdapter = new MainMenuAdapter(mActivity);
			for (MainMenuItem item : MENU_ITEMS) {
				sAdapter.add(item);
			}
			final MainMenuIndexer indexer = new MainMenuIndexer();
			indexer.buildIndex(mActivity, sAdapter);
			sAdapter.setIndexer(indexer);
		}

		sAdapter.setCurrentClass(mActivity.getClass());
		mMenuListView.setAdapter(sAdapter);

		if (sSavedPosition >= 0) { // initialized to -1
			mMenuListView.setSelectionFromTop(sSavedPosition, sSavedListTop);
		}

		mMenuListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Ne pas permettre à l'utilisateur de cliquer n'importe où...
				mMenuListView.setClickable(false);

				// Sauvegarder l'état de la listview
				sSavedPosition = mMenuListView.getFirstVisiblePosition();
				final View firstVisibleView = mMenuListView.getChildAt(0);
				sSavedListTop = (firstVisibleView == null) ? 0 : firstVisibleView.getTop();

				final MainMenuItem item = (MainMenuItem) mMenuListView.getItemAtPosition(position);

				if (item instanceof LinkMainMenuItem) {

					final LinkMainMenuItem linkItem = (LinkMainMenuItem) item;
					final Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(linkItem.getUrl()));
					mActivity.startActivity(intent);

				} else {

					if (mActivity.getClass().equals(item.getIntentClass())) {
						// Même activité
						slidingMenu.closeMenu();
						mMenuListView.setClickable(true);
					} else {
						// Nouvelle activité
						sAdapter.setCurrentClass(item.getIntentClass());
						sAdapter.notifyDataSetChanged();

						final Intent intent = new Intent(mActivity, item.getIntentClass());
						intent.putExtra("fromMenu", true);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						mActivity.startActivity(intent);
						mActivity.overridePendingTransition(0, 0);
					}

				}

			}
		});

	}

	public void setupActionBar(final ActionBar actionBar) {
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
}
