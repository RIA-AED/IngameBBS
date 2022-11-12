package ink.ltm.ingameBBS.utils

import java.util.zip.CRC32

fun calculateCRC32(str: String): String {
    val crc = CRC32()
    crc.update(str.toByteArray())
    return crc.value.toString()
}