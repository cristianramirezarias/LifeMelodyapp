// --- Fragmento de lógica de comunicación con el Servicio de Música ---
private val serviceConnection = object : ServiceConnection {
    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        val binder = service as MusicService.LocalBinder
        musicService = binder.getService()
        isBound = true
        requestCurrentStatus() // Sincroniza el estado de reproducción al conectar
    }

    override fun onServiceDisconnected(arg0: ComponentName) {
        isBound = false
        musicService = null
    }
}

// Escuchador de actualizaciones del servicio (Progreso y Metadatos)
private val musicUpdateReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val title = it.getStringExtra("TITLE")
            isPlaying = it.getBooleanExtra("IS_PLAYING", false)
            // Lógica para actualizar UI dinámicamente según el tipo de media (Video/Audio)
            updateMediaView(it.getParcelableExtra("MEDIA_URI"))
        }
    }
}

private fun applyThemeColor() {
    val sharedPref = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    val colorString = sharedPref.getString("theme_color", "#C34B92")
    
    try {
        val colorInt = Color.parseColor(colorString)
        // Aplicación de sombras y tintes dinámicos a componentes de la UI
        binding.btnSiguiente.setColorFilter(colorInt)
        binding.barraProgreso.progressTintList = ColorStateList.valueOf(colorInt)
        
        if (themeMode == SettingsActivity.THEME_GRADIENT) {
            applyGradientBackground(colorInt) // Generación de gradientes programáticos
        }
    } catch (e: IllegalArgumentException) { /* Fallback handling */ }
}

private fun toggleFavorite() {
    currentSong?.let { song ->
        lifecycleScope.launch {
            val isFavorite = appDatabase.favoriteSongDao().isFavorite(song.id)
            if (isFavorite) {
                appDatabase.favoriteSongDao().delete(FavoriteSongEntity.fromSong(song))
            } else {
                appDatabase.favoriteSongDao().insert(FavoriteSongEntity.fromSong(song))
            }
            checkFavoriteStatus(song.id)
        }
    }
}

