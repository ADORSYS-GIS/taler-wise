package de.adorsys.opba.fintech.api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Getter;

@Entity
@Table(name = "opb_bank")
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;
    // Getters and setters
    @Getter
    private String name;
    @Getter
    private String bic;

    @Getter
    @Column(name = "bank_code")
    private String bankCode;

    private boolean active;

}
