package com.ebookfrenzy.dynamicfeature

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.widget.Toast

import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

import java.util.*

import com.ebookfrenzy.dynamicfeature.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var mySessionId = 0
    private lateinit var binding: ActivityMainBinding
    private lateinit var manager: SplitInstallManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SplitCompat.install(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        manager = SplitInstallManagerFactory.create(this)
    }

    fun launchFeature(view: View) {

        if (manager.installedModules.contains("my_dynamic_feature")) {
            startActivity(Intent(
                "com.ebookfrenzy.my_dynamic_feature.MyFeatureActivity"))
        } else {
            binding.statusText.text = "Feature not yet installed"
        }
    }

    fun installFeature(view: View) {

        manager.registerListener(listener)

        val request = SplitInstallRequest.newBuilder()
            .addModule("my_dynamic_feature")
            .build()

        manager.startInstall(request)
            .addOnSuccessListener { sessionId ->
                mySessionId = sessionId
                Toast.makeText(this@MainActivity,
                    "Module installation started",
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this@MainActivity,
                    "Module installation failed: $exception",
                    Toast.LENGTH_SHORT).show()
            }
    }

    fun deleteFeature(view: View) {
        manager.deferredUninstall(Arrays.asList("my_dynamic_feature"))
    }

    private var listener: SplitInstallStateUpdatedListener =
        SplitInstallStateUpdatedListener { state ->
            if (state.sessionId() == mySessionId) {
                when (state.status()) {

                    SplitInstallSessionStatus.DOWNLOADING -> {
                        val size = state.totalBytesToDownload()
                        val downloaded = state.bytesDownloaded()
                        binding.statusText.text =
                            String.format(Locale.getDefault(),
                                "%d of %d bytes downloaded.", downloaded, size)
                    }

                    SplitInstallSessionStatus.INSTALLING ->
                        binding.statusText.text = "Installing feature"

                    SplitInstallSessionStatus.DOWNLOADED ->
                        binding.statusText.text = "Download Complete"

                    SplitInstallSessionStatus.INSTALLED ->
                        binding.statusText.text = "Installed - Feature is ready"

                    SplitInstallSessionStatus.CANCELED ->
                        binding.statusText.text = "Installation cancelled"

                    SplitInstallSessionStatus.PENDING ->
                        binding.statusText.text = "Installation pending"
                    SplitInstallSessionStatus.FAILED ->

                        binding.statusText.text =
                            String.format(Locale.getDefault(),
                                "Installation Failed. Error code: %s", state.errorCode())
                }
            }
        }

    override fun onResume() {
        manager.registerListener(listener)
        super.onResume()
    }

    override fun onPause() {
        manager.unregisterListener(listener)
        super.onPause()
    }
}