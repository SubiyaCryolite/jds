CREATE PROCEDURE proc_entity_overview_light(p_composite_key IN NVARCHAR2)
AS
  BEGIN
    MERGE INTO jds_entity_overview_light dest
    USING DUAL
    ON (p_composite_key = composite_key)
    WHEN NOT MATCHED THEN
      INSERT (composite_key)
      VALUES (p_composite_key);
  END proc_entity_overview_light;