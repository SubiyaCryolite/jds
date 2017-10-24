CREATE TABLE JdsEntityOverview
  (
      Uuid          NVARCHAR(48),
      DateCreated         DATETIME,
      DateModified        DATETIME,
      Version             BIGINT,
      Live                BIT,
      PRIMARY KEY         (Uuid)
  );