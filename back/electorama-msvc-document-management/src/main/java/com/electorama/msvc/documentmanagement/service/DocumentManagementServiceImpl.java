package com.electorama.msvc.documentmanagement.service;

import com.electorama.msvc.documentmanagement.models.dto.DocumentDto;
import com.electorama.msvc.documentmanagement.models.mapper.DocumentMapper;
import com.electorama.msvc.documentmanagement.models.repository.DocumentRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DocumentManagementServiceImpl implements DocumentManagementService {

  @Autowired private DocumentRepository documentRepository;

  private static final DocumentMapper documentMapper = new DocumentMapper();

  @Override
  public Optional<DocumentDto> byId(Long id) {
    return Optional.ofNullable(
        documentMapper.convertToDto(
            documentRepository.findById(id).orElseThrow(EntityNotFoundException::new)));
  }
}
