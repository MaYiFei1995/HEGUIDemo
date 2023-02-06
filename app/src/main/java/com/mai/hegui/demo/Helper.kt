@file:Suppress("DEPRECATION")

package com.mai.hegui.demo

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.media.MediaDrm
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Settings
import android.telephony.TelephonyManager
import android.telephony.cdma.CdmaCellLocation
import android.telephony.gsm.GsmCellLocation
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.mai.oaid.helper.OAIDError
import com.mai.oaid.helper.OAIDHelper
import com.mai.oaid.helper.OAIDHelper.InitListener
import java.net.NetworkInterface
import java.util.*
import kotlin.math.abs


@SuppressLint("MissingPermission", "HardwareIds")
object Helper {

    fun getImei(context: Context): String {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        try {
            return telephonyManager.deviceId
        } catch (tr: Throwable) {
            tr.printStackTrace()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                return telephonyManager.getDeviceId(0)
            } catch (tr: Throwable) {
                tr.printStackTrace()
            }
            try {
                return telephonyManager.getDeviceId(1)
            } catch (tr: Throwable) {
                tr.printStackTrace()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                return telephonyManager.imei
            } catch (tr: Throwable) {
                tr.printStackTrace()
            }
            try {
                return telephonyManager.getImei(0)
            } catch (tr: Throwable) {
                tr.printStackTrace()
            }
            try {
                return telephonyManager.getImei(1)
            } catch (tr: Throwable) {
                tr.printStackTrace()
            }
            try {
                return telephonyManager.meid
            } catch (tr: Throwable) {
                tr.printStackTrace()
            }
            try {
                return telephonyManager.getMeid(0)
            } catch (tr: Throwable) {
                tr.printStackTrace()
            }
            try {
                return telephonyManager.getMeid(1)
            } catch (tr: Throwable) {
                tr.printStackTrace()
            }
        }
        return "error"
    }

    fun getMeid(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            try {
                return telephonyManager.meid
            } catch (tr: Throwable) {
                tr.printStackTrace()
            }
            try {
                return telephonyManager.getMeid(0)
            } catch (tr: Throwable) {
                tr.printStackTrace()
            }
            try {
                return telephonyManager.getMeid(1)
            } catch (tr: Throwable) {
                tr.printStackTrace()
            }
            "error"
        } else {
            "RequireApi(26)"
        }
    }

    fun getOaid(app: Application?) {
        Thread {
            OAIDHelper.get().useSdk(true).init(app!!, object : InitListener {
                override fun onSuccess(oaid: String?) {
                    Log.i("Sherlock", "getOaidSuccess: $oaid")
                }

                override fun onFailure(error: OAIDError) {
                    Log.i("Sherlock", "getOaidFailed:" + error.errCode + "-" + error.errMsg)
                }
            })
        }.start()
    }

    val widevineID: String
        get() = try {
            //See https://stackoverflow.com/questions/16369818/how-to-get-crypto-scheme-uuid
            //You can find some UUIDs in the https://github.com/google/ExoPlayer source code
            val uuid = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
            val mediaDrm = MediaDrm(uuid)
            val widevineId = mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
            val sb = StringBuilder()
            for (aByte in widevineId) {
                sb.append(String.format("%02x", aByte))
            }
            sb.toString()
        } catch (tr: Throwable) {
            tr.printStackTrace()
            tr.javaClass.simpleName
        }

    fun getGaid(context: Context) {
        try {
            Thread {
                val ret = AdvertisingIdClient.getAdvertisingIdInfo(context).id
                Handler(Looper.getMainLooper()).post {
                    Log.i("Sherlock", "GAID: $ret")
                    Toast.makeText(context, "GAID:$ret", Toast.LENGTH_SHORT).show()
                }
            }.start()
        } catch (tr: Throwable) {
            tr.printStackTrace()
            tr.javaClass.simpleName
        }
    }

    fun getNai(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val telephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                telephonyManager.nai
            } catch (tr: Throwable) {
                tr.printStackTrace()
                tr.javaClass.simpleName
            }
        } else {
            "RequireApi(28)"
        }
    }

    fun getImsi(context: Context): String {
        return try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.subscriberId
        } catch (tr: Throwable) {
            tr.printStackTrace()
            tr.javaClass.simpleName
        }
    }

    fun getSimSerialNumber(context: Context): String {
        return try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephonyManager.simSerialNumber
        } catch (tr: Throwable) {
            tr.printStackTrace()
            tr.javaClass.simpleName
        }
    }

    val serial: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Build.getSerial()
            } catch (tr: Throwable) {
                tr.javaClass.simpleName
            }
        } else {
            Build.SERIAL
        }

    fun getAndroidID(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun getMacAddress(context: Context): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val all: List<NetworkInterface> =
                    Collections.list(NetworkInterface.getNetworkInterfaces())
                for (nif in all) {
                    if (!nif.name.equals("wlan0", ignoreCase = true)) continue
                    val macBytes = nif.hardwareAddress ?: return null
                    val res1 = StringBuilder()
                    for (b in macBytes) {
                        res1.append(String.format("%02X:", b))
                    }
                    if (res1.isNotEmpty()) {
                        res1.deleteCharAt(res1.length - 1)
                    }
                    return res1.toString()
                }
                "empty"
            } catch (tr: Throwable) {
                tr.printStackTrace()
                tr.javaClass.simpleName
            }
        } else {
            try {
                val wifiManager =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                wifiInfo.macAddress.trim { it <= ' ' }
            } catch (tr: Throwable) {
                tr.printStackTrace()
                tr.javaClass.simpleName
            }
        }
    }

    fun getIP(context: Context): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val all: List<NetworkInterface> =
                    Collections.list(NetworkInterface.getNetworkInterfaces())
                for (nif in all) {
                    if (!nif.name.equals("wlan0", ignoreCase = true)) continue
                    val macBytes = nif.hardwareAddress ?: return null
                    val res1 = StringBuilder()
                    for (b in macBytes) {
                        res1.append(String.format("%02X:", b))
                    }
                    if (res1.isNotEmpty()) {
                        res1.deleteCharAt(res1.length - 1)
                    }
                    return res1.toString()
                }
                "empty"
            } catch (tr: Throwable) {
                tr.printStackTrace()
                tr.javaClass.simpleName
            }
        } else {
            try {
                val wifiManager =
                    context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                wifiInfo.ipAddress.toString()
            } catch (tr: Throwable) {
                tr.printStackTrace()
                tr.javaClass.simpleName
            }
        }
    }

    fun getLastKnownLocation(context: Context): String {
        return try {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                val location = locationManager.getLastKnownLocation(provider!!)
                if (location != null) {
                    return location.longitude.toString() + "," + location.latitude
                }
            }
            "empty;"
        } catch (tr: Throwable) {
            tr.printStackTrace()
            tr.javaClass.simpleName
        }
    }

    fun getCellLocation(context: Context): String {
        return try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return when (val location = telephonyManager.cellLocation) {
                is GsmCellLocation -> {
                    "${location.lac},${location.cid}"
                }
                is CdmaCellLocation -> {
                    "${location.baseStationLongitude/14400f},${location.baseStationLatitude/14400f}"
                }
                else -> {
                    "Unknown class ${location.javaClass.simpleName}"
                }
            }
        } catch (tr: Throwable) {
            tr.printStackTrace()
            tr.javaClass.simpleName
        }
    }

    fun getNetworkInfo(context: Context): String {
        try {
            NetworkInterface.getNetworkInterfaces()
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) ?: return "无网络连接"
            connectivityManager as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val networkCapabilities =
                        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                    if (networkCapabilities == null) {
                        return "请打开网络"
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return "当前使用移动网络"
                    } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return "当前使用WIFI网络"
                    }
                } catch (tr: Throwable) {
                    tr.printStackTrace()
                }
            }
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null) {
                return when (activeNetworkInfo.type) {
                    ConnectivityManager.TYPE_MOBILE -> "当前使用移动网络"
                    ConnectivityManager.TYPE_WIFI -> "当前使用WIFI网络"
                    ConnectivityManager.TYPE_BLUETOOTH -> "当前使用蓝牙网络"
                    else -> "UNKNOWN"
                }
            }
            val wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (wifiInfo != null && wifiInfo.state == NetworkInfo.State.CONNECTED) {
                return "当前使用WIFI网络"
            }
            val networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            if (networkInfo != null && networkInfo.state == NetworkInfo.State.CONNECTED) {
                return networkInfo.subtypeName
            }
        } catch (tr: Throwable) {
            tr.printStackTrace()
            return tr.javaClass.simpleName
        }
        return "empty"
    }

    fun getInstalledPackageSize(context: Context): Int {
        val flags = (PackageManager.GET_META_DATA
                or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES)
                or PackageManager.GET_PROVIDERS
                or PackageManager.GET_ACTIVITIES
                or PackageManager.GET_SERVICES
                or PackageManager.GET_RECEIVERS)
        return context.packageManager.getInstalledPackages(flags).size
    }

    fun getContactsSize(context: Context): Int {
        var ret = -1
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            ret = cursor.count
            cursor.close()
        }
        return ret
    }

    fun getSmsSize(context: Context): Int {
        var ret = -1
        val uri = Uri.parse("content://sms/inbox")
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            ret = cursor.count
            cursor.close()
        }
        return ret
    }

    fun getCallLogSize(context: Context): Int {
        var ret = -1
        val uri = CallLog.Calls.CONTENT_URI
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            ret = cursor.count
            cursor.close()
        }
        return ret
    }

    val storageDirSize: Int
        get() {
            var ret = -1
            val files = Environment.getExternalStorageDirectory().listFiles()
            if (files != null) {
                ret = 0
                for (file in files) {
                    if (file.isDirectory) {
                        ret += 1
                    }
                }
            }
            return ret
        }

    fun getClipBroad(context: Context): String {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var text = ""
        clipboardManager.primaryClip?.let {
            text =
                clipboardManager.primaryClip!!.getItemAt(0)?.text.toString().trim().replace("=", "")
        }
        clipboardManager.setPrimaryClip(ClipData.newPlainText("Base64",
            String(Base64.decode(text, Base64.NO_PADDING))))
        return text
    }

    fun getAccountsSize(context: Context): Int {
        val manager = AccountManager.get(context)
        return manager.accounts.size
    }

    fun getPhoneNumber(context: Context): String {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        try {
            return telephonyManager.line1Number
        } catch (tr: Throwable) {
            tr.printStackTrace()
        }
        try {
            return telephonyManager.simOperator
        } catch (tr: Throwable) {
            tr.printStackTrace()
        }
        try {
            return telephonyManager.networkOperator
        } catch (tr: Throwable) {
            tr.printStackTrace()
        }
        try {
            return telephonyManager.networkOperatorName
        } catch (tr: Throwable) {
            tr.printStackTrace()
        }
        try {
            return telephonyManager.simOperatorName
        } catch (tr: Throwable) {
            tr.printStackTrace()
        }
        return "empty"
    }

    fun getSensor(context: Context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val values = event!!.values
                val x = values[0]
                val y = values[1]
                val z = values[2]
                val threshold = 19
                if (abs(x) > threshold || abs(y) > threshold || abs(z) > threshold) {
                    Toast.makeText(context, "摇一摇", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // ignore
            }
        }
        try {
            sensorManager.registerListener(sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL)
            Handler(Looper.getMainLooper()).postDelayed({
                sensorManager.unregisterListener(sensorListener)
            }, 5000L)
        } catch (ignore: Throwable) {

        }
    }

}