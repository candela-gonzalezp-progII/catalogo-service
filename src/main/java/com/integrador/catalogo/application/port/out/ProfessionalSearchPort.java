package com.integrador.catalogo.application.port.out;

import com.integrador.catalogo.domain.model.PageResult;
import com.integrador.catalogo.domain.model.ProfessionalSearchCriteria;
import com.integrador.catalogo.domain.model.ProfessionalView;

import java.util.Optional;

/** Consultas de lectura sobre la copia local (separadas de los repositorios de escritura: ISP). */
public interface ProfessionalSearchPort {

    PageResult<ProfessionalView> search(ProfessionalSearchCriteria criteria);

    Optional<ProfessionalView> findViewById(Long id);
}
