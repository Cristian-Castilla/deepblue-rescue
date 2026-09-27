package com.deepblue.rescue.domain;

import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EnumType;
import jakarta.persistence.GenerationType;

import java.time.LocalDate;

@Entity
@Table(name = "rescue_cases")
public class RescueCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String case_code;

    @Column (nullable = false)
    private LocalDate rescue_date;

    @Column (nullable = false, length = 200)
    private String rescue_location;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false, length = 30)
    private RescueStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rescue_center_id", nullable = false)
    private RescueCenter rescue_center;

    @OneToOne(
            mappedBy = "rescueCase",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Animal animal;

    protected RescueCase(){
    }

    private RescueCase (String case_code, LocalDate rescue_date, String rescue_location, RescueCenter rescue_center){
        this.case_code = case_code;
        this.rescue_date = rescue_date;
        this.rescue_location = rescue_location;
        this.rescue_center = rescue_center;
        this.status = RescueStatus.ADMITTED;
    }

    public Long getId() { return id; }
    public String getCaseCode() { return case_code; }
    public LocalDate getRescueDate() { return rescue_date; }
    public String getRescueLocation() { return rescue_location; }
    public RescueStatus getStatus() { return status; }
    public RescueCenter getRescueCenter() { return rescue_center; }
    public Animal getAnimal() { return animal; }

    public void setRescueCenter(RescueCenter rescue_center) {
        this.rescue_center = rescue_center;
    }

    public void assignAnimal(Animal animal) {
        this.animal = animal;
        animal.setRescueCase(this);
    }
}

