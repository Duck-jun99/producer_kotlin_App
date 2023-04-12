package com.kotlinstudy.kotlin_pydio

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

// * SerializedName: 서버(PHP)에서 안드로이드에 응답할 때 보내는 배열(객체)의 키
//ex) array("Log_ID" => "$Log_ID" , "Sensor_x" => "$Sensor_x" )
// * 위의 PHP 코드에서 "Log_ID" 부분이 serializedName('파라미터값')의 파라미터 값 입니다.

data class PostModel(
    @Expose
    @SerializedName("log_ID")
    var log_ID: String? = null,

    @Expose
    @SerializedName("sensor_x")
    var sensor_x: String? = null,

    @Expose
    @SerializedName("sensor_y")
    var sensor_y: String? = null,

    @Expose
    @SerializedName("sensor_z")
    var sensor_z: String? = null
)