package com.integrador.catalogo.infrastructure.catedra;

import com.integrador.catalogo.application.exception.CatedraAuthenticationException;
import com.integrador.catalogo.application.exception.CatedraUnavailableException;
import com.integrador.catalogo.domain.model.IntegrationConfig;
import com.integrador.catalogo.domain.model.ProvisioningStatus;
import com.integrador.catalogo.domain.model.TechnicalSession;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.ExpectedCount;

import static com.integrador.catalogo.infrastructure.catedra.CatedraClientFixtures.AUTH_JSON;
import static com.integrador.catalogo.infrastructure.catedra.CatedraClientFixtures.BASE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

/** CU-04: autenticar la cuenta tecnica y recuperar la configuracion. */
class CatedraAuthAdapterTest {

    private final CatedraClientFixtures fx = new CatedraClientFixtures();
    private final CatedraAuthAdapter adapter = new CatedraAuthAdapter(fx.tokens, fx.api);

    @Test
    void autenticaConLaCuentaTecnicaYDevuelveTokenYConfiguracion() {
        fx.server.expect(requestTo(BASE + "/api/authenticate"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> assertThat(((MockClientHttpRequest) request).getBodyAsString())
                        .contains("\"username\":\"candela\"")
                        .contains("\"rememberMe\":true"))
                .andRespond(withSuccess(AUTH_JSON, MediaType.APPLICATION_JSON));

        TechnicalSession session = adapter.authenticate();

        assertThat(session.token()).isEqualTo("jwt-tecnico");
        assertThat(session.integration().groupId()).isEqualTo("proyecto-candela");
        assertThat(session.integration().redisPort()).isEqualTo(6379);
        assertThat(session.integration().isProvisioned()).isTrue();
        fx.server.verify();
    }

    @Test
    void creedencialesRechazadasSeTraducenAUnErrorDeAutenticacion() {
        fx.server.expect(requestTo(BASE + "/api/authenticate")).andRespond(withUnauthorizedRequest());

        assertThatThrownBy(adapter::authenticate)
                .isInstanceOf(CatedraAuthenticationException.class)
                .hasMessageContaining("401");
    }

    @Test
    void siLaCatedraNoRespondeReintentaDeFormaAcotada() {
        fx.server.expect(ExpectedCount.times(3), requestTo(BASE + "/api/authenticate"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(adapter::authenticate).isInstanceOf(CatedraUnavailableException.class);

        fx.server.verify();
    }

    @Test
    void recuperaLaConfiguracionVigenteUsandoElTokenTecnico() {
        fx.server.expect(requestTo(BASE + "/api/authenticate"))
                .andRespond(withSuccess(AUTH_JSON, MediaType.APPLICATION_JSON));
        fx.server.expect(requestTo(BASE + "/api/student/integration"))
                .andExpect(header("Authorization", "Bearer jwt-tecnico"))
                .andRespond(withSuccess(integrationJson("PROVISIONED"), MediaType.APPLICATION_JSON));

        IntegrationConfig config = adapter.fetchIntegration();

        assertThat(config.provisioningStatus()).isEqualTo(ProvisioningStatus.PROVISIONED);
        assertThat(config.kafkaCatalogTopic()).isEqualTo("catedra.catalog.proyecto-candela");
        fx.server.verify();
    }

    @Test
    void unaIntegracionPendienteSeReportaSinUsarRedis() {
        fx.server.expect(requestTo(BASE + "/api/authenticate"))
                .andRespond(withSuccess(AUTH_JSON, MediaType.APPLICATION_JSON));
        fx.server.expect(requestTo(BASE + "/api/student/integration"))
                .andRespond(withSuccess("{\"groupId\":\"proyecto-candela\",\"provisioningStatus\":\"PENDING\"}",
                        MediaType.APPLICATION_JSON));

        IntegrationConfig config = adapter.fetchIntegration();

        assertThat(config.isProvisioned()).isFalse();
        assertThat(config.redisPassword()).isNull();
    }

    private static String integrationJson(String status) {
        return "{\"groupId\":\"proyecto-candela\",\"redisHost\":\"redis.catedra.internal\",\"redisPort\":6379,"
                + "\"kafkaCatalogTopic\":\"catedra.catalog.proyecto-candela\",\"provisioningStatus\":\"" + status + "\"}";
    }
}
