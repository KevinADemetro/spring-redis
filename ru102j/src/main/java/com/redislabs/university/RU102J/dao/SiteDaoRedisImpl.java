package com.redislabs.university.RU102J.dao;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.redislabs.university.RU102J.api.Site;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class SiteDaoRedisImpl implements SiteDao {
    private final JedisPool jedisPool;

    public SiteDaoRedisImpl(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    // When we insert a site, we set all of its values into a single hash.
    // We then store the site's id in a set for easy access.
    @Override
    public void insert(Site site) {
        try (Jedis jedis = jedisPool.getResource()) {
            String hashKey = RedisSchema.getSiteHashKey(site.getId());
            String siteIdKey = RedisSchema.getSiteIDsKey();
            jedis.hmset(hashKey, site.toMap());
            jedis.sadd(siteIdKey, hashKey);
        }
    }

    @Override
    public Site findById(long id) {
        try(Jedis jedis = jedisPool.getResource()) {
            String key = RedisSchema.getSiteHashKey(id);
            Map<String, String> fields = jedis.hgetAll(key);
            if (fields == null || fields.isEmpty()) {
                return null;
            } else {
                return new Site(fields);
            }
        }
    }

    // Challenge #1
    @Override
    public Set<Site> findAll() {
      try (Jedis jedis = jedisPool.getResource()) {
        Set<String> idKeys = jedis.smembers(RedisSchema.getSiteIDsKey());
        
        return idKeys.stream()
          .map(id -> new Site(jedis.hgetAll(id)))
          .collect(Collectors.toSet());
      } 
    }
}
