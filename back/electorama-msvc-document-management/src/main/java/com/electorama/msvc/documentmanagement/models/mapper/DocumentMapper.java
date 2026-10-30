package com.electorama.msvc.documentmanagement.models.mapper;

import com.electorama.msvc.documentmanagement.models.dto.DocumentDto;
import com.electorama.msvc.documentmanagement.models.entity.DocumentEntity;
import org.springframework.beans.BeanUtils;

public class DocumentMapper extends BaseMapper<DocumentEntity, DocumentDto> {

  private final ReceiverMapper receiverMapper = new ReceiverMapper();

  private final SenderMapper senderMapper = new SenderMapper();

  @Override
  public DocumentEntity convertToEntity(DocumentDto dto, Object... args) {
    DocumentEntity entity = new DocumentEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    if (dto.getReceiver() != null) {
      entity.setReceiver(receiverMapper.convertToEntity(dto.getReceiver()));
    }
    if (dto.getSender() != null) {
      entity.setReceiver(receiverMapper.convertToEntity(dto.getReceiver()));
    }
    return entity;
  }

  @Override
  public DocumentDto convertToDto(DocumentEntity entity, Object... args) {
    DocumentDto dto = new DocumentDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    if (entity.getReceiver() != null) {
      dto.setReceiver(receiverMapper.convertToDto(entity.getReceiver()));
    }
    if (entity.getSender() != null) {
      dto.setSender(senderMapper.convertToDto(entity.getSender()));
    }
    return dto;
  }
}
