CREATE PROCEDURE procStoreBlob(pEntityGuid IN NVARCHAR2, pFieldId IN NUMBER, pValue IN BLOB)
AS
BEGIN
	MERGE INTO JdsStoreBlob dest
	USING DUAL ON (pEntityGuid = EntityGuid AND pFieldId = FieldId)
	WHEN MATCHED THEN
		UPDATE SET Value = pValue
	WHEN NOT MATCHED THEN
		INSERT(EntityGuid,FieldId,Value)   VALUES(pEntityGuid,  pFieldId, pValue);
END procStoreBlob;