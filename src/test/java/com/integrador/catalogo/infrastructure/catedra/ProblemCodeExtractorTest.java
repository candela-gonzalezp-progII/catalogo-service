package com.integrador.catalogo.infrastructure.catedra;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemCodeExtractorTest {

    @Test
    void extraeElCodeFuncionalDelProblemJson() {
        String body = "{\"title\":\"Conflict\",\"status\":409,\"code\": \"SLOT_ALREADY_HELD\"}";

        assertThat(ProblemCodeExtractor.extract(body)).isEqualTo("SLOT_ALREADY_HELD");
    }

    @Test
    void devuelveNullSiNoHayCode() {
        assertThat(ProblemCodeExtractor.extract("{\"status\":401}")).isNull();
        assertThat(ProblemCodeExtractor.extract("")).isNull();
        assertThat(ProblemCodeExtractor.extract(null)).isNull();
    }
}
