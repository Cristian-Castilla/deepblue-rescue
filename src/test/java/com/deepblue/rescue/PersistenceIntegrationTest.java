package com.deepblue.rescue;

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
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer(
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
}
