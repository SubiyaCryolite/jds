package tests

import org.apache.commons.codec.digest.DigestUtils
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import java.util.Base64.getEncoder
import com.sun.deploy.util.Base64Wrapper.encodeToString
import java.util.*


class MD5Hash {

    @Test
    fun testDates() {
        //from memory
        val version = LocalDate.of(2017, Month.OCTOBER, 10) //card version, will help with future and backward compatibility
        val issued = LocalDate.of(2006, Month.DECEMBER, 21) //from client record date
        val modified = LocalDate.of(2009, Month.FEBRUARY, 3) //current time as of save

        //prepare format
        val dateFormat = "yyyyMMdd"
        val dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat)

        //formatted string
        val fmtVersion = dateTimeFormatter.format(version)
        val fmtIssued = dateTimeFormatter.format(issued)
        val fmtModified = dateTimeFormatter.format(modified)

        //encryption password
        var password = ""
        password += when (fmtVersion[3]) {
            '1', '5', '9' -> fmtIssued
            else -> fmtVersion
        }
        password += '~'
        password += when (fmtIssued[5]) {
            '4', '9', '1' -> fmtModified
            else -> fmtIssued
        }
        password += '-'
        password += when (fmtIssued[7]) {
            '2', '6', '7' -> fmtVersion
            else -> fmtModified
        }

        //This will be da record. Duh
        val rawJson = "{\"widget\":{\"debug\":\"on\",\"window\":{\"title\":\"Sample Konfabulator Widget\",\"name\":\"main_window\",\"width\":500,\"height\":500},\"image\":{\"src\":\"Images/Sun.png\",\"name\":\"sun1\",\"hOffset\":250,\"vOffset\":250,\"alignment\":\"center\"},\"text\":{\"data\":\"Click Here\",\"size\":36,\"style\":\"bold\",\"name\":\"text1\",\"hOffset\":250,\"vOffset\":100,\"alignment\":\"center\",\"onMouseUp\":\"sun1.opacity = (sun1.opacity / 100) * 90;\"}}}"
        //used to ensure its accurate
        val hashcode = DigestUtils.md5Hex(rawJson)

        //use the password to encode the string


        println("=================================================")
        println("Raw imput    = $rawJson")

        //write as a string?
        val finalOutput = "$fmtVersion$fmtIssued$fmtModified$hashcode$rawJson"
        println("=================================================")
        println("Final output = $finalOutput")

        var v = finalOutput.subSequence(0, 8)
        var i = finalOutput.subSequence(8, 16)
        var m = finalOutput.subSequence(16, 24)
        var h = finalOutput.subSequence(24, 56)
        var b = finalOutput.subSequence(56, finalOutput.length - 1)

        println("=================================================")
        println("Version = $v")
        println("Initiated = $i")
        println("Modified = $m")
        println("Hash = $h")
        println("Body = $b")
    }
}