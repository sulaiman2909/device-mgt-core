-- -----------------------------------------------------
-- Table AP_APP
-- -----------------------------------------------------
CREATE TABLE AP_APP(
    ID INTEGER NOT NULL IDENTITY,
    NAME VARCHAR(45) NOT NULL,
    DESCRIPTION VARCHAR(max) NULL,
    TYPE VARCHAR(200) NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    STATUS VARCHAR(45) NOT NULL DEFAULT 'ACTIVE',
    SUB_TYPE VARCHAR(45) NOT NULL,
    CURRENCY VARCHAR(45) NULL DEFAULT '$',
    RATING FLOAT NULL DEFAULT NULL,
    DEVICE_TYPE_ID INTEGER NOT NULL,
    PRIMARY KEY (ID)
);

-- -----------------------------------------------------
-- Table AP_APP_RELEASE
-- -----------------------------------------------------
CREATE TABLE AP_APP_RELEASE(
    ID INTEGER NOT NULL IDENTITY,
    DESCRIPTION VARCHAR(max) NOT NULL,
    VERSION VARCHAR(70) NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    UUID VARCHAR(200) NOT NULL,
    RELEASE_TYPE VARCHAR(45) NOT NULL,
    PACKAGE_NAME VARCHAR(150) NOT NULL,
    APP_PRICE DECIMAL(6, 2) NULL DEFAULT NULL,
    INSTALLER_LOCATION VARCHAR(100) NOT NULL,
    ICON_LOCATION VARCHAR(100) NOT NULL,
    BANNER_LOCATION VARCHAR(100) NULL DEFAULT NULL,
    SC_1_LOCATION VARCHAR(100) NOT NULL,
    SC_2_LOCATION VARCHAR(100) NULL DEFAULT NULL,
    SC_3_LOCATION VARCHAR(100) NULL DEFAULT NULL,
    APP_HASH_VALUE VARCHAR(1000) NOT NULL,
    SHARED_WITH_ALL_TENANTS BIT NOT NULL DEFAULT 0,
    APP_META_INFO VARCHAR(max) NULL DEFAULT NULL,
    SUPPORTED_OS_VERSIONS VARCHAR(45) NOT NULL,
    RATING FLOAT NULL DEFAULT NULL,
    CURRENT_STATE VARCHAR(45) NOT NULL,
    RATED_USERS INTEGER NULL,
    AP_APP_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT UUID_CONSTRAINT UNIQUE (UUID),
    CONSTRAINT fk_AP_APP_RELEASE_AP_APP1
       FOREIGN KEY (AP_APP_ID)
           REFERENCES AP_APP (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_APP_RELEASE_AP_APP1_idx ON AP_APP_RELEASE (AP_APP_ID ASC);

-- -----------------------------------------------------
-- Table AP_APP_REVIEW
-- -----------------------------------------------------
CREATE TABLE AP_APP_REVIEW(
    ID INTEGER NOT NULL IDENTITY,
    TENANT_ID INTEGER NOT NULL,
    COMMENT VARCHAR(max) NOT NULL,
    ROOT_PARENT_ID INTEGER NOT NULL,
    IMMEDIATE_PARENT_ID INTEGER NOT NULL,
    CREATED_AT DATETIME2(0) NOT NULL,
    MODIFIED_AT DATETIME2(0) NOT NULL,
    RATING INTEGER NULL,
    USERNAME VARCHAR(45) NOT NULL,
    ACTIVE_REVIEW BIT NOT NULL DEFAULT 1,
    AP_APP_RELEASE_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_APP_COMMENT_AP_APP_RELEASE1
      FOREIGN KEY (AP_APP_RELEASE_ID)
          REFERENCES AP_APP_RELEASE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_APP_COMMENT_AP_APP_RELEASE1_idx ON AP_APP_REVIEW (AP_APP_RELEASE_ID ASC);

-- -----------------------------------------------------
-- Table AP_APP_LIFECYCLE_STATE
-- -----------------------------------------------------
CREATE TABLE AP_APP_LIFECYCLE_STATE(
    ID INTEGER NOT NULL IDENTITY,
    CURRENT_STATE VARCHAR(45) NOT NULL,
    PREVIOUS_STATE VARCHAR(45) NOT NULL,
    TENANT_ID INTEGER NOT NULL,
    UPDATED_BY VARCHAR(100) NOT NULL,
    UPDATED_AT DATETIME2(0) NOT NULL,
    AP_APP_RELEASE_ID INTEGER NOT NULL,
    REASON VARCHAR(max) DEFAULT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_APP_LIFECYCLE_STATE_AP_APP_RELEASE1
       FOREIGN KEY (AP_APP_RELEASE_ID)
           REFERENCES AP_APP_RELEASE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_APP_LIFECYCLE_STATE_AP_APP_RELEASE1_idx ON AP_APP_LIFECYCLE_STATE( AP_APP_RELEASE_ID ASC);

-- -----------------------------------------------------
-- Table AP_APP_TAG
-- -----------------------------------------------------
CREATE TABLE AP_APP_TAG(
    ID INTEGER NOT NULL IDENTITY,
    TENANT_ID INTEGER NOT NULL,
    TAG VARCHAR(100) NOT NULL,
    PRIMARY KEY (ID)
);

-- -----------------------------------------------------
-- Table AP_DEVICE_SUBSCRIPTION
-- -----------------------------------------------------
CREATE TABLE AP_DEVICE_SUBSCRIPTION(
    ID INTEGER NOT NULL IDENTITY,
    TENANT_ID INTEGER NOT NULL,
    SUBSCRIBED_BY VARCHAR(100) NOT NULL,
    SUBSCRIBED_TIMESTAMP DATETIME2(0) NOT NULL,
    UNSUBSCRIBED BIT NOT NULL DEFAULT 'false',
    UNSUBSCRIBED_BY VARCHAR(100) NULL DEFAULT NULL,
    UNSUBSCRIBED_TIMESTAMP DATETIME2(0) NULL DEFAULT NULL,
    ACTION_TRIGGERED_FROM VARCHAR(45) NOT NULL,
    STATUS VARCHAR(45) NOT NULL,
    DM_DEVICE_ID INTEGER NOT NULL,
    AP_APP_RELEASE_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_DEVICE_SUBSCRIPTION_AP_APP_RELEASE1
       FOREIGN KEY (AP_APP_RELEASE_ID)
           REFERENCES AP_APP_RELEASE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_DEVICE_SUBSCRIPTION_AP_APP_RELEASE1_idx ON AP_DEVICE_SUBSCRIPTION (AP_APP_RELEASE_ID ASC);

-- -----------------------------------------------------
-- Table AP_GROUP_SUBSCRIPTION
-- -----------------------------------------------------
CREATE TABLE AP_GROUP_SUBSCRIPTION(
    ID INTEGER NOT NULL IDENTITY,
    TENANT_ID INTEGER NOT NULL,
    SUBSCRIBED_BY VARCHAR(100) NOT NULL,
    SUBSCRIBED_TIMESTAMP DATETIME2(0) NOT NULL,
    UNSUBSCRIBED BIT NOT NULL DEFAULT 'false',
    UNSUBSCRIBED_BY VARCHAR(100) NULL DEFAULT NULL,
    UNSUBSCRIBED_TIMESTAMP DATETIME2(0) NULL DEFAULT NULL,
    GROUP_NAME VARCHAR(100) NOT NULL,
    AP_APP_RELEASE_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_GROUP_SUBSCRIPTION_AP_APP_RELEASE1
      FOREIGN KEY (AP_APP_RELEASE_ID)
          REFERENCES AP_APP_RELEASE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_GROUP_SUBSCRIPTION_AP_APP_RELEASE1_idx ON AP_GROUP_SUBSCRIPTION (AP_APP_RELEASE_ID ASC);

-- -----------------------------------------------------
-- Table AP_ROLE_SUBSCRIPTION
-- -----------------------------------------------------
CREATE TABLE AP_ROLE_SUBSCRIPTION(
    ID INTEGER NOT NULL IDENTITY,
    TENANT_ID INTEGER NOT NULL,
    ROLE_NAME VARCHAR(100) NOT NULL,
    SUBSCRIBED_BY VARCHAR(100) NOT NULL,
    SUBSCRIBED_TIMESTAMP DATETIME2(0) NOT NULL,
    UNSUBSCRIBED BIT NOT NULL DEFAULT 'false',
    UNSUBSCRIBED_BY VARCHAR(100) NULL DEFAULT NULL,
    UNSUBSCRIBED_TIMESTAMP DATETIME2(0) NULL DEFAULT NULL,
    AP_APP_RELEASE_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_ROLE_SUBSCRIPTION_AP_APP_RELEASE1
     FOREIGN KEY (AP_APP_RELEASE_ID)
         REFERENCES AP_APP_RELEASE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_ROLE_SUBSCRIPTION_AP_APP_RELEASE1_idx ON AP_ROLE_SUBSCRIPTION (AP_APP_RELEASE_ID ASC);

-- -----------------------------------------------------
-- Table AP_UNRESTRICTED_ROLE
-- -----------------------------------------------------
CREATE TABLE AP_UNRESTRICTED_ROLE(
    ID INTEGER NOT NULL IDENTITY,
    TENANT_ID INTEGER NOT NULL,
    ROLE VARCHAR(45) NOT NULL,
    AP_APP_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_APP_VISIBILITY_AP_APP1
     FOREIGN KEY (AP_APP_ID)
         REFERENCES AP_APP (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_APP_VISIBILITY_AP_APP1_idx ON AP_UNRESTRICTED_ROLE (AP_APP_ID ASC);

-- -----------------------------------------------------
-- Table AP_USER_SUBSCRIPTION
-- -----------------------------------------------------
CREATE TABLE AP_USER_SUBSCRIPTION(
    ID INTEGER NOT NULL IDENTITY,
    TENANT_ID INTEGER NOT NULL,
    USER_NAME VARCHAR(100) NOT NULL,
    SUBSCRIBED_BY VARCHAR(100) NOT NULL,
    SUBSCRIBED_TIMESTAMP DATETIME2(0) NOT NULL,
    UNSUBSCRIBED BIT NOT NULL DEFAULT 'false',
    UNSUBSCRIBED_BY VARCHAR(100) NULL DEFAULT NULL,
    UNSUBSCRIBED_TIMESTAMP DATETIME2(0) NULL DEFAULT NULL,
    AP_APP_RELEASE_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_USER_SUBSCRIPTION_AP_APP_RELEASE1
     FOREIGN KEY (AP_APP_RELEASE_ID)
         REFERENCES AP_APP_RELEASE (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_USER_SUBSCRIPTION_AP_APP_RELEASE1_idx ON AP_USER_SUBSCRIPTION (AP_APP_RELEASE_ID ASC);

-- -----------------------------------------------------
-- Table AP_APP_CATEGORY
-- -----------------------------------------------------
CREATE TABLE AP_APP_CATEGORY(
    ID INTEGER NOT NULL IDENTITY,
    TENANT_ID INTEGER NOT NULL,
    CATEGORY VARCHAR(45) NOT NULL,
    CATEGORY_ICON VARCHAR(45) NULL,
    PRIMARY KEY (ID)
);

-- -----------------------------------------------------
-- Table AP_APP_TAG_MAPPING
-- -----------------------------------------------------
CREATE TABLE AP_APP_TAG_MAPPING(
    ID INTEGER NOT NULL IDENTITY,
    TENANT_ID INTEGER NOT NULL,
    AP_APP_TAG_ID INTEGER NOT NULL,
    AP_APP_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_APP_TAG_copy1_AP_APP_TAG1
       FOREIGN KEY (AP_APP_TAG_ID)
           REFERENCES AP_APP_TAG (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_AP_APP_TAG_copy1_AP_APP1
       FOREIGN KEY (AP_APP_ID)
           REFERENCES AP_APP (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_APP_TAG_copy1_AP_APP_TAG1_idx ON AP_APP_TAG_MAPPING (AP_APP_TAG_ID ASC);
CREATE INDEX fk_AP_APP_TAG_copy1_AP_APP1_idx ON AP_APP_TAG_MAPPING (AP_APP_ID ASC);

-- -----------------------------------------------------
-- Table AP_APP_CATEGORY_MAPPING
-- -----------------------------------------------------
CREATE TABLE AP_APP_CATEGORY_MAPPING(
    ID INTEGER NOT NULL IDENTITY,
    TENANT_ID INTEGER NOT NULL,
    AP_APP_CATEGORY_ID INTEGER NOT NULL,
    AP_APP_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_APP_CATEGORY_copy1_AP_APP_CATEGORY1
        FOREIGN KEY (AP_APP_CATEGORY_ID)
            REFERENCES AP_APP_CATEGORY (ID) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_AP_APP_CATEGORY_copy1_AP_APP1
        FOREIGN KEY (AP_APP_ID)
            REFERENCES AP_APP (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_APP_CATEGORY_copy1_AP_APP_CATEGORY1_idx ON AP_APP_CATEGORY_MAPPING (AP_APP_CATEGORY_ID ASC);
CREATE INDEX fk_AP_APP_CATEGORY_copy1_AP_APP1_idx ON AP_APP_CATEGORY_MAPPING (AP_APP_ID ASC);

-- -----------------------------------------------------
-- Table AP_APP_SUB_OP_MAPPING
-- -----------------------------------------------------
CREATE TABLE AP_APP_SUB_OP_MAPPING (
    ID INTEGER NOT NULL IDENTITY,
    TENANT_ID INTEGER NOT NULL,
    OPERATION_ID INTEGER NOT NULL,
    AP_DEVICE_SUBSCRIPTION_ID INTEGER NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_APP_SUB_OP_MAPPING_AP_DEVICE_SUBSCRIPTION1
       FOREIGN KEY (AP_DEVICE_SUBSCRIPTION_ID)
           REFERENCES AP_DEVICE_SUBSCRIPTION (ID) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX fk_AP_APP_SUB_OP_MAPPING_AP_DEVICE_SUBSCRIPTION1_idx ON AP_APP_SUB_OP_MAPPING (AP_DEVICE_SUBSCRIPTION_ID ASC);

-- -----------------------------------------------------
-- Table AP_SCHEDULED_SUBSCRIPTION
-- -----------------------------------------------------
CREATE TABLE AP_SCHEDULED_SUBSCRIPTION(
    ID INTEGER NOT NULL IDENTITY,
    TASK_NAME VARCHAR(100) NOT NULL,
    APPLICATION_UUID VARCHAR(200) NOT NULL,
    SUBSCRIBER_LIST VARCHAR(MAX) NOT NULL,
    STATUS VARCHAR(15) NOT NULL,
    SCHEDULED_AT DATETIME2(0) NOT NULL,
    SCHEDULED_BY VARCHAR(100) NOT NULL,
    SCHEDULED_TIMESTAMP DATETIME2(0) NOT NULL,
    DELETED BIT,
    PRIMARY KEY (ID),
    CONSTRAINT fk_AP_SCHEDULED_SUBSCRIPTION_AP_APP_RELEASE
        FOREIGN KEY (APPLICATION_UUID)
            REFERENCES AP_APP_RELEASE (UUID) ON DELETE NO ACTION ON UPDATE NO ACTION
);