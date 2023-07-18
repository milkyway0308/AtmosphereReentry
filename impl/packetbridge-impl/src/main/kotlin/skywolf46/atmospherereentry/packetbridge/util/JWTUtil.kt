package skywolf46.atmospherereentry.packetbridge.util

import arrow.core.Either
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.security.KeyPair

object JWTUtil {
    private lateinit var key: KeyPair
    private lateinit var parser: JwtParser

    fun getKeyPair() : KeyPair {
        return key
    }

    fun initializeKey() {
        key = Keys.keyPairFor(SignatureAlgorithm.RS512)
        parser = Jwts.parserBuilder().setSigningKey(key.private).build()
    }

    fun createIdentifier(allowedServerId: String): String {
        return Jwts.builder()
            .signWith(key.private)
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