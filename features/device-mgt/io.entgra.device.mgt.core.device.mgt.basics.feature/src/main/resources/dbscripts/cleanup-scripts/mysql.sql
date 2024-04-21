/*
    This cleanup script can be run in 2 different ways:

    1) Logging all queries to a file: mysql -u user -p < mysql.sql --verbose > mysql.log 2>&1

        - Change the mysql.log file path as needed
        - Can be run without the --verbose flag which will only log the timestamped select queries or any errors
            e.g: SELECT CONCAT(NOW(), ': Starting cleanup script...') AS '';

    2) Logging queries to general_log file/table

        - Do note that in order for this to work the user need to have SUPER or SYSTEM_VARIABLES_ADMIN privilege(s)
        - Uncomment lines 36-38 and 288 to log to a file and change the general.log file path as needed
        - To log queries to a table, comment line 36 and change log_output to 'TABLE'
        - The log_output = 'TABLE' will have queries in BLOB type which needs to be converted to readable format
            i.e. SELECT CONVERT(argument using utf8) FROM mysql.general_log;

 */
SET @startTime = NOW();
SELECT CONCAT(@startTime, ': Starting cleanup script...') AS '';

/*
    Rename DM_DB to the CDM MySQL datasource that is running on your server
 */
USE DM_DB;

/*
    Change @retention date to the date that you want the records to be retained after a certain date.
    Any records that are older than the retention date will be dropped after this script has successfully
    executed
    RETENTION date format - YYYY-MM-DD HH:MM:SS
 */
SET @retention = '2022-08-16 00:00:00';
SELECT CONCAT(NOW(), ': Retention date set to - ', @retention, '.') AS '';

-- SET GLOBAL general_log_file = '/var/log/mysql/general.log';
-- SET GLOBAL log_output = 'TABLE';
-- SET GLOBAL general_log = 'ON';

/*
    Create table structure for NEW_DM_ENROLMENT_OP_MAPPING
*/
SELECT CONCAT(NOW(), ': Creating NEW_DM_ENROLMENT_OP_MAPPING table.') AS '';
CREATE TABLE NEW_DM_ENROLMENT_OP_MAPPING (
    ID INTEGER NOT NULL AUTO_INCREMENT,
    ENROLMENT_ID INTEGER NOT NULL,
    OPERATION_ID INTEGER NOT NULL,
    STATUS VARCHAR(50) NULL,
    PUSH_NOTIFICATION_STATUS VARCHAR(50) NULL,
    CREATED_TIMESTAMP INTEGER NOT NULL,
    UPDATED_TIMESTAMP INTEGER NOT NULL,
    OPERATION_CODE VARCHAR(50) NOT NULL,
    INITIATED_BY VARCHAR(100) NULL,
    TYPE VARCHAR(20) NOT NULL,
    DEVICE_ID INTEGER DEFAULT NULL,
    DEVICE_TYPE VARCHAR(300) NOT NULL,
    DEVICE_IDENTIFICATION VARCHAR(300) DEFAULT NULL,
    TENANT_ID INTEGER DEFAULT 0,
    PRIMARY KEY (ID)
);

/*
    Set the AUTO_INCREMENT value of the NEW_DM_ENROLMENT_OP_MAPPING table to match existing DM_ENROLMENT_OP_MAPPING
    table's value
*/
SET @DM_ENROLMENT_OP_MAPPING_INCREMENT_VALUE := (SELECT AUTO_INCREMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='DM_ENROLMENT_OP_MAPPING' LIMIT 1);
SELECT CONCAT(NOW(), ': Set AUTO_INCREMENT value of NEW_DM_ENROLMENT_OP_MAPPING table to - ', @DM_ENROLMENT_OP_MAPPING_INCREMENT_VALUE, '.') AS '';
SET @sql1 = CONCAT('ALTER TABLE NEW_DM_ENROLMENT_OP_MAPPING AUTO_INCREMENT = ', @DM_ENROLMENT_OP_MAPPING_INCREMENT_VALUE);
PREPARE st FROM @sql1;
EXECUTE st;
DEALLOCATE PREPARE st;

/*
    Drop the foreign keys of the original DM_ENROLMENT_OP_MAPPING table and then rename DM_ENROLMENT_OP_MAPPING to
    OLD_DM_ENROLMENT_OP_MAPPING and NEW_DM_ENROLMENT_OP_MAPPING to DM_ENROLMENT_OP_MAPPING
 */
SELECT CONCAT(NOW(), ': Dropping foreign keys of DM_ENROLMENT_OP_MAPPING.') AS '';
ALTER TABLE DM_ENROLMENT_OP_MAPPING DROP FOREIGN KEY fk_dm_device_operation_mapping_device;
ALTER TABLE DM_ENROLMENT_OP_MAPPING DROP FOREIGN KEY fk_dm_device_operation_mapping_operation;

SELECT CONCAT(NOW(), ': Renaming DM_ENROLMENT_OP_MAPPING to OLD_DM_ENROLMENT_OP_MAPPING and NEW_DM_ENROLMENT_OP_MAPPING to DM_ENROLMENT_OP_MAPPING.') AS '';
RENAME TABLE DM_ENROLMENT_OP_MAPPING TO OLD_DM_ENROLMENT_OP_MAPPING,  NEW_DM_ENROLMENT_OP_MAPPING TO DM_ENROLMENT_OP_MAPPING;

/*
    Create table structure for NEW_DM_DEVICE_OPERATION_RESPONSE
*/
SELECT CONCAT(NOW(), ': Creating NEW_DM_DEVICE_OPERATION_RESPONSE table.') AS '';
CREATE TABLE NEW_DM_DEVICE_OPERATION_RESPONSE (
    ID INTEGER NOT NULL AUTO_INCREMENT,
    ENROLMENT_ID INTEGER NOT NULL,
    OPERATION_ID INTEGER NOT NULL,
    EN_OP_MAP_ID INTEGER NOT NULL,
    OPERATION_RESPONSE VARCHAR(1024) DEFAULT NULL,
    IS_LARGE_RESPONSE BOOLEAN NOT NULL DEFAULT FALSE,
    RECEIVED_TIMESTAMP TIMESTAMP NULL,
    PRIMARY KEY (ID)
);

/*
    Set the AUTO_INCREMENT value of the NEW_DM_DEVICE_OPERATION_RESPONSE table to match existing
    DM_DEVICE_OPERATION_RESPONSE table's value
*/
SET @DM_DEVICE_OPERATION_RESPONSE_INCREMENT_VALUE := (SELECT AUTO_INCREMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='DM_DEVICE_OPERATION_RESPONSE' LIMIT 1);
SELECT CONCAT(NOW(), ': Set AUTO_INCREMENT value of NEW_DM_DEVICE_OPERATION_RESPONSE table to - ', @DM_DEVICE_OPERATION_RESPONSE_INCREMENT_VALUE, '.') AS '';
SET @sql2 = CONCAT('ALTER TABLE NEW_DM_DEVICE_OPERATION_RESPONSE AUTO_INCREMENT = ', @DM_DEVICE_OPERATION_RESPONSE_INCREMENT_VALUE);
PREPARE st FROM @sql2;
EXECUTE st;
DEALLOCATE PREPARE st;

/*
    Drop the foreign keys of the original DM_DEVICE_OPERATION_RESPONSE table and then rename
    DM_DEVICE_OPERATION_RESPONSE to OLD_DM_DEVICE_OPERATION_RESPONSE and NEW_DM_DEVICE_OPERATION_RESPONSE to
    DM_DEVICE_OPERATION_RESPONSE
 */
SELECT CONCAT(NOW(), ': Dropping foreign keys of DM_DEVICE_OPERATION_RESPONSE.') AS '';
ALTER TABLE DM_DEVICE_OPERATION_RESPONSE DROP FOREIGN KEY fk_dm_device_operation_response_enrollment;
ALTER TABLE DM_DEVICE_OPERATION_RESPONSE DROP FOREIGN KEY fk_dm_device_operation_response_operation;
ALTER TABLE DM_DEVICE_OPERATION_RESPONSE DROP FOREIGN KEY fk_dm_en_op_map_response;

SELECT CONCAT(NOW(), ': Renaming DM_DEVICE_OPERATION_RESPONSE to OLD_DM_DEVICE_OPERATION_RESPONSE and NEW_DM_DEVICE_OPERATION_RESPONSE to DM_DEVICE_OPERATION_RESPONSE.') AS '';
RENAME TABLE DM_DEVICE_OPERATION_RESPONSE TO OLD_DM_DEVICE_OPERATION_RESPONSE, NEW_DM_DEVICE_OPERATION_RESPONSE TO DM_DEVICE_OPERATION_RESPONSE;

/*
    Create table structure for NEW_DM_DEVICE_OPERATION_RESPONSE_LARGE
*/
SELECT CONCAT(NOW(), ': Creating NEW_DM_DEVICE_OPERATION_RESPONSE_LARGE table.') AS '';
CREATE TABLE NEW_DM_DEVICE_OPERATION_RESPONSE_LARGE (
    ID INTEGER NOT NULL,
    OPERATION_RESPONSE LONGBLOB,
    OPERATION_ID INTEGER NOT NULL,
    EN_OP_MAP_ID INTEGER NOT NULL,
    RECEIVED_TIMESTAMP TIMESTAMP NULL,
    DEVICE_IDENTIFICATION VARCHAR(300) DEFAULT NULL
);

/*
    Drop the foreign keys of the original DM_DEVICE_OPERATION_RESPONSE_LARGE table and then rename
    DM_DEVICE_OPERATION_RESPONSE_LARGE to OLD_DM_DEVICE_OPERATION_RESPONSE_LARGE and
    NEW_DM_DEVICE_OPERATION_RESPONSE_LARGE to DM_DEVICE_OPERATION_RESPONSE_LARGE
 */
SELECT CONCAT(NOW(), ': Dropping foreign keys of DM_DEVICE_OPERATION_RESPONSE_LARGE.') AS '';
ALTER TABLE DM_DEVICE_OPERATION_RESPONSE_LARGE DROP FOREIGN KEY fk_dm_device_operation_response_large;
ALTER TABLE DM_DEVICE_OPERATION_RESPONSE_LARGE DROP FOREIGN KEY fk_dm_en_op_map_response_large;

SELECT CONCAT(NOW(), ': Renaming DM_DEVICE_OPERATION_RESPONSE_LARGE to OLD_DM_DEVICE_OPERATION_RESPONSE_LARGE and NEW_DM_DEVICE_OPERATION_RESPONSE_LARGE to DM_DEVICE_OPERATION_RESPONSE_LARGE.') AS '';
RENAME TABLE DM_DEVICE_OPERATION_RESPONSE_LARGE TO OLD_DM_DEVICE_OPERATION_RESPONSE_LARGE, NEW_DM_DEVICE_OPERATION_RESPONSE_LARGE TO DM_DEVICE_OPERATION_RESPONSE_LARGE;

/*
    Create table structure for NEW_DM_OPERATION
*/
SELECT CONCAT(NOW(), ': Creating NEW_DM_OPERATION table.') AS '';
CREATE TABLE NEW_DM_OPERATION (
    ID INTEGER AUTO_INCREMENT NOT NULL,
    TYPE VARCHAR(20) NOT NULL,
    CREATED_TIMESTAMP BIGINT(15) NOT NULL,
    RECEIVED_TIMESTAMP BIGINT(15) NULL,
    OPERATION_CODE VARCHAR(50) NOT NULL,
    INITIATED_BY VARCHAR(100) NULL,
    OPERATION_DETAILS BLOB DEFAULT NULL,
    OPERATION_PROPERTIES BLOB DEFAULT NULL,
    ENABLED BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (ID)
);

/*
    Set the AUTO_INCREMENT value of the NEW_DM_OPERATION table to match existing DM_OPERATION table's value
*/
SET @DM_OPERATION_INCREMENT_VALUE := (SELECT AUTO_INCREMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='DM_OPERATION' LIMIT 1);
SELECT CONCAT(NOW(), ': Set AUTO_INCREMENT value of NEW_DM_OPERATION table to - ', @DM_OPERATION_INCREMENT_VALUE, '.') AS '';
SET @sql3 = CONCAT('ALTER TABLE NEW_DM_OPERATION AUTO_INCREMENT = ', @DM_OPERATION_INCREMENT_VALUE);
PREPARE st FROM @sql3;
EXECUTE st;
DEALLOCATE PREPARE st;

/*
    Rename DM_OPERATION to OLD_DM_OPERATION and NEW_DM_OPERATION to DM_OPERATION
 */
SELECT CONCAT(NOW(), ': Renaming DM_OPERATION to OLD_DM_OPERATION and NEW_DM_OPERATION to DM_OPERATION.') AS '';
RENAME TABLE DM_OPERATION TO OLD_DM_OPERATION, NEW_DM_OPERATION TO DM_OPERATION;

/*
    Insert data into DM_DEVICE_OPERATION_RESPONSE from OLD_DM_DEVICE_OPERATION_RESPONSE that is greater than or
    equal to the retention date declared at the start of the script
*/
SELECT CONCAT(NOW(), ': Inserting data into DM_DEVICE_OPERATION_RESPONSE FROM OLD_DM_DEVICE_OPERATION_RESPONSE.') AS '';
INSERT INTO DM_DEVICE_OPERATION_RESPONSE (ID, ENROLMENT_ID, OPERATION_ID, EN_OP_MAP_ID, OPERATION_RESPONSE, IS_LARGE_RESPONSE, RECEIVED_TIMESTAMP)
SELECT ID, ENROLMENT_ID, OPERATION_ID, EN_OP_MAP_ID, OPERATION_RESPONSE, IS_LARGE_RESPONSE, RECEIVED_TIMESTAMP
FROM OLD_DM_DEVICE_OPERATION_RESPONSE
WHERE RECEIVED_TIMESTAMP >= @retention;
SELECT CONCAT(NOW(), ': Inserted ', ROW_COUNT(),' records to DM_DEVICE_OPERATION_RESPONSE.') AS '';

/*
    Insert data into DM_DEVICE_OPERATION_RESPONSE_LARGE from OLD_DM_DEVICE_OPERATION_RESPONSE_LARGE that is greater
    than or equal to the retention date declared at the start of the script
*/
SELECT CONCAT(NOW(), ': Inserting data into DM_DEVICE_OPERATION_RESPONSE_LARGE FROM OLD_DM_DEVICE_OPERATION_RESPONSE_LARGE.') AS '';
INSERT INTO DM_DEVICE_OPERATION_RESPONSE_LARGE (ID, OPERATION_RESPONSE, OPERATION_ID, EN_OP_MAP_ID, RECEIVED_TIMESTAMP, DEVICE_IDENTIFICATION)
SELECT ID, OPERATION_RESPONSE, OPERATION_ID, EN_OP_MAP_ID, RECEIVED_TIMESTAMP, DEVICE_IDENTIFICATION
FROM OLD_DM_DEVICE_OPERATION_RESPONSE_LARGE
WHERE RECEIVED_TIMESTAMP >= @retention;
SELECT CONCAT(NOW(), ': Inserted ', ROW_COUNT(),' records to DM_DEVICE_OPERATION_RESPONSE_LARGE.') AS '';

/*
    Insert data into DM_ENROLMENT_OP_MAPPING from OLD_DM_ENROLMENT_OP_MAPPING that has any mapping IDs that are from
    DM_DEVICE_OPERATION_RESPONSE or with the status of 'PENDING' / 'IN_PROGRESS'.
    This is done because if there are any records with the status of 'PENDING' or 'IN_PROGRESS' that are older than
    the retention date the below statement will retain them
*/
SELECT CONCAT(NOW(), ': Inserting data into DM_ENROLMENT_OP_MAPPING FROM OLD_DM_ENROLMENT_OP_MAPPING.') AS '';
INSERT INTO DM_ENROLMENT_OP_MAPPING (ID, ENROLMENT_ID, OPERATION_ID, STATUS, PUSH_NOTIFICATION_STATUS, CREATED_TIMESTAMP, UPDATED_TIMESTAMP,
OPERATION_CODE, INITIATED_BY, TYPE, DEVICE_ID, DEVICE_TYPE, DEVICE_IDENTIFICATION, TENANT_ID)
SELECT ID, ENROLMENT_ID, OPERATION_ID, STATUS, PUSH_NOTIFICATION_STATUS, CREATED_TIMESTAMP, UPDATED_TIMESTAMP, OPERATION_CODE, INITIATED_BY,
TYPE, DEVICE_ID, DEVICE_TYPE, DEVICE_IDENTIFICATION, TENANT_ID
FROM OLD_DM_ENROLMENT_OP_MAPPING
WHERE ID IN(SELECT EN_OP_MAP_ID FROM DM_DEVICE_OPERATION_RESPONSE) OR STATUS = 'PENDING' OR STATUS = 'IN_PROGRESS';
SELECT CONCAT(NOW(), ': Inserted ', ROW_COUNT(),' records to DM_ENROLMENT_OP_MAPPING.') AS '';

/*
    Insert data into NEW_DM_OPERATION from DM_OPERATION only with the OPERATION_IDs from DM_ENROLMENT_OP_MAPPING
*/
SELECT CONCAT(NOW(), ': Inserting data into DM_OPERATION FROM OLD_DM_OPERATION.') AS '';
INSERT INTO DM_OPERATION (ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE, INITIATED_BY, OPERATION_DETAILS, OPERATION_PROPERTIES, ENABLED)
SELECT ID, TYPE, CREATED_TIMESTAMP, RECEIVED_TIMESTAMP, OPERATION_CODE, INITIATED_BY, OPERATION_DETAILS, OPERATION_PROPERTIES, ENABLED
FROM OLD_DM_OPERATION
WHERE ID IN(SELECT OPERATION_ID FROM DM_ENROLMENT_OP_MAPPING);
SELECT CONCAT(NOW(), ': Inserted ', ROW_COUNT(),' records to DM_OPERATION.') AS '';

/*
    Add back foreign key constraints to the new tables that were created which contains data from the retention date
    onwards
*/
SELECT CONCAT(NOW(), ': Adding foreign key constraints to DM_ENROLMENT_OP_MAPPING.') AS '';
ALTER TABLE DM_ENROLMENT_OP_MAPPING ADD CONSTRAINT fk_dm_device_operation_mapping_device FOREIGN KEY (ENROLMENT_ID) REFERENCES DM_ENROLMENT(ID) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE DM_ENROLMENT_OP_MAPPING ADD CONSTRAINT fk_dm_device_operation_mapping_operation FOREIGN KEY (OPERATION_ID) REFERENCES DM_OPERATION(ID) ON DELETE NO ACTION ON UPDATE NO ACTION;

SELECT CONCAT(NOW(), ': Adding foreign key constraints to DM_DEVICE_OPERATION_RESPONSE_LARGE.') AS '';
ALTER TABLE DM_DEVICE_OPERATION_RESPONSE_LARGE ADD CONSTRAINT fk_dm_device_operation_response_large FOREIGN KEY (ID) REFERENCES DM_DEVICE_OPERATION_RESPONSE(ID) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE DM_DEVICE_OPERATION_RESPONSE_LARGE ADD CONSTRAINT fk_dm_en_op_map_response_large FOREIGN KEY (EN_OP_MAP_ID) REFERENCES DM_ENROLMENT_OP_MAPPING(ID) ON DELETE NO ACTION ON UPDATE NO ACTION;

SELECT CONCAT(NOW(), ': Adding foreign key constraints to DM_DEVICE_OPERATION_RESPONSE.') AS '';
ALTER TABLE DM_DEVICE_OPERATION_RESPONSE ADD CONSTRAINT fk_dm_device_operation_response_enrollment FOREIGN KEY (ENROLMENT_ID) REFERENCES DM_ENROLMENT(ID) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE DM_DEVICE_OPERATION_RESPONSE ADD CONSTRAINT fk_dm_device_operation_response_operation FOREIGN KEY (OPERATION_ID) REFERENCES DM_OPERATION(ID) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE DM_DEVICE_OPERATION_RESPONSE ADD CONSTRAINT fk_dm_en_op_map_response FOREIGN KEY (EN_OP_MAP_ID) REFERENCES DM_ENROLMENT_OP_MAPPING(ID) ON DELETE NO ACTION ON UPDATE NO ACTION;

/*
    Drop the original tables that were renamed and truncate DM_NOTIFICATION. DM_NOTIFICATION table is truncated
    because the data is not necessary to be retained
 */
SELECT CONCAT(NOW(), ': Dropping table OLD_DM_ENROLMENT_OP_MAPPING.') AS '';
DROP TABLE OLD_DM_ENROLMENT_OP_MAPPING;

SELECT CONCAT(NOW(), ': Dropping table OLD_DM_DEVICE_OPERATION_RESPONSE.') AS '';
DROP TABLE OLD_DM_DEVICE_OPERATION_RESPONSE;

SELECT CONCAT(NOW(), ': Dropping table OLD_DM_DEVICE_OPERATION_RESPONSE_LARGE.') AS '';
DROP TABLE OLD_DM_DEVICE_OPERATION_RESPONSE_LARGE;

SELECT CONCAT(NOW(), ': Dropping table OLD_DM_OPERATION.') AS '';
DROP TABLE OLD_DM_OPERATION;

SELECT CONCAT(NOW(), ': Truncating table DM_NOTIFICATION.') AS '';
TRUNCATE TABLE DM_NOTIFICATION;

/*
    Create new indexes for the new tables which contains data from the retention date onwards
 */
SELECT CONCAT(NOW(), ': Adding indexes to DM_ENROLMENT_OP_MAPPING.') AS '';
CREATE INDEX fk_dm_device_operation_mapping_operation ON DM_ENROLMENT_OP_MAPPING(OPERATION_ID);
CREATE INDEX IDX_DM_ENROLMENT_OP_MAPPING ON DM_ENROLMENT_OP_MAPPING(ENROLMENT_ID, OPERATION_ID);
CREATE INDEX ID_DM_ENROLMENT_OP_MAPPING_UPDATED_TIMESTAMP ON DM_ENROLMENT_OP_MAPPING(UPDATED_TIMESTAMP);
CREATE INDEX IDX_ENROLMENT_OP_MAPPING ON DM_ENROLMENT_OP_MAPPING(UPDATED_TIMESTAMP);
CREATE INDEX IDX_EN_OP_MAPPING_EN_ID ON DM_ENROLMENT_OP_MAPPING(ENROLMENT_ID);
CREATE INDEX IDX_EN_OP_MAPPING_OP_ID ON DM_ENROLMENT_OP_MAPPING(OPERATION_ID);
CREATE INDEX IDX_EN_OP_MAPPING_EN_ID_STATUS ON DM_ENROLMENT_OP_MAPPING(ENROLMENT_ID, STATUS);

SELECT CONCAT(NOW(), ': Adding indexes to DM_DEVICE_OPERATION_RESPONSE.') AS '';
CREATE INDEX IDX_DM_RES_RT ON DM_DEVICE_OPERATION_RESPONSE(RECEIVED_TIMESTAMP);
CREATE INDEX IDX_ENID_OP_ID ON DM_DEVICE_OPERATION_RESPONSE(OPERATION_ID, ENROLMENT_ID);
CREATE INDEX IDX_DM_EN_OP_MAP_ID ON DM_DEVICE_OPERATION_RESPONSE(EN_OP_MAP_ID);

SELECT CONCAT(NOW(), ': Adding indexes to DM_DEVICE_OPERATION_RESPONSE_LARGE.') AS '';
CREATE INDEX IDX_DM_RES_LRG_RT ON DM_DEVICE_OPERATION_RESPONSE_LARGE(RECEIVED_TIMESTAMP);
CREATE INDEX IDX_DM_EN_OP_MAP_ID ON DM_DEVICE_OPERATION_RESPONSE_LARGE(EN_OP_MAP_ID);

-- SET GLOBAL general_log = 'OFF';
SET @endTime = NOW();
SELECT CONCAT(@endTime, ': Cleanup script finished executing in ', TIME_FORMAT(TIMEDIFF(@endTime, @startTime), '%H:%i:%s'),'.') AS '';