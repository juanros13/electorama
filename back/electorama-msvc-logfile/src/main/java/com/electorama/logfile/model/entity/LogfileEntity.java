package com.electorama.logfile.model.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "logfile")
@Getter
@Setter
@NoArgsConstructor
public class LogfileEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String action;

  private Integer quantity;

  private String message;

  private String data;

  private String userAuthId;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdAt;

  private Long accionId;
  private String module;
  private String ip;
}
