package com.robgthai.xprinter.demo.xprintertest

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.graphics.BitmapFactory
import android.support.v4.app.Fragment
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.robgthai.xprinter.demo.xprintertest.printer.IPrintModule
import com.robgthai.xprinter.demo.xprintertest.printer.xprinter.POSPrinter
import kotlinx.android.synthetic.main.fragment_main.*
import net.posprinter.posprinterface.IMyBinder
import net.posprinter.posprinterface.UiExecute
import net.posprinter.service.PosprinterService
import net.posprinter.utils.DataForSendToPrinterPos80
import java.util.ArrayList

/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment() {

    companion object {
        val ENABLE_BLUETOOTH = 1
    }

    var binder : IMyBinder? = null
    val deviceList: MutableList<String> = mutableListOf()
    val deviceReceiver = DeviceReceiver(deviceList)

    var isConnected = false
    var btAdapter: BluetoothAdapter? = null
    lateinit var printModule: IPrintModule

    val printerConnectionResult = object : UiExecute {

        override fun onsucess() {
            isConnected = true
            Log.d("Connection", "Connected")
            binder!!.acceptdatafromprinter(object : UiExecute {

                override fun onsucess() {
                    Log.d("Connection", "Accepted Data from Printer")
                }

                override fun onfailed() {
                    isConnected = false
                    Log.d("Connection", "Fail to talk to Printer")
                }
            })
        }

        override fun onfailed() {
            isConnected = false
            Log.d("Connection", "Unable to connect")
        }
    }

    val service = object: ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("Fragment", "Service Disconnected")
            binder = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("Fragment", "Service Connected for $name")
            binder = service as IMyBinder
            printModule = POSPrinter(binder!!)
        }

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val intent = Intent(activity, PosprinterService::class.java)
        activity.bindService(intent, service, Context.BIND_AUTO_CREATE)
        activity.registerReceiver(deviceReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        activity.registerReceiver(deviceReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))

        btnConnect.setOnClickListener { connectViaNetwork() }
        btnPrint.setOnClickListener {
            printText()
            printImage()
            printManual()
            printModule.feed(2)
        }

        setBluetooth()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectPort()
        activity.unbindService(service)
        activity.unregisterReceiver(deviceReceiver)
    }

    private fun printData(data: ByteArray) {
        if (isConnected) {
            binder!!.write(data, object : UiExecute {

                override fun onsucess() {
                    Log.d("Printing", "Printed successfully")
                }

                override fun onfailed() {
                    Log.d("Printing", "Printing failed")
                }
            })
        } else {
            Log.e("ERROR", "Printer is not connected")
        }
    }

    private fun printText() {
        printModule.init()
//        val data0 = DataForSendToPrinterTSC.sizeBydot(480, 240)
//        val data1 = DataForSendToPrinterTSC.cls()
//
//        val data2 = DataForSendToPrinterTSC.text(10, 10, "0", 0, 2, 2, "Test Printing ทดสอบๆ")
//        val data3 = DataForSendToPrinterTSC.print(1)
//        val data = byteMerger(byteMerger(byteMerger(data0, data1), data2), data3)
//        val data = DataForSendToPrinterTSC.feed(20)
//        printData(data)
        printModule.printText(
            "Test text print",
            "ทดสอบ ภาษาไทย 123456 ABC",
            "โลโม่ ชูว์ ล่า ม่า ฮาร์ท เม โม้ ช็อค ล้ำ ล่ำ พลั้ง"
        )
    }

    private fun printManual() {
        if(isConnected) {
            binder!!.writeDataByYouself(
                object : UiExecute {
                    override fun onfailed() {

                    }

                    override fun onsucess() {

                    }
                },
                {
                    val list = ArrayList<ByteArray>()
                    list.add(DataForSendToPrinterPos80.initializePrinter())
                    list.add(DataForSendToPrinterPos80.selectAlignment(1))
                    list.add(DataForSendToPrinterPos80.selectHRICharacterPrintPosition(2))
                    list.add(DataForSendToPrinterPos80.setBarcodeWidth(3))
                    list.add(DataForSendToPrinterPos80.setBarcodeHeight(162))
                    list.add(DataForSendToPrinterPos80.printBarcode(65, 11, "01234567890"))
                    list.add(DataForSendToPrinterPos80.printAndFeedLine())
                    list
                }
            )
        }
    }

    private fun printImage() {
//        printModule.printImage(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round))
//        printModule.printImage(BitmapFactory.decodeResource(resources, R.drawable.intro_openshop))
        printModule.printImage(BitmapFactory.decodeResource(resources, R.drawable.image_test_jpg))
//        printModule.printImage(BitmapFactory.decodeResource(resources, R.drawable.image_test_png))
//        if(isConnected) {
//            binder!!.writeDataByYouself(
//                object : UiExecute {
//                    override fun onfailed() {
//
//                    }
//
//                    override fun onsucess() {
//
//                    }
//                },
//                {
//                    val list = ArrayList<ByteArray>()
//                    list.add(DataForSendToPrinterTSC.bitmap(10, 10, 0,
//                            bitmap, BitmapToByteData.BmpType.Threshold))
//                    list.add(DataForSendToPrinterTSC.print(1))
//                    list
//                }
//            )
//        }
    }

    private fun byteMerger(byte_1: ByteArray, byte_2: ByteArray): ByteArray {
        val byte_3 = ByteArray(byte_1.size + byte_2.size)
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.size)
        System.arraycopy(byte_2, 0, byte_3, byte_1.size, byte_2.size)
        return byte_3
    }

    private fun disconnectPort() {
        try {
            binder!!.disconnectCurrentPort(object : UiExecute {
                override fun onfailed() {
                    Log.d("Fragment", "Port Cannot Disconnect")
                }

                override fun onsucess() {
                    Log.d("Fragment", "Port Disconnected successfully")
                }

            })
        } catch(e: UninitializedPropertyAccessException) {
        }
    }

    private fun setBluetooth() {
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        Log.i("BLUETOOTH", "Bluetooth $btAdapter")
        if (btAdapter != null && !btAdapter!!.isEnabled()) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, MainActivityFragment.ENABLE_BLUETOOTH)

        } else {
            showBTList()
        }

    }

    private fun showBTList() {
        if (btAdapter?.isDiscovering != true) {
            btAdapter?.startDiscovery()
        }
//        val inflater = LayoutInflater.from(activity)
//        dialogView = inflater.inflate(R.layout.printer_list, null)
//        adapter1 = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceList_bonded)
//        lv1 = dialogView.findViewById(R.id.listView1) as ListView
//        btn_scan = dialogView.findViewById(R.id.btn_scan) as Button
//        ll1 = dialogView.findViewById(R.id.ll1) as LinearLayout
//        lv2 = dialogView.findViewById(R.id.listView2) as ListView
//        adapter2 = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceList_found)
//        lv1.setAdapter(adapter1)
//        lv2.setAdapter(adapter2)
//        dialog = AlertDialog.Builder(this).setTitle("BLE").setView(dialogView).create()
//        dialog.show()
//        setlistener()
        findAvailableDevice()
    }

    private fun findAvailableDevice() {
        val device = btAdapter!!.bondedDevices

//        if (btAdapter != null && btAdapter!!.isDiscovering) {
//            adapter1.notifyDataSetChanged()
//        }

        if (device.size > 0) {
            val it = device.iterator()
            while (it.hasNext()) {
                val btd = it.next()
                Log.i("Printer", "Found ${btd.fullName()}")
            }
        } else {
            Log.i("Printer", "No Printer Found")
        }
    }

    private fun connectViaNetwork() {
        val address = "192.168.1.50"
        binder!!.connectNetPort(address, 9100, printerConnectionResult)
    }

    private fun connectViaBluetooth() {
        /**
         * Printer001 WTF is this??
         * 00:13:04:84:14:0A
         *
         * XP-I58
         * 00:13:04:84:0E:11
         *
         * XP-Q800
         * 00:7D:1B:50:56:DB
         * 192.168.1.50
         */
        val address = "192.168.1.50"
        binder!!.connectBtPort(address, printerConnectionResult)
    }
}
