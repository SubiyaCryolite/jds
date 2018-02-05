CREATE PROCEDURE proc_store_long(p_composite_key IN NVARCHAR2, p_field_id IN NUMBER, p_value IN NUMBER)
AS
  BEGIN
    MERGE INTO jds_store_long dest
    USING DUAL
    ON (p_composite_key = composite_key AND p_field_id = field_id)
    WHEN MATCHED THEN
      UPDATE SET value = p_value
    WHEN NOT MATCHED THEN
      INSERT (composite_key, field_id, value)   VALUES (p_composite_key, p_field_id, p_value);
  END proc_store_long;