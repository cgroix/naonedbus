package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.activity.impl.ArretsActivity;
import net.naonedbus.activity.impl.PlanActivity;
import net.naonedbus.bean.TypeLigne;
import net.naonedbus.fragment.CustomCursorFragment;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.TypeLigneManager;
import net.naonedbus.provider.impl.LigneProvider;
import net.naonedbus.provider.table.LigneTable;
import net.naonedbus.widget.adapter.impl.LigneCursorAdapter;
import net.naonedbus.widget.indexer.impl.LigneCursorIndexer;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnCloseListener;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;

public class LignesFragment extends CustomCursorFragment implements CustomFragmentActions, OnQueryTextListener,
		FilterQueryProvider {

	private static final String LOG_TAG = "LignesFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	private LigneCursorAdapter mAdapter;
	private LigneManager mLigneManager;
	private boolean mFilterFavoris;
	private String mFilterConstraint;

	public LignesFragment() {
		super(R.string.title_fragment_lignes, R.layout.fragment_listview_section);
		mLigneManager = LigneManager.getInstance();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (DBG)
			Log.d(LOG_TAG, "onActivityCreated");
		registerForContextMenu(getListView());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		if (DBG)
			Log.d(LOG_TAG, "onCreateOptionsMenu");

		final SherlockFragmentActivity activity = getSherlockActivity();
		if (activity != null) {
			final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
			menuInflater.inflate(R.menu.fragment_lignes, menu);

			final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
			searchView.setOnQueryTextListener(this);
			searchView.setQueryHint(getString(R.string.search_lignes_hint));
			searchView.setOnCloseListener(new OnCloseListener() {
				@Override
				public boolean onClose() {
					mFilterConstraint = null;
					return false;
				}
			});

			final AutoCompleteTextView searchText = (AutoCompleteTextView) searchView
					.findViewById(R.id.abs__search_src_text);
			searchText.setHintTextColor(getResources().getColor(R.color.query_hint_color));

			if (mFilterConstraint != null) {
				searchView.setQuery(mFilterConstraint, false);
				searchView.setIconifiedByDefault(false);
			}
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (DBG)
			Log.d(LOG_TAG, "onCreateContextMenu");

		super.onCreateContextMenu(menu, v, menuInfo);
		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;

		final CursorWrapper ligne = (CursorWrapper) getListAdapter().getItem(cmi.position);
		final String lettreLigne = ligne.getString(ligne.getColumnIndex(LigneTable.LETTRE));

		final android.view.MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.fragment_lignes_contextual, menu);

		menu.setHeaderTitle(getString(R.string.dialog_title_menu_lignes, lettreLigne));
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if (DBG)
			Log.d(LOG_TAG, "onContextItemSelected");

		final AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		final CursorWrapper ligne = (CursorWrapper) getListAdapter().getItem(cmi.position);
		final String codeLigne = ligne.getString(ligne.getColumnIndex(LigneTable.CODE));

		switch (item.getItemId()) {
		case R.id.menu_show_plan:
			menuShowPlan(codeLigne);
			break;
		default:
			break;
		}

		return true;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (DBG)
			Log.d(LOG_TAG, "onListItemClick");
		final CursorWrapper ligne = (CursorWrapper) getListAdapter().getItem(position);
		final String codeLigne = ligne.getString(ligne.getColumnIndex(LigneTable.CODE));

		final ParamIntent intent = new ParamIntent(getActivity(), ArretsActivity.class);
		intent.putExtra(ArretsActivity.Param.codeLigne, codeLigne);
		getActivity().startActivity(intent);
	}

	private void menuShowPlan(final String codeLigne) {
		if (DBG)
			Log.d(LOG_TAG, "menuShowPlan");

		final ParamIntent intent = new ParamIntent(getActivity(), PlanActivity.class);
		intent.putExtra(PlanActivity.Param.codeLigne, codeLigne);
		startActivity(intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		if (DBG)
			Log.d(LOG_TAG, "onCreateLoader");

		final CursorLoader cursorLoader = new CursorLoader(getActivity(), LigneProvider.CONTENT_URI, null, null, null,
				null);
		return cursorLoader;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if (DBG)
			Log.d(LOG_TAG, "onQueryTextChange");

		mFilterConstraint = newText;
		mAdapter.getFilter().filter(newText);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return true;
	}

	@Override
	public Cursor runQuery(CharSequence constraint) {
		if (DBG)
			Log.d(LOG_TAG, "runQuery");

		return mLigneManager.getLignesSearch(getActivity().getContentResolver(), constraint.toString());
	}

	@Override
	protected CursorAdapter getCursorAdapter(Context context) {
		if (DBG)
			Log.d(LOG_TAG, "getCursorAdapter");

		mAdapter = new LigneCursorAdapter(getActivity(), null);
		mAdapter.setFilterQueryProvider(this);

		final TypeLigneManager typeLigneManager = TypeLigneManager.getInstance();
		final List<TypeLigne> typesLignes = typeLigneManager.getAll(getActivity().getContentResolver());
		final List<String> types = new ArrayList<String>();
		for (TypeLigne type : typesLignes) {
			types.add(type.nom);
		}

		mAdapter.setIndexer(new LigneCursorIndexer(null, types, LigneTable.TYPE));

		return mAdapter;
	}

}
