package com.integrador.catalogo.application.service;

import com.integrador.catalogo.application.exception.ProfessionalNotFoundException;
import com.integrador.catalogo.application.port.out.ProfessionalCategoryRepositoryPort;
import com.integrador.catalogo.application.port.out.ProfessionalSearchPort;
import com.integrador.catalogo.domain.model.PageResult;
import com.integrador.catalogo.domain.model.Professional;
import com.integrador.catalogo.domain.model.ProfessionalCategory;
import com.integrador.catalogo.domain.model.ProfessionalSearchCriteria;
import com.integrador.catalogo.domain.model.ProfessionalView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfessionalQueryServiceTest {

    private final ProfessionalView ana = new ProfessionalView(
            new Professional(15L, "Ana", "Gomez", 1L, true), new ProfessionalCategory(1L, "Clinica", null, true), List.of());

    private final ProfessionalSearchPort searchPort = new ProfessionalSearchPort() {
        @Override
        public PageResult<ProfessionalView> search(ProfessionalSearchCriteria criteria) {
            return new PageResult<>(List.of(ana), criteria.page(), criteria.size(), 1);
        }

        @Override
        public Optional<ProfessionalView> findViewById(Long id) {
            return id == 15L ? Optional.of(ana) : Optional.empty();
        }
    };

    private final ProfessionalCategoryRepositoryPort categoryPort = new ProfessionalCategoryRepositoryPort() {
        @Override
        public ProfessionalCategory save(ProfessionalCategory category) {
            return category;
        }

        @Override
        public Optional<ProfessionalCategory> findById(Long id) {
            return Optional.empty();
        }

        @Override
        public List<ProfessionalCategory> findAll() {
            return List.of(new ProfessionalCategory(2L, "pediatria", null, true),
                    new ProfessionalCategory(1L, "Clinica", null, true));
        }

        @Override
        public void deleteAll() {
        }
    };

    private final ProfessionalQueryService service = new ProfessionalQueryService(searchPort, categoryPort);

    @Test
    void buscaDelegandoEnElPuertoDeLecturaLocal() {
        var result = service.search(ProfessionalSearchCriteria.all());

        assertThat(result.items()).containsExactly(ana);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void devuelveElDetalleDeUnProfesional() {
        assertThat(service.getById(15L)).isSameAs(ana);
    }

    @Test
    void unProfesionalInexistenteEsUnError() {
        assertThatThrownBy(() -> service.getById(99L)).isInstanceOf(ProfessionalNotFoundException.class);
    }

    @Test
    void listaLasCategoriasOrdenadasPorNombre() {
        assertThat(service.listAll()).extracting(ProfessionalCategory::getName).containsExactly("Clinica", "pediatria");
    }
}
