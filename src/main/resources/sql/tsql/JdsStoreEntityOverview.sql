CREATE TABLE JdsStoreEntityOverview
  (
      EntityGuid          NVARCHAR(48),
      DateCreated         DATETIME,
      DateModified        DATETIME,
      Version             BIGINT,
      Live                BIT,
      PRIMARY KEY         (EntityGuid)
  );