package com.deepblue.rescue.repository;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
    List<Animal> findByAnimalOrderByPerformedAtAsc(Long animalId);

    @Query("""
        select t
        from Treatment t
        where t.performedAt between :start and :end
        order by t.performedAt asc
""")
    List<Treatment> findByPerformedAtBetween(LocalDate start, LocalDate end);

    @Query("""
        select t
        from Treatment 
        join t.Animal a
        join a.rescueCase rc
        join rc.rescueCenter rcenter
        where rcenter.code = :centercode
""")
    List<Treatment> findByRescueCenterCode(String centerCode);

    @Query("""
        select distinct t 
        from Treatment t
        join t.specialist s 
        join s.expertiseAreas e
        where lower(e.name) = lower(:expertiseName)
""")
    List<Treatment> findBySpecialistExpertiseAreasNameIgnoreCase(String expertiseName);
}
