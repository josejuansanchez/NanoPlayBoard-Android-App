package org.josejuansanchez.nanoplayboard.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Created by josejuansanchez on 04/10/16.
 *
 * Reference:
 * http://self-learning-java-tutorial.blogspot.com.es/2015/10/gson-ignore-null-and-empty-fields.html
 */

public class CollectionAdapter implements JsonSerializer<Collection<?>> {
    @Override
    public JsonElement serialize(Collection<?> src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null || src.isEmpty())
            return null;

        JsonArray array = new JsonArray();

        for (Object child : src) {
            JsonElement element = context.serialize(child);
            array.add(element);
        }

        return array;
    }
}