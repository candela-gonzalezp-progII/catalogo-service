package com.integrador.catalogo.infrastructure.persistence.adapter;

import com.integrador.catalogo.application.port.out.ProfessionalSearchPort;
import com.integrador.catalogo.domain.model.PageResult;
import com.integrador.catalogo.domain.model.ProfessionalCategory;
import com.integrador.catalogo.domain.model.ProfessionalSearchCriteria;
import com.integrador.catalogo.domain.model.ProfessionalView;
import com.integrador.catalogo.domain.model.WeeklySchedule;
import com.integrador.catalogo.infrastructure.persistence.entity.ProfessionalEntity;
import com.integrador.catalogo.infrastructure.persistence.jpa.ProfessionalCategoryJpaRepository;
import com.integrador.catalogo.infrastructure.persistence.jpa.ProfessionalJpaRepository;
import com.integrador.catalogo.infrastructure.persistence.jpa.WeeklyScheduleJpaRepository;
import com.integrador.catalogo.infrastructure.persistence.mapper.ProfessionalCategoryMapper;
import com.integrador.catalogo.infrastructure.persistence.mapper.ProfessionalMapper;
import com.integrador.catalogo.infrastructure.persistence.mapper.WeeklyScheduleMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Busqueda sobre la copia local. La consulta se arma dinamicamente: solo se agregan las
 * condiciones de los filtros informados, y los valores siempre viajan como parametros
 * (nunca concatenados), por lo que no hay inyeccion de JPQL.
 */
@Component
@Transactional(readOnly = true)
public class ProfessionalSearchAdapter implements ProfessionalSearchPort {

    @PersistenceContext
    private EntityManager em;

    private final ProfessionalJpaRepository professionals;
    private final ProfessionalCategoryJpaRepository categories;
    private final WeeklyScheduleJpaRepository schedules;

    public ProfessionalSearchAdapter(ProfessionalJpaRepository professionals,
                                     ProfessionalCategoryJpaRepository categories,
                                     WeeklyScheduleJpaRepository schedules) {
        this.professionals = professionals;
        this.categories = categories;
        this.schedules = schedules;
    }

    @Override
    public PageResult<ProfessionalView> search(ProfessionalSearchCriteria criteria) {
        QueryParts parts = buildWhere(criteria);
        long total = count(parts);
        List<ProfessionalEntity> page = fetchPage(parts, criteria);
        return new PageResult<>(toViews(page), criteria.page(), criteria.size(), total);
    }

    @Override
    public Optional<ProfessionalView> findViewById(Long id) {
        return professionals.findById(id).map(entity -> toViews(List.of(entity)).get(0));
    }

    // ---- armado de la consulta -------------------------------------------------

    private QueryParts buildWhere(ProfessionalSearchCriteria c) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        if (c.categoryId() != null) {
            conditions.add("p.categoryId = :categoryId");
            params.put("categoryId", c.categoryId());
        }
        if (c.name() != null) {
            conditions.add("lower(concat(p.firstName, ' ', p.lastName)) like :name escape '\\'");
            params.put("name", "%" + escapeLike(c.name().toLowerCase()) + "%");
        }
        if (c.enabled() != null) {
            conditions.add("p.active = :enabled");
            params.put("enabled", c.enabled());
        }
        if (c.day() != null) {
            conditions.add(availabilityCondition(c, params));
        }
        return new QueryParts(conditions.isEmpty() ? "" : " where " + String.join(" and ", conditions), params);
    }

    private static String availabilityCondition(ProfessionalSearchCriteria c, Map<String, Object> params) {
        params.put("day", c.day());
        StringBuilder sb = new StringBuilder(
                "exists (select 1 from WeeklyScheduleEntity s where s.professionalId = p.id"
                        + " and s.active = true and s.dayOfWeek = :day");
        if (c.time() != null) {
            sb.append(" and s.startTime <= :time and s.endTime > :time");
            params.put("time", c.time());
        }
        return sb.append(")").toString();
    }

    private static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private long count(QueryParts parts) {
        TypedQuery<Long> query = em.createQuery("select count(p) from ProfessionalEntity p" + parts.where(), Long.class);
        parts.params().forEach(query::setParameter);
        return query.getSingleResult();
    }

    private List<ProfessionalEntity> fetchPage(QueryParts parts, ProfessionalSearchCriteria c) {
        TypedQuery<ProfessionalEntity> query = em.createQuery(
                "select p from ProfessionalEntity p" + parts.where() + " order by p.lastName, p.firstName, p.id",
                ProfessionalEntity.class);
        parts.params().forEach(query::setParameter);
        query.setFirstResult(c.page() * c.size());
        query.setMaxResults(c.size());
        return query.getResultList();
    }

    // ---- armado de las vistas (sin N+1: dos consultas por pagina) -----------------

    private List<ProfessionalView> toViews(List<ProfessionalEntity> page) {
        if (page.isEmpty()) {
            return List.of();
        }
        List<Long> ids = page.stream().map(ProfessionalEntity::getId).toList();
        Map<Long, List<WeeklySchedule>> schedulesByProfessional = schedules.findByProfessionalIdIn(ids).stream()
                .map(WeeklyScheduleMapper::toDomain)
                .collect(Collectors.groupingBy(WeeklySchedule::getProfessionalId));
        Map<Long, ProfessionalCategory> categoriesById = loadCategories(page);
        return page.stream()
                .map(entity -> new ProfessionalView(
                        ProfessionalMapper.toDomain(entity),
                        categoriesById.get(entity.getCategoryId()),
                        schedulesByProfessional.getOrDefault(entity.getId(), List.of())))
                .toList();
    }

    private Map<Long, ProfessionalCategory> loadCategories(Collection<ProfessionalEntity> page) {
        List<Long> categoryIds = page.stream().map(ProfessionalEntity::getCategoryId).distinct().toList();
        return categories.findAllById(categoryIds).stream()
                .map(ProfessionalCategoryMapper::toDomain)
                .collect(Collectors.toMap(ProfessionalCategory::getId, c -> c));
    }

    private record QueryParts(String where, Map<String, Object> params) {
    }
}
