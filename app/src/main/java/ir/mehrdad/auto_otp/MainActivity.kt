package ir.mehrdad.auto_otp

import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.material.textfield.TextInputEditText
import ir.mehrdad.auto_otp.databinding.ActivityMainBinding
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val SMS_CONSENT_REQUEST = 2  // Set to an unused request code
    var smsBroadcastReceiver: SmsBroadcastReceiver? = null
    var etOtp: TextInputEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        etOtp = binding.editTextNumber
        startSmartUserConsent()
    }

    private fun startSmartUserConsent() {
        // Start listening for SMS User Consent broadcasts from senderPhoneNumber
// The Task<Void> will be successful if SmsRetriever was able to start
// SMS User Consent, and will error if there was an error starting.
        val task = SmsRetriever.getClient(this).startSmsUserConsent(null /* or null */)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            // ...
            SMS_CONSENT_REQUEST ->
                // Obtain the phone number from the result
                if (resultCode == RESULT_OK && data != null) {
                    // Get SMS message content
                    val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                    // Extract one-time code from the message and complete verification
                    // `message` contains the entire text of the SMS message, so you will need
                    // to parse the string.
//                    val oneTimeCode = parseOneTimeCode(message) // define this function
                    parseOneTimeCode(message)
                    // send one time code to the server
                } else {
                    // Consent denied. User can type OTC manually.
                }
        }
    }

    private fun parseOneTimeCode(message: String?){
        val otpPattern = Pattern.compile("([+-]?(?=\\.\\d|\\d)(?:\\d+)?(?:\\.?\\d*))(?:[eE]([+-]?\\d+))?")
        val matcher = otpPattern.matcher(message)
        if(matcher.find()) {
            etOtp!!.setText(matcher.group())
        }
    }

    private fun registerBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver!!.smsBroadcastReceiverListener =
            object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
                override fun onSuccess(intent: Intent?) {
                    startActivityForResult(intent, SMS_CONSENT_REQUEST)
                }

                override fun onFailure() {
                    TODO("Not yet implemented")
                }

            }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsBroadcastReceiver, intentFilter)
    }

    override fun onStart() {
        super.onStart()
        registerBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsBroadcastReceiver)
    }
}