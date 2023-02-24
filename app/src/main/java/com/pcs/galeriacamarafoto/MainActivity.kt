package com.pcs.galeriacamarafoto

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pcs.galeriacamarafoto.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var uriFoto: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btGaleria1.setOnClickListener {
            if (applicationContext.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                abrirGaleria.launch(
                    Intent(
                        Intent.ACTION_GET_CONTENT,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    ).apply {
                        type = "image/*"
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                )
            } else {
                pedirPermisoLectura.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        binding.btTomarFoto.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (applicationContext.checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    tomarFoto()
                } else {
                    pedirPermisoCamaraSd.launch(arrayOf(android.Manifest.permission.CAMERA))
                }
            } else {
                if (applicationContext.checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    applicationContext.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                ) {
                    tomarFoto()
                } else {
                    pedirPermisoCamaraSd.launch(
                        arrayOf(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                }
            }
        }

        binding.btGuardar.setOnClickListener {
            if(binding.ivFoto.drawable == null) {
                mostrarMensaje("ADVERTENCIA", "No existe imagen para guardar")
                return@setOnClickListener
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                pedirPermisoEscritura.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                guardarFotoImageview()
            }
        }
    }

    private fun mostrarMensaje(titulo: String, mensaje: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun obtenerRutaDesdeUri(uri: Uri): String {
        var resultado = uri.path.toString()

        val cursor = this.contentResolver.query(uri, null, null, null, null)

        if (cursor != null) {
            cursor.moveToFirst()
            val indiceColumna = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            resultado = cursor.getString(indiceColumna)
            cursor.close()
        }

        return resultado
    }

    private val abrirGaleria =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                try {
                    uriFoto = it.data?.data

                    //Imagen en mapa de bit
                    /*if(Build.VERSION.SDK_INT < 28) {
                        val bitmap = MediaStore.Images.Media.getBitmap(
                            this.contentResolver,
                            uriFoto
                        )
                        binding.ivFoto.setImageBitmap(bitmap)
                    } else {
                        val source = ImageDecoder.createSource(this.contentResolver, uriFoto!!)
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        binding.ivFoto.setImageBitmap(bitmap)
                    }*/

                    binding.ivFoto.setImageURI(uriFoto)

                    //mostrarMensaje("RUTA_REAL", obtenerRutaDesdeUri(it.data?.data!!))
                } catch (e: Exception) {
                    android.util.Log.e("ERROR", e.message.toString())
                    mostrarMensaje("ERROR", e.message.toString())
                }
            }
        }

    private val pedirPermisoLectura =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                abrirGaleria.launch(
                    Intent(
                        Intent.ACTION_GET_CONTENT,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    ).apply {
                        type = "image/*"
                    }
                )
            } else {
                Toast.makeText(this, "Necesita otorgar permiso de lectura al sd", Toast.LENGTH_LONG)
                    .show()
            }
        }

    private fun tomarFoto() {
        try {
            val myUriImagen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            else
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val contenido = ContentValues()
            contenido.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                System.currentTimeMillis().toString() + ".jpeg"
            )
            contenido.put(MediaStore.Images.Media.MIME_TYPE, "images/*")

            uriFoto = contentResolver.insert(myUriImagen, contenido)

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriFoto)

            startForResultCamara.launch(intent)
        } catch (e: Exception) {
            mostrarMensaje("ERROR", e.message.toString())
        }

    }

    private val startForResultCamara =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                binding.ivFoto.setImageURI(uriFoto)
            }
        }

    private val pedirPermisoCamaraSd =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permisos ->
            var estado = true
            permisos.entries.forEach {
                if (!it.value)
                    estado = false
            }

            if (estado) {
                tomarFoto()
            } else {
                Toast.makeText(
                    this,
                    "Necesita otorgar permisos de camara y sd",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun guardarFotoImageview() {
        val uriImagen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        else
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val contenido = ContentValues()
        contenido.put(
            MediaStore.Images.Media.DISPLAY_NAME,
            System.currentTimeMillis().toString() + ".jpeg"
        )
        contenido.put(MediaStore.Images.Media.MIME_TYPE, "images/*")

        val uri = contentResolver.insert(uriImagen, contenido)

        try {
            val bitmapDrawable = binding.ivFoto.drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap

            val outputStream = contentResolver.openOutputStream(uri!!)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            Objects.requireNonNull(outputStream)

            mostrarMensaje("HECHO", uriFoto.toString())
        } catch (e: Exception) {
            mostrarMensaje("ERROR", e.message.toString())
        }
    }

    private val pedirPermisoEscritura =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                guardarFotoImageview()
            } else {
                Toast.makeText(
                    this,
                    "Necesita otorgar permiso de escritura al sd",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
}