package com.yellowtwigs.knockin.utils

import android.content.Context
import android.util.Log
import com.yellowtwigs.knockin.R

object RandomDefaultImage {
    fun randomDefaultImage(avatarId: Int, cxt: Context): Int {
        val sharedPrefMultiColor = cxt.getSharedPreferences("IsMultiColor", Context.MODE_PRIVATE)
        val multiColor = sharedPrefMultiColor.getInt("IsMultiColor", 0)
        val sharedPreferencesContactsColor =
            cxt.getSharedPreferences("ContactsColor", Context.MODE_PRIVATE)
        val contactsColorPosition = sharedPreferencesContactsColor.getInt("contactsColor", 0)

        Log.i("randomDefaultImage", "multiColor : $multiColor")
        Log.i("randomDefaultImage", "contactsColorPosition : $contactsColorPosition")

        return if (multiColor == 0) {
            when (avatarId) {
                0 -> R.drawable.ic_user_purple
                1 -> R.drawable.ic_user_blue
                2 -> R.drawable.ic_user_cyan_teal
                3 -> R.drawable.ic_user_green
                4 -> R.drawable.ic_user_om
                5 -> R.drawable.ic_user_orange
                6 -> R.drawable.ic_user_red
                else -> R.drawable.ic_user_blue
            }
        } else {
            when (contactsColorPosition) {
                0 -> when (avatarId) {
                    0 -> R.drawable.ic_user_blue
                    1 -> R.drawable.ic_user_blue_indigo1
                    2 -> R.drawable.ic_user_blue_indigo2
                    3 -> R.drawable.ic_user_blue_indigo3
                    4 -> R.drawable.ic_user_blue_indigo4
                    5 -> R.drawable.ic_user_blue_indigo5
                    6 -> R.drawable.ic_user_blue_indigo6
                    else -> R.drawable.ic_user_om
                }
                1 -> when (avatarId) {
                    0 -> R.drawable.ic_user_green
                    1 -> R.drawable.ic_user_green_lime1
                    2 -> R.drawable.ic_user_green_lime2
                    3 -> R.drawable.ic_user_green_lime3
                    4 -> R.drawable.ic_user_green_lime4
                    5 -> R.drawable.ic_user_green_lime5
                    else -> R.drawable.ic_user_green_lime6
                }
                2 -> when (avatarId) {
                    0 -> R.drawable.ic_user_purple
                    1 -> R.drawable.ic_user_purple_grape1
                    2 -> R.drawable.ic_user_purple_grape2
                    3 -> R.drawable.ic_user_purple_grape3
                    4 -> R.drawable.ic_user_purple_grape4
                    5 -> R.drawable.ic_user_purple_grape5
                    else -> R.drawable.ic_user_purple
                }
                3 -> when (avatarId) {
                    0 -> R.drawable.ic_user_red
                    1 -> R.drawable.ic_user_red1
                    2 -> R.drawable.ic_user_red2
                    3 -> R.drawable.ic_user_red3
                    4 -> R.drawable.ic_user_red4
                    5 -> R.drawable.ic_user_red5
                    else -> R.drawable.ic_user_red
                }
                4 -> when (avatarId) {
                    0 -> R.drawable.ic_user_grey
                    1 -> R.drawable.ic_user_grey1
                    2 -> R.drawable.ic_user_grey2
                    3 -> R.drawable.ic_user_grey3
                    4 -> R.drawable.ic_user_grey4
                    else -> R.drawable.ic_user_grey1
                }
                5 -> when (avatarId) {
                    0 -> R.drawable.ic_user_orange
                    1 -> R.drawable.ic_user_orange1
                    2 -> R.drawable.ic_user_orange2
                    3 -> R.drawable.ic_user_orange3
                    4 -> R.drawable.ic_user_orange4
                    else -> R.drawable.ic_user_orange3
                }
                6 -> when (avatarId) {
                    0 -> R.drawable.ic_user_cyan_teal
                    1 -> R.drawable.ic_user_cyan_teal1
                    2 -> R.drawable.ic_user_cyan_teal2
                    3 -> R.drawable.ic_user_cyan_teal3
                    4 -> R.drawable.ic_user_cyan_teal4
                    else -> R.drawable.ic_user_cyan_teal
                }
                else -> when (avatarId) {
                    0 -> R.drawable.ic_user_purple
                    1 -> R.drawable.ic_user_blue
                    2 -> R.drawable.ic_user_cyan_teal
                    3 -> R.drawable.ic_user_green
                    4 -> R.drawable.ic_user_om
                    5 -> R.drawable.ic_user_orange
                    6 -> R.drawable.ic_user_red
                    else -> R.drawable.ic_user_blue
                }
            }
        }
    }

    fun randomDefaultImage(avatarId: Int, cxt: Context, createOrGet: String): Int {
        val sharedPreferencesIsMultiColor =
            cxt.getSharedPreferences("IsMultiColor", Context.MODE_PRIVATE)
        val multiColor = sharedPreferencesIsMultiColor.getInt("IsMultiColor", 0)

        if (createOrGet == "Create") {
            return kotlin.random.Random.nextInt(0, 7)
        } else if (createOrGet == "Get") {
            return if (multiColor == 0) {
                when (avatarId) {
                    0 -> R.drawable.ic_user_purple
                    1 -> R.drawable.ic_user_blue
                    2 -> R.drawable.ic_user_cyan_teal
                    3 -> R.drawable.ic_user_green
                    4 -> R.drawable.ic_user_om
                    5 -> R.drawable.ic_user_orange
                    6 -> R.drawable.ic_user_red
                    else -> R.drawable.ic_user_blue
                }
            } else {
                val sharedPreferencesContactsColor =
                    cxt.getSharedPreferences("ContactsColor", Context.MODE_PRIVATE)
                val contactsColorPosition =
                    sharedPreferencesContactsColor.getInt("contactsColor", 0)
                when (contactsColorPosition) {
                    0 -> when (avatarId) {
                        0 -> R.drawable.ic_user_blue
                        1 -> R.drawable.ic_user_blue_indigo1
                        2 -> R.drawable.ic_user_blue_indigo2
                        3 -> R.drawable.ic_user_blue_indigo3
                        4 -> R.drawable.ic_user_blue_indigo4
                        5 -> R.drawable.ic_user_blue_indigo5
                        6 -> R.drawable.ic_user_blue_indigo6
                        else -> R.drawable.ic_user_om
                    }
                    1 -> when (avatarId) {
                        0 -> R.drawable.ic_user_green
                        1 -> R.drawable.ic_user_green_lime1
                        2 -> R.drawable.ic_user_green_lime2
                        3 -> R.drawable.ic_user_green_lime3
                        4 -> R.drawable.ic_user_green_lime4
                        5 -> R.drawable.ic_user_green_lime5
                        else -> R.drawable.ic_user_green_lime6
                    }
                    2 -> when (avatarId) {
                        0 -> R.drawable.ic_user_purple
                        1 -> R.drawable.ic_user_purple_grape1
                        2 -> R.drawable.ic_user_purple_grape2
                        3 -> R.drawable.ic_user_purple_grape3
                        4 -> R.drawable.ic_user_purple_grape4
                        5 -> R.drawable.ic_user_purple_grape5
                        else -> R.drawable.ic_user_purple
                    }
                    3 -> when (avatarId) {
                        0 -> R.drawable.ic_user_red
                        1 -> R.drawable.ic_user_red1
                        2 -> R.drawable.ic_user_red2
                        3 -> R.drawable.ic_user_red3
                        4 -> R.drawable.ic_user_red4
                        5 -> R.drawable.ic_user_red5
                        else -> R.drawable.ic_user_red
                    }
                    4 -> when (avatarId) {
                        0 -> R.drawable.ic_user_grey
                        1 -> R.drawable.ic_user_grey1
                        2 -> R.drawable.ic_user_grey2
                        3 -> R.drawable.ic_user_grey3
                        4 -> R.drawable.ic_user_grey4
                        else -> R.drawable.ic_user_grey1
                    }
                    5 -> when (avatarId) {
                        0 -> R.drawable.ic_user_orange
                        1 -> R.drawable.ic_user_orange1
                        2 -> R.drawable.ic_user_orange2
                        3 -> R.drawable.ic_user_orange3
                        4 -> R.drawable.ic_user_orange4
                        else -> R.drawable.ic_user_orange3
                    }
                    6 -> when (avatarId) {
                        0 -> R.drawable.ic_user_cyan_teal
                        1 -> R.drawable.ic_user_cyan_teal1
                        2 -> R.drawable.ic_user_cyan_teal2
                        3 -> R.drawable.ic_user_cyan_teal3
                        4 -> R.drawable.ic_user_cyan_teal4
                        else -> R.drawable.ic_user_cyan_teal
                    }
                    else -> when (avatarId) {
                        0 -> R.drawable.ic_user_purple
                        1 -> R.drawable.ic_user_blue
                        2 -> R.drawable.ic_user_cyan_teal
                        3 -> R.drawable.ic_user_green
                        4 -> R.drawable.ic_user_om
                        5 -> R.drawable.ic_user_orange
                        6 -> R.drawable.ic_user_red
                        else -> R.drawable.ic_user_blue
                    }
                }
            }
        }
        return -1
    }
}