package com.robgthai.xprinter.demo.xprintertest.printer.xprinter

import android.graphics.Bitmap
import com.robgthai.xprinter.demo.xprintertest.printer.IPrintModule
import net.posprinter.posprinterface.IMyBinder
import net.posprinter.posprinterface.UiExecute
import net.posprinter.utils.BitmapToByteData
import net.posprinter.utils.DataForSendToPrinterPos80
import java.io.UnsupportedEncodingException
import java.util.ArrayList

open class POSPrinter(override val binder: IMyBinder): IPrintModule {

    val printCallback = object : UiExecute {
        override fun onfailed() {

        }

        override fun onsucess() {

        }
    }

    override fun init() {
//        binder.writeDataByYouself(
//            printCallback,
//            {
//                val list = ArrayList<ByteArray>()
//                list.add(DataForSendToPrinterPos80.selectInternationalCharacterSets(70))
//                list
//            }
//        )
//        printText("Hi")
    }

    override fun feed(lines: Int) {
        binder.writeDataByYouself(
            printCallback,
            {
                val list = ArrayList<ByteArray>()
                list.add(DataForSendToPrinterPos80.printAndFeedForward(lines))
                list
            }
        )
    }

    override fun printText(vararg strs: String) {
        binder.writeDataByYouself(
            printCallback,
            {
                val list = ArrayList<ByteArray>()
//                val str1 = "Welcome to use the impact and thermal printer manufactured by professional POS receipt printer company!"
//                val data1 = strTobytes(str1)
//                list.add(data1)

                list.add(DataForSendToPrinterPos80.selectInternationalCharacterSets(70))
                strs.mapTo(list) { strTobytes(it) }
                list.add(DataForSendToPrinterPos80.horizontalPositioning())
                list.add(DataForSendToPrinterPos80.selectInternationalCharacterSets(255))
                strs.mapTo(list) { strTobytes(it) }

//                list.add(DataForSendToPrinterPos80.printAndFeedLine())
//                list.add(DataForSendToPrinterPos80.printAndFeed(50))
//                list.add(DataForSendToPrinterPos80.printAndFeedForward(5))
                list
            }
        )
    }

    override fun printImage(image: Bitmap) {
        binder.writeDataByYouself(
            printCallback,
            {
                val list = ArrayList<ByteArray>()
//                val w = image.width
//                val h = image.height
//
//                var x = 0
//                if (w < 576) {
//                    x = (576 - w) / 2
//                }
//                val m = x % 256
//                val n = x / 256

//                Log.d("POSPrinter", "Printing image $w $h $m $n")

                list.add(
                    DataForSendToPrinterPos80.printRasterBmp(
                        0,
                        image,
                        BitmapToByteData.BmpType.Threshold,
                        BitmapToByteData.AlignType.Center,
                        576
                    )
                )
                list
            }
        )
    }

    fun strTobytes(str: String): ByteArray {
        var b: ByteArray?
        var data: ByteArray? = null
        try {
            b = str.toByteArray(charset("utf-8"))
            data = String(b).toByteArray()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return data!!
    }
}