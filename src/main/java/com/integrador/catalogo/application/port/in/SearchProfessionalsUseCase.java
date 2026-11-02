package com.integrador.catalogo.application.port.in;

import com.integrador.catalogo.domain.model.PageResult;
import com.integrador.catalogo.domain.model.ProfessionalSearchCriteria;
import com.integrador.catalogo.domain.model.ProfessionalView;

/** CU-08: buscar y filtrar profesionales usando SOLO la copia local del catalogo. */
public interface SearchProfessionalsUseCase {

    PageResult<ProfessionalView> search(ProfessionalSearchCriteria criteria);
}
