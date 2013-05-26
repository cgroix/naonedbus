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
package net.naonedbus.utils;

import net.naonedbus.R;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;

public abstract class FormatUtils {

	public static final String SENS_ARROW = "\u2192";
	public static final String TOUT_LE_RESEAU = "\u221E";
	public static final String DOT = "\u2022";

	private FormatUtils() {
	}

	public static String formatSens(final String sens) {
		return SENS_ARROW + " " + sens;
	}

	public static String formatArretSens(final String arret, final String sens) {
		return arret + " " + SENS_ARROW + " " + sens;
	}

	public static String formatTitle(final String ligne, final String sens) {
		return ligne + " " + SENS_ARROW + " " + sens;
	}

	public static String formatTitle(final String ligne, final String arret, final String sens) {
		return ligne + " " + DOT + " " + arret + " " + SENS_ARROW + " " + sens;
	}

	public static String formatWithDot(final String a, final String b) {
		return a + " " + DOT + " " + b;
	}

	public static CharSequence formatTimeAmPm(final Context context, final String time) {
		if (android.text.format.DateFormat.is24HourFormat(context)) {
			return time;
		} else {
			final SpannableString spannable = new SpannableString(time);
			spannable.setSpan(new RelativeSizeSpan(0.45f), time.length() - 3, time.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			return spannable;
		}
	}

	public static String formatMinutes(final Context context, final long millisecondes) {
		String delay = "";
		final int minutes = (int) (millisecondes / 60000);

		if (minutes < 60) {
			delay = context.getString(R.string.format_minutes, minutes);
		} else {
			final int heures = minutes / 60;
			final int reste = minutes - heures * 60;
			delay = context.getString(R.string.format_heures, heures, reste);
		}

		return delay;
	}

}
