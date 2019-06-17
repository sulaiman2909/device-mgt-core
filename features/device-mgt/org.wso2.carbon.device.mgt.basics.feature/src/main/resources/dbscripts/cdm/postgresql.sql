CREATE TABLE IF NOT EXISTS  DM_DEVICE_TYPE (
  ID	BIGSERIAL PRIMARY KEY,
  NAME	VARCHAR(300) DEFAULT NULL,
  DEVICE_TYPE_META VARCHAR(20000) DEFAULT NULL,
  LAST_UPDATED_TIMESTAMP TIMESTAMP NOT NULL,
  PROVIDER_TENANT_ID	INTEGER DEFAULT 0,
  SHARED_WITH_ALL_TENANTS	BOOLEAN NOT NULL DEFAULT FALSE,
  UNIQUE(NAME, PROVIDER_TENANT_ID)
);

CREATE TABLE IF NOT EXISTS DM_GROUP (
  ID                  BIGSERIAL NOT NULL PRIMARY KEY,
  GROUP_NAME          VARCHAR(100) DEFAULT NULL,
  DESCRIPTION         TEXT         DEFAULT NULL,
  OWNER               VARCHAR(255)  DEFAULT NULL,
  TENANT_ID           INTEGER      DEFAULT 0
);

CREATE TABLE IF NOT EXISTS DM_ROLE_GROUP_MAP (
  ID        BIGSERIAL NOT NULL PRIMARY KEY,
  GROUP_ID  INTEGER     DEFAULT NULL,
  ROLE      VARCHAR(45) DEFAULT NULL,
  TENANT_ID INTEGER     DEFAULT 0,
  CONSTRAINT fk_DM_ROLE_GROUP_MAP_DM_GROUP2 FOREIGN KEY (GROUP_ID)
  REFERENCES DM_GROUP (ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX IDX_DEVICE_TYPE ON DM_DEVICE_TYPE (NAME, PROVIDER_TENANT_ID);

CREATE TABLE IF NOT EXISTS  DM_DEVICE (
  ID                    BIGSERIAL NOT NULL PRIMARY KEY,
  DESCRIPTION           TEXT DEFAULT NULL,
  NAME                  VARCHAR(100) DEFAULT NULL,
  DEVICE_TYPE_ID        INTEGER DEFAULT NULL,
  DEVICE_IDENTIFICATION VARCHAR(300) DEFAULT NULL,
  LAST_UPDATED_TIMESTAMP TIMESTAMP NOT NULL,
  TENANT_ID INTEGER DEFAULT 0,
  CONSTRAINT fk_DM_DEVICE_DM_DEVICE_TYPE2 FOREIGN KEY (DEVICE_TYPE_ID )
  REFERENCES DM_DEVICE_TYPE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS DM_DEVICE_PROPERTIES (
     DEVICE_TYPE_NAME VARCHAR(300) NOT NULL,
     DEVICE_IDENTIFICATION VARCHAR(300) NOT NULL,
     PROPERTY_NAME VARCHAR(100) DEFAULT 0,
     PROPERTY_VALUE VARCHAR(100) DEFAULT NULL,
     TENANT_ID INTEGER DEFAULT 0,
     PRIMARY KEY (DEVICE_TYPE_NAME, DEVICE_IDENTIFICATION, PROPERTY_NAME, TENANT_ID)
);

CREATE INDEX IDX_DM_DEVICE ON DM_DEVICE(TENANT_ID, DEVICE_TYPE_ID);

CREATE TABLE IF NOT EXISTS DM_DEVICE_GROUP_MAP (
  ID        BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_ID INTEGER DEFAULT NULL,
  GROUP_ID  INTEGER DEFAULT NULL,
  TENANT_ID INTEGER DEFAULT 0,
  CONSTRAINT fk_DM_DEVICE_GROUP_MAP_DM_DEVICE2 FOREIGN KEY (DEVICE_ID)
  REFERENCES DM_DEVICE (ID) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_DM_DEVICE_GROUP_MAP_DM_GROUP2 FOREIGN KEY (GROUP_ID)
  REFERENCES DM_GROUP (ID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS  DM_OPERATION (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  TYPE VARCHAR(50) NOT NULL,
  CREATED_TIMESTAMP TIMESTAMP NOT NULL,
  RECEIVED_TIMESTAMP TIMESTAMP NULL,
  OPERATION_CODE VARCHAR(1000) NOT NULL,
  INITIATED_BY VARCHAR(50) NULL
);

CREATE TABLE IF NOT EXISTS  DM_CONFIG_OPERATION (
  OPERATION_ID INTEGER NOT NULL,
  OPERATION_CONFIG BYTEA DEFAULT NULL,
  ENABLED BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (OPERATION_ID),
  CONSTRAINT fk_dm_operation_config FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_COMMAND_OPERATION (
  OPERATION_ID INTEGER NOT NULL,
  ENABLED BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (OPERATION_ID),
  CONSTRAINT fk_dm_operation_command FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_POLICY_OPERATION (
  OPERATION_ID INTEGER NOT NULL,
  ENABLED INTEGER NOT NULL DEFAULT 0,
  OPERATION_DETAILS BYTEA DEFAULT NULL,
  PRIMARY KEY (OPERATION_ID),
  CONSTRAINT fk_dm_operation_policy FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_PROFILE_OPERATION (
  OPERATION_ID INTEGER NOT NULL,
  ENABLED INTEGER NOT NULL DEFAULT 0,
  OPERATION_DETAILS BYTEA DEFAULT NULL,
  PRIMARY KEY (OPERATION_ID),
  CONSTRAINT fk_dm_operation_profile FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_ENROLMENT (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_ID INTEGER NOT NULL,
  OWNER VARCHAR(255) NOT NULL,
  OWNERSHIP VARCHAR(45) DEFAULT NULL,
  STATUS VARCHAR(50) NULL,
  DATE_OF_ENROLMENT TIMESTAMP NULL DEFAULT NULL,
  DATE_OF_LAST_UPDATE TIMESTAMP NULL DEFAULT NULL,
  TENANT_ID INTEGER NOT NULL,
  CONSTRAINT fk_dm_device_enrolment FOREIGN KEY (DEVICE_ID) REFERENCES
    DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX IDX_ENROLMENT_FK_DEVICE_ID ON DM_ENROLMENT(DEVICE_ID);
CREATE INDEX IDX_ENROLMENT_DEVICE_ID_TENANT_ID ON DM_ENROLMENT(DEVICE_ID, TENANT_ID);

CREATE TABLE IF NOT EXISTS  DM_ENROLMENT_OP_MAPPING (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  ENROLMENT_ID INTEGER NOT NULL,
  OPERATION_ID INTEGER NOT NULL,
  STATUS VARCHAR(50) NULL,
  PUSH_NOTIFICATION_STATUS VARCHAR(50) NULL,
  CREATED_TIMESTAMP INTEGER NOT NULL,
  UPDATED_TIMESTAMP INTEGER NOT NULL,
  CONSTRAINT fk_dm_device_operation_mapping_device FOREIGN KEY (ENROLMENT_ID) REFERENCES
    DM_ENROLMENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT fk_dm_device_operation_mapping_operation FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX IDX_ENROLMENT_OP_MAPPING ON DM_ENROLMENT_OP_MAPPING (UPDATED_TIMESTAMP);
CREATE INDEX IDX_EN_OP_MAPPING_EN_ID ON DM_ENROLMENT_OP_MAPPING(ENROLMENT_ID);
CREATE INDEX IDX_EN_OP_MAPPING_OP_ID ON DM_ENROLMENT_OP_MAPPING(OPERATION_ID);

CREATE TABLE IF NOT EXISTS  DM_DEVICE_OPERATION_RESPONSE (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  ENROLMENT_ID INTEGER NOT NULL,
  OPERATION_ID INTEGER NOT NULL,
  EN_OP_MAP_ID  INTEGER NOT NULL,
  OPERATION_RESPONSE BYTEA DEFAULT NULL,
  RECEIVED_TIMESTAMP TIMESTAMP NULL,
  CONSTRAINT fk_dm_device_operation_response_enrollment FOREIGN KEY (ENROLMENT_ID) REFERENCES
    DM_ENROLMENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT fk_dm_device_operation_response_operation FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT  fk_dm_en_op_map_response FOREIGN KEY (EN_OP_MAP_ID) REFERENCES
    DM_ENROLMENT_OP_MAPPING (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE INDEX IDX_ENID_OPID ON DM_DEVICE_OPERATION_RESPONSE(OPERATION_ID, ENROLMENT_ID);
CREATE INDEX IDX_DM_EN_OP_MAP_RES ON DM_DEVICE_OPERATION_RESPONSE(EN_OP_MAP_ID);
-- POLICY RELATED TABLES ---

CREATE TABLE IF NOT EXISTS DM_PROFILE (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  PROFILE_NAME VARCHAR(45) NOT NULL ,
  TENANT_ID INTEGER NOT NULL ,
  DEVICE_TYPE VARCHAR(300) NOT NULL ,
  CREATED_TIME TIMESTAMP NOT NULL ,
  UPDATED_TIME TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS DM_POLICY (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  NAME VARCHAR(45) DEFAULT NULL ,
  DESCRIPTION VARCHAR(1000) NULL,
  TENANT_ID INTEGER NOT NULL ,
  PROFILE_ID INTEGER NOT NULL ,
  OWNERSHIP_TYPE VARCHAR(45) NULL,
  COMPLIANCE VARCHAR(100) NULL,
  PRIORITY INTEGER NOT NULL,
  ACTIVE INTEGER NOT NULL,
  UPDATED INTEGER NULL,
  CONSTRAINT FK_DM_PROFILE_DM_POLICY
  FOREIGN KEY (PROFILE_ID )
  REFERENCES DM_PROFILE (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS DM_DEVICE_POLICY (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_ID INTEGER NOT NULL ,
  ENROLMENT_ID INTEGER NOT NULL,
  DEVICE BYTEA NOT NULL,
  POLICY_ID INTEGER NOT NULL ,
  CONSTRAINT FK_POLICY_DEVICE_POLICY
  FOREIGN KEY (POLICY_ID )
  REFERENCES DM_POLICY (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,
  CONSTRAINT FK_DEVICE_DEVICE_POLICY
  FOREIGN KEY (DEVICE_ID )
  REFERENCES DM_DEVICE (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS DM_DEVICE_TYPE_POLICY (
  ID INTEGER NOT NULL,
  DEVICE_TYPE VARCHAR(300) NOT NULL ,
  POLICY_ID INTEGER NOT NULL ,
  PRIMARY KEY (ID) ,
  CONSTRAINT FK_DEVICE_TYPE_POLICY
  FOREIGN KEY (POLICY_ID )
  REFERENCES DM_POLICY (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS DM_PROFILE_FEATURES (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  PROFILE_ID INTEGER NOT NULL,
  FEATURE_CODE VARCHAR(100) NOT NULL,
  DEVICE_TYPE VARCHAR(300) NOT NULL ,
  TENANT_ID INTEGER NOT NULL ,
  CONTENT BYTEA NULL DEFAULT NULL,
  CONSTRAINT FK_DM_PROFILE_DM_POLICY_FEATURES
  FOREIGN KEY (PROFILE_ID)
  REFERENCES DM_PROFILE (ID)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS DM_ROLE_POLICY (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  ROLE_NAME VARCHAR(45) NOT NULL ,
  POLICY_ID INTEGER NOT NULL,
  CONSTRAINT FK_ROLE_POLICY_POLICY
  FOREIGN KEY (POLICY_ID )
  REFERENCES DM_POLICY (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS DM_USER_POLICY (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  POLICY_ID INT NOT NULL ,
  USERNAME VARCHAR(45) NOT NULL,
  CONSTRAINT DM_POLICY_USER_POLICY
  FOREIGN KEY (POLICY_ID )
  REFERENCES DM_POLICY (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS DM_DEVICE_POLICY_APPLIED (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_ID INTEGER NOT NULL ,
  ENROLMENT_ID INTEGER NOT NULL,
  POLICY_ID INTEGER NOT NULL ,
  POLICY_CONTENT BYTEA NULL ,
  TENANT_ID INTEGER NOT NULL,
  APPLIED BOOLEAN NULL ,
  CREATED_TIME TIMESTAMP NULL ,
  UPDATED_TIME TIMESTAMP NULL ,
  APPLIED_TIME TIMESTAMP NULL ,
  CONSTRAINT FK_DM_POLICY_DEVICE_APPLIED
  FOREIGN KEY (DEVICE_ID )
  REFERENCES DM_DEVICE (ID )
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_CRITERIA (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  TENANT_ID INT NOT NULL,
  NAME VARCHAR(50) NULL
);

CREATE TABLE IF NOT EXISTS  DM_POLICY_CRITERIA (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  CRITERIA_ID INT NOT NULL,
  POLICY_ID INT NOT NULL,
  CONSTRAINT FK_CRITERIA_POLICY_CRITERIA
  FOREIGN KEY (CRITERIA_ID)
  REFERENCES DM_CRITERIA (ID)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,
  CONSTRAINT FK_POLICY_POLICY_CRITERIA
  FOREIGN KEY (POLICY_ID)
  REFERENCES DM_POLICY (ID)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_POLICY_CRITERIA_PROPERTIES (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  POLICY_CRITERION_ID INT NOT NULL,
  PROP_KEY VARCHAR(45) NULL,
  PROP_VALUE VARCHAR(100) NULL,
  CONTENT BYTEA NULL,
  CONSTRAINT FK_POLICY_CRITERIA_PROPERTIES
  FOREIGN KEY (POLICY_CRITERION_ID)
  REFERENCES DM_POLICY_CRITERIA (ID)
  ON DELETE CASCADE
  ON UPDATE NO ACTION
);
COMMENT ON COLUMN DM_POLICY_CRITERIA_PROPERTIES.CONTENT IS 'This is used to ';

CREATE TABLE IF NOT EXISTS  DM_POLICY_COMPLIANCE_STATUS (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_ID INTEGER NOT NULL,
  ENROLMENT_ID INTEGER NOT NULL,
  POLICY_ID INTEGER NOT NULL,
  TENANT_ID INTEGER NOT NULL,
  STATUS INTEGER NULL,
  LAST_SUCCESS_TIME TIMESTAMP NULL,
  LAST_REQUESTED_TIME TIMESTAMP NULL,
  LAST_FAILED_TIME TIMESTAMP NULL,
  ATTEMPTS INTEGER NULL
);

CREATE TABLE IF NOT EXISTS  DM_POLICY_CHANGE_MGT (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  POLICY_ID INTEGER NOT NULL,
  DEVICE_TYPE VARCHAR(300) NOT NULL,
  TENANT_ID INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS  DM_POLICY_COMPLIANCE_FEATURES (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  COMPLIANCE_STATUS_ID INTEGER NOT NULL,
  TENANT_ID INTEGER NOT NULL,
  FEATURE_CODE VARCHAR(100) NOT NULL,
  STATUS INTEGER NULL,
  CONSTRAINT FK_COMPLIANCE_FEATURES_STATUS
  FOREIGN KEY (COMPLIANCE_STATUS_ID)
  REFERENCES DM_POLICY_COMPLIANCE_STATUS (ID)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS  DM_APPLICATION (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  NAME VARCHAR(150) NOT NULL,
  APP_IDENTIFIER VARCHAR(150) NOT NULL,
  PLATFORM VARCHAR(50) DEFAULT NULL,
  CATEGORY VARCHAR(50) NULL,
  VERSION VARCHAR(50) NULL,
  TYPE VARCHAR(50) NULL,
  LOCATION_URL VARCHAR(100) DEFAULT NULL,
  IMAGE_URL VARCHAR(100) DEFAULT NULL,
  APP_PROPERTIES BYTEA NULL,
  MEMORY_USAGE INTEGER NULL,
  IS_ACTIVE BOOLEAN NOT NULL DEFAULT FALSE,
  TENANT_ID INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS DM_DEVICE_APPLICATION_MAPPING (
    ID BIGSERIAL NOT NULL PRIMARY KEY,
    DEVICE_ID INTEGER NOT NULL,
    ENROLMENT_ID INTEGER NOT NULL,
    APPLICATION_ID INTEGER NOT NULL,
    APP_PROPERTIES BYTEA NULL,
    MEMORY_USAGE INTEGER NULL,
    IS_ACTIVE BOOLEAN NOT NULL DEFAULT FALSE,
    TENANT_ID INTEGER NOT NULL,
    CONSTRAINT fk_dm_device FOREIGN KEY (DEVICE_ID) REFERENCES
    DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_dm_application FOREIGN KEY (APPLICATION_ID) REFERENCES
    DM_APPLICATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT FK_DM_APP_MAP_DM_ENROL FOREIGN KEY (ENROLMENT_ID) REFERENCES
    DM_ENROLMENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- POLICY RELATED TABLES  FINISHED --

-- POLICY AND DEVICE GROUP MAPPING --

CREATE TABLE IF NOT EXISTS DM_DEVICE_GROUP_POLICY (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_GROUP_ID INTEGER NOT NULL,
  POLICY_ID INTEGER NOT NULL,
  TENANT_ID INTEGER NOT NULL,
  CONSTRAINT FK_DM_DEVICE_GROUP_POLICY
    FOREIGN KEY (DEVICE_GROUP_ID)
    REFERENCES DM_GROUP (ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE ,
  CONSTRAINT FK_DM_DEVICE_GROUP_DM_POLICY
    FOREIGN KEY (POLICY_ID)
    REFERENCES DM_POLICY (ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);

-- END OF POLICY AND DEVICE GROUP MAPPING --

-- NOTIFICATION TABLE --
CREATE TABLE IF NOT EXISTS  DM_NOTIFICATION (
  NOTIFICATION_ID BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_ID INTEGER NOT NULL,
  OPERATION_ID INTEGER NULL,
  TENANT_ID INTEGER NOT NULL,
  STATUS VARCHAR(10) NULL,
  DESCRIPTION VARCHAR(1000) NULL,
  LAST_UPDATED_TIMESTAMP TIMESTAMP NOT NULL,
  CONSTRAINT fk_dm_device_notification FOREIGN KEY (DEVICE_ID) REFERENCES
    DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

-- NOTIFICATION TABLE END --

-- Device Info and Search Table --


CREATE TABLE IF NOT EXISTS DM_DEVICE_INFO (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_ID INT NOT NULL,
  ENROLMENT_ID INT NOT NULL,
  KEY_FIELD VARCHAR(45) NULL,
  VALUE_FIELD VARCHAR(1000) NULL,
  CONSTRAINT DM_DEVICE_INFO_DEVICE
    FOREIGN KEY (DEVICE_ID)
    REFERENCES DM_DEVICE (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT DM_DEVICE_INFO_DEVICE_ENROLLMENT
    FOREIGN KEY (ENROLMENT_ID)
    REFERENCES DM_ENROLMENT (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);


CREATE TABLE IF NOT EXISTS DM_DEVICE_LOCATION (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_ID INT NOT NULL,
  ENROLMENT_ID INT NOT NULL,
  LATITUDE DOUBLE PRECISION NULL,
  LONGITUDE DOUBLE PRECISION NULL,
  STREET1 VARCHAR(255) NULL,
  STREET2 VARCHAR(45) NULL,
  CITY VARCHAR(45) NULL,
  ZIP VARCHAR(10) NULL,
  STATE VARCHAR(45) NULL,
  COUNTRY VARCHAR(45) NULL,
  GEO_HASH VARCHAR(45) NULL,
  UPDATE_TIMESTAMP BIGINT NOT NULL,
  CONSTRAINT DM_DEVICE_LOCATION_DEVICE
    FOREIGN KEY (DEVICE_ID)
    REFERENCES DM_DEVICE (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT DM_DEVICE_LOCATION_DM_ENROLLMENT
    FOREIGN KEY (ENROLMENT_ID)
    REFERENCES DM_ENROLMENT (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

CREATE INDEX DM_DEVICE_LOCATION_GEO_hashx ON DM_DEVICE_LOCATION(GEO_HASH ASC);

CREATE TABLE IF NOT EXISTS DM_DEVICE_DETAIL (
  ID BIGSERIAL NOT NULL PRIMARY KEY,
  DEVICE_ID INT NOT NULL,
  ENROLMENT_ID INT NOT NULL,
  DEVICE_MODEL VARCHAR(45) NULL,
  VENDOR VARCHAR(45) NULL,
  OS_VERSION VARCHAR(45) NULL,
  OS_BUILD_DATE VARCHAR(100) NULL,
  BATTERY_LEVEL DECIMAL(4) NULL,
  INTERNAL_TOTAL_MEMORY DECIMAL(30,3) NULL,
  INTERNAL_AVAILABLE_MEMORY DECIMAL(30,3) NULL,
  EXTERNAL_TOTAL_MEMORY DECIMAL(30,3) NULL,
  EXTERNAL_AVAILABLE_MEMORY DECIMAL(30,3) NULL,
  CONNECTION_TYPE VARCHAR(50) NULL,
  SSID VARCHAR(45) NULL,
  CPU_USAGE DECIMAL(5) NULL,
  TOTAL_RAM_MEMORY DECIMAL(30,3) NULL,
  AVAILABLE_RAM_MEMORY DECIMAL(30,3) NULL,
  PLUGGED_IN BOOLEAN NOT NULL DEFAULT FALSE,
  UPDATE_TIMESTAMP BIGINT NOT NULL,
  CONSTRAINT FK_DM_DEVICE_DETAILS_DEVICE
    FOREIGN KEY (DEVICE_ID)
    REFERENCES DM_DEVICE (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT FK_DM_ENROLMENT_DEVICE_DETAILS
    FOREIGN KEY (ENROLMENT_ID)
    REFERENCES DM_ENROLMENT (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

CREATE SEQUENCE `DM_DEVICE_TYPE_PLATFORM`_seq;

CREATE TABLE IF NOT EXISTS `DM_DEVICE_TYPE_PLATFORM` (
  ID INT NOT NULL DEFAULT NEXTVAL ('`DM_DEVICE_TYPE_PLATFORM`_seq'),
  DEVICE_TYPE_ID INT DEFAULT 0,
  VERSION_NAME VARCHAR(100) NULL,
  VERSION_STATUS VARCHAR(100) DEFAULT 'ACTIVE',
  PRIMARY KEY (ID),
  CONSTRAINT DM_DEVICE_TYPE_DM_DEVICE_TYPE_PLATFORM_MAPPING FOREIGN KEY (DEVICE_TYPE_ID)
  REFERENCES DM_DEVICE_TYPE (ID)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT device_type_version_uk UNIQUE  (DEVICE_TYPE_ID, VERSION_NAME)
  )

-- DASHBOARD RELATED VIEWS --

CREATE VIEW POLICY_COMPLIANCE_INFO AS
SELECT
DEVICE_INFO.DEVICE_ID,
DEVICE_INFO.DEVICE_IDENTIFICATION,
DEVICE_INFO.PLATFORM,
DEVICE_INFO.OWNERSHIP,
DEVICE_INFO.CONNECTIVITY_STATUS,
COALESCE(DEVICE_WITH_POLICY_INFO.POLICY_ID, -1) AS POLICY_ID,
COALESCE(DEVICE_WITH_POLICY_INFO.IS_COMPLIANT, -1) AS IS_COMPLIANT,
DEVICE_INFO.TENANT_ID
FROM
(SELECT
DM_DEVICE.ID AS DEVICE_ID,
DM_DEVICE.DEVICE_IDENTIFICATION,
DM_DEVICE_TYPE.NAME AS PLATFORM,
DM_ENROLMENT.OWNERSHIP,
DM_ENROLMENT.STATUS AS CONNECTIVITY_STATUS,
DM_DEVICE.TENANT_ID
FROM DM_DEVICE, DM_DEVICE_TYPE, DM_ENROLMENT
WHERE DM_DEVICE.DEVICE_TYPE_ID = DM_DEVICE_TYPE.ID AND DM_DEVICE.ID = DM_ENROLMENT.DEVICE_ID) DEVICE_INFO
LEFT JOIN
(SELECT
DEVICE_ID,
POLICY_ID,
STATUS AS IS_COMPLIANT
FROM DM_POLICY_COMPLIANCE_STATUS) DEVICE_WITH_POLICY_INFO
ON DEVICE_INFO.DEVICE_ID = DEVICE_WITH_POLICY_INFO.DEVICE_ID
ORDER BY DEVICE_INFO.DEVICE_ID;

CREATE VIEW FEATURE_NON_COMPLIANCE_INFO AS
SELECT
DM_DEVICE.ID AS DEVICE_ID,
DM_DEVICE.DEVICE_IDENTIFICATION,
DM_DEVICE_DETAIL.DEVICE_MODEL,
DM_DEVICE_DETAIL.VENDOR,
DM_DEVICE_DETAIL.OS_VERSION,
DM_ENROLMENT.OWNERSHIP,
DM_ENROLMENT.OWNER,
DM_ENROLMENT.STATUS AS CONNECTIVITY_STATUS,
DM_POLICY_COMPLIANCE_STATUS.POLICY_ID,
DM_DEVICE_TYPE.NAME AS PLATFORM,
DM_POLICY_COMPLIANCE_FEATURES.FEATURE_CODE,
DM_POLICY_COMPLIANCE_FEATURES.STATUS AS IS_COMPLAINT,
DM_DEVICE.TENANT_ID
FROM
DM_POLICY_COMPLIANCE_FEATURES, DM_POLICY_COMPLIANCE_STATUS, DM_ENROLMENT, DM_DEVICE, DM_DEVICE_TYPE, DM_DEVICE_DETAIL
WHERE
DM_POLICY_COMPLIANCE_FEATURES.COMPLIANCE_STATUS_ID = DM_POLICY_COMPLIANCE_STATUS.ID AND
DM_POLICY_COMPLIANCE_STATUS.ENROLMENT_ID = DM_ENROLMENT.ID AND
DM_POLICY_COMPLIANCE_STATUS.DEVICE_ID = DM_DEVICE.ID AND
DM_DEVICE.DEVICE_TYPE_ID = DM_DEVICE_TYPE.ID AND
DM_DEVICE.ID = DM_DEVICE_DETAIL.DEVICE_ID
ORDER BY TENANT_ID, DEVICE_ID;

-- END OF DASHBOARD RELATED VIEWS --
