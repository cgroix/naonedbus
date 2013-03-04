package net.naonedbus.helper;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Groupe;
import net.naonedbus.manager.impl.GroupeManager;
import net.naonedbus.provider.table.FavorisGroupesTable;
import net.naonedbus.provider.table.GroupeTable;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.database.Cursor;
import android.view.WindowManager;

public class GroupesHelper {

	private Context mContext;
	private GroupeManager mGroupeManager;

	public GroupesHelper(final Context context) {
		mContext = context;
		mGroupeManager = GroupeManager.getInstance();
	}

	public void linkFavori(final List<Integer> idFavoris) {
		final Cursor c = mGroupeManager.getCursor(mContext.getContentResolver(), idFavoris);
		final boolean[] checked = new boolean[c.getCount()];

		if (c.getCount() > 0) {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				checked[c.getPosition()] = c.getInt(c.getColumnIndex(FavorisGroupesTable.LINKED)) > 0;
				c.moveToNext();
			}
		}

		final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.dialog_title_groupes);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Groupe groupe;
				for (int i = 0; i < checked.length; i++) {
					c.moveToPosition(i);
					groupe = mGroupeManager.getSingleFromCursor(c);

					// TODO : Bulk insert / delete
					if (checked[i]) {
						for (Integer idFavori : idFavoris) {
							mGroupeManager.addFavoriToGroup(mContext.getContentResolver(), groupe.getId(), idFavori);
						}
					} else {
						for (Integer idFavori : idFavoris) {
							mGroupeManager.removeFavoriFromGroup(mContext.getContentResolver(), groupe.getId(),
									idFavori);
						}
					}
				}
			}
		});

		builder.setMultiChoiceItems(c, FavorisGroupesTable.LINKED, GroupeTable.NOM, new OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				checked[which] = isChecked;
			}
		});

		final AlertDialog alert = builder.create();
		alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		alert.show();
	}
}