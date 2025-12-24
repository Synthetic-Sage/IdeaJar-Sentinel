package com.example.ideajar

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

object SoundManager {
    private var soundPool: SoundPool? = null
    private var saveSoundId: Int = 0
    private var whooshSoundId: Int = 0
    
    fun init(context: Context) {
        if (soundPool == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build()

            // Load sounds
            saveSoundId = soundPool?.load(context, R.raw.confirm_tap, 1) ?: 0
            whooshSoundId = soundPool?.load(context, R.raw.magic_teleport_whoosh, 1) ?: 0
        }
    }

    fun playSaveSound() {
        if (saveSoundId != 0) {
            soundPool?.play(saveSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun playWhooshSound() {
        if (whooshSoundId != 0) {
            soundPool?.play(whooshSoundId, 0.4f, 0.4f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        saveSoundId = 0
        whooshSoundId = 0
    }
}
