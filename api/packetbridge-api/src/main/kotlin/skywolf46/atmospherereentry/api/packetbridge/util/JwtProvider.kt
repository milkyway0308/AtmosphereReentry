package skywolf46.atmospherereentry.api.packetbridge.util

import arrow.core.Either
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.security.PrivateKey

class JwtProvider {
    private lateinit var key: PrivateKey
    private lateinit var parser: JwtParser

    fun getKeyPair(): PrivateKey {
        return key
    }

    fun exportKeyTo(file: File) {
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
        }
        file.outputStream().use {
            ObjectOutputStream(it).apply {
                writeObject(key)
                flush()
            }
        }
    }

    fun importOrInitializeKeyFrom(file: File) {
        if (file.exists()) {
            file.inputStream().use {
                ObjectInputStream(it).apply {
                    key = readObject() as PrivateKey
                    parser = Jwts.parserBuilder().setSigningKey(key).build()
                }
            }
        } else {
            initializeKey()
        }
    }

    fun initializeKey() {
        key = Keys.keyPairFor(SignatureAlgorithm.RS512).private
        parser = Jwts.parserBuilder().setSigningKey(key).build()
    }

    fun createIdentifier(allowedServerId: String): String {
        return Jwts.builder()
            .signWith(key)
            .claim("jwt-type", "packet-bridge")
            .claim("allowed-server", allowedServerId)
            .compact()
    }

    fun checkIdentifier(signedKey: String): Either<Throwable, String> {
        return Either.catch {
            parser.parseClaimsJws(signedKey).body["allowed-server"] as String
        }
    }
}