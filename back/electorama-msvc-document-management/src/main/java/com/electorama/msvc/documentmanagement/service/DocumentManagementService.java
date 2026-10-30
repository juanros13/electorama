package com.electorama.msvc.documentmanagement.service;

import com.electorama.msvc.documentmanagement.models.dto.DocumentDto;
import java.util.Optional;

public interface DocumentManagementService {

  Optional<DocumentDto> byId(Long id);
}
