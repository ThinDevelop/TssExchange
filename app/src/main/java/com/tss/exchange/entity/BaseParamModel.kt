package com.tss.exchange.entity

data class BaseParamModel(
        var id: String?,
        var date: String?,
        var customerName: String?,
        var docNumber: String?,
        var address: String?,
        var phone: String?,
        var objective: String?,
        var summary: String?,
        var adminSign: String?,
        var customerSign: String?,
        var currencyList: ArrayList<CurrencyModel>?) {

    open fun getParamModel(): ParamModel {
        return ParamModel(id, date, customerName,docNumber, address,phone, objective, summary, adminSign, customerSign)
    }

    open fun getCurrencyListModel(): ArrayList<CurrencyModel>? {
        return currencyList
    }
}