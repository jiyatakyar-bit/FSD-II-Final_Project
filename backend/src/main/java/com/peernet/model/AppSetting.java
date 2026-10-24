package com.peernet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_settings")
@Getter @Setter @NoArgsConstructor
public class AppSetting {
    @Id @Column(length = 60)
    private String name;
    @Column(nullable = false, length = 100)
    private String val;
}
