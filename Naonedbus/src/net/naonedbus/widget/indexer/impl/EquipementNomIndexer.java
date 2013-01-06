package net.naonedbus.widget.indexer.impl;

import net.naonedbus.bean.Equipement;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;

public class EquipementNomIndexer extends ArraySectionIndexer<Equipement> {

	@Override
	protected String getSectionLabel(Context context, Equipement item) {
		return item.getNom().substring(0, 1);
	}

	@Override
	protected void prepareSection(Equipement equipement) {
		equipement.setSection(equipement.getNom().substring(0, 1));
	}

}
