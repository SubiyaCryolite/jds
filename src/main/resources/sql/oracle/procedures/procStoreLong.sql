CREATE PROCEDURE procStoreLong(pUuid IN NVARCHAR2, pFieldId IN NUMBER, pValue IN NUMBER)
AS
BEGIN
	MERGE INTO JdsStoreLong dest
	USING DUAL ON (pUuid = Uuid AND pFieldId = FieldId)
	WHEN MATCHED THEN
		UPDATE SET Value = pValue
	WHEN NOT MATCHED THEN
		INSERT(Uuid,FieldId,Value)   VALUES(pUuid,  pFieldId, pValue);
END procStoreLong;