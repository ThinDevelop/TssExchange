package com.tss.exchange.entity

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CurrencyModel(
    var currencyName: String?,
    var number: String?,
    var price: String?,
    var summary: String?): Parcelable {

    private companion object : Parceler<CurrencyModel> {
        override fun CurrencyModel.write(parcel: Parcel, flags: Int) {
            parcel.writeString(currencyName)
            parcel.writeString(number)
            parcel.writeString(price)
            parcel.writeString(summary)
        }

        override fun create(parcel: Parcel): CurrencyModel {
            return CurrencyModel(parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString())
        }
    }
}