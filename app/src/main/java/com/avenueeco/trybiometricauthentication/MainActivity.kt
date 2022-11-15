package com.avenueeco.trybiometricauthentication

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.os.CancellationSignal
import com.avenueeco.trybiometricauthentication.databinding.ActivityMainBinding
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    private var cancellationSignal : CancellationSignal? = null
    private val  authenticationCallback : BiometricPrompt.AuthenticationCallback

    get() =
    object : BiometricPrompt.AuthenticationCallback(){

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            notifyUser("Authentication Error: $errString")
            getCancellationSignal().cancel()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            notifyUser("Authentication Success")
            startActivity(Intent(this@MainActivity,SecretActivity::class.java))
        }
    }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkBiometricSupport()

        binding.btnAuthenticate.setOnClickListener {
            val  biometricPrompt = BiometricPrompt(this,MainThreadExecutor(),authenticationCallback,)

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Title of prompt")
                .setSubtitle("Authenticate is required")
                .setDescription("This app uses finger print protection to keep your data secure")
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }


    private fun getCancellationSignal() : CancellationSignal{
         cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
             notifyUser("Authentication was cancelled by the user")
        }
        return cancellationSignal as CancellationSignal
    }

    private fun checkBiometricSupport() : Boolean{
         val keyGuardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (!keyGuardManager.isKeyguardSecure){
            notifyUser("Finger Print Authentication has not been enabled in settings")
            return false
        }

        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.USE_BIOMETRIC)
            != PackageManager.PERMISSION_GRANTED
        ){
            notifyUser("Finger print Authentication permission is not enabled")
            return false
        }

       return if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)){
             true
        }  else true
    }

    private fun notifyUser(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    class MainThreadExecutor : Executor {
        private val handler = Handler(Looper.getMainLooper())

        override fun execute(r: Runnable) {
            handler.post(r)
        }
    }
}