package com.kotlinstudy.producer_kotlin_App

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
import android.widget.Toast
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback
import java.util.*
class MainActivity : AppCompatActivity(), SensorEventListener {

    val insert_api by lazy { InsertApi.create() }
    val delete_api by lazy { DeleteApi.create() }

    private lateinit var btnSensor : Button
    private lateinit var btnClose : Button
    private lateinit var btnDbDel : Button
    private lateinit var tvPostFirst : TextView
    private lateinit var tvPostLast : TextView

    //센서를 위한 코드
    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null
    //private var moment_log_ID : Int = 0
    private var moment_sensor_x : Float = 0F
    private var moment_sensor_y : Float = 0F
    private var moment_sensor_z : Float = 0F

    private lateinit var Log_ID : String
    private lateinit var Sensor_x : String
    private lateinit var Sensor_y : String
    private lateinit var Sensor_z : String

    //Coroutine 사용
    private val scope = CoroutineScope(Dispatchers.Default)

    //timer 설정
    private var timer: Job? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSensor = findViewById<Button>(R.id.btn_sensor)
        btnClose = findViewById<Button>(R.id.btn_post_close)
        btnDbDel = findViewById<Button>(R.id.btn_db_delete)

        //센서 매니저 초기화 코드
        initSensorManager()

        btnSensor.setOnClickListener {

            Toast.makeText(this, "지금부터 1분간(delay값) 서버에 계속 값 전달", Toast.LENGTH_SHORT).show()

            //0.5초 마다 --> 0.1초로 수정
            // 1)Log_ID시간 적용
            // Log_ID와 Sensor_x 값을 Server로 전송
            timer = scope.launch {
                repeat(100) { //100번 반복

                    //moment_log_ID를 현재 시간으로 설정
                    var moment_log_ID = generateLogID()

                    Log_ID = moment_log_ID.toString()
                    Sensor_x = moment_sensor_x.toString()
                    Sensor_y = moment_sensor_y.toString()
                    Sensor_z = moment_sensor_z.toString()

                    Log.e("Log_ID, Sensor_x,y,z", "Log_ID: $Log_ID,\n Sensor_x: $Sensor_x\n Sensor_y: $Sensor_y\n Sensor_z: $Sensor_z\n 4개 값 POST")


                    //Post방식으로 서버에 전달할 데이터를 파라미터에 입력
                    //val postModel = PostModel(etLogID.text.toString(), etSensor.text.toString()) //사용 안함
                    insert_api.insertData(Log_ID, Sensor_x, Sensor_y, Sensor_z).enqueue(object : Callback<InsertPostModel>
                    {
                        //서버 요청 성공
                        override fun onResponse(call: Call<InsertPostModel>, response: Response<InsertPostModel>) {
                            Log.e("Successful Message: ", "데이터 성공적으로 수신")
                            Log.e("Result: ", response.body().toString())
                        }
                        //서버 요청 실패
                        override fun onFailure(call: Call<InsertPostModel>, t: Throwable)
                        {
                            Log.e("Error Message : ",  t.message.toString())
                        }
                    })

                    withContext(Dispatchers.Main) {
                        //Toast.makeText(this@MainActivity, "Log_ID: $Log_ID,\n Sensor_x: $Sensor_x\n 두개 값 POST", Toast.LENGTH_SHORT).show()
                    }

                    delay(100 /*10초: 10000*/)
                }
            }
        }


        btnClose.setOnClickListener {

            //타이머를 꺼서 post 중지
            timer?.cancel()
            timer = null

            Toast.makeText(this, "Post 중지, 타이머 종료", Toast.LENGTH_SHORT).show()

        }

        btnDbDel.setOnClickListener{
            //sensor 테이블의 모든 데이터를 delete하기 위한 부분.
            delete_api.deleteData().enqueue(object : Callback<InsertPostModel>
            {
                //서버 요청 성공
                override fun onResponse(call: Call<InsertPostModel>, response: Response<InsertPostModel>) {
                    Log.e("Successful Message: ", "데이터 성공적으로 수신")
                    Log.e("Result: ", response.body().toString())
                }
                //서버 요청 실패
                override fun onFailure(call: Call<InsertPostModel>, t: Throwable)
                {
                    Log.e("Error Message : ",  t.message.toString())
                }
            })

            Toast.makeText(this, "모든 데이터 삭제", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateLogID(): String {
        val currentTime = Calendar.getInstance()
        val year = currentTime.get(Calendar.YEAR)
        val month = currentTime.get(Calendar.MONTH) + 1
        val day = currentTime.get(Calendar.DAY_OF_MONTH)
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute = currentTime.get(Calendar.MINUTE)
        val second = currentTime.get(Calendar.SECOND)
        val millis = currentTime.get(Calendar.MILLISECOND).toString().padStart(3, '0')
        return "$year/$month/$day/$hour/$minute/$second/$millis"
    }

    private fun initSensorManager() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor == accelerometerSensor) {
                moment_sensor_x = event!!.values[0]
                moment_sensor_y = event!!.values[1]
                moment_sensor_z = event!!.values[2]
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

        // 타이머 중지
        timer?.cancel()
        timer = null
    }
}