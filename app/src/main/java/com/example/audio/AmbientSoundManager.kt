package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import android.media.MediaPlayer
import android.net.Uri
import kotlin.math.sin
import kotlin.random.Random

class AmbientSoundManager(context: Context) {
    private val appContext = context.applicationContext
    private var audioTrack: AudioTrack? = null
    private var mediaPlayer: MediaPlayer? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    @Suppress("DEPRECATION")
    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            stop()
        }
    }

    @Volatile
    private var isPlaying = false
    private var activeSoundType = "None"

    fun start(soundType: String) {
        if (isPlaying && activeSoundType == soundType) return
        stop()

        activeSoundType = soundType
        if (soundType == "None") return

        // Request audio focus
        @Suppress("DEPRECATION")
        val result = audioManager.requestAudioFocus(
            focusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
        )

        @Suppress("DEPRECATION")
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return
        }

        isPlaying = true

        if (soundType.startsWith("content://") || soundType.startsWith("file://")) {
            playCustomUri(soundType)
        } else {
            val assetFileName = when (soundType) {
                "Rain" -> "audio/rain.mp3"
                "Lo-Fi" -> "audio/lofi.mp3"
                else -> null
            }
            
            var assetExists = false
            if (assetFileName != null) {
                try {
                    val afd = appContext.assets.openFd(assetFileName)
                    afd.close()
                    assetExists = true
                } catch (e: Exception) {
                    assetExists = false
                }
            }

            if (assetExists && assetFileName != null) {
                playAsset(assetFileName)
            } else {
                synthJob = scope.launch {
                    runSynthesizer(soundType)
                }
            }
        }
    }

    private fun playAsset(assetFileName: String) {
        try {
            val afd = appContext.assets.openFd(assetFileName)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AmbientSoundManager", "Error playing asset: ${e.message}")
        }
    }

    private fun playCustomUri(uriString: String) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(appContext, Uri.parse(uriString))
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AmbientSoundManager", "Error playing custom URI: ${e.message}")
        }
    }

    fun stop() {
        isPlaying = false
        synthJob?.cancel()
        synthJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {}
        mediaPlayer = null
        @Suppress("DEPRECATION")
        audioManager.abandonAudioFocus(focusChangeListener)
    }

    private suspend fun runSynthesizer(soundType: String) {
        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ) * 2

        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Exception) {
            Log.e("AmbientSoundManager", "Could not build AudioTrack: ${e.message}")
            return
        }

        audioTrack = track
        try {
            track.play()
        } catch (e: Exception) {
            Log.e("AmbientSoundManager", "Could not play AudioTrack: ${e.message}")
            return
        }

        val buffer = ShortArray(bufferSize / 2)
        var phase = 0.0
        val random = Random(System.currentTimeMillis())

        // Filter variables for Rain/Cafe and crackle
        var b0 = 0.0
        var b1 = 0.0
        var b2 = 0.0
        var b3 = 0.0
        var b4 = 0.0
        var b5 = 0.0
        var b6 = 0.0

        var loFiTime = 0L

        while (isPlaying) {
            for (i in buffer.indices) {
                var sample = 0.0

                when (soundType) {
                    "Rain" -> {
                        // Pink-ish noise generation with slow rumbling
                        val white = random.nextDouble() * 2.0 - 1.0
                        b0 = 0.99886 * b0 + white * 0.0555179
                        b1 = 0.99332 * b1 + white * 0.0750759
                        b2 = 0.96900 * b2 + white * 0.1538520
                        b3 = 0.86650 * b3 + white * 0.3104856
                        b4 = 0.55000 * b4 + white * 0.5329522
                        b5 = -0.7616 * b5 - white * 0.0168980
                        val pink = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362
                        b6 = white * 0.115926

                        // Slow rolling wind gusts
                        val mod = 0.70 + 0.30 * sin(phase / 12000.0)
                        sample = pink * mod * 0.07
                    }
                    "Lo-Fi" -> {
                        // Warm fundamental 110Hz (A2) + cozy intervals
                        val t = loFiTime + i
                        val s1 = sin(2.0 * Math.PI * 110.0 * t / sampleRate)
                        val s2 = sin(2.0 * Math.PI * 130.81 * t / sampleRate) // Minor third
                        val s3 = sin(2.0 * Math.PI * 165.0 * t / sampleRate) // Fifth
                        
                        // Cozy rolling low frequency tremolo
                        val tremolo = 0.7 + 0.3 * sin(2.0 * Math.PI * 0.05 * t / sampleRate)
                        val drones = (s1 * 0.4 + s2 * 0.35 + s3 * 0.25) * tremolo

                        // Rare warm record pops
                        var crackle = 0.0
                        if (random.nextDouble() < 0.0003) {
                            crackle = (random.nextDouble() * 2.0 - 1.0) * 0.3
                        }

                        sample = (drones * 0.4 + crackle * 0.08)
                    }
                }

                phase += 1.0
                val clamped = sample.coerceIn(-1.0, 1.0)
                buffer[i] = (clamped * 32767).toInt().toShort()
            }

            loFiTime += buffer.size
            if (activeSoundType == "None") break

            try {
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    track.write(buffer, 0, buffer.size)
                }
            } catch (e: Exception) {
                break
            }
        }

        try {
            track.stop()
            track.release()
        } catch (e: Exception) {
            // Ignore on cleanup
        }
    }
}
