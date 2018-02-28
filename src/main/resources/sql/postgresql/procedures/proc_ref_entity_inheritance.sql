CREATE FUNCTION proc_ref_entity_inheritance(p_parent_entity_id BIGINT, p_child_entity_id BIGINT)
  RETURNS VOID AS $$
BEGIN
  INSERT INTO jds_ref_entity_inheritance (parent_entity_id, child_entity_id)
  VALUES (p_parent_entity_id, p_child_entity_id)
  ON CONFLICT (parent_entity_id, child_entity_id)
    DO NOTHING;
END;
$$
LANGUAGE plpgsql;