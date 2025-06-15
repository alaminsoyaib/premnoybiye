package com.bytebender.premnoybiye.DBConnection;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Simple in-memory cache for user data to improve performance
 */
public class DataCache {
    private static DataCache instance;
    
    // Cache for individual users
    private final Map<String, CacheEntry<userInfo>> userCache = new ConcurrentHashMap<>();
    
    // Cache for users by gender
    private final Map<String, CacheEntry<List<userInfo>>> genderCache = new ConcurrentHashMap<>();
    
    // Cache expiry time (5 minutes)
    private final Duration CACHE_EXPIRY = Duration.ofMinutes(5);
    
    private DataCache() {}
    
    public static synchronized DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;
    }
    
    /**
     * Cache entry with timestamp for expiry checking
     */
    private static class CacheEntry<T> {
        private final T data;
        private final LocalDateTime timestamp;
        
        public CacheEntry(T data) {
            this.data = data;
            this.timestamp = LocalDateTime.now();
        }
        
        public T getData() {
            return data;
        }
        
        public boolean isExpired(Duration expiry) {
            return Duration.between(timestamp, LocalDateTime.now()).compareTo(expiry) > 0;
        }
    }
    
    /**
     * Get cached user by ID
     */
    public userInfo getCachedUser(String userId) {
        CacheEntry<userInfo> entry = userCache.get(userId);
        if (entry != null && !entry.isExpired(CACHE_EXPIRY)) {
            return entry.getData();
        }
        return null;
    }
    
    /**
     * Cache a user
     */
    public void cacheUser(String userId, userInfo user) {
        if (user != null) {
            userCache.put(userId, new CacheEntry<>(user));
        }
    }
    
    /**
     * Get cached users by gender
     */
    public List<userInfo> getCachedUsersByGender(String gender) {
        CacheEntry<List<userInfo>> entry = genderCache.get(gender.toLowerCase());
        if (entry != null && !entry.isExpired(CACHE_EXPIRY)) {
            return new CopyOnWriteArrayList<>(entry.getData());
        }
        return null;
    }
    
    /**
     * Cache users by gender
     */
    public void cacheUsersByGender(String gender, List<userInfo> users) {
        if (users != null && !users.isEmpty()) {
            genderCache.put(gender.toLowerCase(), new CacheEntry<>(new CopyOnWriteArrayList<>(users)));
        }
    }
    
    /**
     * Clear all cached data
     */
    public void clearCache() {
        userCache.clear();
        genderCache.clear();
    }
    
    /**
     * Clear cache for a specific user
     */
    public void clearUserCache(String userId) {
        userCache.remove(userId);
    }
    
    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        return String.format("User Cache: %d entries, Gender Cache: %d entries", 
                           userCache.size(), genderCache.size());
    }
}
