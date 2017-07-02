package com.robgthai.xprinter.demo.xprintertest.printer

import android.graphics.Bitmap
import net.posprinter.posprinterface.IMyBinder

interface IPrintModule {
    val binder: IMyBinder

    fun init()
    fun feed(lines: Int)
    fun printText(vararg strs: String)
    fun printImage(image: Bitmap)
}