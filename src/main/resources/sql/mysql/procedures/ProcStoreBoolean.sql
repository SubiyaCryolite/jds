CREATE PROCEDURE proc_store_boolean(IN PUUID VARCHAR(96), IN PFIELD_ID BIGINT, IN PVALUE BOOLEAN)
  BEGIN
    INSERT INTO jds_store_boolean (uuid, field_id, value)
    VALUES (puuid, pfield_id, pvalue)
    ON DUPLICATE KEY UPDATE value = pvalue;
  END