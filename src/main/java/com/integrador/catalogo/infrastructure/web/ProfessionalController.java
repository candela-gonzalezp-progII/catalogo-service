package com.integrador.catalogo.infrastructure.web;

import com.integrador.catalogo.application.port.in.GetProfessionalUseCase;
import com.integrador.catalogo.application.port.in.ListCategoriesUseCase;
import com.integrador.catalogo.application.port.in.SearchProfessionalsUseCase;
import com.integrador.catalogo.domain.model.ProfessionalSearchCriteria;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Adaptador de entrada de CU-08. Solo traduce HTTP <-> casos de uso. */
@RestController
@RequestMapping("/api")
public class ProfessionalController {

    private final SearchProfessionalsUseCase search;
    private final GetProfessionalUseCase getProfessional;
    private final ListCategoriesUseCase listCategories;

    public ProfessionalController(SearchProfessionalsUseCase search, GetProfessionalUseCase getProfessional,
                                  ListCategoriesUseCase listCategories) {
        this.search = search;
        this.getProfessional = getProfessional;
        this.listCategories = listCategories;
    }

    @GetMapping("/professionals")
    public PageResponse<ProfessionalResponse> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) DayOfWeek day,
            @RequestParam(required = false) String time,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + ProfessionalSearchCriteria.DEFAULT_PAGE_SIZE) int size) {
        var criteria = new ProfessionalSearchCriteria(categoryId, name, enabled, day, parseTime(time), page, size);
        return PageResponse.from(search.search(criteria), ProfessionalResponse::from);
    }

    @GetMapping("/professionals/{id}")
    public ProfessionalResponse get(@PathVariable Long id) {
        return ProfessionalResponse.from(getProfessional.getById(id));
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return listCategories.listAll().stream().map(CategoryResponse::from).toList();
    }

    private static LocalTime parseTime(String time) {
        if (time == null || time.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(time.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("time debe tener formato HH:mm");
        }
    }
}
