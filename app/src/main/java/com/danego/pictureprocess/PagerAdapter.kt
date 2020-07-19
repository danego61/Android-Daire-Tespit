package com.danego.pictureprocess

import android.graphics.Bitmap
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class PagerAdapter constructor(
    manager: FragmentManager):FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val pages = listOf(
        ResimFragment(0),
        ResimFragment(1),
        ResimFragment(2),
        ResimFragment(3),
        ResimFragment(4)
    )
    private var currentposition = -1
    private var stage = 0

    override fun setPrimaryItem(container: ViewGroup, position: Int, objec: Any) {
        super.setPrimaryItem(container, position, objec)
        if (position != currentposition) {
            val fragment = objec as Fragment?
            val pager = container as CustomViewPager?
            val view = fragment?.view
            if (fragment != null && pager != null && view != null) {
                currentposition = position
                pager.measureCurrentView(view)
            }
        }
    }

    override fun getItem(position: Int): Fragment {
        return pages[position]
    }

    override fun getCount(): Int {
        return stage
    }

    fun SetResim(resim: Bitmap, index: Int, yazi: String = "") {
        stage++
        notifyDataSetChanged()
        pages[index].Setresim(resim, yazi)
    }

    fun Sifirla() {
        stage = 0
        notifyDataSetChanged()
        pages[0].clear()
        pages[1].clear()
        pages[2].clear()
        pages[3].clear()
    }
}