package inu.codin.auth.dto.apple.key;

public record ApplePublicKey(String kty,
                             String kid,
                             String alg,
                             String n,
                             String e) {
}
