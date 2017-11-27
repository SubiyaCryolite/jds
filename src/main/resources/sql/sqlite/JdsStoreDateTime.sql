CREATE TABLE JdsStoreDateTime(
	FieldId         BIGINT,
	Uuid      TEXT,
	Value           TIMESTAMP,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);