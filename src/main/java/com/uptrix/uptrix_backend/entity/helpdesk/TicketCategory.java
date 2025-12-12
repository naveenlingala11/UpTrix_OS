package com.uptrix.uptrix_backend.entity.helpdesk;

import com.uptrix.uptrix_backend.entity.Employee;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "ticket_categories")
public class TicketCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;

    private String name;

    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_owner_id")
    private Employee defaultOwner;

    private Integer defaultSlaHours;

    private boolean active = true;

}
