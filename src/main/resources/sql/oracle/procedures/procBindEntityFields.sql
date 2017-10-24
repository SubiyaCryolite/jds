CREATE PROCEDURE procBindEntityFields(pEntityId IN NUMBER, pFieldId IN NUMBER)
AS
BEGIN
	MERGE INTO JdsEntityFields dest
	USING DUAL ON (pEntityId = EntityId AND pFieldId = FieldId)
	WHEN NOT MATCHED THEN
		INSERT(EntityId, FieldId) VALUES(pEntityId, pFieldId);
END procBindEntityFields;