package com.d3if2099.jagomenabung.model

data class Transaksi(
    val id: String? = null,
    val kategori: String? = null,
    val tanggal: Long? = null,
    val judul: String? = null,
    var jumlahSaldo: Int? = null,
    val keterangan: String? = null
)