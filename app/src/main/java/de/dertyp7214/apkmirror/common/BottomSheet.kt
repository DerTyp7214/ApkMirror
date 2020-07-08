/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package de.dertyp7214.apkmirror.common

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.dertyp7214.apkmirror.R

class BottomSheet(private var title: String, private var adapter: RecyclerView.Adapter<*>) :
    BottomSheetDialogFragment() {

    companion object {
        private val map = HashMap<String?, BottomSheet>()

        fun close(tag: String) {
            if (map.containsKey(tag)) map[tag]!!.dismiss()
        }
    }

    @SuppressLint("InflateParams", "RestrictedApi")
    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style)

        val themeManager = ThemeManager.getInstance(context!!)
        val v = LayoutInflater.from(context).inflate(R.layout.bottom_sheet, null)
        dialog!!.setContentView(v)

        val title = v.findViewById<TextView>(R.id.txt_title)
        val recyclerView = v.findViewById<RecyclerView>(R.id.rv)

        if (!themeManager.darkMode) title.elevation = 0F
        title.text = this.title
        title.setTextColor(if (themeManager.darkMode) Color.WHITE else Color.BLACK)

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    override fun show(manager: FragmentManager?, tag: String?) {
        super.show(manager, tag)
        map[tag] = this
    }
}