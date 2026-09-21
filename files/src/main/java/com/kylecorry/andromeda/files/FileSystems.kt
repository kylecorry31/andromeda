package com.kylecorry.andromeda.files

import android.content.Context

class LocalFileSystem(context: Context) : BaseFileSystem(context, context.filesDir.path)
class CacheFileSystem(context: Context) : BaseFileSystem(context, context.cacheDir.path)
class DataFileSystem(context: Context) : BaseFileSystem(context, context.dataDir.path)
