package com.nnp.common.config.crypto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuantumDecryptRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonProperty("kem_ciphertext")
    private String kemCiphertext;

    @JsonProperty("x25519_ephemeral_public")
    private String x25519EphemeralPublic;

    @JsonProperty("nonce")
    private String nonce;

    @JsonProperty("ciphertext")
    private String ciphertext;
}
