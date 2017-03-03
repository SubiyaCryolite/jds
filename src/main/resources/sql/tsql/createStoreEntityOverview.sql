CREATE TABLE JdsStoreEntityOverview
  (
      EntityGuid          NVARCHAR(48),
      ParentEntityGuid    NVARCHAR(48),
      DateCreated         DATETIME,
      DateModified        DATETIME,
      EntityId            BIGINT,
      PRIMARY KEY         (EntityGuid)
  );