CREATE TABLE JdsStoreEntityOverview
(
    EntityGuid          TEXT,
    ParentEntityGuid    TEXT,
    DateCreated         TIMESTAMP,
    DateModified        TIMESTAMP,
    EntityId            BIGINT,
    PRIMARY KEY         (EntityGuid),
    FOREIGN KEY (ParentEntityGuid) REFERENCES JdsRefEntityOverview(EntityGuid) ON DELETE CASCADE
);