package com.laochinabank.mb


import android.util.Log
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.location.LocationServices
import com.huawei.hms.location.LocationSettingsRequest
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import com.huawei.hms.location.LocationRequest

private const val HMS_LOCATION = "com.laochinabank.mb/hms_location"

class MainActivity : FlutterFragmentActivity() {

    private var channel: MethodChannel? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, HMS_LOCATION)

        channel?.setMethodCallHandler { call, result ->
            when (call.method) {
                "getLocation" -> {
                    val settingsClient = LocationServices.getSettingsClient(this)
                    val builder = LocationSettingsRequest.Builder()
                    var mLocationRequest = LocationRequest()
                    builder.addLocationRequest(mLocationRequest)
                    val locationSettingsRequest = builder.build()
                    // 检查设备定位设置
                    settingsClient.checkLocationSettings(locationSettingsRequest)
                        // 检查设备定位设置接口调用成功监听
                        .addOnSuccessListener(OnSuccessListener { locationSettingsResponse ->
                            val locationSettingsStates =
                                locationSettingsResponse.locationSettingsStates
                            val stringBuilder = StringBuilder()
                            // 定位开关是否打开
                            stringBuilder.append("isLocationUsable=")
                                .append(locationSettingsStates.isLocationUsable)
                            // HMS Core是否可用
                            stringBuilder.append(",\nisHMSLocationUsable=")
                                .append(locationSettingsStates.isHMSLocationUsable)
                            Log.i("TAG", "checkLocationSetting onComplete:$stringBuilder")
                        })
                        // 检查设备定位设置接口失败监听回调
                        .addOnFailureListener(OnFailureListener { e ->
                            Log.i("TAG", "checkLocationSetting onFailure:" + e.message)
                        })
                    result.success(true)
                }
            }
        }
    }
}
