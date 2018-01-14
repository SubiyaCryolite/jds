CREATE PROCEDURE proc_store_entity_inheritance(p_entity_uuid IN NVARCHAR2, p_entity_id IN NUMBER)
AS
  BEGIN
    MERGE INTO jds_entity_instance dest
    USING DUAL
    ON (p_entity_uuid = entity_uuid AND p_entity_id = entity_id)
    WHEN NOT MATCHED THEN
      INSERT (entity_uuid, entity_id) VALUES (p_entity_uuid, p_entity_id);
  END proc_store_entity_inheritance;