package com.electorama.googlecalendar.exception;

public class EntityNotFoundException extends SimpleCalendarGlobalException {
  public EntityNotFoundException() {
    super("Requested entity not present in the DB.", GlobalErrorCode.ERROR_ENTITY_NOT_FOUND);
  }

  public EntityNotFoundException(String message) {
    super(message, GlobalErrorCode.ERROR_ENTITY_NOT_FOUND);
  }
}
