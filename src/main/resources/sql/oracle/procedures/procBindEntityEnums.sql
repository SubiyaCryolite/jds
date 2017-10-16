CREATE PROCEDURE procBindEntityEnums(pEntityId IN NUMBER, pFieldId IN NUMBER)
AS
BEGIN
	MERGE INTO JdsBindEntityEnums dest
	USING DUAL ON (pEntityId = EntityId AND pFieldId = FieldId)
	WHEN NOT MATCHED THEN
		INSERT(EntityId, FieldId) VALUES(pEntityId, pFieldId);
END procBindEntityEnums;