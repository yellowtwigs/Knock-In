package com.yellowtwigs.knockin.model.database.data

data class PromotionCode (val content: String, val isUsed: Boolean){
    constructor(): this("", false)
}
