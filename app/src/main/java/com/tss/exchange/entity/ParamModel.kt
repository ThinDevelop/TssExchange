package com.tss.exchange.entity

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ParamModel(
    var id: String?,
    var date: String?,
    var customerName: String?,
    var docNumber: String?,
    var address: String?,
    var phone: String?,
    var objective: String?,
    var summary: String?,
    var adminSign: String?,
    var customerSign: String?) : Parcelable {


    private companion object : Parceler<ParamModel> {
        override fun ParamModel.write(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(date)
            parcel.writeString(customerName)
            parcel.writeString(docNumber)
            parcel.writeString(address)
            parcel.writeString(phone)
            parcel.writeString(objective)
            parcel.writeString(summary)
            parcel.writeString(adminSign)
            parcel.writeString(customerSign)
        }

        override fun create(parcel: Parcel): ParamModel {
            return ParamModel(parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString())
        }
    }

}