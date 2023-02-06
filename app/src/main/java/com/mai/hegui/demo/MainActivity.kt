package com.mai.hegui.demo

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bun.miitmdid.core.MdidSdkHelper
import com.mai.hegui.demo.databinding.ActivityMainBinding
import com.permissionx.guolindev.PermissionX
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binder = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binder.root)
        binder.recycler.layoutManager = GridLayoutManager(this, 2)
        binder.recycler.addItemDecoration(DividerItemDecoration(this,
            DividerItemDecoration.VERTICAL))
        binder.recycler.adapter = Adapter(getData())
        MdidSdkHelper.logd(true, "test123")
    }

    /**
     * 参照 《腾讯应用开放平台-隐私政策整改方法》
     *
     * @link {https://wikinew.open.qq.com/index.html#/iwiki/886144166}
     */
    private fun getData(): List<Item> {
        val ret = LinkedList<Item>()
        ret.add(Item("IMEI",
            object : Callback(this@MainActivity, Manifest.permission.READ_PHONE_STATE) {
                override fun callback(activity: MainActivity): String {
                    return "IMEI:" + Helper.getImei(activity)
                }
            }))
        ret.add(Item("MEID",
            object : Callback(this@MainActivity, Manifest.permission.READ_PHONE_STATE) {
                override fun callback(activity: MainActivity): String {
                    return "MEID:" + Helper.getMeid(activity)
                }
            }))
        ret.add(Item("OAID", object : Callback(this@MainActivity) {
            override fun callback(activity: MainActivity): String {
                Helper.getOaid(application)
                return "OAID为异步获取"
            }
        }))
        ret.add(Item("WidevineID", object : Callback(this@MainActivity) {
            override fun callback(activity: MainActivity): String {
                return "WidevineID:" + Helper.widevineID
            }
        }))
        ret.add(Item("GAID", object : Callback(this@MainActivity) {
            override fun callback(activity: MainActivity): String {
                Helper.getGaid(activity)
                return "GAID为异步获取"
            }
        }))
        ret.add(Item("NAI",
            object : Callback(this@MainActivity, Manifest.permission.READ_PHONE_STATE) {
                override fun callback(activity: MainActivity): String {
                    return "NAI:" + Helper.getNai(activity)
                }
            }))
        ret.add(Item("用户识别码",
            object : Callback(this@MainActivity, Manifest.permission.READ_PHONE_STATE) {
                override fun callback(activity: MainActivity): String {
                    return "IMSI:" + Helper.getImsi(activity)
                }
            }))
        ret.add(Item("ICCID",
            object : Callback(this@MainActivity, Manifest.permission.READ_PHONE_STATE) {
                override fun callback(activity: MainActivity): String {
                    return "SimSerial:" + Helper.getSimSerialNumber(activity)
                }
            }))
        ret.add(Item("Serial",
            object : Callback(this@MainActivity, Manifest.permission.READ_PHONE_STATE) {
                override fun callback(activity: MainActivity): String {
                    return "Serial:" + Helper.serial
                }
            }))
        ret.add(Item("AndroidID", object : Callback(this@MainActivity) {
            override fun callback(activity: MainActivity): String {
                return "Android:" + Helper.getAndroidID(activity)
            }
        }))
        ret.add(Item("MAC", object : Callback(this@MainActivity) {
            override fun callback(activity: MainActivity): String {
                return "MAC:" + Helper.getMacAddress(activity)
            }
        }))
        ret.add(Item("IP", object : Callback(this@MainActivity) {
            override fun callback(activity: MainActivity): String {
                return "IP:" + Helper.getIP(activity)
            }
        }))
        ret.add(Item("LOCATION",
            object : Callback(this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION) {
                override fun callback(activity: MainActivity): String {
                    return "Location:" + Helper.getLastKnownLocation(activity)
                }
            }))
        ret.add(Item("Cell Location",
            object : Callback(this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION) {
                override fun callback(activity: MainActivity): String {
                    return "CellLocation:" + Helper.getCellLocation(activity)
                }
            }))
        ret.add(Item("网络接入标识等", object : Callback(this@MainActivity) {
            override fun callback(activity: MainActivity): String {
                return "网络接入标识等: " + Helper.getNetworkInfo(activity)
            }
        }))
        ret.add(Item("已安装应用列表", object : Callback(this@MainActivity,
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) listOf(Manifest.permission.QUERY_ALL_PACKAGES) else emptyList())) {
            override fun callback(activity: MainActivity): String {
                return "已安装应用数: " + Helper.getInstalledPackageSize(activity)
            }
        }))
        ret.add(Item("通讯录",
            object : Callback(this@MainActivity, Manifest.permission.READ_CONTACTS) {
                override fun callback(activity: MainActivity): String {
                    return "通讯录数: " + Helper.getContactsSize(activity)
                }
            }))
        ret.add(Item("短信数", object : Callback(this@MainActivity, Manifest.permission.READ_SMS) {
            override fun callback(activity: MainActivity): String {
                return "短信数: " + Helper.getSmsSize(activity)
            }
        }))
        ret.add(Item("通话记录",
            object : Callback(this@MainActivity, Manifest.permission.READ_CALL_LOG) {
                override fun callback(activity: MainActivity): String {
                    return "通话记录数: " + Helper.getCallLogSize(activity)
                }
            }))
        ret.add(Item("本地存储",
            object : Callback(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE) {
                override fun callback(activity: MainActivity): String {
                    return "目录数: " + Helper.storageDirSize
                }
            }))
        ret.add(Item("剪切板",
            object : Callback(this@MainActivity) {
                override fun callback(activity: MainActivity): String {
                    return "剪切板: " + Helper.getClipBroad(activity)
                }
            }))
        ret.add(Item("设备账户信息",
            object : Callback(this@MainActivity, Manifest.permission.GET_ACCOUNTS) {
                override fun callback(activity: MainActivity): String {
                    return "账户数: " + Helper.getAccountsSize(activity)
                }
            }))
        ret.add(Item("手机号码|服务商",
            object : Callback(this@MainActivity, Manifest.permission.READ_PHONE_STATE) {
                override fun callback(activity: MainActivity): String {
                    return "手机号: " + Helper.getPhoneNumber(activity)
                }
            }))
        ret.add(Item("传感器信息",
            object : Callback(this@MainActivity) {
                override fun callback(activity: MainActivity): String {
                    Helper.getSensor(activity)
                    return "传感器"
                }
            }))
        return ret
    }

    private fun toast(msg: String) {
        Log.i("Sherlock", msg)
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    class Adapter(private val data: List<Item>) :
        RecyclerView.Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(Button(parent.context))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }

    class ViewHolder(private val btn: Button) : RecyclerView.ViewHolder(
        btn) {
        fun bind(item: Item) {
            btn.text = item.label
            btn.setOnClickListener { item.callback.invoke() }
        }

        init {
            btn.setLines(1)
            btn.maxLines = 1
            itemView.layoutParams = FrameLayout.LayoutParams(-1, -2)
        }
    }

    class Item(
        val label: String,
        val callback: Callback,
    )

    abstract class Callback(
        private val activity: MainActivity,
        private val permissions: List<String>? = null,
    ) {
        constructor(activity: MainActivity, vararg permissions: String) : this(activity,
            listOf<String>(*permissions))

        operator fun invoke() {
            if (permissions == null || permissions.isEmpty()) {
                granted()
            } else {
                PermissionX.init(activity)
                    .permissions(permissions)
                    .request { _, grantedList, _ ->
                        if (grantedList.size > 0) {
                            granted()
                        } else {
                            activity.toast("没有权限")
                        }
                    }
            }
        }

        private fun granted() {
            activity.toast(callback(activity))
        }

        protected abstract fun callback(activity: MainActivity): String

    }

}