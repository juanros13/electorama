package com.electorama.msvc.documentmanagement.models.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "document")
public class DocumentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Date documentDate;
  private Date receivedDate;
  private String subject;
  private String body;
  private Date expirationDate;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private ReceiverEntity receiver;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SenderEntity sender;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date updated;

  private Date deleted;
}
