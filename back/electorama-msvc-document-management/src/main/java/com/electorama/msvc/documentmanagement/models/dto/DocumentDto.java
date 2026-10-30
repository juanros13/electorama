package com.electorama.msvc.documentmanagement.models.dto;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Data;

@Data
public class DocumentDto {

  private Long id;

  private Date documentDate;
  private Date receivedDate;
  private String subject;
  private String body;
  private Date expirationDate;
  private ReceiverDto receiver;

  private SenderDto sender;

  private Date created;
  private Date updated;
  private Date deleted;
}
