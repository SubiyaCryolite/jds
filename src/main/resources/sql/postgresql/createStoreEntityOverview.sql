CREATE TABLE JdsStoreEntityOverview
(
    EntityGuid          VARCHAR(48),
    ParentEntityGuid    VARCHAR(48),
    DateCreated         TIMESTAMP,
    DateModified        TIMESTAMP,
    EntityId            BIGINT,
    PRIMARY KEY         (EntityGuid),
    FOREIGN KEY (ParentEntityGuid) REFERENCES JdsRefEntityOverview(EntityGuid) ON DELETE CASCADE
);