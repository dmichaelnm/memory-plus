package de.dmichael.android.memory.plus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import de.dmichael.android.memory.plus.system.Activity

class DialogActivity : Activity() {

    companion object {

        private const val TITLE_ID = "titleId"
        private const val MESSAGE_ID = "messageId"
        private const val OPTION1_ID = "option1Id"
        private const val OPTION2_ID = "option2Id"
        private const val OPTION3_ID = "option3Id"
        private const val RESULT_ID = "resultId"

        private var data: Any? = null

        fun getLauncher(
            activity: Activity,
            callback: (dialogOption: Int, data: Any?) -> Unit
        ): ActivityResultLauncher<Intent> {
            return activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.data != null) {
                    val option = (result.data as Intent).getIntExtra(RESULT_ID, -1)
                    callback(option, data)
                }
            }
        }

        fun launch(
            activity: Activity,
            titleId: Int,
            messageId: Int,
            option1Id: Int,
            option2Id: Int,
            option3Id: Int,
            data: Any?,
            launcher: ActivityResultLauncher<Intent>
        ) {
            this.data = data

            val intent = Intent(activity, DialogActivity::class.java)
            intent.putExtra(TITLE_ID, titleId)
            intent.putExtra(MESSAGE_ID, messageId)
            intent.putExtra(OPTION1_ID, option1Id)
            intent.putExtra(OPTION2_ID, option2Id)
            intent.putExtra(OPTION3_ID, option3Id)
            launcher.launch(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)
        setUpAppearance()

        val titleId = intent.getIntExtra(TITLE_ID, -1)
        val messageId = intent.getIntExtra(MESSAGE_ID, -1)
        val option1Id = intent.getIntExtra(OPTION1_ID, -1)
        val option2Id = intent.getIntExtra(OPTION2_ID, -1)
        val option3Id = intent.getIntExtra(OPTION3_ID, -1)

        val tvTitle = findViewById<TextView>(R.id.dialog_title)
        tvTitle.setText(titleId)
        val tvMessage = findViewById<TextView>(R.id.dialog_message)
        tvMessage.setText(messageId)
        val btOption1 = findViewById<Button>(R.id.dialog_button_option_1)
        btOption1.setText(option1Id)
        registerButton(btOption1, 1)
        val btOption2 = findViewById<Button>(R.id.dialog_button_option_2)
        btOption2.setText(option2Id)
        registerButton(btOption2, 2)
        val btOption3 = findViewById<Button>(R.id.dialog_button_option_3)
        if (option3Id != -1) {
            btOption3.visibility = View.VISIBLE
            btOption3.setText(option3Id)
            registerButton(btOption3, 3)
        } else {
            btOption3.visibility = View.INVISIBLE
        }
    }

    private fun registerButton(button: Button, result: Int) {
        button.setOnClickListener {
            val intent = Intent()
            intent.putExtra(RESULT_ID, result)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}