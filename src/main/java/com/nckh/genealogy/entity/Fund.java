package com.nckh.genealogy.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "funds")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Fund {
    @Id
    @GeneratedValue
    @UuidGenerator
    UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tree_id", nullable = false)
    Tree tree;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "tree_event_id",unique = true, nullable = false)
    TreeEvent treeEvent;

    @Column(name = "name", unique = true, nullable = false)
    String name;

}
