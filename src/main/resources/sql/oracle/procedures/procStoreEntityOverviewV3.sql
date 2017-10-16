CREATE PROCEDURE procStoreEntityOverviewV3(pEntityGuid IN NVARCHAR2, pDateCreated IN DATE, pDateModified IN DATE, pLive IN NUMBER, pVersion IN NUMBER)
AS
BEGIN
	MERGE INTO JdsStoreEntityOverview dest
	USING DUAL ON (pEntityGuid = EntityGuid)
	WHEN MATCHED THEN
		UPDATE SET DateModified = pDateModified, Live = pLive, Version = pVersion
	WHEN NOT MATCHED THEN
		INSERT(EntityGuid, DateCreated, DateModified, Live, Version) VALUES(pEntityGuid, pDateCreated, pDateModified, pLive, pVersion);
END procStoreEntityOverviewV3;