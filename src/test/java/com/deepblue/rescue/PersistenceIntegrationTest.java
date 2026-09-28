package com.deepblue.rescue;

import com.deepblue.rescue.domain.*;
import com.deepblue.rescue.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:18-alpine")
                    .withDatabaseName("deepblue-test")
                    .withUsername("deepblue")
                    .withPassword("deepblue");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RescueCenterRepository rescueCenterRepository;
    @Autowired
    private RescueCaseRepository rescueCaseRepository;
    @Autowired
    private AnimalRepository animalRepository;
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    @Autowired
    private SpecialistRepository specialistRepository;
    @Autowired
    private ExpertiseRepository expertiseRepository;
    @Autowired
    private TreatmentRepository treatmentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // =========================================================================
    // Test de Flyway
    // =========================================================================
    @Test
    @DisplayName("Flyway debe ejecutar al menos V1 y V2")
    void flywayShouldExecuteMigrations() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true",
                Integer.class
        );

        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    // =========================================================================
    // Test de métodos heredados
    // =========================================================================
    @Test
    @DisplayName("Métodos heredados: save, findById, existsById, count")
    void shouldUseInheritedMethods() {
        RescueCenter center = rescueCenterRepository.saveAndFlush(new RescueCenter
                ("DB-CAR", "DeepBlue Caribbean Center", "Santa Marta"));
        rescueCenterRepository.save(center);

        Optional<RescueCenter> found = rescueCenterRepository.findById(center.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("DB-CAR");

        assertThat(rescueCenterRepository.existsById(center.getId())).isTrue();
        assertThat(rescueCenterRepository.count()).isGreaterThanOrEqualTo(1);
    }

    // =========================================================================
    // Test relación 1:N
    // =========================================================================
    @Test
    @DisplayName("RescueCenter 1:N RescueCase - dos casos pertenecen al mismo centro")
    void shouldPersistOneToMany(){
        RescueCenter center = rescueCenterRepository.saveAndFlush(new RescueCenter
                ("DB-CAR", "DeepBlue Caribbean", "Santa Marta"));
        rescueCenterRepository.save(center);

        RescueCase case1 = rescueCaseRepository.saveAndFlush(new RescueCase
                ("RES-2026-001", LocalDate.of(2026, 8, 1),"Bahía Concha", center, RescueStatus.IN_REHABILITATION));
        rescueCaseRepository.save(case1);

        RescueCase case2 = rescueCaseRepository.saveAndFlush(new RescueCase
                ("RES-2026-002", LocalDate.of(2026, 8, 5),"Rodadero", center, RescueStatus.ADMITTED));
        rescueCaseRepository.save(case1);

        RescueCase foundCase1 = rescueCaseRepository.findById(case1.getId()).orElseThrow();
        RescueCase foundCase2 = rescueCaseRepository.findById(case2.getId()).orElseThrow();

        assertThat(foundCase1.getRescueCenter().getCode()).isEqualTo("DB-CAR");
        assertThat(foundCase2.getRescueCenter().getCode()).isEqualTo("DB-CAR");
    }

    // =========================================================================
    // Test RescueCase 1:1 Animal
    // =========================================================================

    @Test
    @DisplayName("RescueCase 1:1 Animal - case.getAnimal y animal.getRescueCase")
    void shouldPersistOneToOneAnimal(){
        RescueCenter center = rescueCenterRepository.saveAndFlush(new RescueCenter
                ("DB-CAR", "DeepBlue Caribbean", "Santa Marta"));
        rescueCenterRepository.save(center);

        RescueCase rescueCase = rescueCaseRepository.saveAndFlush(new RescueCase
                ("RES-2026-001", LocalDate.of(2026, 8, 1),"Bahía Concha", center, RescueStatus.IN_REHABILITATION));
        rescueCaseRepository.save(rescueCase);

        Animal animal = animalRepository.saveAndFlush(new Animal(
                "AN-2026-001",
                "Green Sea Turtle",
                "Chelonia mydas",
                AnimalSex.FEMALE,
                rescueCase
        ));
        animalRepository.save(animal);
        animalRepository.flush();

        // Refrescar desde la base de datos
        entityManager.refresh(rescueCase);

        RescueCase found = rescueCaseRepository.findById(rescueCase.getId()).orElseThrow();
        Animal foundAnimal = animalRepository.findById(animal.getId()).orElseThrow();

        assertThat(found.getAnimal()).isNotNull();
        assertThat(foundAnimal.getRescueCase().getCaseCode()).isEqualTo("RES-2026-001");
    }
}
