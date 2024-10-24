package com.laochinabank.mb


import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.location.FusedLocationProviderClient
import com.huawei.hms.location.LocationCallback
import com.huawei.hms.location.LocationRequest
import com.huawei.hms.location.LocationResult
import com.huawei.hms.location.LocationServices
import com.huawei.hms.location.LocationSettingsRequest
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.location.Location
import android.os.Build
import androidx.core.app.ActivityCompat
import com.google.gson.Gson
import com.huawei.hms.location.HWLocation
import com.huawei.hms.location.LocationAvailability



private const val HMS_LOCATION = "com.laochinabank.mb/hms_location"

class MainActivity : FlutterFragmentActivity() {

    private var channel: MethodChannel? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, HMS_LOCATION)

        channel?.setMethodCallHandler { call, result ->
            when (call.method) {
                "getLocation" -> {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                        Log.i(TAG, "android sdk <= 28 Q")
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            val strings = arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            ActivityCompat.requestPermissions(this, strings, 1)
                        }
                    } else {
                        // Android SDK > 28 所需权限动态申请，需添加“android.permission.ACCESS_BACKGROUND_LOCATION”权限。
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                this,
                                "android.permission.ACCESS_BACKGROUND_LOCATION"
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            val strings = arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                "android.permission.ACCESS_BACKGROUND_LOCATION"
                            )
                            ActivityCompat.requestPermissions(this, strings, 2)
                        }
                    }
                    val settingsClient = LocationServices.getSettingsClient(this)
                    val builder = LocationSettingsRequest.Builder()
                    val mLocationRequest = LocationRequest()
                    builder.addLocationRequest(mLocationRequest)
                    val locationSettingsRequest = builder.build()
                    var mLocationCallback: LocationCallback? = null
                    var mFusedLocationProviderClient: FusedLocationProviderClient
                    // 检查设备定位设置
                    settingsClient.checkLocationSettings(locationSettingsRequest)
                        // 检查设备定位设置接口调用成功监听
                        .addOnSuccessListener(OnSuccessListener { locationSettingsResponse ->
                            val locationSettingsStates =
                                locationSettingsResponse.locationSettingsStates
                            // HMS Core是否可用 」 定位开关是否打开
                            if (locationSettingsStates.isLocationUsable && locationSettingsStates.isHMSLocationUsable) {
                                // 获取FusedLocationProviderClient实例
                                mFusedLocationProviderClient =
                                    LocationServices.getFusedLocationProviderClient(this)

                                // 设置位置回调的时间间隔为6000ms，默认是5000ms。
                                mLocationRequest.setInterval(6000)

                                // 设置定位类型，PRIORITY_HIGH_ACCURACY为融合定位模式。
                                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

                                // 设置回调次数为100，默认值为Integer.MAX_VALUE；。
                                // 特殊情况：单次定位场景，只需设置回调次数为1，仅会发起一次定位，定位结束之后，也不需要移除定位请求。
                                mLocationRequest.setNumUpdates(1)
                                if (null == mLocationCallback) {
                                    mLocationCallback = object : LocationCallback() {
                                        override fun onLocationResult(locationResult: LocationResult?) {
                                            if (locationResult != null) {

                                                val location:HWLocation = locationResult.lastHWLocation
                                                result.success(Gson().toJson(location))
                                            }
                                        }

                                        override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                                            locationAvailability?.let {
                                                val flag: Boolean =
                                                    locationAvailability.isLocationAvailable
                                                Log.i(
                                                    TAG,
                                                    "onLocationAvailability isLocationAvailable:$flag"
                                                )
                                            }
                                        }
                                    }
                                }
                                // 发起定位
                                mFusedLocationProviderClient
                                    .requestLocationUpdates(
                                        mLocationRequest,
                                        mLocationCallback,
                                        Looper.getMainLooper()
                                    );
                            }

                        })
                        // 检查设备定位设置接口失败监听回调
                        .addOnFailureListener(OnFailureListener { e ->
                            Log.i("TAG", "checkLocationSetting onFailure:" + e.message)
                        })
                }
            }
        }
    }
}
