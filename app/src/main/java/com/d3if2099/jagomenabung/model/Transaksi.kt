package com.d3if2099.jagomenabung.model

import java.time.LocalDate

data class Transaksi(
    val id: String? = null,
    val kategori: String? = null,
    val tanggal: String? = null,
    val judul: String? = null,
    var jumlahSaldo: Int? = null,
    val keterangan: String? = null
)