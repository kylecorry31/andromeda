package com.kylecorry.andromeda.signal

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.CellInfo
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoTdscdma
import android.telephony.CellInfoWcdma
import android.telephony.CellSignalStrengthNr
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.math.MathUtils
import com.kylecorry.andromeda.core.coroutines.SafeExecutor
import com.kylecorry.andromeda.core.sensors.AbstractSensor
import com.kylecorry.andromeda.core.sensors.Quality
import com.kylecorry.andromeda.core.time.AndroidTime
import com.kylecorry.andromeda.core.time.CoroutineTimer
import com.kylecorry.andromeda.core.tryOrNothing
import com.kylecorry.andromeda.permissions.Permissions
import java.time.Duration
import java.time.Instant

class CellSignalSensor(
    private val context: Context,
    private val updateCellCache: Boolean,
    private val removeUnregisteredSignals: Boolean = true,
    private val pathLossFactor: Float = 1f
) :
    AbstractSensor(), ICellSignalSensor {

    private val telephony by lazy { context.getSystemService<TelephonyManager>() }
    private val executor by lazy { ContextCompat.getMainExecutor(context) }
    private var isAlsoConnectedTo5g = false
    private var dbmSecondary5g: Int? = null
    private var levelSecondary5g: Int? = null
    private var timeSecondary5g: Instant? = null
    private val telephonyCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        object : TelephonyCallback(),
            TelephonyCallback.DisplayInfoListener,
            TelephonyCallback.SignalStrengthsListener {
            override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                isAlsoConnectedTo5g = telephonyDisplayInfo.overrideNetworkType in listOf(
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA,
                    TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED
                )
                updateSignals(notify = true)
            }

            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                val nrSignal = signalStrength.cellSignalStrengths.firstOrNull {
                    it is CellSignalStrengthNr
                }

                dbmSecondary5g = nrSignal?.dbm
                levelSecondary5g = nrSignal?.level
                timeSecondary5g = if (nrSignal != null) Instant.now() else null
                updateSignals(notify = true)
            }
        }
    } else {
        null
    }

    override val hasValidReading: Boolean
        get() = hasReading

    override val signals: List<CellSignal>
        get() = _signals

    private var _signals = listOf<CellSignal>()
    private var oldSignals = listOf<RawCellSignal>()
    private var hasReading = false

    private val intervalometer = CoroutineTimer {
        tryOrNothing {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && updateCellCache) {
                updateCellInfoCache()
            } else {
                loadFromCache()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateCellInfoCache() {
        tryOrNothing {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                return@tryOrNothing
            }

            if (!Permissions.canGetFineLocation(context)) {
                return@tryOrNothing
            }
            telephony?.requestCellInfoUpdate(
                SafeExecutor.newSingleThreadExecutor {
                    tryOrNothing {
                        Handler(Looper.getMainLooper()).post {
                            updateCellInfo(emptyList())
                        }
                    }
                },
                @SuppressLint("NewApi")
                object : TelephonyManager.CellInfoCallback() {
                    override fun onCellInfo(cellInfo: MutableList<CellInfo>) {
                        tryOrNothing {
                            Handler(Looper.getMainLooper()).post {
                                updateCellInfo(cellInfo)
                            }
                        }
                    }

                    override fun onError(errorCode: Int, detail: Throwable?) {
                        onCellInfo(mutableListOf())
                    }
                })
        }

    }

    @Suppress("DEPRECATION")
    private fun updateCellInfo(cells: List<CellInfo>, notify: Boolean = true) {
        val newSignals =
            cells.filter { !removeUnregisteredSignals || it.isRegistered }.mapNotNull {
                when {
                    it is CellInfoWcdma -> {
                        RawCellSignal(
                            it.cellIdentity.cid.toString(),
                            getTimestamp(it),
                            it.cellSignalStrength.dbm,
                            it.cellSignalStrength.level,
                            CellNetwork.Wcdma,
                            it.isRegistered,
                            it.isConnected()
                        )
                    }

                    it is CellInfoGsm -> {
                        RawCellSignal(
                            it.cellIdentity.cid.toString(),
                            getTimestamp(it),
                            it.cellSignalStrength.dbm,
                            it.cellSignalStrength.level,
                            CellNetwork.Gsm,
                            it.isRegistered,
                            it.isConnected(),
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) it.cellSignalStrength.timingAdvance else null,
                            GsmCellSignalDistanceCalculator()
                        )
                    }

                    it is CellInfoLte -> {
                        RawCellSignal(
                            it.cellIdentity.ci.toString(),
                            getTimestamp(it),
                            it.cellSignalStrength.dbm,
                            it.cellSignalStrength.level,
                            CellNetwork.Lte,
                            it.isRegistered,
                            it.isConnected(),
                            it.cellSignalStrength.timingAdvance,
                            LteCellSignalDistanceCalculator()
                        )
                    }

                    it is CellInfoCdma -> {
                        RawCellSignal(
                            it.cellIdentity.basestationId.toString(),
                            getTimestamp(it),
                            it.cellSignalStrength.dbm,
                            it.cellSignalStrength.level,
                            CellNetwork.Cdma,
                            it.isRegistered,
                            it.isConnected()
                        )
                    }

                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && it is CellInfoTdscdma -> {
                        RawCellSignal(
                            it.cellIdentity.cid.toString(),
                            getTimestamp(it),
                            it.cellSignalStrength.dbm,
                            it.cellSignalStrength.level,
                            CellNetwork.Tdscdma,
                            it.isRegistered,
                            it.isConnected()
                        )
                    }

                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && it is CellInfoNr -> {
                        val signalStrength = it.cellSignalStrength as? CellSignalStrengthNr
                        RawCellSignal(
                            it.cellIdentity.operatorAlphaLong.toString(),
                            getTimestamp(it),
                            it.cellSignalStrength.dbm,
                            it.cellSignalStrength.level,
                            CellNetwork.Nr,
                            it.isRegistered,
                            it.isConnected(),
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) signalStrength?.timingAdvanceMicros else null,
                            // TODO: Detect subcarrier frequency from cell identity
                            NrCellSignalDistanceCalculator()
                        )
                    }

                    else -> null
                }
            }

        updateSignals(newSignals, notify)
    }

    private fun updateSignals(raw: List<RawCellSignal>? = null, notify: Boolean = true) {
        synchronized(this) {
            hasReading = true

            var latestSignals = (raw ?: oldSignals).map {
                val old = oldSignals.find { signal -> it.id == signal.id }
                if (old == null) {
                    it
                } else {
                    if (old.time > it.time) old else it
                }
            }

            latestSignals = applyOverrides(latestSignals)

            oldSignals = latestSignals

            _signals = latestSignals.map {
                CellSignal(
                    it.id,
                    it.percent,
                    it.dbm,
                    it.quality,
                    it.network,
                    it.isRegistered,
                    it.time,
                    it.timingDistanceMeters,
                    it.timingDistanceErrorMeters?.plus(
                        (it.timingDistanceMeters ?: 0f) * pathLossFactor
                    )
                )
            }

            if (notify) {
                notifyListeners()
            }
        }
    }

    private fun applyOverrides(signals: List<RawCellSignal>): List<RawCellSignal> {
        if (!isAlsoConnectedTo5g) {
            return signals
        }
        val connected5g = signals.firstOrNull { it.network == CellNetwork.Nr && it.isConnected }
        val connected = signals.firstOrNull { it.isConnected }

        // Add the 5G override
        return signals.filter { it.network != CellNetwork.Nr || !it.isConnected } + listOf(
            RawCellSignal(
                connected5g?.id ?: "${connected?.id ?: "unknown"}-5g",
                timeSecondary5g ?: connected5g?.time ?: connected?.time ?: Instant.now(),
                dbmSecondary5g ?: connected5g?.dbm ?: connected?.dbm ?: 0,
                levelSecondary5g ?: connected5g?.level ?: connected?.level ?: 0,
                CellNetwork.Nr,
                isRegistered = true,
                isConnected = true
            )
        )
    }

    @SuppressLint("MissingPermission")
    override fun startImpl() {
        if (!Permissions.canGetFineLocation(context)) {
            _signals = listOf()
            notifyListeners()
            return
        }

        loadFromCache(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && telephonyCallback != null) {
            telephony?.registerTelephonyCallback(executor, telephonyCallback)
        }
        intervalometer.interval(Duration.ofSeconds(5))
    }

    @SuppressLint("MissingPermission")
    private fun loadFromCache(notify: Boolean = true) {
        if (!Permissions.canGetFineLocation(context)) return
        tryOrNothing {
            updateCellInfo(telephony?.allCellInfo ?: listOf(), notify)
        }
    }

    @Suppress("DEPRECATION")
    private fun getTimestamp(cellInfo: CellInfo): Instant {
        val timeSinceBoot =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) cellInfo.timestampMillis else (cellInfo.timeStamp / 1000000)
        return AndroidTime.millisSinceBootToInstant(timeSinceBoot)

    }


    override fun stopImpl() {
        intervalometer.stop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && telephonyCallback != null) {
            telephony?.unregisterTelephonyCallback(telephonyCallback)
        }
    }

    private fun CellInfo.isConnected(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            this.cellConnectionStatus == CellInfo.CONNECTION_PRIMARY_SERVING || this.cellConnectionStatus == CellInfo.CONNECTION_SECONDARY_SERVING
        } else {
            false
        }
    }

    private class RawCellSignal(
        val id: String,
        val time: Instant,
        val dbm: Int,
        val level: Int,
        val network: CellNetwork,
        val isRegistered: Boolean,
        val isConnected: Boolean,
        val timingAdvanceDistance: Int? = null,
        val distanceCalculator: CellSignalDistanceCalculator? = null
    ) {
        val percent: Float
            get() {
                return MathUtils.clamp(
                    100f * (dbm - network.minDbm) / (network.maxDbm - network.minDbm).toFloat(),
                    0f,
                    100f
                )
            }

        val quality: Quality
            get() = when (level) {
                3, 4 -> Quality.Good
                2 -> Quality.Moderate
                0 -> Quality.Unknown
                else -> Quality.Poor
            }

        val timingDistanceMeters: Float?
            get() {
                return if (timingAdvanceDistance == null || timingAdvanceDistance <= 0 || timingAdvanceDistance == CellInfo.UNAVAILABLE || distanceCalculator == null) {
                    null
                } else {
                    distanceCalculator.getTimingAdvanceDistance(timingAdvanceDistance)
                }
            }

        val timingDistanceErrorMeters: Float?
            get() {
                return if (timingAdvanceDistance == null || timingAdvanceDistance <= 0 || timingAdvanceDistance == CellInfo.UNAVAILABLE || distanceCalculator == null) {
                    null
                } else {
                    distanceCalculator.getTimingAdvanceDistanceError(timingAdvanceDistance)
                }
            }
    }
}