CREATE TABLE JdsStoreZonedDateTime(
	FieldId         BIGINT,
	Uuid      TEXT,
	Value           BIGINT,
	PRIMARY KEY (FieldId,Uuid),
	FOREIGN KEY (Uuid) REFERENCES JdsEntityOverview(Uuid) DEFERRABLE INITIALLY DEFERRED --we use REPLACE INTO, so hopefully this maintains integrity
);