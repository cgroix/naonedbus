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

import java.text.DateFormat;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.horaire.EmptyHoraire;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.widget.ClockDrawer;
import net.naonedbus.widget.ClockView;
import net.naonedbus.widget.adapter.ArraySectionAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class HoraireArrayAdapter extends ArraySectionAdapter<Horaire> {

	final DateFormat mTimeFormat;

	public HoraireArrayAdapter(final Context context, final List<Horaire> objects) {
		super(context, R.layout.list_item_horaire, objects);
		mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
	}

	@Override
	public boolean isEnabled(final int position) {
		final Horaire item = getItem(position);
		if (item instanceof EmptyHoraire)
			return false;
		return super.isEnabled(position);
	}

	@Override
	public void bindView(final View view, final Context context, final int position) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		final Horaire item = getItem(position);

		if (item instanceof EmptyHoraire) {
			bindEmptyView(holder, (EmptyHoraire) item);
		} else {
			bindHoraireView(holder, item);
		}
	}

	private void bindHoraireView(final ViewHolder holder, final Horaire item) {
		holder.itemTitle.setText(mTimeFormat.format(item.getHoraire().toDate()));
		holder.itemTitle.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		holder.itemTitle.setEnabled(true);

		final String terminus = item.getTerminus();
		holder.itemDescription.setText(terminus);
		holder.itemDescription.setVisibility(terminus == null ? View.GONE : View.VISIBLE);

		final String delai = item.getDelai();
		holder.itemTime.setText(delai);
		holder.itemTime.setVisibility(delai == null ? View.GONE : View.VISIBLE);

		if (item.isBeforeNow()) {
			holder.itemTitle.setEnabled(false);
			holder.itemDescription.setEnabled(false);
			holder.itemIcon.setEnabled(false);
		} else {
			holder.itemTitle.setEnabled(true);
			holder.itemDescription.setEnabled(true);
			holder.itemIcon.setEnabled(true);
		}

		holder.itemIcon.setMinutes(item.getHoraire().minuteOfDay().get());
		holder.itemIcon.setVisibility(View.VISIBLE);
	}

	private void bindEmptyView(final ViewHolder holder, final EmptyHoraire item) {
		holder.itemTitle.setText(item.getTextId());
		holder.itemTitle.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
		holder.itemTitle.setEnabled(false);
		holder.itemIcon.setVisibility(View.GONE);
		holder.itemDescription.setVisibility(View.GONE);
	}

	@Override
	public void bindViewHolder(final View view) {
		final ViewHolder holder = new ViewHolder();
		holder.itemTitle = (TextView) view.findViewById(R.id.itemTitle);
		holder.itemDescription = (TextView) view.findViewById(R.id.itemDescription);
		holder.itemTime = (TextView) view.findViewById(R.id.itemTime);
		holder.itemIcon = (ClockView) view.findViewById(R.id.itemIcon);
		view.setTag(holder);
	}

	private static class ViewHolder {
		ClockView itemIcon;
		TextView itemTitle;
		TextView itemDescription;
		TextView itemTime;
	}

}
