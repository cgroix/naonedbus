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
package net.naonedbus.widget.adapter.impl;

import net.naonedbus.R;
import net.naonedbus.bean.Favori;
import net.naonedbus.manager.impl.FavoriManager;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FormatUtils;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FavoriCursorAdapter extends CursorAdapter {

	private final LayoutInflater mLayoutInflater;
	private final FavoriManager mFavoriManager;

	public FavoriCursorAdapter(final Context context, final Cursor c) {
		super(context, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		mLayoutInflater = LayoutInflater.from(context);
		mFavoriManager = FavoriManager.getInstance();
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Favori item = mFavoriManager.getSingleFromCursor(cursor);

		if (item.getBackground() == null) {
			final Drawable background = ColorUtils.getCircle(item.getCouleurBackground());
			item.setBackground(background);
		}

		holder.ligneCode.setText(item.getLettre());
		holder.ligneCode.setBackgroundDrawable(item.getBackground());
		holder.ligneCode.setTextColor(item.getCouleurTexte());

		if (item.getNomFavori() == null) {
			holder.itemTitle.setText(item.getNomArret());
			holder.itemDescription.setText(FormatUtils.formatSens(item.getNomSens()));
		} else {
			holder.itemTitle.setText(item.getNomFavori());
			holder.itemDescription.setText(FormatUtils.formatSens(item.getNomArret(), item.getNomSens()));
		}

		if (item.getDelay() == null) {
			holder.nextHoraire.setVisibility(View.GONE);
			holder.progressBar.setVisibility(View.VISIBLE);
		} else {
			holder.progressBar.setVisibility(View.GONE);
			holder.nextHoraire.setText(item.getDelay());
			holder.nextHoraire.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup root) {
		final View view = mLayoutInflater.inflate(R.layout.list_item_favori, root, false);

		final ViewHolder holder = new ViewHolder();
		holder.ligneCode = (TextView) view.findViewById(R.id.itemSymbole);
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemDescription = (TextView) view.findViewById(R.id.itemDescription);
		holder.progressBar = (ProgressBar) view.findViewById(R.id.loading);
		holder.nextHoraire = (TextView) view.findViewById(R.id.itemTime);

		view.setTag(holder);

		return view;
	}

	private static class ViewHolder {
		TextView ligneCode;
		TextView itemTitle;
		TextView itemDescription;
		ProgressBar progressBar;
		TextView nextHoraire;
	}
}
