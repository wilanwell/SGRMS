package com.wilanwell.sgrms.ui.components

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.wilanwell.sgrms.ui.theme.ColorSurface
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.theme.ColorOnSurface
import com.wilanwell.sgrms.ui.theme.ColorOnSurfaceVariant
import com.wilanwell.sgrms.utils.decodeBase64ToImageBytes
import coil.compose.AsyncImage
import java.io.File
import android.graphics.Bitmap
import androidx.compose.runtime.produceState
import com.wilanwell.sgrms.data.repository.ProfileImageRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@Composable
fun ProfileImagePicker(
    imageUrl: String,
    isUploading: Boolean,
    modifier: Modifier = Modifier,
    size: Int = 96,
    enabled: Boolean = true,
    legacyImageBase64: String = "",
    onImageSelected: (Uri) -> Unit
) {
    val context = LocalContext.current

    var showOptionDialog by remember {
        mutableStateOf(false)
    }

    var cameraImageUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                onImageSelected(uri)
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { isSuccess ->
            if (isSuccess) {
                cameraImageUri?.let { uri ->
                    onImageSelected(uri)
                }
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                val uri = createCameraImageUri(context)
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            }
        }
    )

    fun openCamera() {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCameraPermission) {
            val uri = createCameraImageUri(context)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    Box(
        modifier = modifier.size((size + 14).dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(ColorPrimary)
                .clickable(enabled = enabled && !isUploading) {
                    showOptionDialog = true
                },
            contentAlignment = Alignment.Center
        ) {
            ProfilePhoto(
                imageUrl = imageUrl,
                legacyImageBase64 = legacyImageBase64,
                size = size
            )

            if (isUploading) {
                Box(
                    modifier = Modifier
                        .size(size.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                }
            }
        }

        if (!isUploading && enabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(ColorPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = stringResource(R.string.change_profile_picture),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showOptionDialog) {
        AlertDialog(
            onDismissRequest = {
                showOptionDialog = false
            },
            title = {
                Text(
                    text = stringResource(R.string.change_profile_photo),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorOnSurface
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileImageOptionCard(
                        title = stringResource(R.string.take_photo),
                        description = stringResource(R.string.use_your_camera),
                        onClick = {
                            showOptionDialog = false
                            openCamera()
                        }
                    )

                    ProfileImageOptionCard(
                        title = stringResource(R.string.choose_from_gallery),
                        description = stringResource(R.string.select_photo_from_your_phone),
                        onClick = {
                            showOptionDialog = false
                            openGallery()
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        showOptionDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = ColorPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
private fun ProfileImageOptionCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = ColorSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ColorPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = title,
                    tint = ColorPrimary,
                    modifier = Modifier.size(23.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorOnSurface
                )

                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = ColorOnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProfileImageView(
    imageUrl: String,
    modifier: Modifier = Modifier,
    size: Int = 90,
    legacyImageBase64: String = ""
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(ColorPrimary),
        contentAlignment = Alignment.Center
    ) {
        ProfilePhoto(
            imageUrl = imageUrl,
            legacyImageBase64 = legacyImageBase64,
            size = size
        )
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface ProfileImageEntryPoint {
    fun profileImageRepository(): ProfileImageRepository
}

/**
 * Shows, in order of preference: a photo saved in profileImages/{uid}
 * (`firestore://` reference), a photo URL, a photo still stored the old way
 * (Base64 on the profile document), or a placeholder.
 */
@Composable
private fun ProfilePhoto(
    imageUrl: String,
    legacyImageBase64: String,
    size: Int
) {
    val context = LocalContext.current

    val firestoreBitmap by produceState<Bitmap?>(initialValue = null, imageUrl) {
        value = if (ProfileImageRepository.isFirestoreReference(imageUrl)) {
            runCatching {
                EntryPointAccessors
                    .fromApplication(context.applicationContext, ProfileImageEntryPoint::class.java)
                    .profileImageRepository()
                    .loadImage(imageUrl)
                    ?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            }.getOrNull()
        } else {
            null
        }
    }

    val legacyBitmap = remember(legacyImageBase64) {
        if (legacyImageBase64.isBlank()) {
            null
        } else {
            runCatching {
                val imageBytes = decodeBase64ToImageBytes(legacyImageBase64)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            }.getOrNull()
        }
    }

    val bitmap = firestoreBitmap ?: legacyBitmap

    when {
        bitmap != null -> Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = stringResource(R.string.profile_picture),
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        imageUrl.isNotBlank() && !ProfileImageRepository.isFirestoreReference(imageUrl) -> AsyncImage(
            model = imageUrl,
            contentDescription = stringResource(R.string.profile_picture),
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        else -> Icon(
            imageVector = Icons.Default.Person,
            contentDescription = stringResource(R.string.default_profile_picture),
            tint = Color.White,
            modifier = Modifier.size((size * 0.52).dp)
        )
    }
}

private fun createCameraImageUri(
    context: Context
): Uri {
    val imageDirectory = File(
        context.cacheDir,
        "profile_images"
    )

    if (!imageDirectory.exists()) {
        imageDirectory.mkdirs()
    }

    val imageFile = File(
        imageDirectory,
        "profile_${System.currentTimeMillis()}.jpg"
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}