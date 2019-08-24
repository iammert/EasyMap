package com.iammert

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.iammert.easymapslib.data.SelectedAddressInfo
import com.iammert.easymapslib.ui.EasyMapsActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivityForResult(EasyMapsActivity.newIntent(this), 11)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 11) {
            if (resultCode == Activity.RESULT_OK) {
                Log.v(
                    "TEST",
                    "data ${data?.extras?.getParcelable<SelectedAddressInfo>(EasyMapsActivity.KEY_SELECTED_ADDRESS)}"
                )
            }
        }
    }
}
