CREATE PROCEDURE procStoreInteger(pEntityGuid IN NVARCHAR2, pFieldId IN NUMBER, pValue NUMBER)
AS
BEGIN
	MERGE INTO JdsStoreInteger dest
	USING DUAL ON (pEntityGuid = EntityGuid AND pFieldId = FieldId)
	WHEN MATCHED THEN
		UPDATE SET Value = pValue
	WHEN NOT MATCHED THEN
		INSERT(EntityGuid,FieldId,Value)   VALUES(pEntityGuid,  pFieldId, pValue);
END procStoreInteger;