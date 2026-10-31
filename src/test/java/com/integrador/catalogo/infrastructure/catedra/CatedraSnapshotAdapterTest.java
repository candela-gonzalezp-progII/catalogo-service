package com.integrador.catalogo.infrastructure.catedra;

import com.integrador.catalogo.application.exception.CatedraIntegrationException;
import com.integrador.catalogo.domain.model.CatalogSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static com.integrador.catalogo.infrastructure.catedra.CatedraClientFixtures.AUTH_JSON;
import static com.integrador.catalogo.infrastructure.catedra.CatedraClientFixtures.BASE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

/** CU-05: el cliente del snapshot (contrato del anexo, seccion 7). */
class CatedraSnapshotAdapterTest {

    private static final String SNAPSHOT_JSON = """
            {"snapshotVersion":7,"generatedAt":"2026-09-10T13:44:59Z","campoNuevo":1,
             "professionalCategories":[{"id":1,"name":"Clinica medica","description":"General","enabled":true,
               "createdAt":"2026-07-01T12:00:00Z","updatedAt":"2026-09-10T13:40:00Z"}],
             "professionals":[{"id":15,"categoryId":1,"firstName":"Ana","lastName":"Gomez","enabled":true}],
             "weeklySchedules":[{"id":21,"professionalId":15,"dayOfWeek":"MONDAY","startTime":"09:00:00",
               "endTime":"12:00:00","slotDurationMinutes":30,"enabled":false}]}
            """;

    private final CatedraClientFixtures fx = new CatedraClientFixtures();
    private final CatedraSnapshotAdapter adapter = new CatedraSnapshotAdapter(fx.api);

    @Test
    void obtieneElSnapshotYLoConvierteADominio() {
        expectAuthentication();
        fx.server.expect(requestTo(BASE + "/api/synchronization/snapshot"))
                .andExpect(header("Authorization", "Bearer jwt-tecnico"))
                .andRespond(withSuccess(SNAPSHOT_JSON, MediaType.APPLICATION_JSON));

        CatalogSnapshot snapshot = adapter.fetchSnapshot();

        assertThat(snapshot.version()).isEqualTo(7L);
        assertThat(snapshot.categories()).hasSize(1);
        assertThat(snapshot.professionals().get(0).fullName()).isEqualTo("Ana Gomez");
        var schedule = snapshot.schedules().get(0);
        assertThat(schedule.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(schedule.getSlotDurationMinutes()).isEqualTo(30);
        assertThat(schedule.isActive()).isFalse();
        fx.server.verify();
    }

    @Test
    void siElTokenFueRechazadoSeReautenticaUnaSolaVezYReintenta() {
        expectAuthentication();
        fx.server.expect(requestTo(BASE + "/api/synchronization/snapshot")).andRespond(withUnauthorizedRequest());
        expectAuthentication();
        fx.server.expect(requestTo(BASE + "/api/synchronization/snapshot"))
                .andRespond(withSuccess(SNAPSHOT_JSON, MediaType.APPLICATION_JSON));

        assertThat(adapter.fetchSnapshot().version()).isEqualTo(7L);

        fx.server.verify();
    }

    @Test
    void reutilizaElTokenEnLasLlamadasSiguientes() {
        expectAuthentication();
        fx.server.expect(requestTo(BASE + "/api/synchronization/snapshot"))
                .andRespond(withSuccess(SNAPSHOT_JSON, MediaType.APPLICATION_JSON));
        fx.server.expect(requestTo(BASE + "/api/synchronization/snapshot"))
                .andExpect(header("Authorization", "Bearer jwt-tecnico"))
                .andRespond(withSuccess(SNAPSHOT_JSON, MediaType.APPLICATION_JSON));

        adapter.fetchSnapshot();
        adapter.fetchSnapshot();

        fx.server.verify();
    }

    @Test
    void rechazaUnSnapshotConDatosInvalidosAntesDeTocarLaBase() {
        String invalido = SNAPSHOT_JSON.replace("\"endTime\":\"12:00:00\"", "\"endTime\":\"08:00:00\"");
        expectAuthentication();
        fx.server.expect(requestTo(BASE + "/api/synchronization/snapshot"))
                .andRespond(withSuccess(invalido, MediaType.APPLICATION_JSON));

        assertThatThrownBy(adapter::fetchSnapshot)
                .isInstanceOf(CatedraIntegrationException.class)
                .hasMessageContaining("Snapshot invalido");
    }

    private void expectAuthentication() {
        fx.server.expect(requestTo(BASE + "/api/authenticate"))
                .andRespond(withSuccess(AUTH_JSON, MediaType.APPLICATION_JSON));
    }
}
