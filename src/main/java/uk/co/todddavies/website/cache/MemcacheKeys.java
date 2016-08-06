package uk.co.todddavies.website.cache;

import uk.co.todddavies.website.notes.data.NotesDocument;

import com.google.common.collect.ImmutableMap;
import com.google.inject.TypeLiteral;

import java.util.LinkedList;

public final class MemcacheKeys {
  
  public enum MemcacheKey {
    NOTES_LIST
  }
  
  public static ImmutableMap<MemcacheKey, TypeLiteral<?>> EXPECTED_TYPES =
      ImmutableMap.<MemcacheKey, TypeLiteral<?>>of(
          MemcacheKey.NOTES_LIST,
          new TypeLiteral<ImmutableMap<String, LinkedList<NotesDocument>>>() {});
  
  public static final ImmutableMap<MemcacheKey, String>  KEY_MAP = 
      ImmutableMap.of(MemcacheKey.NOTES_LIST, "notes-list");
  
  private MemcacheKeys() {/* Private constructor */}
}
