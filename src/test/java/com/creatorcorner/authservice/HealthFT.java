package com.creatorcorner.authservice;

import org.junit.jupiter.api.Test;

class HealthFT extends AbstractBaseFT {

    @Test
    void healthCheckReturns200() {
        this.client.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("status").isEqualTo("UP")
                .jsonPath("components.r2dbc.status").isEqualTo("UP");
    }
}
