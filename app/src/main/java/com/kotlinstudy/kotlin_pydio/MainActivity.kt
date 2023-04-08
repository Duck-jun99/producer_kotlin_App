package com.kotlinstudy.kotlin_pydio

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.kotlinstudy.kotlin_pydio.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity(), SensorEventListener {

    val api by lazy { MyApi.create() }

    private lateinit var btnSensor : Button
    private lateinit var etSensor : EditText
    private lateinit var etLogID : EditText

    //아래 네줄은 센서를 위한 코드
    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null
    private var moment_log_ID : Int = 0
    private var moment_sensor_x : Float = 0F


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSensor = findViewById<Button>(R.id.btn_sensor)
        etLogID = findViewById<EditText>(R.id.et_log_id)
        etSensor = findViewById<EditText>(R.id.et_sensor)

        //센서 매니저 초기화 코드
        initSensorManager()

        moment_sensor_x

        btnSensor.setOnClickListener {
            Log.e("Log_ID, Sensor_x", "지금부터 1분간(delay값) 서버에 계속 값 전달")

            //val Log_ID : String = etLogID.text.toString()
            //val Sensor_x : String = etSensor.text.toString()
            
            //10초마다 
            // 1)Log_ID는 1씩 계속 증가,
            // Log_ID와 Sensor_x 값을 Server로 전송
            val timer = fixedRateTimer(name = "Sensor Logger", period = 10000) {
                moment_log_ID++

                val Log_ID : String = moment_log_ID.toString()
                val Sensor_x : String = moment_sensor_x.toString()

                Log.e("Log_ID, Sensor_x", "Log_ID: $Log_ID,\n Sensor_x: $Sensor_x\n 두개 값 POST")

                //Post방식으로 서버에 전달할 데이터를 파라미터에 입력
                //val postModel = PostModel(etLogID.text.toString(), etSensor.text.toString()) //사용 안함
                api.insertData(Log_ID, Sensor_x).enqueue(object : Callback<PostModel>
                {

                    //서버 요청 성공
                    override fun onResponse(call: Call<PostModel>, response: Response<PostModel>) {
                        Log.e("Successful Message: ", "데이터 성공적으로 수신")
                        Log.e("Result: ", response.body().toString())
                    }
                    //서버 요청 실패
                    override fun onFailure(call: Call<PostModel>, t: Throwable)
                    {
                        Log.e("Error Message : ",  t.message.toString())
                    }
                })

                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Log_ID: $Log_ID,\n Sensor_x: $Sensor_x\n 두개 값 POST", Toast.LENGTH_SHORT).show()
                }

            }
            runBlocking {
                delay(60000) // 프로그램을 1분간 실행
                timer.cancel() // 타이머 중단
            }


        }
    }

    private fun initSensorManager() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == accelerometerSensor) {
            moment_sensor_x = event!!.values[0]

            //Toast.makeText(applicationContext, sensor_x, Toast.LENGTH_SHORT).show()
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }
}