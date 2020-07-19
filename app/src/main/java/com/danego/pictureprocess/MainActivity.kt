package com.danego.pictureprocess

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.coroutines.*
import java.util.ArrayList
import kotlin.math.*

private const val PICK_IMAGE = 1
private const val READ_EXTERNAL_STORAGE_REQUEST_CODE = 44
class MainActivity : AppCompatActivity() {

    var islem = false
    var adapter: PagerAdapter? = null
    var stage = -1
    var resim: Bitmap? = null
    var ilkresim: Bitmap? = null
    var bulunansayi = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        adapter = PagerAdapter(supportFragmentManager)
        pager.adapter = adapter

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val sta = menu.findItem(R.id.stage)
        if (islem) {
            menu.findItem(R.id.ac).isEnabled = false
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.progress)
            sta.title = "İşleniyor"
            sta.isEnabled = false
        } else {
            sta.isEnabled = stage in 0..3
            sta.title = "Sonraki Adım"
            menu.findItem(R.id.ac).isEnabled = true
            menu.findItem(R.id.menu_refresh).actionView = null
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            ResimSec()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                resim = uriToBitmap(uri)
                if (resim != null) {
                    pager.setCurrentItem(0, true)
                    adapter?.Sifirla()
                    adapter?.SetResim(resim!!, 0)
                    stage = 0
                    invalidateOptionsMenu()
                    ilkresim = resim
                }
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        val input = contentResolver.openInputStream(uri)
        if (input != null)
            return BitmapFactory.Options().run {
                inJustDecodeBounds = true
                val by = input.readBytes()
                input.close()
                BitmapFactory.decodeByteArray(by, 0, by.count(), this)
                inSampleSize = calculateInSampleSize(this)
                inJustDecodeBounds = false

                BitmapFactory.decodeByteArray(by, 0, by.count(), this)
            }
        return null
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        val reqWidth = 500
        val reqHeight = 500
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth)
                inSampleSize *= 2
        }

        return inSampleSize
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.ac) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            )
                ResimSec()
            else
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_EXTERNAL_STORAGE),
                    READ_EXTERNAL_STORAGE_REQUEST_CODE
                )
        } else {
            stage++
            islem = true
            invalidateOptionsMenu()
            DialoAc()
        }
        return true
    }

    private fun ElipsTespit(): Bitmap? {
        if (resim != null) {
            val res = resim!!.copy(Bitmap.Config.ARGB_8888, true)
            val height = res.height
            val width = res.width
            val bulunanlar = arrayListOf<Elips>()
            val denenenler = arrayListOf<Nokta>()
            for (x in 0 until width) {
                Dongu@ for (y in 0 until height) {
                    val color = res.getPixel(x, y)
                    val r = Color.red(color)
                    if (r == 255) {
                        for (bulunan in bulunanlar) {
                            val rx = bulunan.uzunkenar / 2
                            val ry = bulunan.kisakenar / 2
                            val h = bulunan.merkez.x
                            val k = bulunan.merkez.y
                            val p =
                                ((x - h) * (x - h) / (rx * rx)) + ((y - k) * (y - k) / (ry * ry))
                            if (p <= 0.9f)
                                continue@Dongu
                        }
                        val merkez = ElipsMerkezBul(x, y)
                        if (merkez != null) {
                            for (dene in denenenler)
                                if (dene == merkez)
                                    continue@Dongu
                            denenenler.add(merkez)
                            val elips = ElipsBul(merkez)
                            if (elips != null)
                                bulunanlar.add(elips)
                        }
                    }
                }
            }
            bulunansayi = bulunanlar.count()
            return ElipsCiz(bulunanlar)
        }
        return null
    }

    private fun ElipsCiz(bulunanlar: ArrayList<Elips>): Bitmap? {
        if (ilkresim != null) {
            val bit = ilkresim!!.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(bit)
            val p = Paint()
            p.style = Paint.Style.STROKE
            p.isAntiAlias = true
            p.isFilterBitmap = true
            p.isDither = true
            p.color = Color.RED
            for (bul in bulunanlar) {
                val x = bul.merkez.x.toFloat()
                val left = x - bul.uzunkenar / 2
                val y = bul.merkez.y.toFloat()
                val top = y - bul.kisakenar / 2
                val right = x + bul.uzunkenar / 2
                val bottom = y + bul.kisakenar / 2
                val rect = RectF(left, top, right, bottom)
                val aci = bul.aci.toFloat()
                canvas.rotate(aci, x, y)
                canvas.drawOval(rect, p)
                canvas.rotate(-aci, x, y)
            }
            return bit
        }
        return null
    }

    private fun ElipsBul(merkez: Nokta): Elips? {
        var enkisa = 500
        var enuzun = 0
        var enkisaaci = 90
        var enuzunaci = 0
        var deneme = 0
        for (i in 0 until 180) {
            val eksiuzunluk = UzunlukBul(merkez.x, merkez.y, i + 180)
            val artiuzunluk = UzunlukBul(merkez.x, merkez.y, i)

            if (eksiuzunluk > 0 && artiuzunluk > 0) {
                val fark = abs(eksiuzunluk - artiuzunluk)
                if (fark < 10) {
                    val uzunluk = eksiuzunluk + artiuzunluk
                    if (enuzun < uzunluk) {
                        enuzun = uzunluk
                        enuzunaci = i
                    }
                    if (enkisa > uzunluk) {
                        enkisa = uzunluk
                        enkisaaci = i
                    }
                } else if (deneme > 80)
                    return null
                else
                    deneme += fark / 4
            } else
                return null
        }
        val acifark = abs(enuzunaci - enkisaaci)
        val kenarfark = enuzun - enkisa
        if (enuzun == 0 || enkisa == 0 || kenarfark < 3)
            return null
        if (acifark > 10) {
            val u1 = ToplamUzunlukBul(merkez.x, merkez.y, enuzunaci + 90)
            val u2 = ToplamUzunlukBul(merkez.x, merkez.y, enuzunaci - 90)
            val uzunluk = if (u1 < 5 && u2 < 5)
                return null
            else if (u1 < 5 || u2 < 5)
                if (u1 < 5)
                    u2
                else
                    u1
            else
                if (u1 > u2) u1
                else u2
            return if (enuzun - uzunluk > 3 && abs(enkisa - uzunluk) < 10)
                Elips(merkez, uzunluk, enuzun, enuzunaci)
            else
                null
        }
        return Elips(merkez, enkisa, enuzun, enuzunaci)
    }

    private fun ToplamUzunlukBul(x: Int, y: Int, aci: Int): Int {
        val u1 = UzunlukBul(x, y, aci)
        val u2 = UzunlukBul(x, y, aci + 180)
        if (u1 > 0 && u2 > 0)
            return u1 + u2
        return -1
    }

    private fun ElipsMerkezBul(x: Int, y: Int): Nokta? {
        val uzunluk = UzunlukBul(x, y, 0)
        if (uzunluk <= 5)
            return null
        return ElipsMerkezBul((uzunluk / 2) + x, y, false)
    }

    private fun ElipsMerkezBul(
        x: Int,
        y: Int,
        yatay: Boolean,
        adim: Int = 0,
        degismez: Int = 0
    ): Nokta? {
        val eksiuzunluk =
            if (yatay) UzunlukBul(x, y, 180)
            else UzunlukBul(x, y, 270)
        val artiuzunluk =
            if (yatay) UzunlukBul(x, y, 0)
            else UzunlukBul(x, y, 90)

        if (eksiuzunluk > 0 && artiuzunluk > 0) {
            val toplamuzunluk = eksiuzunluk + artiuzunluk
            val merkezx =
                if (yatay)
                    if (artiuzunluk < eksiuzunluk)
                        x - ((toplamuzunluk / 2) - artiuzunluk)
                    else
                        x + ((toplamuzunluk / 2) - eksiuzunluk)
                else x
            val merkezy =
                if (yatay)
                    y
                else
                    if (artiuzunluk < eksiuzunluk)
                        y - ((toplamuzunluk / 2) - artiuzunluk)
                    else
                        y + ((toplamuzunluk / 2) - eksiuzunluk)
            val farkx = abs(x - merkezx)
            val farky = abs(y - merkezy)
            val fark = abs(eksiuzunluk - artiuzunluk)
            return if (fark == 0 && farkx == 0 && farky == 0)
                if (degismez > 1)
                    Nokta(merkezx, merkezy)
                else
                    ElipsMerkezBul(merkezx, merkezy, !yatay, adim + 1, degismez + 1)
            else
                if (adim > 12)
                    null
                else
                    ElipsMerkezBul(merkezx, merkezy, !yatay, adim + 1, degismez)
        }
        return null
    }

    private fun UzunlukBul(x: Int, y: Int, aci: Int): Int {
        if (resim != null) {
            val res = resim!!.copy(Bitmap.Config.ARGB_8888, true)
            val height = res.height
            val width = res.width
            val durdur = (height / 8).coerceAtMost(width / 8)
            var uzunluk = 0
            while (true) {
                val yenix = x + (uzunluk * cos(aci * Math.PI / 180)).toInt()
                val yeniy = y + (uzunluk * sin(aci * Math.PI / 180)).toInt()
                if (uzunluk >= durdur ||
                    yenix >= width || yeniy >= height ||
                    yenix <= 0 || yeniy <= 0
                )
                    return -1
                val color = res.getPixel(yenix, yeniy)
                val r = Color.red(color)
                if (r == 255) {
                    if (uzunluk > 5)
                        return uzunluk
                }
                uzunluk++
            }
        }
        return -1
    }

    private fun Roberts(robertsThreshold: Int): Bitmap? {
        if (resim != null) {
            val res = resim!!.copy(Bitmap.Config.ARGB_8888, true)
            val height = res.height
            val width = res.width
            val k1 = arrayOf(
                arrayOf(1, 0),
                arrayOf(0, -1)
            )
            val k2 = arrayOf(
                arrayOf(0, 1),
                arrayOf(-1, 0)
            )
            val retpicture = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val matrixSize = 2
            val halfSize = matrixSize / 2

            for (x in 0 until width - halfSize) {
                for (y in 0 until height - halfSize) {
                    var sumR = 0
                    var sumR1 = 0
                    for (m in 0..halfSize) {
                        for (n in 0..halfSize) {
                            val color = res.getPixel(x + m, y + n)
                            val r = Color.red(color)
                            sumR += k1[m][n] * r
                            sumR1 += k2[m][n] * r
                        }
                    }
                    var R = abs(sumR) + abs(sumR1)
                    R = if (R > robertsThreshold) 255
                    else 0
                    retpicture.setPixel(x, y, Color.argb(255, R, 0, 0))
                }
            }
            return retpicture
        }
        return null
    }

    private fun ColorToGrayscale(tolerans: Int, tolerans2: Int): Bitmap? {
        if (resim != null) {
            val res = resim!!.copy(Bitmap.Config.ARGB_8888, true)
            val height = res.height
            val width = res.width
            val retpicture = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val color = res.getPixel(x, y)
                    val r = Color.red(color)
                    val g = Color.green(color)
                    val b = Color.blue(color)
                    var gray = 0
                    if (r != 0 && g != 0 && b != 0)
                        if (r <= tolerans || g <= tolerans || b <= tolerans) {
                            val rg = abs(r - g)
                            val rb = abs(r - b)
                            val bg = abs(b - g)
                            if (rg <= tolerans2 && rb <= tolerans2 && bg <= tolerans2)
                                gray = 255
                        }
                    if (gray == 255)
                        retpicture.setPixel(x, y, Color.argb(255, 255, 0, 0))
                }
            }
            return retpicture
        }
        return null
    }

    private fun Median(hassasiyet: Int): Bitmap? {
        if (resim != null) {
            if(hassasiyet == 0)
                return resim
            val res = resim!!.copy(Bitmap.Config.ARGB_8888, true)
            val height = res.height
            val width = res.width
            val uzunluk = (hassasiyet * 2 + 1) * (hassasiyet * 2 + 1)
            val arrayR = arrayOfNulls<Int>(uzunluk)
            val arrayG = arrayOfNulls<Int>(uzunluk)
            val arrayB = arrayOfNulls<Int>(uzunluk)
            val retpicture = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in hassasiyet until width - hassasiyet) {
                for (y in hassasiyet until height - hassasiyet) {
                    var index = 0
                    for (m in -hassasiyet..hassasiyet) {
                        for (n in -hassasiyet..hassasiyet) {
                            val color = res.getPixel(x + m, y + n)
                            arrayR[index] = Color.red(color)
                            arrayG[index] = Color.green(color)
                            arrayB[index] = Color.blue(color)
                            index++
                        }
                    }
                    arrayB.sort()
                    arrayG.sort()
                    arrayR.sort()
                    val R = arrayR[uzunluk / 2]
                    val G = arrayG[uzunluk / 2]
                    val B = arrayB[uzunluk / 2]
                    retpicture.setPixel(x, y, Color.argb(255, R!!, G!!, B!!))
                }
            }
            return retpicture
        }
        return null
    }

    private fun ResimSec() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"

        val pickIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        pickIntent.type = "image/*"

        val chooserIntent = Intent.createChooser(getIntent, "Resim Seçin")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

        startActivityForResult(chooserIntent, PICK_IMAGE)
    }

    private fun DialoAc() {
        if (stage < 3) {
            val dialog = Dialog(this)
            dialog.setCancelable(true)
            dialog.setContentView(
                if (stage == 1) R.layout.median
                else R.layout.gray
            )
            dialog.setOnCancelListener {
                stage--
                islem = false
                invalidateOptionsMenu()
            }
            dialog.findViewById<Button>(R.id.kaydet).setOnClickListener {
                dialog.dismiss()
                if (stage == 1) {
                    val deger = dialog.findViewById<EditText>(R.id.median).text.toString().toInt()
                    Islem(deger1 = deger)
                } else {
                    val deger1 = dialog.findViewById<EditText>(R.id.tole1).text.toString().toInt()
                    val deger2 = dialog.findViewById<EditText>(R.id.tole2).text.toString().toInt()
                    Islem(deger1, deger2)
                }
            }
            dialog.show()
        } else
            Islem()
    }

    private fun Islem(deger1: Int = 0, deger2: Int = 0) {
        GlobalScope.launch(Dispatchers.Default) {
            val sresim = when (stage) {
                1 -> Median(deger1)
                2 -> ColorToGrayscale(deger1, deger2)
                3 -> Roberts(250)
                4 -> ElipsTespit()
                else -> null
            }
            withContext(Dispatchers.Main) {
                if (sresim == null)
                    stage--
                else {
                    if (stage == 4) {
                        adapter?.SetResim(sresim, stage, bulunansayi.toString())
                        bulunansayi = -1
                    } else
                        adapter?.SetResim(sresim, stage)
                    pager.setCurrentItem(stage, true)
                    resim = sresim
                }
                islem = false
                invalidateOptionsMenu()
            }

        }
    }
}

data class Elips(
    val merkez:Nokta,
    val kisakenar:Int,
    val uzunkenar:Int,
    val aci:Int
)
data class Nokta(
    val x:Int,
    val y:Int
)