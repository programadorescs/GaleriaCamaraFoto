# Tomar y Guardar una Foto en la Galería de Fotos con Kotlin

Este es un pequeño ejemplo de cómo tomar una foto con la cámara de un dispositivo Android y guardarla en la galería de fotos. También se incluye cómo abrir la foto guardada en la galería de fotos.

## Pre-requisitos

    Un dispositivo Android con cámara.
    Android Studio instalado en su computadora.
    Conocimiento básico de Kotlin.

## Pasos

1. Primero, agregue permisos de cámara y almacenamiento externo en el archivo AndroidManifest.xml:
```
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.CAMERA"/>
```
2. Cree un botón en su diseño de interfaz de usuario (activity_main.xml) para tomar una foto:
```
    <ImageView
        android:id="@+id/iv_foto"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:layout_marginStart="8dp"
        android:layout_marginTop="8dp"
        android:layout_marginEnd="8dp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        tools:srcCompat="@tools:sample/avatars" />

    <Button
        android:id="@+id/bt_galeria_1"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp"
        android:layout_marginTop="8dp"
        android:layout_marginEnd="8dp"
        android:text="Galeria 1"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/iv_foto" />

    <Button
        android:id="@+id/bt_tomar_foto"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp"
        android:layout_marginTop="8dp"
        android:layout_marginEnd="8dp"
        android:text="Tomar foto"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/bt_galeria_1" />

    <Button
        android:id="@+id/bt_guardar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp"
        android:layout_marginTop="8dp"
        android:layout_marginEnd="8dp"
        android:text="Guardar Imagen ImageView"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/bt_tomar_foto" />
```
3. En el archivo MainActivity.kt, declare las siguientes variables:
```kotlin
private var uriFoto: Uri? = null
```
4. Crear un metodo para abrir la cámara y tomar una foto:
```kotlin
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
```

5. Agregue el siguiente código para abrir la galería de imagenes y visualizarla en un ImageView:

```kotlin
    private val abrirGaleria =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                try {
                    uriFoto = it.data?.data
                    binding.ivFoto.setImageURI(uriFoto)
                } catch (e: Exception) {
                    mostrarMensaje("ERROR", e.message.toString())
                }
            }
        }
```

6. Código para guardar la imagen que se encuentra en un ImageView:

```kotlin
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
```

7. Código para el botón Galería:

```kotlin
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
```

8. Código para el botón Tomar Foto:

```kotlin
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
```

9. Código para el botón Guardar Imagen ImageView:

```kotlin
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
```

## Conclusión

Tomar y guardar una foto en la galería de fotos de un dispositivo Android es un proceso relativamente sencillo de implementar en Kotlin. Con los pasos descritos anteriormente, se puede crear una aplicación básica que permita a los usuarios tomar una foto y guardarla en la galería de fotos de su dispositivo. Además, se ha incluido el código necesario para abrir la imagen guardada en la galería de fotos, lo que permite a los usuarios ver la imagen que han tomado. Asímismo se incluye código para pedir permiso de escritura, lectura y cámara en tiempo de ejecución.