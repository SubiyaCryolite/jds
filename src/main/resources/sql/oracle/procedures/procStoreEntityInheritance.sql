CREATE PROCEDURE procStoreEntityInheritance(pUuid IN NVARCHAR2, pEntityId IN NUMBER)
AS
BEGIN
	MERGE INTO JdsEntityInstance dest
	USING DUAL ON (pUuid = Uuid AND pEntityId = EntityId)
	WHEN NOT MATCHED THEN
		INSERT(Uuid, EntityId) VALUES(pUuid, pEntityId);
END procStoreEntityInheritance;