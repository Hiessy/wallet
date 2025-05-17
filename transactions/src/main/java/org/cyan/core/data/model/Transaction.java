package org.cyan.core.data.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private Alias sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private Alias receiver;

    private Double amount;
}