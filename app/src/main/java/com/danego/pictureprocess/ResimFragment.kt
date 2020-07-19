package com.danego.pictureprocess

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.alexvasilkov.gestures.views.GestureFrameLayout


private const val yazi0="Ana Resim"
private const val yazi1="Ana resime medyan filtre uygulanmış hali. " +
        "Burada zeytinlerin ürendeki parlak ve çizgili alanları azaltmak için uygunlandı."
private const val yazi2="Medyan filtre uygulanmış resmin binary resme çevrilmesi. "
private const val yazi3="Binariye çevrilen resmin roberts kenar dedektörü algoritması çıktısı."
private const val yazi4="Roberts kenar dedektörü resme elips tespiti yapılıp bulunan elipslerin ana resime çizilmiş hali."

class ResimFragment constructor(private val index:Int) : Fragment() {

    private var layout: View? = null
    private var resim: ImageView? = null
    private var text: TextView? = null
    private var gesture: GestureFrameLayout? = null
    private var yeniresim: Bitmap? = null
    private var yenitext = ""
    private var yazi: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (layout == null) {
            if (index == 4) {
                layout = inflater.inflate(R.layout.fragmentson, container, false)
                text = layout!!.findViewById(R.id.bulunan)
            } else
                layout = inflater.inflate(R.layout.fragment, container, false)
            resim = layout!!.findViewById(R.id.resim)
            yazi = layout!!.findViewById(R.id.yazi)
            yazi!!.text = when (index) {
                0 -> yazi0
                1 -> yazi1
                2 -> yazi2
                3 -> yazi3
                4 -> yazi4
                else -> ""
            }
            Setresim(yeniresim, yenitext)
        }
        return layout
    }

    fun Setresim(rsm: Bitmap?, yazi: String = "") {
        if (resim == null) {
            yeniresim = rsm
            yenitext = yazi
        } else {
            if (index == 4)
                text?.text = "Toplam Bulunan Zeytin Sayısı = $yazi"
            resim?.setImageBitmap(rsm)
            if (index == 0)
                if (rsm != null)
                    this.yazi?.text =
                        "$yazi0 \n Resim Boyutu: Yükseklik = ${rsm!!.height} Genişlik = ${rsm.width}"
        }
        gesture?.controller?.resetState()
    }

    fun clear() {
        resim?.setImageBitmap(null)
    }
}