package uk.co.todddavies.website.cache;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

public final class MemcacheModule extends AbstractModule {

  private static final ImmutableMap<Integer, Object> CACHE_PROPERTIES =
      ImmutableMap.<Integer, Object>of(
          // Expire items after ten minutes
          GCacheFactory.EXPIRATION_DELTA, 600);
  
  @Override
  protected void configure() { /* Nothing to configure here. */}
  
  @Provides
  @Singleton
  Optional<Cache> produceCache() {
    try {
      CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
      return Optional.of(cacheFactory.createCache(CACHE_PROPERTIES));
    } catch (CacheException e) {
      e.printStackTrace();
      return Optional.absent();
    }
  }
}
