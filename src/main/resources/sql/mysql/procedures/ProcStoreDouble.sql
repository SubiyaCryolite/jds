CREATE PROCEDURE proc_store_double(IN PUUID VARCHAR(96), IN PFIELD_ID BIGINT, IN PVALUE DOUBLE)
  BEGIN
    INSERT INTO jds_store_double (uuid, field_id, value)
    VALUES (puuid, pfield_id, pvalue)
    ON DUPLICATE KEY UPDATE value = pvalue;
  END