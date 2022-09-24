package com.kylecorry.andromeda.files

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

class LocalFileSystem(context: Context) : BaseFileSystem(context, context.filesDir.path)
class CacheFileSystem(context: Context) : BaseFileSystem(context, context.cacheDir.path)

@RequiresApi(Build.VERSION_CODES.N)
class DataFileSystem(context: Context) : BaseFileSystem(context, context.dataDir.path)