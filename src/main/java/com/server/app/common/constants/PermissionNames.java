package com.server.app.common.constants;

import java.util.List;

public class PermissionNames {
  public static final String USER_CREATE = "USER_CREATE";
  public static final String USER_READ = "USER_READ";
  public static final String USER_UPDATE = "USER_UPDATE";
  public static final String USER_DELETE = "USER_DELETE";

  public static final String ROLE_CREATE = "ROLE_CREATE";
  public static final String ROLE_READ = "ROLE_READ";
  public static final String ROLE_UPDATE = "ROLE_UPDATE";
  public static final String ROLE_DELETE = "ROLE_DELETE";

  public static final String PERMISSION_READ = "PERMISSION_READ";
  public static final String PERMISSION_UPDATE = "PERMISSION_UPDATE";


  // ── Banca digital ──────────────────────────────────────────────────────
  public static final String ACCOUNT_CREATE = "ACCOUNT_CREATE";
  public static final String ACCOUNT_READ = "ACCOUNT_READ";
  public static final String ACCOUNT_READ_ALL = "ACCOUNT_READ_ALL";
  public static final String ACCOUNT_UPDATE = "ACCOUNT_UPDATE";

  public static final String TRANSFER_CREATE = "TRANSFER_CREATE";
  public static final String TRANSACTION_CREATE = "TRANSACTION_CREATE";
  public static final String TRANSACTION_READ = "TRANSACTION_READ";
  public static final String TRANSACTION_READ_ALL = "TRANSACTION_READ_ALL";

  public static final String BENEFICIARY_CREATE = "BENEFICIARY_CREATE";
  public static final String BENEFICIARY_READ = "BENEFICIARY_READ";
  public static final String BENEFICIARY_DELETE = "BENEFICIARY_DELETE";

  public static final String AUDIT_READ = "AUDIT_READ";

  /** Permisos que recibe el rol CLIENT al arrancar (operar sobre lo propio). */
  public static final List<String> CLIENT_PERMISSIONS = List.of(
      ACCOUNT_READ, TRANSFER_CREATE, TRANSACTION_READ,
      BENEFICIARY_CREATE, BENEFICIARY_READ, BENEFICIARY_DELETE);

  public static final List<String> HIERARCHY = List.of(
      USER_CREATE,
      USER_DELETE,
      USER_READ,
      USER_UPDATE,
      ROLE_CREATE,
      ROLE_DELETE,
      ROLE_READ,
      ROLE_UPDATE,
      PERMISSION_READ,
      PERMISSION_UPDATE,
      ACCOUNT_CREATE,
      ACCOUNT_READ,
      ACCOUNT_READ_ALL,
      ACCOUNT_UPDATE,
      TRANSFER_CREATE,
      TRANSACTION_CREATE,
      TRANSACTION_READ,
      TRANSACTION_READ_ALL,
      BENEFICIARY_CREATE,
      BENEFICIARY_READ,
      BENEFICIARY_DELETE,
      AUDIT_READ);

  private PermissionNames() {
  }
}
