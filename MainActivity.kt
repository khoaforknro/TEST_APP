package com.plock.wibubankqr

import android.content.ContentValues
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import java.io.File
import java.io.FileOutputStream

private val Pink = Color(0xFFE96A9A)
private val Pale = Color(0xFFFFF8FB)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WibuBankQRApp() }
    }
}

@Composable
fun WibuBankQRApp() {
    val context = LocalContext.current
    var qrUri by remember { mutableStateOf<Uri?>(null) }
    var bgUri by remember { mutableStateOf<Uri?>(null) }
    var receiver by remember { mutableStateOf("NGUYEN VAN A") }
    var bank by remember { mutableStateOf("MB BANK") }
    var account by remember { mutableStateOf("****1234") }
    var title by remember { mutableStateOf("WIBU PAYMENT") }

    val pickQr = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> qrUri = uri }

    val pickBg = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> bgUri = uri }

    fun saveCard() {
        val qr = qrUri ?: run {
            Toast.makeText(context, "Chọn ảnh QR trước", Toast.LENGTH_SHORT).show()
            return
        }

        val qrBitmap = loadBitmap(context, qr)
        val bgBitmap = bgUri?.let { loadBitmap(context, it) }
        val bitmap = WibuCardRenderer.render(
            qr = qrBitmap,
            background = bgBitmap,
            title = title,
            receiver = receiver,
            bank = bank,
            account = account
        )

        val name = "wibu_qr_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/WibuBankQR")
        }
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            Toast.makeText(context, "Đã lưu vào Pictures/WibuBankQR", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = Pale,
        topBar = {
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wibu Bank QR", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text("v0.1", color = Pink, fontSize = 12.sp)
                }
            }
        }
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(Modifier.padding(14.dp)) {
                    WibuPreview(
                        qrUri = qrUri,
                        bgUri = bgUri,
                        title = title,
                        receiver = receiver,
                        bank = bank,
                        account = account
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        pickQr.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                ) { Text("Chọn QR") }

                OutlinedButton(
                    onClick = {
                        pickBg.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                ) { Text("Ảnh anime") }
            }

            Spacer(Modifier.height(8.dp))

            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            TextField(
                value = receiver,
                onValueChange = { receiver = it },
                label = { Text("Tên người nhận") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = bank,
                    onValueChange = { bank = it },
                    label = { Text("Ngân hàng") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = account,
                    onValueChange = { account = it },
                    label = { Text("STK") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { saveCard() },
                colors = ButtonDefaults.buttonColors(containerColor = Pink),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("XUẤT ẢNH PNG", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "QR gốc được giữ nguyên — chỉ trang trí phần xung quanh.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun WibuPreview(
    qrUri: Uri?,
    bgUri: Uri?,
    title: String,
    receiver: String,
    bank: String,
    account: String
) {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFFFEAF2))
    ) {
        bgUri?.let {
            val bmp = loadBitmap(LocalContext.current, it)
            bmp?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.38f
                )
            }
        }

        Column(
            Modifier.fillMaxSize().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("✦ ANIME PAYMENT CARD ✦", color = Pink, fontSize = 11.sp)

            Spacer(Modifier.weight(1f))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(Modifier.padding(12.dp)) {
                    if (qrUri != null) {
                        val bmp = loadBitmap(LocalContext.current, qrUri)
                        bmp?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "QR ngân hàng",
                                modifier = Modifier.size(190.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        Box(
                            Modifier.size(190.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("CHỌN ẢNH QR", color = Color.Gray) }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(receiver, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("$bank  •  $account", color = Color.DarkGray, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text("♡ Thank you for your support ♡", color = Pink, fontSize = 12.sp)

            Spacer(Modifier.weight(0.6f))
        }
    }
}

private fun loadBitmap(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        }
    } catch (_: Exception) {
        null
    }
}

object WibuCardRenderer {
    fun render(
        qr: Bitmap,
        background: Bitmap?,
        title: String,
        receiver: String,
        bank: String,
        account: String
    ): Bitmap {
        val w = 1200
        val h = 1660
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(out)

        c.drawColor(Color.rgb(255, 248, 251))

        background?.let {
            val src = Rect(0, 0, it.width, it.height)
            val dst = Rect(0, 0, w, h)
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 75 }
            c.drawBitmap(it, src, dst, p)
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(35, 30, 34)
            typeface = Typeface.create("sans", Typeface.BOLD)
        }

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 68f
        c.drawText(title, w / 2f, 110f, paint)

        paint.color = Color.rgb(233, 106, 154)
        paint.textSize = 30f
        c.drawText("✦ ANIME PAYMENT CARD ✦", w / 2f, 155f, paint)

        val card = RectF(90f, 250f, 1110f, 1510f)
        paint.color = Color.WHITE
        paint.setShadowLayer(28f, 0f, 10f, 0x33000000)
        c.drawRoundRect(card, 48f, 48f, paint)
        paint.clearShadowLayer()

        val qrSize = 620
        val qrLeft = (w - qrSize) / 2
        val qrTop = 390
        val qrRect = Rect(qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize)
        c.drawBitmap(qr, null, qrRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))

        paint.color = Color.rgb(35, 30, 34)
        paint.textSize = 48f
        c.drawText(receiver, w / 2f, 1120f, paint)

        paint.color = Color.DKGRAY
        paint.textSize = 32f
        c.drawText("$bank  •  $account", w / 2f, 1175f, paint)

        paint.color = Color.rgb(233, 106, 154)
        paint.textSize = 28f
        c.drawText("♡ Thank you for your support ♡", w / 2f, 1280f, paint)

        paint.color = Color.LTGRAY
        paint.textSize = 22f
        c.drawText("QR ORIGINAL • DO NOT MODIFY", w / 2f, 1435f, paint)

        return out
    }
}
