IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_TYPE]') AND TYPE IN (N'U'))
CREATE TABLE DM_DEVICE_TYPE (
     ID INTEGER IDENTITY(1,1) NOT NULL,
     NAME VARCHAR(300) DEFAULT NULL,
     DEVICE_TYPE_META VARCHAR(3000) DEFAULT NULL,
     LAST_UPDATED_TIMESTAMP DATETIME2 NOT NULL,
     PROVIDER_TENANT_ID INTEGER NULL,
     SHARED_WITH_ALL_TENANTS BIT NOT NULL DEFAULT 0,
     PRIMARY KEY (ID),
     CONSTRAINT DEVICE_TYPE_NAME UNIQUE(NAME, PROVIDER_TENANT_ID)
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_CERTIFICATE]') AND TYPE IN (N'U'))
CREATE TABLE DM_DEVICE_CERTIFICATE (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  SERIAL_NUMBER VARCHAR(500) DEFAULT NULL,
  CERTIFICATE   VARBINARY(max) DEFAULT NULL,
  TENANT_ID     INTEGER DEFAULT 0,
  USERNAME      VARCHAR(500) DEFAULT NULL,
PRIMARY KEY (ID)
);

IF NOT  EXISTS (SELECT * FROM SYS.INDEXES WHERE NAME = 'IDX_DEVICE_TYPE' AND  OBJECT_ID = OBJECT_ID('DM_DEVICE_TYPE'))
CREATE INDEX IDX_DEVICE_TYPE ON DM_DEVICE_TYPE (NAME, PROVIDER_TENANT_ID);

IF NOT EXISTS(SELECT *
              FROM SYS.OBJECTS
              WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_GROUP]') AND TYPE IN (N'U'))
  CREATE TABLE DM_GROUP (
    ID          INTEGER IDENTITY (1, 1) NOT NULL,
    GROUP_NAME  VARCHAR(100) DEFAULT NULL,
    STATUS VARCHAR(50) DEFAULT NULL,
    DESCRIPTION VARCHAR(MAX) DEFAULT NULL,
    OWNER       VARCHAR(255)  DEFAULT NULL,
    PARENT_PATH VARCHAR(255) DEFAULT NULL,
    TENANT_ID   INTEGER      DEFAULT 0,
    PRIMARY KEY (ID)
  );

IF NOT EXISTS(SELECT *
              FROM SYS.OBJECTS
              WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_ROLE_GROUP_MAP]') AND TYPE IN (N'U'))
  CREATE TABLE DM_ROLE_GROUP_MAP (
    ID        INTEGER IDENTITY (1, 1) NOT NULL,
    GROUP_ID  INTEGER     DEFAULT NULL,
    ROLE      VARCHAR(45) DEFAULT NULL,
    TENANT_ID INTEGER     DEFAULT 0,
    PRIMARY KEY (ID),
    CONSTRAINT FK_DM_ROLE_GROUP_MAP_DM_GROUP2 FOREIGN KEY (GROUP_ID)
    REFERENCES DM_GROUP (ID)
      ON DELETE CASCADE
      ON UPDATE CASCADE
  );

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE]') AND TYPE IN (N'U'))
CREATE TABLE DM_DEVICE (
     ID INTEGER IDENTITY(1,1) NOT NULL,
     DESCRIPTION VARCHAR(MAX) DEFAULT NULL,
     NAME VARCHAR(100) DEFAULT NULL,
     DEVICE_TYPE_ID INTEGER DEFAULT NULL,
     DEVICE_IDENTIFICATION VARCHAR(300) DEFAULT NULL,
     LAST_UPDATED_TIMESTAMP DATETIME2 NOT NULL,
     TENANT_ID INTEGER DEFAULT 0,
     PRIMARY KEY (ID),
     CONSTRAINT FK_DM_DEVICE_DM_DEVICE_TYPE2 FOREIGN KEY (DEVICE_TYPE_ID)
     REFERENCES DM_DEVICE_TYPE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_PROPERTIES]') AND TYPE IN (N'U'))
CREATE TABLE DM_DEVICE_PROPERTIES (
     DEVICE_TYPE_NAME VARCHAR(300) NOT NULL,
     DEVICE_IDENTIFICATION VARCHAR(300) NOT NULL,
     PROPERTY_NAME VARCHAR(100) DEFAULT 0,
     PROPERTY_VALUE VARCHAR(100) DEFAULT NULL,
     TENANT_ID VARCHAR(100),
     PRIMARY KEY (DEVICE_TYPE_NAME, DEVICE_IDENTIFICATION, PROPERTY_NAME, TENANT_ID)
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[GROUP_PROPERTIES]') AND TYPE IN (N'U'))
CREATE TABLE GROUP_PROPERTIES (
     GROUP_ID INTEGER NOT NULL,
     PROPERTY_NAME VARCHAR(100) DEFAULT 0,
     PROPERTY_VALUE VARCHAR(100) DEFAULT NULL,
     TENANT_ID VARCHAR(100),
     PRIMARY KEY (GROUP_ID, PROPERTY_NAME, TENANT_ID)
);

IF NOT EXISTS(SELECT *
              FROM SYS.OBJECTS
              WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_GROUP_MAP]') AND TYPE IN (N'U'))
  CREATE TABLE DM_DEVICE_GROUP_MAP (
    ID        INTEGER IDENTITY (1, 1) NOT NULL,
    DEVICE_ID INTEGER DEFAULT NULL,
    GROUP_ID  INTEGER DEFAULT NULL,
    TENANT_ID INTEGER DEFAULT 0,
    PRIMARY KEY (ID),
    CONSTRAINT FK_DM_DEVICE_GROUP_MAP_DM_DEVICE2 FOREIGN KEY (DEVICE_ID)
    REFERENCES DM_DEVICE (ID)
      ON DELETE CASCADE
      ON UPDATE CASCADE,
    CONSTRAINT FK_DM_DEVICE_GROUP_MAP_DM_GROUP2 FOREIGN KEY (GROUP_ID)
    REFERENCES DM_GROUP (ID)
      ON DELETE CASCADE
      ON UPDATE CASCADE
  );

IF NOT  EXISTS (SELECT * FROM SYS.INDEXES WHERE NAME = 'IDX_DM_DEVICE' AND  OBJECT_ID = OBJECT_ID('DM_DEVICE'))
CREATE INDEX IDX_DM_DEVICE ON DM_DEVICE(TENANT_ID, DEVICE_TYPE_ID);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_OPERATION]') AND TYPE IN (N'U'))
CREATE TABLE DM_OPERATION (
    ID INTEGER IDENTITY(1,1) NOT NULL,
    TYPE VARCHAR(20) NOT NULL,
    CREATED_TIMESTAMP BIGINT NOT NULL,
    RECEIVED_TIMESTAMP BIGINT NULL,
    OPERATION_CODE VARCHAR(50) NOT NULL,
    INITIATED_BY VARCHAR(100) NULL,
    OPERATION_DETAILS VARBINARY(MAX) DEFAULT NULL,
    ENABLED BIT NOT NULL DEFAULT 0,
    PRIMARY KEY (ID)
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_ENROLMENT]') AND TYPE IN (N'U'))
CREATE TABLE DM_ENROLMENT (
    ID INTEGER IDENTITY(1,1) NOT NULL,
    DEVICE_ID INTEGER NOT NULL,
    OWNER VARCHAR(255) NOT NULL,
    OWNERSHIP VARCHAR(45) DEFAULT NULL,
    STATUS VARCHAR(50) NULL,
    IS_TRANSFERRED TINYINT NOT NULL DEFAULT 0,
    DATE_OF_ENROLMENT DATETIME2 DEFAULT NULL,
    DATE_OF_LAST_UPDATE DATETIME2 DEFAULT NULL,
    TENANT_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT FK_DM_DEVICE_ENROLMENT FOREIGN KEY (DEVICE_ID) REFERENCES
    DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_STATUS]') AND TYPE IN (N'U'))
CREATE TABLE DM_DEVICE_STATUS (
    ID INTEGER IDENTITY(1,1) NOT NULL,
    ENROLMENT_ID INTEGER NOT NULL,
    DEVICE_ID INTEGER NOT NULL,
    STATUS VARCHAR(50) DEFAULT NULL,
    UPDATE_TIME DATETIME2 DEFAULT NULL,
    CHANGED_BY VARCHAR(255) NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT FK_DM_DEVICE_STATUS_DEVICE FOREIGN KEY (DEVICE_ID) REFERENCES
        DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT FK_DM_DEVICE_STATUS_ENROLMENT FOREIGN KEY (ENROLMENT_ID) REFERENCES
        DM_ENROLMENT (ID) ON DELETE CASCADE ON UPDATE CASCADE
);

IF NOT  EXISTS (SELECT * FROM SYS.INDEXES WHERE NAME = 'IDX_ENROLMENT_FK_DEVICE_ID' AND  OBJECT_ID = OBJECT_ID('DM_ENROLMENT'))
CREATE INDEX IDX_ENROLMENT_FK_DEVICE_ID ON DM_ENROLMENT(DEVICE_ID);
IF NOT  EXISTS (SELECT * FROM SYS.INDEXES WHERE NAME = 'IDX_ENROLMENT_DEVICE_ID_TENANT_ID' AND  OBJECT_ID = OBJECT_ID('DM_ENROLMENT'))
CREATE INDEX IDX_ENROLMENT_DEVICE_ID_TENANT_ID ON DM_ENROLMENT(DEVICE_ID, TENANT_ID);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_ENROLMENT_OP_MAPPING]') AND TYPE IN (N'U'))
CREATE TABLE DM_ENROLMENT_OP_MAPPING (
    ID INTEGER IDENTITY(1,1) NOT NULL,
    ENROLMENT_ID INTEGER NOT NULL,
    OPERATION_ID INTEGER NOT NULL,
    STATUS VARCHAR(50) NULL,
    PUSH_NOTIFICATION_STATUS VARCHAR(50) NULL,
    CREATED_TIMESTAMP BIGINT NOT NULL,
    UPDATED_TIMESTAMP BIGINT NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT FK_DM_DEVICE_OPERATION_MAPPING_DEVICE FOREIGN KEY (ENROLMENT_ID) REFERENCES
    DM_ENROLMENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT FK_DM_DEVICE_OPERATION_MAPPING_OPERATION FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.INDEXES WHERE NAME = 'IDX_ENROLMENT_OP_MAPPING' AND  OBJECT_ID = OBJECT_ID('DM_ENROLMENT_OP_MAPPING'))
CREATE INDEX IDX_ENROLMENT_OP_MAPPING ON DM_ENROLMENT_OP_MAPPING (UPDATED_TIMESTAMP);
IF NOT  EXISTS (SELECT * FROM SYS.INDEXES WHERE NAME = 'IDX_EN_OP_MAPPING_EN_ID' AND  OBJECT_ID = OBJECT_ID('DM_ENROLMENT_OP_MAPPING'))
CREATE INDEX IDX_EN_OP_MAPPING_EN_ID ON DM_ENROLMENT_OP_MAPPING(ENROLMENT_ID);
IF NOT  EXISTS (SELECT * FROM SYS.INDEXES WHERE NAME = 'IDX_EN_OP_MAPPING_OP_ID' AND  OBJECT_ID = OBJECT_ID('DM_ENROLMENT_OP_MAPPING'))
CREATE INDEX IDX_EN_OP_MAPPING_OP_ID ON DM_ENROLMENT_OP_MAPPING(OPERATION_ID);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_OPERATION_RESPONSE]') AND TYPE IN (N'U'))
CREATE TABLE DM_DEVICE_OPERATION_RESPONSE (
    ID INTEGER IDENTITY(1,1) NOT NULL,
    ENROLMENT_ID INTEGER NOT NULL,
    OPERATION_ID INTEGER NOT NULL,
    EN_OP_MAP_ID INTEGER NOT NULL,
    OPERATION_RESPONSE VARCHAR(1024) DEFAULT NULL,
    IS_LARGE_RESPONSE  BIT NOT NULL DEFAULT 0,
    RECEIVED_TIMESTAMP DATETIME2 DEFAULT NULL
    PRIMARY KEY (ID),
    CONSTRAINT FK_DM_DEVICE_OPERATION_RESP_ENROLMENT FOREIGN KEY (ENROLMENT_ID) REFERENCES
    DM_ENROLMENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT FK_DM_DEVICE_OPERATION_RESP_OPERATION FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT FK_DM_EN_OP_MAP_RESPONSE FOREIGN KEY (EN_OP_MAP_ID) REFERENCES
    DM_ENROLMENT_OP_MAPPING (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.INDEXES WHERE NAME = 'IDX_DM_RES_RT' AND  OBJECT_ID = OBJECT_ID('DM_DEVICE_OPERATION_RESPONSE'))
CREATE INDEX IDX_DM_RES_RT ON DM_DEVICE_OPERATION_RESPONSE(RECEIVED_TIMESTAMP);

IF NOT  EXISTS (SELECT * FROM SYS.INDEXES WHERE NAME = 'IDX_ENID_OPID' AND  OBJECT_ID = OBJECT_ID('DM_DEVICE_OPERATION_RESPONSE'))
CREATE INDEX IDX_ENID_OPID ON DM_DEVICE_OPERATION_RESPONSE(OPERATION_ID, ENROLMENT_ID);

IF NOT  EXISTS (SELECT * FROM SYS.INDEXES WHERE NAME = 'IDX_DM_EN_OP_MAP_RES' AND  OBJECT_ID = OBJECT_ID('DM_DEVICE_OPERATION_RESPONSE'))
CREATE INDEX IDX_DM_EN_OP_MAP_RES ON DM_DEVICE_OPERATION_RESPONSE(EN_OP_MAP_ID);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_OPERATION_RESPONSE_LARGE]') AND TYPE IN (N'U'))
CREATE TABLE DM_DEVICE_OPERATION_RESPONSE_LARGE (
    ID INTEGER NOT NULL,
    OPERATION_ID INTEGER NOT NULL,
    EN_OP_MAP_ID INTEGER NOT NULL,
    OPERATION_RESPONSE VARBINARY(MAX) DEFAULT NULL,
    RECEIVED_TIMESTAMP DATETIME2 DEFAULT NULL,
    DEVICE_IDENTIFICATION VARCHAR(300) DEFAULT NULL
    PRIMARY KEY (ID),
    CONSTRAINT FK_DM_DEVICE_OPERATION_RESP_LARGE_OPERATION FOREIGN KEY (OPERATION_ID) REFERENCES
    DM_OPERATION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT FK_DM_EN_OP_MAP_RESPONSE_LARGE FOREIGN KEY (EN_OP_MAP_ID) REFERENCES
    DM_ENROLMENT_OP_MAPPING (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.INDEXES WHERE NAME = 'IDX_ENID_OPID_LARGE' AND OBJECT_ID = OBJECT_ID('DM_DEVICE_OPERATION_RESPONSE_LARGE'))
CREATE INDEX IDX_ENID_OPID_LARGE ON DM_DEVICE_OPERATION_RESPONSE_LARGE(OPERATION_ID);

IF NOT  EXISTS (SELECT * FROM SYS.INDEXES WHERE NAME = 'IDX_DM_EN_OP_MAP_RES_LARGE' AND OBJECT_ID = OBJECT_ID('DM_DEVICE_OPERATION_RESPONSE_LARGE'))
CREATE INDEX IDX_DM_EN_OP_MAP_RES_LARGE ON DM_DEVICE_OPERATION_RESPONSE_LARGE(EN_OP_MAP_ID);

-- POLICY RELATED TABLES --

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_PROFILE]') AND TYPE IN (N'U'))
CREATE TABLE DM_PROFILE (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  PROFILE_NAME VARCHAR(45) NOT NULL ,
  TENANT_ID INTEGER NOT NULL ,
  DEVICE_TYPE VARCHAR(300) NOT NULL ,
  CREATED_TIME DATETIME NOT NULL ,
  UPDATED_TIME DATETIME NOT NULL ,
  PRIMARY KEY (ID)
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_POLICY]') AND TYPE IN (N'U'))
CREATE TABLE DM_POLICY (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  NAME VARCHAR(45) DEFAULT NULL ,
  PAYLOAD_VERSION VARCHAR (45) DEFAULT NULL,
  DESCRIPTION VARCHAR(1000) NULL,
  TENANT_ID INTEGER NOT NULL ,
  PROFILE_ID INTEGER NOT NULL ,
  OWNERSHIP_TYPE VARCHAR(45) NULL,
  COMPLIANCE VARCHAR(100) NULL,
  PRIORITY INTEGER NOT NULL,
  ACTIVE BIT NOT NULL DEFAULT 0,
  UPDATED BIT NULL DEFAULT 0,
  POLICY_TYPE VARCHAR(45) NULL,
  PRIMARY KEY (ID) ,
  CONSTRAINT FK_DM_PROFILE_DM_POLICY FOREIGN KEY (PROFILE_ID) REFERENCES DM_PROFILE (ID)
  ON DELETE NO ACTION ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_POLICY]') AND TYPE IN (N'U'))
CREATE TABLE DM_DEVICE_POLICY (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  DEVICE_ID INTEGER NOT NULL ,
  ENROLMENT_ID INTEGER NOT NULL,
  DEVICE VARBINARY(MAX) NOT NULL,
  POLICY_ID INTEGER NOT NULL ,
  PRIMARY KEY (ID) ,
  CONSTRAINT FK_POLICY_DEVICE_POLICY FOREIGN KEY (POLICY_ID) REFERENCES DM_POLICY (ID)
  ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT FK_DEVICE_DEVICE_POLICY FOREIGN KEY (DEVICE_ID) REFERENCES DM_DEVICE (ID)
  ON DELETE NO ACTION ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_TYPE_POLICY]') AND TYPE IN (N'U'))
CREATE TABLE DM_DEVICE_TYPE_POLICY (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  DEVICE_TYPE_ID INTEGER NOT NULL ,
  POLICY_ID INTEGER NOT NULL ,
  PRIMARY KEY (ID) ,
  CONSTRAINT FK_DEVICE_TYPE_POLICY FOREIGN KEY (POLICY_ID) REFERENCES DM_POLICY (ID)
  ON DELETE NO ACTION ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_PROFILE_FEATURES]') AND TYPE IN (N'U'))
CREATE TABLE DM_PROFILE_FEATURES (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  PROFILE_ID INTEGER NOT NULL,
  FEATURE_CODE VARCHAR(100) NOT NULL,
  DEVICE_TYPE VARCHAR(300) NOT NULL,
  TENANT_ID INTEGER NOT NULL ,
  CONTENT VARBINARY(MAX) NULL DEFAULT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT FK_DM_PROFILE_DM_POLICY_FEATURES FOREIGN KEY (PROFILE_ID) REFERENCES DM_PROFILE (ID)
  ON DELETE NO ACTION ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_POLICY_CORRECTIVE_ACTION]') AND TYPE IN (N'U'))
CREATE TABLE DM_POLICY_CORRECTIVE_ACTION (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  ACTION_TYPE VARCHAR(45) NOT NULL,
  CORRECTIVE_POLICY_ID INTEGER DEFAULT NULL,
  POLICY_ID INTEGER NOT NULL,
  FEATURE_ID INTEGER DEFAULT NULL,
  IS_REACTIVE BIT NOT NULL DEFAULT 0,
  PRIMARY KEY (ID),
  CONSTRAINT FK_DM_POLICY_DM_POLICY_CORRECTIVE_ACTION
    FOREIGN KEY (POLICY_ID)
    REFERENCES DM_POLICY (ID)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_ROLE_POLICY]') AND TYPE IN (N'U'))
CREATE TABLE DM_ROLE_POLICY (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  ROLE_NAME VARCHAR(45) NOT NULL ,
  POLICY_ID INTEGER NOT NULL ,
  PRIMARY KEY (ID) ,
  CONSTRAINT FK_ROLE_POLICY_POLICY FOREIGN KEY (POLICY_ID) REFERENCES DM_POLICY (ID)
  ON DELETE NO ACTION ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_USER_POLICY]') AND TYPE IN (N'U'))
CREATE TABLE DM_USER_POLICY (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  POLICY_ID INTEGER NOT NULL ,
  USERNAME VARCHAR(45) NOT NULL ,
  PRIMARY KEY (ID) ,
  CONSTRAINT DM_POLICY_USER_POLICY FOREIGN KEY (POLICY_ID) REFERENCES DM_POLICY (ID)
  ON DELETE NO ACTION ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_POLICY_APPLIED]') AND TYPE IN (N'U'))
 CREATE TABLE DM_DEVICE_POLICY_APPLIED (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  DEVICE_ID INTEGER NOT NULL ,
  ENROLMENT_ID INTEGER NOT NULL,
  POLICY_ID INTEGER NOT NULL ,
  POLICY_CONTENT VARBINARY(MAX) NULL ,
  TENANT_ID INTEGER NOT NULL,
  APPLIED BIT NULL ,
  CREATED_TIME DATETIME2 NULL ,
  UPDATED_TIME DATETIME2 NULL ,
  APPLIED_TIME DATETIME2 NULL ,
  PRIMARY KEY (ID) ,
  CONSTRAINT FK_DM_POLICY_DEVCIE_APPLIED FOREIGN KEY (DEVICE_ID) REFERENCES DM_DEVICE (ID)
  ON DELETE NO ACTION ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_CRITERIA]') AND TYPE IN (N'U'))
CREATE TABLE DM_CRITERIA (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  TENANT_ID INTEGER NOT NULL,
  NAME VARCHAR(50) NULL,
  PRIMARY KEY (ID)
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_POLICY_CRITERIA]') AND TYPE IN (N'U'))
CREATE TABLE DM_POLICY_CRITERIA (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  CRITERIA_ID INTEGER NOT NULL,
  POLICY_ID INTEGER NOT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT FK_CRITERIA_POLICY_CRITERIA FOREIGN KEY (CRITERIA_ID) REFERENCES DM_CRITERIA (ID)
  ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT FK_POLICY_POLICY_CRITERIA FOREIGN KEY (POLICY_ID) REFERENCES DM_POLICY (ID)
  ON DELETE NO ACTION ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_POLICY_CRITERIA_PROPERTIES]') AND TYPE IN (N'U'))
CREATE TABLE DM_POLICY_CRITERIA_PROPERTIES (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  POLICY_CRITERION_ID INTEGER NOT NULL,
  PROP_KEY VARCHAR(45) NULL,
  PROP_VALUE VARCHAR(100) NULL,
  CONTENT VARBINARY(MAX) NULL,
  PRIMARY KEY (ID),
  CONSTRAINT FK_POLICY_CRITERIA_PROPERTIES FOREIGN KEY (POLICY_CRITERION_ID) REFERENCES DM_POLICY_CRITERIA (ID)
  ON DELETE CASCADE ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_POLICY_COMPLIANCE_STATUS]') AND TYPE IN (N'U'))
CREATE TABLE DM_POLICY_COMPLIANCE_STATUS (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  DEVICE_ID INTEGER NOT NULL,
  ENROLMENT_ID INTEGER NOT NULL,
  POLICY_ID INTEGER NOT NULL,
  TENANT_ID INTEGER NOT NULL,
  STATUS INTEGER NULL,
  LAST_SUCCESS_TIME DATETIME2 NULL,
  LAST_REQUESTED_TIME DATETIME2 NULL,
  LAST_FAILED_TIME DATETIME2 NULL,
  ATTEMPTS INTEGER NULL,
  PRIMARY KEY (ID)
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_POLICY_CHANGE_MGT]') AND TYPE IN (N'U'))
CREATE TABLE DM_POLICY_CHANGE_MGT (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  POLICY_ID INTEGER NOT NULL,
  DEVICE_TYPE VARCHAR(300) NOT NULL,
  TENANT_ID INTEGER NOT NULL,
  PRIMARY KEY (ID)
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_POLICY_COMPLIANCE_FEATURES]') AND TYPE IN (N'U'))
CREATE TABLE DM_POLICY_COMPLIANCE_FEATURES (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  COMPLIANCE_STATUS_ID INTEGER NOT NULL,
  TENANT_ID INTEGER NOT NULL,
  FEATURE_CODE VARCHAR(100) NOT NULL,
  STATUS INTEGER NULL,
  PRIMARY KEY (ID),
  CONSTRAINT FK_COMPLIANCE_FEATURES_STATUS FOREIGN KEY (COMPLIANCE_STATUS_ID) REFERENCES DM_POLICY_COMPLIANCE_STATUS (ID)
  ON DELETE NO ACTION ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_APPLICATION]') AND TYPE IN (N'U'))
CREATE TABLE DM_APPLICATION (
    ID INTEGER IDENTITY(1,1) NOT NULL,
    NAME VARCHAR(150) NOT NULL,
    APP_IDENTIFIER VARCHAR(150) NOT NULL,
    PLATFORM VARCHAR(50) DEFAULT NULL,
    CATEGORY VARCHAR(50) NULL,
    VERSION VARCHAR(50) NULL,
    TYPE VARCHAR(50) NULL,
    LOCATION_URL VARCHAR(100) DEFAULT NULL,
    IMAGE_URL VARCHAR(100) DEFAULT NULL,
    APP_PROPERTIES VARBINARY(MAX) NULL,
    MEMORY_USAGE INTEGER NULL,
    IS_ACTIVE BIT NOT NULL DEFAULT 0,
    DEVICE_ID INTEGER NOT NULL,
    ENROLMENT_ID INTEGER NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT FK_DM_DEVICE FOREIGN KEY (DEVICE_ID) REFERENCES
    DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT FK_DM_ENROLMENT FOREIGN KEY (ENROLMENT_ID) REFERENCES
    DM_ENROLMENT (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);

IF NOT  EXISTS (SELECT * FROM SYS.INDEXES WHERE NAME = 'IDX_DM_APPLICATION' AND  OBJECT_ID = OBJECT_ID('DM_APPLICATION'))
CREATE INDEX IDX_DM_APPLICATION ON DM_APPLICATION(DEVICE_ID, ENROLMENT_ID, TENANT_ID);

-- POLICY RELATED TABLES  FINISHED --

-- POLICY AND DEVICE GROUP MAPPING --
IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_GROUP_POLICY]') AND TYPE IN (N'U'))
CREATE TABLE DM_DEVICE_GROUP_POLICY (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  DEVICE_GROUP_ID INTEGER NOT NULL,
  POLICY_ID INTEGER NOT NULL,
  TENANT_ID INTEGER NOT NULL,
  PRIMARY KEY (ID),
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
IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_NOTIFICATION]') AND TYPE IN (N'U'))
CREATE TABLE DM_NOTIFICATION (
    NOTIFICATION_ID INTEGER IDENTITY(1,1) NOT NULL,
    DEVICE_ID INTEGER NOT NULL,
    OPERATION_ID INTEGER NULL,
    TENANT_ID INTEGER NOT NULL,
    STATUS VARCHAR(10) NULL,
    DESCRIPTION VARCHAR(1000) NULL,
    LAST_UPDATED_TIMESTAMP DATETIME2 NOT NULL,
    PRIMARY KEY (NOTIFICATION_ID),
    CONSTRAINT FL_DM_NOTIFICATION FOREIGN KEY (DEVICE_ID) REFERENCES
    DM_DEVICE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
-- NOTIFICATION TABLE END --

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_INFO]') AND TYPE IN (N'U'))
CREATE TABLE DM_DEVICE_INFO (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  DEVICE_ID INTEGER NULL,
  ENROLMENT_ID INTEGER NOT NULL,
  KEY_FIELD VARCHAR(45) NULL,
  VALUE_FIELD VARCHAR(1500) NULL,
  PRIMARY KEY (ID),
  INDEX DM_DEVICE_INFO_DEVICE_idx (DEVICE_ID ASC),
  INDEX DM_DEVICE_INFO_DEVICE_ENROLLMENT_idx (ENROLMENT_ID ASC),
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

IF NOT  EXISTS (SELECT * FROM SYS.INDEXES WHERE NAME = 'IDX_DM_DEVICE_INFO_DID_EID_KFIELD' AND OBJECT_ID = OBJECT_ID('DM_DEVICE_INFO'))
CREATE INDEX IDX_DM_DEVICE_INFO_DID_EID_KFIELD ON DM_DEVICE_INFO(DEVICE_ID, ENROLMENT_ID, KEY_FIELD);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_LOCATION]') AND TYPE IN (N'U'))
CREATE TABLE DM_DEVICE_LOCATION (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  DEVICE_ID INTEGER NULL,
  ENROLMENT_ID INTEGER NOT NULL,
  LATITUDE FLOAT NULL,
  LONGITUDE FLOAT NULL,
  STREET1 VARCHAR(255) NULL,
  STREET2 VARCHAR(45) NULL,
  CITY VARCHAR(45) NULL,
  ZIP VARCHAR(10) NULL,
  STATE VARCHAR(45) NULL,
  COUNTRY VARCHAR(45) NULL,
  GEO_HASH VARCHAR(45) NULL,
  UPDATE_TIMESTAMP BIGINT NOT NULL,
  ALTITUDE FLOAT NULL,
  SPEED FLOAT NULL,
  BEARING FLOAT NULL,
  DISTANCE FLOAT NULL,
  PRIMARY KEY (ID),
  INDEX DM_DEVICE_LOCATION_DEVICE_idx (DEVICE_ID ASC),
  INDEX DM_DEVICE_LOCATION_GEO_hashx (GEO_HASH ASC),
  INDEX DM_DEVICE_LOCATION_DM_ENROLLMENT_idx (ENROLMENT_ID ASC),
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

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_HISTORY_LAST_SEVEN_DAYS]') AND TYPE IN (N'U'))
CREATE TABLE DM_DEVICE_HISTORY_LAST_SEVEN_DAYS(
    ID               INTEGER IDENTITY (1,1) NOT NULL,
    DEVICE_ID        INTEGER                NOT NULL,
    DEVICE_ID_NAME   VARCHAR(255)           NOT NULL,
    TENANT_ID        INTEGER                NOT NULL,
    DEVICE_TYPE_NAME VARCHAR(45)            NOT NULL,
    LATITUDE         FLOAT                  NULL,
    LONGITUDE        FLOAT                  NULL,
    SPEED            FLOAT                  NULL,
    HEADING          FLOAT                  NULL,
    TIMESTAMP        BIGINT                 NOT NULL,
    GEO_HASH         VARCHAR(45)            NULL,
    DEVICE_OWNER     VARCHAR(45)            NULL,
    DEVICE_ALTITUDE  FLOAT                  NULL,
    DISTANCE         FLOAT                  NULL,
    PRIMARY KEY (ID)
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_DETAIL]') AND TYPE IN (N'U'))
CREATE TABLE DM_DEVICE_DETAIL (
  ID INTEGER IDENTITY(1,1) NOT NULL,
  DEVICE_ID INTEGER NOT NULL,
  ENROLMENT_ID INTEGER NOT NULL,
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
  PLUGGED_IN INTEGER NULL,
  UPDATE_TIMESTAMP BIGINT NOT NULL,
  PRIMARY KEY (ID),
  INDEX FK_DM_DEVICE_DETAILS_DEVICE_idx (DEVICE_ID ASC),
  INDEX FK_DM_ENROLMENT_DEVICE_DETAILS_idx (ENROLMENT_ID ASC),
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

IF NOT  EXISTS (SELECT * FROM SYS.INDEXES WHERE NAME = 'IDX_DM_DEVICE_DETAIL_DID_EID' AND OBJECT_ID = OBJECT_ID('DM_DEVICE_DETAIL'))
CREATE INDEX IDX_DM_DEVICE_DETAIL_DID_EID ON DM_DEVICE_DETAIL(DEVICE_ID, ENROLMENT_ID);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_DEVICE_TYPE_PLATFORM]') AND TYPE IN (N'U'))
CREATE TABLE DM_DEVICE_TYPE_PLATFORM(
    ID int IDENTITY(1, 1) NOT NULL,
    DEVICE_TYPE_ID int NULL DEFAULT 0,
    VERSION_NAME varchar(100) NULL,
    VERSION_STATUS varchar(100) NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (ID),
    CONSTRAINT DM_DEVICE_TYPE_DM_DEVICE_TYPE_PLATFORM_MAPPING
      FOREIGN KEY (DEVICE_TYPE_ID)
      REFERENCES DM_DEVICE_TYPE (ID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT device_type_version_uk
      UNIQUE (
        DEVICE_TYPE_ID,
        VERSION_NAME
      )
);

-- METADATA TABLE --
IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DM_METADATA]') AND TYPE IN (N'U'))
CREATE TABLE DM_METADATA (
    METADATA_ID INTEGER IDENTITY(1,1) NOT NULL,
    DATA_TYPE VARCHAR(16) NOT NULL,
    METADATA_KEY VARCHAR(128) NOT NULL,
    METADATA_VALUE VARCHAR(8000) NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    PRIMARY KEY (METADATA_ID),
    CONSTRAINT METADATA_KEY_TENANT_ID UNIQUE(METADATA_KEY, TENANT_ID)
);
-- END OF METADATA TABLE --

-- DM_OTP_DATA TABLE --
CREATE TABLE DM_OTP_DATA (
     ID INT IDENTITY NOT NULL,
     OTP_TOKEN VARCHAR(100) NOT NULL,
     TENANT_ID INTEGER NOT NULL,
     USERNAME VARCHAR(500) NOT NULL,
     EMAIL VARCHAR(100) NOT NULL,
     EMAIL_TYPE VARCHAR(20) NOT NULL,
     META_INFO VARCHAR(8000) NOT NULL,
     CREATED_AT DATETIME2(0) NOT NULL,
     EXPIRY_TIME INT NOT NULL DEFAULT 3600,
     IS_EXPIRED BIT DEFAULT 0,
     PRIMARY KEY (ID),
     CONSTRAINT email_type_uk UNIQUE (EMAIL, EMAIL_TYPE)
);
-- END OF DM_OTP_DATA TABLE --

-- DASHBOARD RELATED VIEWS --

IF NOT  EXISTS (SELECT * FROM SYS.VIEWS WHERE NAME = 'POLICY_COMPLIANCE_INFO')
exec('CREATE VIEW POLICY_COMPLIANCE_INFO AS
SELECT TOP 100 PERCENT
DEVICE_INFO.DEVICE_ID,
DEVICE_INFO.DEVICE_IDENTIFICATION,
DEVICE_INFO.PLATFORM,
DEVICE_INFO.OWNERSHIP,
DEVICE_INFO.CONNECTIVITY_STATUS,
ISNULL(DEVICE_WITH_POLICY_INFO.POLICY_ID, -1) AS POLICY_ID,
ISNULL(DEVICE_WITH_POLICY_INFO.IS_COMPLIANT, -1) AS
IS_COMPLIANT,
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
FROM
DM_POLICY_COMPLIANCE_STATUS) DEVICE_WITH_POLICY_INFO
ON DEVICE_INFO.DEVICE_ID = DEVICE_WITH_POLICY_INFO.DEVICE_ID
ORDER BY DEVICE_INFO.DEVICE_ID');

IF NOT  EXISTS (SELECT * FROM SYS.VIEWS WHERE NAME = 'CREATE VIEW FEATURE_NON_COMPLIANCE_INFO')
exec('CREATE VIEW FEATURE_NON_COMPLIANCE_INFO AS
SELECT TOP 100 PERCENT
DM_DEVICE.ID AS DEVICE_ID,
DM_DEVICE.DEVICE_IDENTIFICATION,
DM_DEVICE_DETAIL.DEVICE_MODEL,
DM_DEVICE_DETAIL.VENDOR,
DM_DEVICE_DETAIL.OS_VERSION,
DM_ENROLMENT.OWNERSHIP,
DM_ENROLMENT.OWNER,
DM_ENROLMENT.STATUS AS CONNECTIVITY_STATUS,
DM_POLICY_COMPLIANCE_STATUS.POLICY_ID,
DM_DEVICE_TYPE.NAME
AS PLATFORM,
DM_POLICY_COMPLIANCE_FEATURES.FEATURE_CODE,
DM_POLICY_COMPLIANCE_FEATURES.STATUS AS IS_COMPLAINT,
DM_DEVICE.TENANT_ID
FROM
DM_POLICY_COMPLIANCE_FEATURES, DM_POLICY_COMPLIANCE_STATUS, DM_ENROLMENT, DM_DEVICE, DM_DEVICE_TYPE, DM_DEVICE_DETAIL
WHERE
DM_POLICY_COMPLIANCE_FEATURES.COMPLIANCE_STATUS_ID = DM_POLICY_COMPLIANCE_STATUS.ID AND
DM_POLICY_COMPLIANCE_STATUS.ENROLMENT_ID =
DM_ENROLMENT.ID AND
DM_POLICY_COMPLIANCE_STATUS.DEVICE_ID = DM_DEVICE.ID AND
DM_DEVICE.DEVICE_TYPE_ID = DM_DEVICE_TYPE.ID AND
DM_DEVICE.ID = DM_DEVICE_DETAIL.DEVICE_ID
ORDER BY TENANT_ID, DEVICE_ID');

-- END OF DASHBOARD RELATED VIEWS --

-- DM_GEOFENCE TABLE--

CREATE TABLE DM_GEOFENCE (
    ID INT IDENTITY NOT NULL,
    FENCE_NAME VARCHAR(255) NOT NULL,
    DESCRIPTION VARCHAR(MAX) DEFAULT NULL,
    LATITUDE FLOAT DEFAULT NULL,
    LONGITUDE FLOAT DEFAULT NULL,
    RADIUS DECIMAL(30,4) DEFAULT NULL,
    GEO_JSON VARCHAR(MAX) DEFAULT NULL,
    FENCE_SHAPE VARCHAR(100) DEFAULT NULL,
    CREATED_TIMESTAMP DATETIME2 NOT NULL,
    OWNER VARCHAR(255) NOT NULL,
    TENANT_ID INTEGER DEFAULT 0,
    PRIMARY KEY (ID)
);

-- END OF DM_GEOFENCE TABLE--

-- DYNAMIC TASK TABLES--
IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DYNAMIC_TASK]') AND TYPE IN (N'U'))
CREATE TABLE DYNAMIC_TASK (
     DYNAMIC_TASK_ID INTEGER IDENTITY(1,1) NOT NULL,
     NAME VARCHAR(255) DEFAULT NULL ,
     CRON VARCHAR(8000) DEFAULT NULL,
     IS_ENABLED BIT NOT NULL DEFAULT 0,
     TASK_CLASS_NAME VARCHAR(8000) DEFAULT NULL,
     TENANT_ID INTEGER DEFAULT 0,
     PRIMARY KEY (DYNAMIC_TASK_ID)
);

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[DYNAMIC_TASK_PROPERTIES]') AND TYPE IN (N'U'))
CREATE TABLE DYNAMIC_TASK_PROPERTIES (
     DYNAMIC_TASK_ID INTEGER NOT NULL,
     PROPERTY_NAME VARCHAR(100) DEFAULT 0,
     PROPERTY_VALUE VARCHAR(100) DEFAULT NULL,
     TENANT_ID VARCHAR(100),
     PRIMARY KEY (DYNAMIC_TASK_ID, PROPERTY_NAME, TENANT_ID),
     CONSTRAINT FK_DYNAMIC_TASK_TASK_PROPERTIES FOREIGN KEY (DYNAMIC_TASK_ID) REFERENCES
             DYNAMIC_TASK (DYNAMIC_TASK_ID) ON DELETE CASCADE ON UPDATE CASCADE
);
-- END OF DYNAMIC TASK TABLE--