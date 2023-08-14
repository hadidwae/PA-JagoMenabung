package com.d3if2099.jagomenabung.model

data class Target(
    val id: String? = null,
    val judul: String? = null,
    val tanggalMulai: Long? = null,
    val tanggalBerakhir: Long? = null,
    val jumlahSaldo: Int? = null,
    val saldoTerkumpul: Int? = null,
    val aktif: Boolean? = null,
    val kunci: Boolean? = null
)
