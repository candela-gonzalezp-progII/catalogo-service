package com.integrador.catalogo.domain.model;

import java.util.List;

/** Pagina de resultados con el total, para que la app pueda paginar. */
public record PageResult<T>(List<T> items, int page, int size, long totalItems) {

    public PageResult {
        items = List.copyOf(items);
    }

    public int totalPages() {
        return (int) Math.ceil((double) totalItems / size);
    }
}
