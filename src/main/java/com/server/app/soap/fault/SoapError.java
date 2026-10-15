package com.server.app.soap.fault;

import com.server.app.common.exceptions.BadRequestException;
import com.server.app.common.exceptions.ConfictException;
import com.server.app.common.exceptions.ForbiddenException;
import com.server.app.common.exceptions.NotFoundException;
import com.server.app.common.exceptions.UnauthorizedException;

/**
 * Códigos de error estables para el cliente SOAP. El cliente programa contra
 * el código, no contra el texto del mensaje (que puede cambiar).
 */
public enum SoapError {

  NOT_FOUND(true),
  BAD_REQUEST(true),
  CONFLICT(true),
  FORBIDDEN(true),
  UNAUTHORIZED(true),
  INTERNAL_ERROR(false);

  private final boolean clientFault;

  SoapError(boolean clientFault) {
    this.clientFault = clientFault;
  }

  /** true = culpa del cliente (Client/Sender), false = culpa nuestra (Server/Receiver). */
  public boolean isClientFault() {
    return clientFault;
  }

  public static SoapError from(Throwable ex) {
    if (ex instanceof NotFoundException) return NOT_FOUND;
    if (ex instanceof BadRequestException) return BAD_REQUEST;
    if (ex instanceof ConfictException) return CONFLICT;
    if (ex instanceof ForbiddenException) return FORBIDDEN;
    if (ex instanceof UnauthorizedException) return UNAUTHORIZED;
    return INTERNAL_ERROR; // cualquier otra cosa, incluida ServerException
  }
}
