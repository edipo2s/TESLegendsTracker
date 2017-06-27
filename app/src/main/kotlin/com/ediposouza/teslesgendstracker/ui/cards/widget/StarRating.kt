package com.ediposouza.teslesgendstracker.ui.cards.widget

import android.content.Context
import android.content.DialogInterface
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.data.Card
import kotlinx.android.synthetic.main.widget_star_rating.view.*
import org.jetbrains.anko.find

/**
 * Created by EdipoSouza on 11/2/16.
 */
class StarRating(ctx: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
        LinearLayout(ctx, attrs, defStyleAttr) {

    var card: Card? = null
    var userRating: Float = 0f
        set(value) {
            if (value > 0) {
                star_rating_qtd.setTextColor(ContextCompat.getColor(context, R.color.amber_500))
            }
        }

    private var ratingDialog: AlertDialog? = null

    init {
        inflate(context, R.layout.widget_star_rating, this)
        setOnClickListener {
            with(View.inflate(context, R.layout.dialog_card_star_rating, null)) {
                var selectedRating: Int = 0
                fun setRating(rating: Float) {
                    selectedRating = rating.toInt()
                    find<ImageView>(R.id.dialog_star_rating_1).setImageResource(getStarResource(1f, rating))
                    find<ImageView>(R.id.dialog_star_rating_2).setImageResource(getStarResource(2f, rating))
                    find<ImageView>(R.id.dialog_star_rating_3).setImageResource(getStarResource(3f, rating))
                    find<ImageView>(R.id.dialog_star_rating_4).setImageResource(getStarResource(4f, rating))
                    find<ImageView>(R.id.dialog_star_rating_5).setImageResource(getStarResource(5f, rating))
                }
                find<View>(R.id.dialog_star_rating_1).setOnClickListener { setRating(1f) }
                find<View>(R.id.dialog_star_rating_2).setOnClickListener { setRating(2f) }
                find<View>(R.id.dialog_star_rating_3).setOnClickListener { setRating(3f) }
                find<View>(R.id.dialog_star_rating_4).setOnClickListener { setRating(4f) }
                find<View>(R.id.dialog_star_rating_5).setOnClickListener { setRating(5f) }
                ratingDialog = AlertDialog.Builder(context)
                        .setTitle(R.string.card_title_star_rating)
                        .setView(this)
                        .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, _ ->
                            postCardRating(selectedRating)
                        })
                        .create()
                        .apply {
                            setOnShowListener {
                                find<ImageView>(R.id.dialog_star_rating_1).setImageResource(getStarResource(1f, userRating))
                                find<ImageView>(R.id.dialog_star_rating_2).setImageResource(getStarResource(2f, userRating))
                                find<ImageView>(R.id.dialog_star_rating_3).setImageResource(getStarResource(3f, userRating))
                                find<ImageView>(R.id.dialog_star_rating_4).setImageResource(getStarResource(4f, userRating))
                                find<ImageView>(R.id.dialog_star_rating_5).setImageResource(getStarResource(5f, userRating))
                            }
                            show()
                        }
            }
        }
    }

    constructor(ctx: Context?) : this(ctx, null, 0)

    constructor(ctx: Context?, attrs: AttributeSet) : this(ctx, attrs, 0)

    fun setGeneralRating(rating: Float, qtd: Int) {
        star_rating_1.setImageResource(getStarResource(1f, rating))
        star_rating_2.setImageResource(getStarResource(2f, rating))
        star_rating_3.setImageResource(getStarResource(3f, rating))
        star_rating_4.setImageResource(getStarResource(4f, rating))
        star_rating_5.setImageResource(getStarResource(5f, rating))
        star_rating_qtd.setText("($qtd)")
    }

    private fun getStarResource(starNumber: Float, rating: Float): Int {
        return when (rating - starNumber) {
            -0.5f -> R.drawable.ic_star_rating_half
            in 0f..5f -> R.drawable.ic_star_rating_on
            else -> R.drawable.ic_star_rating_off
        }
    }

    private fun postCardRating(rating: Int) {

        ratingDialog?.dismiss()
    }

}