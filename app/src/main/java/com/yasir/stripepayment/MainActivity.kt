package com.yasir.stripepayment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.yasir.stripepayment.Utils.PUBLISHABLE_KEY
import com.yasir.stripepayment.databinding.ActivityMainBinding
import com.yasir.stripepayment.models.CustomerModel
import com.yasir.stripepayment.models.PaymentIntentModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var paymentSheet: PaymentSheet
    private lateinit var customerId:String
    private lateinit var ephemeralKey: String
    private lateinit var clientSecret: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        PaymentConfiguration.init(this, PUBLISHABLE_KEY)
        getCustomerId()
        binding.paymentBtn.setOnClickListener{
            paymentFlow()
        }
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
    }

    private fun paymentFlow() {
        paymentSheet.presentWithPaymentIntent(
            clientSecret,
            PaymentSheet.Configuration(
                "Yasir Alam",
                PaymentSheet.CustomerConfiguration(
                    customerId,
                    ephemeralKey
                )
            )
        )
    }


    private var apiInterface = ApiUtilities.getApiInterface()

    private fun getCustomerId() {
        lifecycleScope.launch(Dispatchers.IO) {
            val res :Response<CustomerModel> = apiInterface.getCustomer()
            withContext(Dispatchers.Main){
                if(res.isSuccessful && res.body()!=null){
                    customerId = res.body()!!.id
                    getEphemeralKey(customerId)
                }

            }
        }
    }

    private fun getEphemeralKey(customerId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val res :Response<CustomerModel> = apiInterface.getEphemeralKey(customerId)
            withContext(Dispatchers.Main){
                if(res.isSuccessful && res.body()!=null){
                    ephemeralKey = res.body()!!.id
                    getPaymentIntent(customerId,ephemeralKey)
                }

            }
        }

    }

    private fun getPaymentIntent(customerId: String, ephemeralKey: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val res :Response<PaymentIntentModel> = apiInterface.getPaymentIntent(customerId)
            withContext(Dispatchers.Main){
                if(res.isSuccessful && res.body()!=null){
                    clientSecret = res.body()!!.client_secret
                    Toast.makeText(this@MainActivity, "Proceed for Payment", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        if (paymentSheetResult is PaymentSheetResult.Completed){
            Toast.makeText(this, "Payment Done", Toast.LENGTH_SHORT).show()
        }
    }
}