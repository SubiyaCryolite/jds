CREATE PROCEDURE procStoreDouble(pEntityGuid IN NVARCHAR2, pFieldId IN NUMBER, pValue BINARY_DOUBLE)
AS
BEGIN
	MERGE INTO JdsStoreDouble dest
	USING DUAL ON (pEntityGuid = EntityGuid AND pFieldId = FieldId)
	WHEN MATCHED THEN
		UPDATE SET Value = pValue
	WHEN NOT MATCHED THEN
		INSERT(EntityGuid,FieldId,Value)   VALUES(pEntityGuid,  pFieldId, pValue);
END procStoreDouble;