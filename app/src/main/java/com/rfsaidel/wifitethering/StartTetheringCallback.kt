package com.rfsaidel.wifitethering

abstract class StartTetheringCallback {
    /**
     * Called when tethering has been successfully started.
     */
    abstract fun onTetheringStarted()

    /**
     * Called when starting tethering failed.
     */
    abstract fun onTetheringFailed()
}