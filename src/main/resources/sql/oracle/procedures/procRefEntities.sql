CREATE PROCEDURE procRefEntities(pEntityId IN NUMBER, pEntityName IN NCLOB)
AS
BEGIN
    MERGE INTO JdsEntities dest
    USING DUAL ON (pEntityId = EntityId)
    WHEN NOT MATCHED THEN
        INSERT(EntityId, EntityName) VALUES(pEntityId, pEntityName)
    WHEN MATCHED THEN
        UPDATE SET EntityName = pEntityName;
END procRefEntities;