CREATE PROCEDURE proc_store_boolean(puuid IN NVARCHAR2, pfield_id IN NUMBER, pvalue IN NUMBER)
AS
  BEGIN
    MERGE INTO jds_store_boolean dest
    USING DUAL
    ON (puuid = uuid AND pfield_id = field_id)
    WHEN MATCHED THEN
      UPDATE SET value = pvalue
    WHEN NOT MATCHED THEN
      INSERT (uuid, field_id, value)   VALUES (puuid, pfield_id, pvalue);
  END proc_store_boolean;