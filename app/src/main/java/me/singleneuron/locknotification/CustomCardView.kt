package me.singleneuron.locknotification

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import me.singleneuron.locknotification.Utils.GeneralUtils
import me.singleneuron.locknotification.databinding.CustomCardviewBinding

class CustomCardView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val binding: CustomCardviewBinding = CustomCardviewBinding.inflate(LayoutInflater.from(context), this, true)
    var cardView: CardView
    var cardViewLinearLayout: LinearLayout
    private var cardViewTitle: TextView
    private var cardViewSummary: TextView
    var cardViewImage: ImageView

    var title: String?
        get() = cardViewTitle.text.toString()
        set(value) {
            cardViewTitle.text = value
        }
    var summary: String?
        get() = cardViewSummary.text.toString()
        set(value) {
            cardViewSummary.text = value
        }
    var enable: Boolean = false
        set(value) {
            color = if (value) ContextCompat.getColor(context, R.color.green) else ContextCompat.getColor(context, R.color.colorError)
            cardViewImage.setImageResource(if (value) R.drawable.ic_check_circle else R.drawable.ic_cancel)
            field = value
        }
    @ColorInt var color: Int = 0
        set(value) {
            if (GeneralUtils.getSharedPreferenceOnUI(context).getBoolean("colorCardView", true))
                cardViewLinearLayout.setBackgroundColor(value)
            field = color
        }

    init {
        cardView = binding.customCardView
        cardViewLinearLayout = binding.customCardViewLinearLayout
        cardViewTitle = binding.customCardViewTitle
        cardViewSummary = binding.customCardViewSummary
        cardViewImage = binding.customCardViewImage
        color = ContextCompat.getColor(context,R.color.colorPrimary)
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CustomCardView, 0, 0)
        try {
            title = a.getString(R.styleable.CustomCardView_customTitle)
            summary = a.getString(R.styleable.CustomCardView_customSummary)
        } finally {
            a.recycle()
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        cardView.setOnClickListener(l)
    }

}