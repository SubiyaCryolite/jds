--Based on https://www.mssqltips.com/sqlservertip/2733/solving-the-sql-server-multiple-cascade-path-issue-with-a-trigger/
CREATE TRIGGER [triggerEntityBindingCascade]
	ON [JdsStoreEntityOverview]
	INSTEAD OF DELETE
AS
BEGIN
 SET NOCOUNT ON;
 DELETE FROM [JdsStoreEntityBinding] WHERE [ChildEntityGuid] IN		(SELECT [EntityGuid] FROM DELETED)
 DELETE FROM [JdsStoreEntityBinding] WHERE [ParentEntityGuid] IN	(SELECT [EntityGuid] FROM DELETED)
 DELETE FROM [JdsStoreEntityOverview] WHERE [EntityGuid] IN			(SELECT [EntityGuid] FROM DELETED)
END