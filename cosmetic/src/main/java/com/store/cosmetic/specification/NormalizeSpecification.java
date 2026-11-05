package com.store.cosmetic.specification;

import com.store.cosmetic.exception.ConflictException;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class NormalizeSpecification {
    private NormalizeSpecification() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    private static final Map<Character, Character> CHARACTER_MAP = new HashMap<>();
    static {
        String origin = "áãàâäçéèëêùûüúóôöïîíÁÀÂÄÃÇÉÈËÊÙÛÜÚÓÔÖÏÎÍñÑ";
        String target = "aaaaaceeeeuuuuoooiiiAAAAACEEEEUUUUOOOIIInN";
        for (int i = 0; i < origin.length(); i++) {
            CHARACTER_MAP.put(origin.charAt(i), target.charAt(i));
        }
    }

    public static String normalize(String s) {
        if (s == null) {
            return null;
        }
        // Apply Unicode NFC normalization
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFC);
        StringBuilder result = new StringBuilder();
        for (char c : normalized.toCharArray()) {
            if (c <= '\u007f') {
                result.append(c); // If it's basic ASCII, it leaves it as is
            } else {
                // Otherwise, try replacing it with a "normal" character.
                result.append(CHARACTER_MAP.getOrDefault(c, c));
            }
        }
        return result.toString();
    }

    public static Predicate normalizedContains(Expression<String> expression, String value, jakarta.persistence.criteria.CriteriaBuilder cb) {
        Expression<String> normalizedField = cb.function("unaccent", String.class, cb.lower(expression));
        if (value != null) {
            String valueNorm = normalize(value).toLowerCase();
            return cb.like(normalizedField, "%" + valueNorm + "%");
        }
        else {
            throw new ConflictException("Search value can not be null");
        }
    }
}
