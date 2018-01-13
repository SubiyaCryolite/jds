CREATE PROCEDURE proc_store_float(IN PUUID VARCHAR(96), IN PFIELD_ID BIGINT, IN PVALUE FLOAT)
  BEGIN
    INSERT INTO jds_store_float (uuid, field_id, value)
    VALUES (puuid, pfield_id, pvalue)
    ON DUPLICATE KEY UPDATE value = pvalue;
  END