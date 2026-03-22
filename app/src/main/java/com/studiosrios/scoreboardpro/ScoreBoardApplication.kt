package com.studiosrios.scoreboardpro

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class ScoreBoardApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Ativa a persistência offline para que os dados fiquem salvos no dispositivo
        // mesmo sem internet ou após fechar o app.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}
