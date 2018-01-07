CREATE TABLE JdsEntityOverview
  (
      Uuid          NVARCHAR(96),
      DateCreated         DATETIME,
      DateModified        DATETIME,
      Version             BIGINT,
      Live                BIT,
      PRIMARY KEY         (Uuid)
  );