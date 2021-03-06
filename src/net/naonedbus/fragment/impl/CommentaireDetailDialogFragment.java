package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Commentaire;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.formatter.CommentaireFomatter;
import net.naonedbus.security.NaonedbusClient;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FormatUtils;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class CommentaireDetailDialogFragment extends SherlockDialogFragment {

	public static final String PARAM_COMMENTAIRE = "commentaire";

	private View mHeaderView;
	private TextView mTitle;
	private TextView mSubtitle;
	private TextView mItemCode;
	private ImageView mItemSymbole;
	private View mHeaderDivider;
	private ImageView mImageView;

	private Commentaire mCommentaire;
	private TextView mItemDescription;
	private TextView mItemDate;
	private TextView mItemSource;

	public CommentaireDetailDialogFragment() {
		setStyle(STYLE_NO_FRAME, R.style.ItineraryDialog);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCommentaire = getArguments().getParcelable(PARAM_COMMENTAIRE);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.dialog_comment_detail, null);

		mHeaderView = view.findViewById(R.id.headerView);
		mTitle = (TextView) view.findViewById(R.id.headerTitle);
		mSubtitle = (TextView) view.findViewById(R.id.headerSubTitle);
		mItemCode = (TextView) view.findViewById(R.id.itemCode);
		mItemSymbole = (ImageView) view.findViewById(R.id.itemSymbole);
		mHeaderDivider = view.findViewById(R.id.headerDivider);
		mImageView = (ImageView) view.findViewById(R.id.menu_share);
		mImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				shareComment();
			}
		});

		mItemDescription = (TextView) view.findViewById(R.id.itemDescription);
		mItemDate = (TextView) view.findViewById(R.id.itemDate);
		mItemSource = (TextView) view.findViewById(R.id.itemSource);

		mItemDescription.setText(mCommentaire.getMessage());

		mItemDate.setText(DateUtils.formatDateTime(view.getContext(), mCommentaire.getTimestamp(),
				DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME));

		final String source = mCommentaire.getSource();

		mItemSource.setText(getString(R.string.source, getString(CommentaireFomatter.getSourceResId(source))));

		final Resources res = getResources();

		if (NaonedbusClient.TWITTER_TAN_TRAFIC.name().equals(source)) {

			setSymbole(R.drawable.ic_tan, res.getColor(R.color.message_tan_header),
					res.getColor(R.color.message_tan_text));
			setTitle(getString(R.string.commentaire_tan_info_trafic));

		} else if (NaonedbusClient.TWITTER_TAN_ACTUS.name().equals(source)) {

			setSymbole(R.drawable.ic_tan, res.getColor(R.color.message_tan_header),
					res.getColor(R.color.message_tan_text));
			setTitle(getString(R.string.commentaire_tan_actus));

		} else if (NaonedbusClient.TWITTER_TAN_INFOS.name().equals(source)) {

			setSymbole(R.drawable.ic_tan_infos, res.getColor(R.color.message_taninfos_header),
					res.getColor(R.color.message_taninfos_text));
			setTitle(getString(R.string.commentaire_tan_infos));

		} else if (NaonedbusClient.NAONEDBUS_SERVICE.name().equals(source)) {

			setSymbole(R.drawable.ic_launcher, res.getColor(R.color.message_service_header),
					res.getColor(R.color.message_service_text));
			setTitle(getString(R.string.commentaire_message_service));

		} else {

			setLigneSensArret(mCommentaire);

		}

		return view;
	}

	public void setCode(final CharSequence code, final int backColor, final int textColor) {
		mItemCode.setText(code);
		mItemCode.setTextColor(textColor);
		mTitle.setTextColor(textColor);
		mSubtitle.setTextColor(textColor & 0xaaffffff);

		setHeaderColor(backColor, textColor);

		mItemSymbole.setVisibility(View.GONE);

		int circleColor;
		if (textColor == Color.WHITE) {
			circleColor = ColorUtils.getDarkerColor(backColor);
		} else {
			circleColor = ColorUtils.getLighterColor(backColor);
		}
		mItemCode.setBackgroundDrawable(ColorUtils.getCircle(circleColor));
	}

	public void setSymbole(final int resId, final int backColor, final int textColor) {
		mItemSymbole.setImageResource(resId);
		mTitle.setTextColor(textColor);
		mSubtitle.setVisibility(View.GONE);

		setHeaderColor(backColor, textColor);

		mItemCode.setVisibility(View.INVISIBLE);
	}

	public void setHeaderColor(final int backColor, final int textColor) {
		mHeaderView.setBackgroundColor(backColor);
		mHeaderDivider.setBackgroundColor(ColorUtils.getDarkerColor(backColor));

		if (textColor == Color.WHITE) {
			mImageView.setImageResource(R.drawable.ic_share);
		} else {
			mImageView.setImageResource(R.drawable.ic_share_black);
		}
	}

	private void setTitle(final String title) {
		mTitle.setText(title);
	}

	/**
	 * Proposer de partager l'information
	 */
	private void shareComment() {
		final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				getCommentaireTitle(mCommentaire) + "\n" + mCommentaire.getMessage());

		startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)));
	}

	/**
	 * Gérer l'affichage de l'information sur la ligne, sens et arrêt
	 * 
	 * @param idLigne
	 * @param idSens
	 * @param idArret
	 */
	protected void setLigneSensArret(final Commentaire commentaire) {
		final Ligne ligne = commentaire.getLigne();
		final Sens sens = commentaire.getSens();
		final Arret arret = commentaire.getArret();

		String title = "";
		String subtitle = "";

		if (ligne != null) {
			setCode(ligne.getLettre(), ligne.getCouleur(), ligne.getCouleurTexte());
			title = ligne.getNom();
		} else {
			setCode(FormatUtils.TOUT_LE_RESEAU, Color.WHITE, Color.BLACK);
		}

		if (arret == null && sens == null && ligne == null) {
			title = getString(R.string.commentaire_tout);
		} else {
			if (arret != null) {
				title = arret.getNomArret();
			}
			if (sens != null) {
				if (arret == null) {
					title = FormatUtils.formatSens(sens.text);
				} else {
					subtitle = FormatUtils.formatSens(sens.text);
				}
			}
		}

		mTitle.setText(title);
		if (TextUtils.isEmpty(subtitle)) {
			mSubtitle.setVisibility(View.GONE);
		} else {
			mSubtitle.setText(subtitle);
		}

	}

	protected String getCommentaireTitle(final Commentaire commentaire) {
		String title = "";

		if (NaonedbusClient.TWITTER_TAN_TRAFIC.name().equals(commentaire.getSource())) {
			title = getString(R.string.commentaire_tan_info_trafic);
		} else if (NaonedbusClient.NAONEDBUS_SERVICE.name().equals(commentaire.getSource())) {
			title = getString(R.string.commentaire_message_service);
		} else {
			final Ligne ligne = commentaire.getLigne();
			final Sens sens = commentaire.getSens();
			final Arret arret = commentaire.getArret();

			if (arret == null && sens == null && ligne == null) {
				title = getString(R.string.commentaire_tout);
			} else if (ligne != null) {
				title = getString(R.string.commentaire_ligne) + " " + ligne.getLettre();
				if (arret != null) {
					title += ", " + arret.getNomArret();
				}
				if (sens != null) {
					title += FormatUtils.formatSens(sens.text);
				}
			}
		}

		return title;
	}
}
