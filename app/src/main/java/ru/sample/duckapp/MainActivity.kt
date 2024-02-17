package ru.sample.duckapp

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.sample.duckapp.domain.Duck
import ru.sample.duckapp.infra.Api

class MainActivity : AppCompatActivity() {
    lateinit var imageView: ImageView;
    lateinit var editText: EditText;
    private val maxCode = 285;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageView)
        editText = findViewById(R.id.editTextDuckId)

        updateImage("")
    }

    fun codeNotFound(code: String) {
        Toast.makeText(applicationContext, "Code $code not found", Toast.LENGTH_SHORT).show()
    }

    fun networkError(){
        Toast.makeText(applicationContext, "Network error",
            Toast.LENGTH_SHORT).show()
    }

    fun isValidStatus(expected_max: Int, real_status: Int?): Boolean {
        return real_status != null && expected_max >= real_status
    }

    fun collectDuckImage(num: String) {
        val status: Int? = num.toIntOrNull()
        if (isValidStatus(maxCode, status)) {Api.ducksApi.getDuckImage(num).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val bitmap = BitmapFactory.decodeStream(response.body()?.byteStream())
                    imageView.setImageBitmap(bitmap)
                } else {
                    networkError()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })}
        else {
            codeNotFound(num)
            collectRandomDuck()
        }
        editText.text = null

    }

    fun collectRandomDuck() {
        Api.ducksApi.getRandomDuck().enqueue(object : Callback<Duck> {
            override fun onResponse(call: Call<Duck>, response: Response<Duck>) {
                if (response.isSuccessful) {
                    Picasso.get().load(response.body()?.url).into(imageView)
                }
            }

            override fun onFailure(call: Call<Duck>, t: Throwable) {}
        })
    }

    fun updateImage(num: String) {
        if (!num.isBlank() && num.isDigitsOnly()) {
            collectDuckImage(num)
        } else if (num.isBlank()) {
            collectRandomDuck()
        }
    }

    fun onSendClick(view: View) {
        updateImage(editText.text.toString())
    }
}