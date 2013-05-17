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

import net.naonedbus.R;
import net.naonedbus.fragment.CustomFragment;
import net.naonedbus.manager.impl.EquipementManager;
import net.naonedbus.widget.adapter.impl.EquipementCursorAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;

import com.actionbarsherlock.view.MenuItem;

public class ItineraireFragment extends CustomFragment {

	private AutoCompleteTextView mFromTextView;
	private AutoCompleteTextView mToTextView;

	public ItineraireFragment() {
		super(R.string.title_fragment_versions, R.layout.fragment_itineraire);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	protected void bindView(final View view, final Bundle savedInstanceState) {
		mFromTextView = (AutoCompleteTextView) view.findViewById(R.id.itineraireFrom);
		mToTextView = (AutoCompleteTextView) view.findViewById(R.id.itineraireTo);

		final EquipementManager equipementManager = EquipementManager.getInstance();
		final EquipementCursorAdapter adapter = new EquipementCursorAdapter(getActivity(),
				equipementManager.getCursor(getActivity().getContentResolver()));

		mFromTextView.setAdapter(adapter);
		mToTextView.setAdapter(adapter);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		return false;
	}

}
