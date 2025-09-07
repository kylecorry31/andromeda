package com.kylecorry.andromeda.sound.stream

class SumAudioStream(private vararg val streams: Pair<Float, AudioStream>) : AudioStream {
    override suspend fun next(sampleRate: Int): Float {
        var amplitude = 0.0
        var totalVolume = 0.0
        for (stream in streams) {
            amplitude += stream.first * stream.second.next(sampleRate)
            totalVolume += stream.first
        }
        return (amplitude / totalVolume).toFloat()
    }

    override suspend fun reset() {
        for (stream in streams) {
            stream.second.reset()
        }
    }
}