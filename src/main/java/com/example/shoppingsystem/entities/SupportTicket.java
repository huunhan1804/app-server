package com.example.shoppingsystem.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "support_ticket")
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TICKET_ID")
    private Long ticketId;

    @Column(name = "TICKET_CODE", unique = true, nullable = false)
    private String ticketCode;

    @Column(name = "ACCOUNT_ID", nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORY", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "PRIORITY")
    private Priority priority = Priority.NORMAL;

    @Column(name = "SUBJECT", nullable = false)
    private String subject;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private Status status = Status.OPEN;

    @Column(name = "ASSIGNED_TO")
    private Long assignedTo;

    @Column(name = "RESOLUTION", columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "CUSTOMER_SATISFACTION")
    private Integer customerSatisfaction;

    @Column(name = "FIRST_RESPONSE_DATE")
    private LocalDateTime firstResponseDate;

    @Column(name = "RESOLVED_DATE")
    private LocalDateTime resolvedDate;

    @Column(name = "CLOSED_DATE")
    private LocalDateTime closedDate;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate = LocalDateTime.now();

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID", insertable = false, updatable = false)
    private Account account;

    // Enums
    public enum Category {
        TECHNICAL, ORDER, PAYMENT, PRODUCT, INSURANCE, OTHER
    }

    public enum Priority {
        LOW, NORMAL, HIGH, URGENT
    }

    public enum Status {
        OPEN, IN_PROGRESS, PENDING_CUSTOMER, RESOLVED, CLOSED
    }

    // Constructors
    public SupportTicket() {}

    // Getters and Setters
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Long getAssignedTo() { return assignedTo; }
    public void setAssignedTo(Long assignedTo) { this.assignedTo = assignedTo; }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }

    public Integer getCustomerSatisfaction() { return customerSatisfaction; }
    public void setCustomerSatisfaction(Integer customerSatisfaction) { this.customerSatisfaction = customerSatisfaction; }

    public LocalDateTime getFirstResponseDate() { return firstResponseDate; }
    public void setFirstResponseDate(LocalDateTime firstResponseDate) { this.firstResponseDate = firstResponseDate; }

    public LocalDateTime getResolvedDate() { return resolvedDate; }
    public void setResolvedDate(LocalDateTime resolvedDate) { this.resolvedDate = resolvedDate; }

    public LocalDateTime getClosedDate() { return closedDate; }
    public void setClosedDate(LocalDateTime closedDate) { this.closedDate = closedDate; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
}
