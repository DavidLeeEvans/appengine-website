package uk.co.todddavies.website.notes.data;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.Value;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

@JsonSerialize(using = NotesDocumentSerializer.class)
public final class NotesDocument {
  final String name, url;
  final ImmutableList<String> tags;
  final int downloads;
  final long key;
  
  //TODO(td): Use autovalue here
  NotesDocument(String name, String url, List<String> tags, int downloads, long key) {
    this.name = name;
    this.url = url;
    this.tags = ImmutableList.<String>builder().addAll(tags).build();
    this.downloads = downloads;
    this.key = key;
  }
  
  public static NotesDocument createFromEntity(Entity entity) {
    ImmutableList.Builder<String> tags = ImmutableList.<String>builder();
    for (Value<String> s : entity.<StringValue>getList("tags")) {
      tags.add(s.get());
    }
    return new NotesDocument(
        entity.getString("name"),
        entity.getString("download_url"),
        tags.build(),
        (int) entity.getLong("downloads"),
        entity.key().id());
  }
  
  @VisibleForTesting
  public static NotesDocument createForTest(
      String name, String url, List<String> tags, int downloads, long key) {
    return new NotesDocument(name, url, tags, downloads, key);
  }

  public static Entity incrementDownloads(Entity entity) {
    return Entity.builder(entity)
        .set("downloads", entity.getLong("downloads") + 1)
        .build();
  }
  
  @Override
  public String toString() {
    return new StringBuilder()
        .append("NotesDocument(")
        .append(name).append(",")
        .append(url).append(",")
        .append(tags).append(",")
        .append(downloads).append(",")
        .append(key).append(")")
        .toString();
  }
  
  @Override
  public boolean equals(Object other) {
    return other instanceof NotesDocument
        ? this.key == ((NotesDocument) other).key
        : false;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  public ImmutableList<String> getTags() {
    return tags;
  }

  public int getDownloads() {
    return downloads;
  }

  public long getKey() {
    return key;
  }
}
