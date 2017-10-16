CREATE PROCEDURE procStoreZonedDateTime(pEntityGuid IN NVARCHAR2, pFieldId IN NUMBER, pValue IN NUMBER)
AS
BEGIN
	MERGE INTO JdsStoreZonedDateTime dest
	USING DUAL ON (pEntityGuid = EntityGuid AND pFieldId = FieldId)
	WHEN MATCHED THEN
		UPDATE SET Value = pValue
	WHEN NOT MATCHED THEN
		INSERT(EntityGuid,FieldId,Value)   VALUES(pEntityGuid,  pFieldId, pValue);
END procStoreZonedDateTime;