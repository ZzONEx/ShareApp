package ru.hutao.shareappbyme

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import ru.hutao.shareappbyme.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    private var textToShare = ""
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.shareImg.setOnClickListener {
            pickImage()

        }
        binding.shareText.setOnClickListener {
            textToShare = binding.shareText.text.toString()
            if (textToShare.isEmpty()) {
                showToast("Введите текст")
            } else {
                shareText()
            }

        }
        binding.btnShareImg.setOnClickListener {
            if (imageUri == null) {
                showToast("Выберите картинку")
            } else {
                shareImage()
            }
        }
    }

    private fun shareImage() {
       val contentUri = getContentUri()
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/plain"
        intent.putExtra(Intent.EXTRA_STREAM, contentUri)

        intent.putExtra(Intent.EXTRA_SUBJECT, "Картинка из приложения ShareBomjik")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(Intent.createChooser(intent, "Выберите приложение:"))
    }

    private fun getContentUri(): Uri {
        val bitmap: Bitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val source = ImageDecoder.createSource(contentResolver, imageUri!!)
            bitmap = ImageDecoder.decodeBitmap(source)
        } else {
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        }
        val imageFolder = File(cacheDir, "images")
        var contentUri: Uri? = null
        try{
            imageFolder.mkdir()
            val file = File(imageFolder, "shared_image.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            contentUri = FileProvider.getUriForFile(this, "ru.hutao.shareappbyme.fileprovider", file)
        } catch (e: java.lang.Exception){
            showToast("${e.message}")
        }
        return contentUri!!
    }

    private fun shareText() {
    val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, textToShare)
        intent.putExtra(Intent.EXTRA_SUBJECT, "Текст из приложения ShareBomjik")
        startActivity(Intent.createChooser(intent, "Выберите приложение:"))
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            imageUri = intent!!.data
            binding.shareImg.setImageURI((imageUri))
        } else {
            showToast("Выбор отменён")
        }
    }
}