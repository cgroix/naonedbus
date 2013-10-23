package net.naonedbus.fragment.header;

import net.naonedbus.R;
import net.naonedbus.fragment.impl.ParkingsPublicsFragment;
import net.naonedbus.fragment.impl.ParkingsRelaisFragment;
import android.content.Context;

public class CarParksFragmentHeader implements FragmentHeader {

	private final Class<?>[] mFragments = new Class<?>[] { ParkingsPublicsFragment.class, ParkingsRelaisFragment.class };

	private final int[] mTitles = new int[] { R.string.publics, R.string.park_and_ride };

	@Override
	public int[] getFragmentsTitles() {
		return mTitles;
	}

	@Override
	public Class<?>[] getFragmentsClasses() {
		return mFragments;
	}

	@Override
	public int getSelectedPosition(final Context context) {
		return 0;
	}

}
