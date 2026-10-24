package com.peernet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "helper_expertise", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "subject_id"}))
@Getter @Setter @NoArgsConstructor
public class HelperExpertise {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private AppUser user;
    @ManyToOne(optional = false) @JoinColumn(name = "subject_id")
    private Subject subject;
    /** true when the user ticked this subject on their profile */
    private boolean declared;
    /** number of accepted answers given in this subject */
    private int provenCount;
}
