CREATE PROCEDURE procStoreEntityInheritance(pEntityGuid IN NVARCHAR2, pEntityId IN NUMBER)
AS
BEGIN
	MERGE INTO JdsStoreEntityInheritance dest
	USING DUAL ON (pEntityGuid = EntityGuid AND pEntityId = EntityId)
	WHEN NOT MATCHED THEN
		INSERT(EntityGuid, EntityId) VALUES(pEntityGuid, pEntityId);
END procStoreEntityInheritance;