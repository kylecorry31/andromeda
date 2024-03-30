package com.kylecorry.andromeda.connection

import java.time.Instant

class NearbyDeviceMessage(val time: Instant, val message: ByteArray)