package com.example.anilist.ui.mediadetails

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.example.anilist.R

class HtmlText @JvmOverloads constructor(
    context: Context,
    text: String,
    color: Int,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.html_parser, this, true)

        // Access the custom view through its ID
        val textView = findViewById<TextView>(R.id.html_parser)

        // Set text or perform any other operations on the custom view
        textView.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
        textView.setTextColor(color)
    }
}
