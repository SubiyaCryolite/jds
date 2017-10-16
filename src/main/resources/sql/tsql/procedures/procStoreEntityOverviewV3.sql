CREATE PROCEDURE procStoreEntityOverviewV3(@EntityGuid NVARCHAR(48), @DateCreated DATETIME, @DateModified DATETIME, @Live BIT, @Version BIGINT)
AS
BEGIN
	MERGE JdsStoreEntityOverview AS dest
	USING (VALUES (@EntityGuid, @DateCreated, @DateModified, @Live, @Version)) AS src([EntityGuid], [DateCreated], [DateModified], [Live], [Version])
	ON (src.EntityGuid = dest.EntityGuid)
	WHEN MATCHED THEN
		UPDATE SET dest.[DateModified] = src.[DateModified], dest.[Live] = src.[Live], dest.[Version] = src.[Version]
	WHEN NOT MATCHED THEN
		INSERT([EntityGuid], [DateCreated], [DateModified], [Live], [Version]) VALUES(src.EntityGuid, src.DateCreated, src.DateModified, src.Live, src.Version);
END