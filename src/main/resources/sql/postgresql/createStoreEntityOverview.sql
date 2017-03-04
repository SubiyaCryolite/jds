CREATE TABLE JdsStoreEntityOverview
(
    EntityGuid          VARCHAR(48),
    ParentEntityGuid    VARCHAR(48),
    DateCreated         TIMESTAMP,
    DateModified        TIMESTAMP,
    EntityId            BIGINT,
    PRIMARY KEY         (EntityGuid),
    FOREIGN KEY (ParentEntityGuid) REFERENCES JdsStoreEntityOverview(EntityGuid) ON DELETE CASCADE
);