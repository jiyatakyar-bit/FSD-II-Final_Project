package com.peernet.config;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Very small in-memory rate limiter used to reduce spam (doubts and answers). */
@Component
public class RateLimiter {
    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    public void check(Long userId, String action, int maxRequests, long windowMs) {
        String key = userId + ":" + action;
        Deque<Long> deque = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        synchronized (deque) {
            while (!deque.isEmpty() && now - deque.peekFirst() > windowMs) {
                deque.pollFirst();
            }
            if (deque.size() >= maxRequests) {
                throw new ApiException(429, "Too many requests. Please wait a minute and try again.");
            }
            deque.addLast(now);
        }
    }
}
