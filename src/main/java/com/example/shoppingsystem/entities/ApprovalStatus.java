package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "approval_status")
public class ApprovalStatus extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STATUS_ID")
    private Long statusId;

    @Column(name = "STATUS_CODE")
    private String statusCode;

    @Column(name = "STATUS_NAME")
    private String statusName;
}
