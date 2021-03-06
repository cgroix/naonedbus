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
package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.ArretDetailActivity;
import net.naonedbus.activity.impl.ArretsActivity.OnChangeSens;
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.PlanActivity;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.comparator.ArretComparator;
import net.naonedbus.comparator.ArretOrdreComparator;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.helper.StateHelper;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.manager.impl.FavoriManager;
import net.naonedbus.provider.impl.NaoLocationManager;
import net.naonedbus.provider.impl.NaoLocationManager.NaoLocationListener;
import net.naonedbus.utils.InfoDialogUtils;
import net.naonedbus.widget.adapter.impl.ArretArrayAdapter;
import net.naonedbus.widget.adapter.impl.ArretArrayAdapter.ViewType;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ArretsFragment extends CustomListFragment implements OnChangeSens, NaoLocationListener {

	public static final String PARAM_LIGNE = "ligne";

	private final static int SORT_NOM = 0;
	private final static int SORT_ORDRE = 1;
	private final static int FILTER_ALL = 2;
	private final static int FILTER_FAVORIS = 3;
	private final static SparseIntArray MENU_MAPPING = new SparseIntArray();
	static {
		MENU_MAPPING.append(SORT_NOM, R.id.menu_sort_name);
		MENU_MAPPING.append(SORT_ORDRE, R.id.menu_sort_ordre);
		MENU_MAPPING.append(FILTER_ALL, R.id.menu_filter_all);
		MENU_MAPPING.append(FILTER_FAVORIS, R.id.menu_filter_favoris);
	}

	private interface DistanceTaskCallback {
		void onNearestStationFound(Integer position);

		void onPostExecute();
	}

	protected final SparseArray<Comparator<Arret>> mComparators;
	protected int mCurrentSort;

	private final FavoriManager mFavoriManager;
	private StateHelper mStateHelper;
	private final NaoLocationManager mLocationProvider;
	private DistanceTask mDistanceTask;
	private DistanceTaskCallback mDistanceTaskCallback;
	private Integer mNearestArretPosition;
	private ArretArrayAdapter mAdapter;
	private int mCurrentFilter = FILTER_ALL;

	private int mDefaultDividerHeight;

	private List<Arret> mArrets;

	private Ligne mLigne;
	private Sens mSens;

	public ArretsFragment() {
		super(R.layout.fragment_listview);
		mLocationProvider = NBApplication.getLocationProvider();
		mLocationProvider.addListener(this);

		mComparators = new SparseArray<Comparator<Arret>>();
		mComparators.append(SORT_NOM, new ArretComparator());
		mComparators.append(SORT_ORDRE, new ArretOrdreComparator());

		mFavoriManager = FavoriManager.getInstance();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		registerForContextMenu(getListView());

		mDefaultDividerHeight = getListView().getDividerHeight();

		mLigne = getArguments().getParcelable(PARAM_LIGNE);

		mStateHelper = new StateHelper(getActivity());
		mCurrentSort = mStateHelper.getSortType(this, SORT_NOM);
		setCurrentFilter(mStateHelper.getFilterType(this, FILTER_ALL));

		mArrets = new ArrayList<Arret>();
		mAdapter = new ArretArrayAdapter(getActivity(), mArrets);
		mAdapter.setRouteColor(mLigne.getCouleur());
		if (mCurrentSort == SORT_ORDRE) {
			mAdapter.setViewType(ViewType.TYPE_METRO);
			getListView().setDividerHeight(0);
		}

		mDistanceTaskCallback = new DistanceTaskCallback() {
			@Override
			public void onNearestStationFound(final Integer position) {
				mNearestArretPosition = position;
				final ArretArrayAdapter adapter = (ArretArrayAdapter) getListAdapter();
				if (adapter != null && mNearestArretPosition != null) {
					adapter.setNearestPosition(mNearestArretPosition);
					adapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onPostExecute() {
				final ArretArrayAdapter adapter = (ArretArrayAdapter) getListAdapter();
				if (adapter != null) {
					adapter.notifyDataSetChanged();
				}
			}
		};
	}

	@Override
	public void onStart() {
		super.onStart();
		mLocationProvider.addListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();

		// Save state
		mStateHelper.setSortType(this, mCurrentSort);
		mStateHelper.setFilterType(this, mCurrentFilter);

		mLocationProvider.removeListener(this);
		if (mDistanceTask != null) {
			mDistanceTask.cancel(true);
		}
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_arrets, menu);
		menu.findItem(MENU_MAPPING.get(mCurrentSort)).setChecked(true);
		menu.findItem(MENU_MAPPING.get(mCurrentFilter)).setChecked(true);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {
		menu.findItem(R.id.menu_location).setVisible(mLocationProvider.isEnabled());
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_sort_name:
			item.setChecked(true);
			changeSortOrder(SORT_NOM, ViewType.TYPE_STANDARD);
			break;
		case R.id.menu_sort_ordre:
			item.setChecked(true);
			changeSortOrder(SORT_ORDRE, ViewType.TYPE_METRO);
			break;
		case R.id.menu_filter_all:
			if (mCurrentFilter != FILTER_ALL) {
				item.setChecked(true);
				setCurrentFilter(FILTER_ALL);
				refreshContent();
			}
			break;
		case R.id.menu_filter_favoris:
			if (mCurrentFilter != FILTER_FAVORIS) {
				item.setChecked(true);
				setCurrentFilter(FILTER_FAVORIS);
				refreshContent();
			}
			break;
		case R.id.menu_show_plan:
			menuShowPlan();
			break;
		case R.id.menu_location:
			menuLocation();
			break;
		case R.id.menu_comment:
			menuComment();
			break;
		}
		return false;
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
		final Arret arret = (Arret) getListView().getItemAtPosition(cmi.position);

		final android.view.MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.fragment_arrets_contextual, menu);

		menu.setHeaderTitle(arret.getNomArret());

		final android.view.MenuItem menuFavori = menu.findItem(R.id.menu_favori);
		if (mFavoriManager.isFavori(getActivity().getContentResolver(), arret.getId())) {
			menuFavori.setTitle(R.string.action_favori_remove);
		} else {
			menuFavori.setTitle(R.string.action_favori_add);
		}
	}

	@Override
	public boolean onContextItemSelected(final android.view.MenuItem item) {
		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final Arret arret = (Arret) getListView().getItemAtPosition(cmi.position);

		switch (item.getItemId()) {
		case R.id.menu_show_plan:
			menuShowMap(arret);
			break;
		case R.id.menu_favori:
			if (mFavoriManager.isFavori(getActivity().getContentResolver(), arret.getId())) {
				removeFromFavoris(arret);
			} else {
				addToFavoris(arret);
			}
			break;
		case R.id.menu_comment:
			menuComment(arret);
			break;
		default:
			break;
		}

		return true;
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		super.onListItemClick(l, v, position, id);
		final Arret arret = (Arret) l.getItemAtPosition(position);

		final Intent intent = new Intent(getActivity(), ArretDetailActivity.class);
		intent.putExtra(ArretDetailActivity.PARAM_LIGNE, mLigne);
		intent.putExtra(ArretDetailActivity.PARAM_SENS, mSens);
		intent.putExtra(ArretDetailActivity.PARAM_ARRET, arret);

		startActivity(intent);
	}

	private void menuShowPlan() {
		final Intent intent = new Intent(getActivity(), PlanActivity.class);
		intent.putExtra(PlanActivity.PARAM_CODE_LIGNE, mLigne.getCode());
		startActivity(intent);
	}

	private void menuComment() {
		final Intent intent = new Intent(getActivity(), CommentaireActivity.class);
		intent.putExtra(CommentaireActivity.PARAM_LIGNE, mLigne);
		intent.putExtra(CommentaireActivity.PARAM_SENS, mSens);
		startActivity(intent);
	}

	private void menuComment(final Arret arret) {
		final Intent intent = new Intent(getActivity(), CommentaireActivity.class);
		intent.putExtra(CommentaireActivity.PARAM_LIGNE, mLigne);
		intent.putExtra(CommentaireActivity.PARAM_SENS, mSens);
		intent.putExtra(CommentaireActivity.PARAM_ARRET, arret);
		startActivity(intent);
	}

	private void addToFavoris(final Arret arret) {
		mFavoriManager.addFavori(getActivity().getContentResolver(), arret);
		Toast.makeText(getActivity(), R.string.toast_favori_ajout, Toast.LENGTH_SHORT).show();
	}

	private void removeFromFavoris(final Arret arret) {
		mFavoriManager.removeFavori(getActivity().getContentResolver(), arret.getId());
		Toast.makeText(getActivity(), R.string.toast_favori_retire, Toast.LENGTH_SHORT).show();
	}

	private void menuShowMap(final Arret arret) {
		final Intent intent = new Intent(getActivity(), MapActivity.class);
		intent.putExtra(MapFragment.PARAM_ITEM_ID, arret.getIdStation());
		intent.putExtra(MapFragment.PARAM_ITEM_TYPE, TypeOverlayItem.TYPE_STATION.getId());
		startActivity(intent);
	}

	@TargetApi(11)
	private void menuLocation() {
		if (mNearestArretPosition != null) {
			final ListView listView = getListView();
			final int listViewHeight = listView.getHeight();
			final int itemHeight = listView.getChildAt(0).getHeight();

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				listView.setSelectionFromTop(mNearestArretPosition, (listViewHeight - itemHeight) / 2);
			} else {
				listView.smoothScrollToPositionFromTop(mNearestArretPosition, (listViewHeight - itemHeight) / 2);
			}
		}
	}

	/**
	 * Définir le filtre courant.
	 * 
	 * @param filter
	 */
	private void setCurrentFilter(final int filter) {
		mCurrentFilter = filter;

		if (mCurrentFilter == FILTER_ALL) {
			setEmptyMessageValues(R.string.error_title_empty, R.string.error_summary_empty, R.drawable.ic_sad_face);
		} else {
			setEmptyMessageValues(R.string.error_title_empty_favori, R.string.error_summary_empty_arrets_favoris,
					R.drawable.ic_star_empty);
		}
	}

	/**
	 * Changer l'ordre de tri des arrêts.
	 * 
	 * @param sortOrder
	 *            L'id du comparator
	 * @param viewType
	 *            Le type de vue de l'adapter
	 */
	private void changeSortOrder(final int sortOrder, final ViewType viewType) {
		final ArretArrayAdapter adapter = (ArretArrayAdapter) getListAdapter();
		mCurrentSort = sortOrder;

		adapter.setViewType(viewType);
		getListView().setSelection(0);

		sort();
		loadDistances();

		if (viewType == ViewType.TYPE_METRO) {
			getListView().setDividerHeight(0);
			InfoDialogUtils.showIfNecessary(getActivity(), R.string.dialog_title_arret_order,
					R.string.dialog_content_arret_order);
		} else {
			getListView().setDividerHeight(mDefaultDividerHeight);
		}
	}

	/**
	 * Trier les parkings selon les préférences.
	 */
	private void sort() {
		final ArretArrayAdapter adapter = (ArretArrayAdapter) getListAdapter();
		if (adapter != null) {
			sort(adapter);
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * Trier les parkings selon les préférences.
	 * 
	 * @param adapter
	 */
	private void sort(final ArretArrayAdapter adapter) {
		final Comparator<Arret> comparator = mComparators.get(mCurrentSort);
		if (comparator != null) {
			adapter.sort(comparator);
		}
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		try {
			final ArretManager arretManager = ArretManager.getInstance();
			final List<Arret> arrets;
			if (mCurrentFilter == FILTER_ALL) {
				arrets = arretManager.getAll(context.getContentResolver(), mSens.codeLigne, mSens.code);
			} else {
				arrets = arretManager.getArretsFavoris(context.getContentResolver(), mSens.codeLigne, mSens.code);
			}

			mArrets.clear();
			mArrets.addAll(arrets);

			result.setResult(mAdapter);
		} catch (final Exception e) {
			result.setException(e);
		}
		return result;
	}

	@Override
	protected void onPostExecute() {
		sort();
		loadDistances();
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onChangeSens(final Sens sens) {
		if (sens.equals(mSens) == false) {
			mSens = sens;
			refreshContent();
		}
	}

	/**
	 * Lancer le calcul des distances.
	 */
	private void loadDistances() {
		if (mDistanceTask != null) {
			mDistanceTask.cancel(true);
		}
		if (getListAdapter() != null) {
			mDistanceTask = (DistanceTask) new DistanceTask(mDistanceTaskCallback, mLocationProvider.getLastLocation(),
					getListAdapter()).execute();
		}
	}

	/**
	 * Classe de calcul de la distance des arrêts.
	 */
	private class DistanceTask extends AsyncTask<Void, Void, Integer> {

		private DistanceTaskCallback mCallback;
		private final ListAdapter mAdapter;
		private final Location mCurrentLocation;

		public DistanceTask(final DistanceTaskCallback callback, final Location currentLocation,
				final ListAdapter adapter) {
			mCallback = callback;
			mCurrentLocation = currentLocation;
			mAdapter = adapter;
		}

		@Override
		protected Integer doInBackground(final Void... params) {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

			Arret arret;
			Integer nearestPosition = null;
			Float nearestDistance = Float.MAX_VALUE;
			Float distance = null;
			final Location equipementLocation = new Location(LocationManager.GPS_PROVIDER);

			if (mCurrentLocation != null) {
				for (int i = 0; i < mAdapter.getCount(); i++) {
					arret = (Arret) mAdapter.getItem(i);
					equipementLocation.setLatitude(arret.getLatitude());
					equipementLocation.setLongitude(arret.getLongitude());

					distance = mCurrentLocation.distanceTo(equipementLocation);
					arret.setDistance(distance);

					if (distance < nearestDistance) {
						nearestDistance = distance;
						nearestPosition = i;
					}

					if (isCancelled()) {
						break;
					}
				}
			}
			return nearestPosition;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			mCallback = null;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			if (!isCancelled() && mCallback != null) {
				mCallback.onPostExecute();
				mCallback.onNearestStationFound(result);
			}
		}

	}

	@Override
	public void onLocationChanged(final Location location) {
		loadDistances();
	}

	@Override
	public void onConnecting() {

	}

	@Override
	public void onDisconnected() {

	}
	
	@Override
	public void onLocationTimeout() {

	}

}
