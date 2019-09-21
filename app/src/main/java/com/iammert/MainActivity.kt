package com.iammert

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.iammert.easymapslib.data.SelectedAddressInfo
import com.iammert.easymapslib.ui.EasyMapsActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var selectedAddressInfo: SelectedAddressInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonSelectAddress.setOnClickListener {
            startActivityForResult(
                EasyMapsActivity.newIntent(
                    context = this,
                    selectedAddressInfo = selectedAddressInfo,
                    validateFields = false
                ), 11
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 11) {
            if (resultCode == Activity.RESULT_OK) {
                selectedAddressInfo =
                    data?.extras?.getParcelable(EasyMapsActivity.KEY_SELECTED_ADDRESS)
                textViewAddress.text = selectedAddressInfo.toString()
            }
        }
    }
}
