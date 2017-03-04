CREATE TABLE JdsStoreEntityOverview
(
    EntityGuid          TEXT,
    ParentEntityGuid    TEXT,
    DateCreated         TIMESTAMP,
    DateModified        TIMESTAMP,
    EntityId            BIGINT,
    PRIMARY KEY         (EntityGuid),
    FOREIGN KEY (ParentEntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);