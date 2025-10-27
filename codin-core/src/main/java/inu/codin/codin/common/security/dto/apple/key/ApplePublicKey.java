package inu.codin.codin.common.security.dto.apple.key;

public record ApplePublicKey(String kty,
                             String kid,
                             String alg,
                             String n,
                             String e) {
}
