package com.yellowtwigs.knockin.ui.statistics.reward

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.yellowtwigs.knockin.databinding.ActivityRewardBinding
import com.yellowtwigs.knockin.model.database.data.PromotionCode
import com.yellowtwigs.knockin.repositories.firebase.FirebaseViewModel
import com.yellowtwigs.knockin.ui.statistics.daily_statistics.DailyStatisticsActivity
import com.yellowtwigs.knockin.utils.EveryActivityUtils.checkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RewardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRewardBinding

    private val viewModel: FirebaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkTheme(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val USER_POINT = "USER_POINT"
        val sharedPreferences: SharedPreferences = application.getSharedPreferences(USER_POINT, Context.MODE_PRIVATE)
        val points = sharedPreferences.getInt(USER_POINT, 0)
        binding.redeemPointsLayout.isVisible = points >= 50
        binding.redeemPoints200.isVisible = points >= 200
        binding.redeemPoints50.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putInt(USER_POINT, points - 50)
            editor.apply()
            binding.promoCodeLayout.isVisible = true
        }
        binding.redeemPoints200.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putInt(USER_POINT, points - 200)
            editor.apply()
        }

        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        Log.i("PromoCodes", "points : ${points}")
        val USER_CODE_RELAX = "USER_CODE_RELAX"
        val sharedPreferencesCodeRelax: SharedPreferences = application.getSharedPreferences(USER_CODE_RELAX, Context.MODE_PRIVATE)
        val codeRelax = sharedPreferencesCodeRelax.getString(USER_CODE_RELAX, "")
        val edit = sharedPreferencesCodeRelax.edit()

        if (points >= 50) {

            viewModel.getPromotionsCodes().observe(this) { list ->
                if (list.isNotEmpty()) {
                    var randomNumber: Int
                    // Get a random number between 0 and the list size, for example 100
                    // Example : get the random number 39

                    Log.i("PromoCodes", "codeRelax : ${codeRelax}")

                    if (codeRelax == "") {
                        do {
                            randomNumber = (0..list.size).random()
                        } while (list[randomNumber].isUsed)

                        edit.putString(USER_CODE_RELAX, list[randomNumber].content)
                        edit.apply()

                        Log.i("PromoCodes", "list[randomNumber].content : ${list[randomNumber].content}")

                        binding.promoCodeText.text = list[randomNumber].content
                    } else {
                        binding.promoCodeText.text = codeRelax
                    }
                }
            }
        }

        binding.copyButton.setOnClickListener {
            binding.promoCodeText.isVisible = true
            val text = binding.promoCodeText.text.toString()
            val clip = ClipData.newPlainText("Copied Text", text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "Copied!", Toast.LENGTH_LONG).show()

            viewModel.editPromotionCodeToIsUsed(PromotionCode(text, true))

            binding.copyButton.isEnabled = false
        }

//        getListOfPromotionCodes().map {
//            viewModel.addPromotionCodeToFirestore(it)
//        }
    }

    private fun getListOfPromotionCodes(): List<PromotionCode> {
        return listOf(
            PromotionCode("7SQNJA2ZHTJ2NB71M4B1R51", false),
            PromotionCode("NJTMSK9ADYYTPGVDEDHLRDL", false),
            PromotionCode("KXJF9GM4F5US037YRM7XAT0", false),
            PromotionCode("8833YSYASSR6R1EWSDJF3FQ", false),
            PromotionCode("LZ29355BU3A1YTZ4P7QR0SM", false),
            PromotionCode("03YWSY1E5ZM8CMU4PZE4AV6", false),
            PromotionCode("UHAMHBW83GQGWSW29E4QCYM", false),
            PromotionCode("QYGZ87T68UX495NNWZCG5QL", false),
            PromotionCode("3SX8RPE3KVEUSGJPX2MYENK", false),
            PromotionCode("NJ8D2NFZW20V90XPG6UVS0J", false),
            PromotionCode("9D339W5CE2NP0WJB4LE9WRQ", false),
            PromotionCode("RZUALLE5FMZ4JXBETM7M03Y", false),
            PromotionCode("GEV3WZF449UEX4LY1NJFAK3", false),
            PromotionCode("TDF0VDA65NTUZ0SRXJZM95H", false),
            PromotionCode("3V03XBFFCZ6SSWRY0L28DM2", false),
            PromotionCode("9GJ0VVU7CE2UQA1S1EW4B5W", false),
            PromotionCode("PY9ZQ9TBDE3V6QS6XV9M3VY", false),
            PromotionCode("9TTE8R5EZ642HSZV66KTW62", false),
            PromotionCode("GB1NP9AFWVMCGDLS7LQ7ZHQ", false),
            PromotionCode("TBSKF0AZP2JNXKVCXJETZ24", false),
            PromotionCode("KGNA2SGCJ66FU9LK0KP99CZ", false),
            PromotionCode("9BL2HQU56S2X6Q8M14Q42D4", false),
            PromotionCode("AFXSUR90E0S3P9ZTE6XHK9W", false),
            PromotionCode("E0N0LQC7V9ZVXUBLSYPQ2A8", false),
            PromotionCode("Q7QMDZKL2LVWFVBNLSZNQ7S", false),
            PromotionCode("PKR2F7VV62FWVHPJRELEVGK", false),
            PromotionCode("EJ2MKVY9DVAVGX7FGJNDVHV", false),
            PromotionCode("X24WUWNLRA5D0DQNBTE4WY8", false),
            PromotionCode("1Y0M0U9UWBBXJ9KS0ZU7FMB", false),
            PromotionCode("1N1TYE7LHETHUK73R2M7FU4", false),
            PromotionCode("CXMFWVYN91UNWBHGBVH88UW", false),
            PromotionCode("B2ZSJ0XAY1WLF2BHC3A8LN2", false),
            PromotionCode("7G22DHC1GJ3N0JKS1MXDP1Z", false),
            PromotionCode("P3C29LHRY5ATW39YEBERRB8", false),
            PromotionCode("3B0CAGVM61PDPKA4YKTLZ3J", false),
            PromotionCode("Q5N2QHV7AN59ULMXYCLNN8T", false),
            PromotionCode("NQY574KJ8UZUHFMV6TAFSGH", false),
            PromotionCode("BT12LUCJS6PFFEVF3BX6KH9", false),
            PromotionCode("5T2GXNX4ZB5U524DXRGW7KH", false),
            PromotionCode("V9E39GKKFCQ9G2RAG46CERJ", false),
            PromotionCode("BD3L2ASG2JN7SGRU90FKD7Z", false),
            PromotionCode("SCGMPELDMMN8VD8XAFA5L95", false),
            PromotionCode("UUYZA09Z5EHTQ3B7836J3TU", false),
            PromotionCode("NQ237W0RC028VB3E9S8WG3D", false),
            PromotionCode("5TSCT29TGVQWTHL39EDRRE4", false),
            PromotionCode("V1D5JMD405YSDW52NETG6UP", false),
            PromotionCode("0NLGJ3AHTXAMBW4CS07D2HS", false),
            PromotionCode("KXFUMT6YLM7WQT756XR0LW8", false),
            PromotionCode("GL5Z63DA33EGXXMV5ZN6BBG", false),
            PromotionCode("T2NWKFNXQ3WLCXW6V4A8D51", false),
            PromotionCode("NR2KF6NUNYM0NYN2L8GUT3H", false),
            PromotionCode("9TW28JJRRGHAH81E7X9DYKK", false),
            PromotionCode("RPVTPNMLUXSCEZQ343PL03P", false),
            PromotionCode("Z8ND8NKDC1S4Z2EGAJA8NX8", false),
            PromotionCode("66VCZQMB0ZTM6KYXTKB9DW8", false),
            PromotionCode("KRJH5FZPVUHS0U8JBKRT8HU", false),
            PromotionCode("WJ060D972YTXAMZ6X5BT69X", false),
            PromotionCode("HY2KHYWSQCC0N4S29UNBW55", false),
            PromotionCode("1KDS4ZPD1RLJBMZKAZV52T7", false),
            PromotionCode("5KNP4MTLWNDXP7A7RD8NRFF", false),
            PromotionCode("M9GK0LAB81VHHYQTR5DDSU1", false),
            PromotionCode("X6UC1NZY7B4SHP48FC90NQV", false),
            PromotionCode("37U7AMYPC82FUHW032NP7YN", false),
            PromotionCode("UAH37UEMEAPSCGBX3490JA8", false),
            PromotionCode("A3EHBZTVVVA3ZA7B7KN6XC6", false),
            PromotionCode("DRDNA5TJCUW4BSYUKV76Z0Z", false),
            PromotionCode("YDCWU4VA7TU6P57W4YVTVSY", false),
            PromotionCode("S6FLJJ9D9P7Z7T15XH0D0M4", false),
            PromotionCode("JN3P2RZ20SGS4Y6S4K0Q3YR", false),
            PromotionCode("ATJSWTJ8RYBWSX199ZLV702", false),
            PromotionCode("SKNPY0LRZ928PBPUT70TSP6", false),
            PromotionCode("X9QXYX461W2S5VH5EHN7FTP", false),
            PromotionCode("3XQ309Q6YBL3K6QRCAV5ZC3", false),
            PromotionCode("W19KR5RJVJGULPCJ8MFGGKJ", false),
            PromotionCode("YC7NGFPGHM4DBAWZ5KVEL68", false),
            PromotionCode("HW8G3HRNZ18M6MD7KW3DTHJ", false),
            PromotionCode("VM00GJPRAF0H969ZZHZ75QH", false),
            PromotionCode("3J2G3EUNED5PQB5429NBJ35", false),
            PromotionCode("VCEF2MAKDUTW2CFGJPZSJ0K", false),
            PromotionCode("4BC1CVXYQPYDWW4TZZWA7HH", false),
            PromotionCode("74T6WSD8AQS013JC31876XH", false),
            PromotionCode("8HTUXTZJYCHEKUZCE45Z77J", false),
            PromotionCode("C9CADLGEYAY4XHYLTC25D90", false),
            PromotionCode("3K9YQN4G8K4A3U97VM6S0GU", false),
            PromotionCode("7SG4KXH8UPRL8WS9DL1XL1F", false),
            PromotionCode("SPBHA4PXCZSSD5T7ZSXXEGQ", false),
            PromotionCode("TUH5AND7YT2LGD7Z7JKD3QJ", false),
            PromotionCode("1NEW01VUYP5YE7E6FL0ADQK", false),
            PromotionCode("2YMPCMZZWA8BNM5TPHPGG35", false),
            PromotionCode("6REDJ097QPGC6T12LPJQV51", false),
            PromotionCode("ZP82Q87RUN4ML3262NKVYTZ", false),
            PromotionCode("GADJ7XB2HAZNHUEPLJATDTY", false),
            PromotionCode("MCRYQ799SGC6DAU4ZTXZNS3", false),
            PromotionCode("PHHUVR5VC71SZJR7BHTDDTC", false),
            PromotionCode("B2BK5K34Y0KDBQE8BWU4UD7", false),
            PromotionCode("72J1CJE8VR6E5ZFWHZYQH7X", false),
            PromotionCode("B8X9M4L9VBG93L7NBJ50KYT", false),
            PromotionCode("SFY4FRX7N5U4STG21P4Z2ZP", false),
            PromotionCode("7ZTHAT9ZAJUSNF74GKF9YP9", false),
            PromotionCode("M3F92YCN92QFBP56V348W29", false),
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            startActivity(Intent(this@RewardActivity, DailyStatisticsActivity::class.java))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}